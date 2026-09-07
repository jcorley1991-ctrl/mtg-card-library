package com.tcgscanner.app;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.atomic.AtomicBoolean;

public class MtgCardAnalyzer implements ImageAnalysis.Analyzer {
    private static final long ANALYSIS_INTERVAL_MS = 180L;

    public interface Listener {
        void onStatus(String message);
        void onCardRecognized(ScryfallCard card, RecognitionConfidence confidence);
        void onRecognitionFailed(String reason);
    }

    private final Listener listener;
    private final TextRecognizer recognizer;
    private final ScryfallClient scryfallClient;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private long lastAnalysisAt = 0L;

    private String lastAcceptedIdentity = null;
    private String lastAcceptedName = null;
    private String lastAcceptedSet = null;
    private String lastAcceptedCollector = null;
    private boolean cardGapObserved = true;

    public MtgCardAnalyzer(Listener listener) {
        this.listener = listener;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        this.scryfallClient = new ScryfallClient();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        long now = System.currentTimeMillis();
        if (now - lastAnalysisAt < ANALYSIS_INTERVAL_MS || !busy.compareAndSet(false, true)) {
            imageProxy.close();
            return;
        }
        lastAnalysisAt = now;

        final Bitmap centeredCard;
        try {
            Bitmap raw = imageProxy.toBitmap();
            Bitmap rotated = CardCropper.rotate(raw, imageProxy.getImageInfo().getRotationDegrees());
            centeredCard = CardCropper.centeredCard(rotated);
        } catch (Exception e) {
            busy.set(false);
            imageProxy.close();
            listener.onRecognitionFailed("Could not read camera frame.");
            return;
        }
        imageProxy.close();

        listener.onStatus("Reading card…");
        InputImage input = InputImage.fromBitmap(centeredCard, 0);
        recognizer.process(input)
                .addOnSuccessListener(text -> {
                    OcrCardData ocr = OcrCardParser.parse(text.getText());
                    if (ocr.nameCandidate == null && !ocr.hasExactPrintingKeys()) {
                        cardGapObserved = true;
                        busy.set(false);
                        if (lastAcceptedIdentity != null) {
                            listener.onStatus("Ready for next card");
                        } else {
                            listener.onRecognitionFailed("Hold steady. I need the card name or bottom printing line.");
                        }
                        return;
                    }

                    if (!cardGapObserved && appearsToBeLastCard(ocr)) {
                        busy.set(false);
                        listener.onStatus("Saved. Move to next card.");
                        return;
                    }

                    if (ocr.hasExactPrintingKeys()) {
                        listener.onStatus("Exact printing keys found. Verifying…");
                    } else {
                        listener.onStatus("Name found. Comparing artwork across printings…");
                    }

                    scryfallClient.resolve(ocr, centeredCard, new ScryfallClient.ResolveCallback() {
                        @Override
                        public void onResolved(ScryfallCard card, RecognitionConfidence confidence) {
                            String identity = cardIdentity(card);
                            boolean sameAsLast = identity.equals(lastAcceptedIdentity);
                            boolean accept = !sameAsLast || cardGapObserved;
                            busy.set(false);

                            if (accept) {
                                lastAcceptedIdentity = identity;
                                lastAcceptedName = card.name;
                                lastAcceptedSet = card.set;
                                lastAcceptedCollector = card.collectorNumber;
                                cardGapObserved = false;
                                listener.onCardRecognized(card, confidence);
                            }

                            if (!accept) {
                                listener.onStatus("Saved. Move to next card.");
                            } else if (confidence == RecognitionConfidence.NAME_ONLY) {
                                listener.onStatus("Saved card; printing is not verified yet. Move to next card.");
                            } else {
                                listener.onStatus("Saved: " + card.name + " • " + card.set.toUpperCase() + " #" + card.collectorNumber);
                            }
                        }

                        @Override
                        public void onFailure(String reason) {
                            busy.set(false);
                            listener.onRecognitionFailed(reason);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    busy.set(false);
                    listener.onRecognitionFailed("Text recognition failed. Try better lighting.");
                });
    }

    private boolean appearsToBeLastCard(OcrCardData ocr) {
        if (ocr.hasExactPrintingKeys()
                && lastAcceptedSet != null
                && lastAcceptedCollector != null
                && ocr.setCode.equalsIgnoreCase(lastAcceptedSet)
                && ocr.collectorNumber.equalsIgnoreCase(lastAcceptedCollector)) {
            return true;
        }
        return ocr.nameCandidate != null
                && lastAcceptedName != null
                && ocr.nameCandidate.equalsIgnoreCase(lastAcceptedName);
    }

    private static String cardIdentity(ScryfallCard card) {
        if (card.id != null && !card.id.trim().isEmpty()) return card.id;
        return String.valueOf(card.name) + "|" + String.valueOf(card.set)
                + "|" + String.valueOf(card.collectorNumber) + "|" + String.valueOf(card.lang);
    }

    public void close() {
        recognizer.close();
        scryfallClient.close();
    }
}
