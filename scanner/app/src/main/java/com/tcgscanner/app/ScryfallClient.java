package com.tcgscanner.app;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScryfallClient {
    private static final String BASE = "https://api.scryfall.com";
    private static final int MAX_ARTWORK_CANDIDATES = 60;
    private static final int ARTWORK_WORKERS = 8;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ExecutorService artworkExecutor = Executors.newFixedThreadPool(ARTWORK_WORKERS);

    public interface ResolveCallback {
        void onResolved(ScryfallCard card, RecognitionConfidence confidence);
        void onFailure(String reason);
    }

    public void resolve(OcrCardData ocr, Bitmap centeredCard, ResolveCallback callback) {
        executor.execute(() -> {
            try {
                if (ocr.hasExactPrintingKeys()) {
                    ScryfallCard exact = getBySetCollector(ocr.setCode, ocr.collectorNumber, ocr.language);
                    if (exact != null) {
                        callback.onResolved(exact, RecognitionConfidence.EXACT_METADATA);
                        return;
                    }
                }

                if (ocr.nameCandidate == null || ocr.nameCandidate.trim().length() < 2) {
                    callback.onFailure("Hold the card steady so the name and bottom line are readable.");
                    return;
                }

                ScryfallCard named = getByFuzzyName(ocr.nameCandidate);
                if (named == null) {
                    callback.onFailure("Card name was not recognized. Re-center the card and try again.");
                    return;
                }

                List<ScryfallCard> printings = searchPrintings(named.name);
                if (printings.isEmpty()) {
                    callback.onResolved(named, RecognitionConfidence.NAME_ONLY);
                    return;
                }

                Bitmap cameraArtwork = CardCropper.artworkRegion(centeredCard);
                CompletionService<ScoredCandidate> completion = new ExecutorCompletionService<>(artworkExecutor);
                int submitted = 0;

                for (ScryfallCard candidate : printings) {
                    if (submitted >= MAX_ARTWORK_CANDIDATES) break;
                    String comparisonUrl = candidate.artworkUrl != null ? candidate.artworkUrl : candidate.imageUrl;
                    if (comparisonUrl == null) continue;

                    completion.submit(() -> scoreCandidate(cameraArtwork, candidate, comparisonUrl));
                    submitted++;
                }

                ScryfallCard best = null;
                double bestScore = -1.0;

                for (int i = 0; i < submitted; i++) {
                    try {
                        Future<ScoredCandidate> future = completion.take();
                        ScoredCandidate scored = future.get();
                        if (scored != null && scored.score > bestScore) {
                            bestScore = scored.score;
                            best = scored.card;
                        }
                    } catch (Exception ignored) {
                        // One image comparison failing should not abort the scan.
                    }
                }

                if (best != null && bestScore >= 0.62) {
                    callback.onResolved(best, RecognitionConfidence.ARTWORK_MATCH);
                } else {
                    callback.onResolved(named, RecognitionConfidence.NAME_ONLY);
                }
            } catch (Exception e) {
                callback.onFailure("Card lookup failed: " + safeMessage(e));
            }
        });
    }

    private ScoredCandidate scoreCandidate(Bitmap cameraArtwork, ScryfallCard candidate, String comparisonUrl) {
        Bitmap candidateImage = SimpleImageLoader.download(comparisonUrl);
        if (candidateImage == null) return null;

        Bitmap candidateArtwork = candidate.artworkUrl != null
                ? candidateImage
                : CardCropper.artworkRegion(candidateImage);
        double score = ArtworkMatcher.similarity(cameraArtwork, candidateArtwork);
        return new ScoredCandidate(candidate, score);
    }

    private ScryfallCard getBySetCollector(String set, String collector, String lang) {
        try {
            String url = BASE + "/cards/" + encPath(set.toLowerCase(Locale.US)) + "/" + encPath(collector);
            if (lang != null && !lang.isEmpty() && !"en".equalsIgnoreCase(lang)) {
                url += "/" + encPath(lang.toLowerCase(Locale.US));
            }
            JSONObject json = getJson(url);
            return parseCard(json);
        } catch (Exception ignored) {
            return null;
        }
    }

    private ScryfallCard getByFuzzyName(String name) {
        try {
            String url = BASE + "/cards/named?fuzzy=" + encQuery(name);
            return parseCard(getJson(url));
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<ScryfallCard> searchPrintings(String exactName) throws Exception {
        String query = "!\"" + exactName.replace("\"", "") + "\"";
        String url = BASE + "/cards/search?q=" + encQuery(query)
                + "&unique=prints&include_multilingual=true&include_variations=true";
        JSONObject root = getJson(url);
        JSONArray data = root.optJSONArray("data");
        List<ScryfallCard> cards = new ArrayList<>();
        if (data == null) return cards;
        for (int i = 0; i < data.length(); i++) {
            ScryfallCard card = parseCard(data.optJSONObject(i));
            if (card != null) cards.add(card);
        }
        return cards;
    }

    private JSONObject getJson(String urlString) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "TCGScanner/0.1 Android card scanner");

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAll(stream);
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Scryfall HTTP " + code);
            }
            return new JSONObject(body);
        } finally {
            if (connection != null) connection.disconnect();
            try {
                Thread.sleep(80L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private ScryfallCard parseCard(JSONObject json) {
        if (json == null) return null;
        JSONObject imageUris = json.optJSONObject("image_uris");
        String oracle = json.optString("oracle_text", "");

        if (imageUris == null) {
            JSONArray faces = json.optJSONArray("card_faces");
            if (faces != null && faces.length() > 0) {
                JSONObject face = faces.optJSONObject(0);
                if (face != null) {
                    imageUris = face.optJSONObject("image_uris");
                    if (oracle.isEmpty()) oracle = face.optString("oracle_text", "");
                }
            }
        }

        String normal = null;
        String art = null;
        if (imageUris != null) {
            normal = nullable(imageUris.optString("normal", null));
            art = nullable(imageUris.optString("art_crop", null));
            if (normal == null) normal = nullable(imageUris.optString("large", null));
            if (normal == null) normal = nullable(imageUris.optString("small", null));
        }

        JSONObject prices = json.optJSONObject("prices");
        String usd = prices == null ? null : nullable(prices.optString("usd", null));
        String usdFoil = prices == null ? null : nullable(prices.optString("usd_foil", null));

        return new ScryfallCard(
                json.optString("id", ""),
                json.optString("name", "Unknown card"),
                json.optString("set", "?"),
                json.optString("set_name", "Unknown set"),
                json.optString("collector_number", "?"),
                json.optString("lang", "en"),
                json.optString("rarity", "unknown"),
                oracle,
                normal,
                art,
                usd,
                usdFoil
        );
    }

    private static String nullable(String value) {
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) return null;
        return value;
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        }
        return builder.toString();
    }

    private static String encQuery(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encPath(String value) {
        return encQuery(value).replace("%2F", "-");
    }

    private static String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isEmpty() ? e.getClass().getSimpleName() : message;
    }

    public void close() {
        executor.shutdownNow();
        artworkExecutor.shutdownNow();
    }

    private static final class ScoredCandidate {
        final ScryfallCard card;
        final double score;

        ScoredCandidate(ScryfallCard card, double score) {
            this.card = card;
            this.score = score;
        }
    }
}
