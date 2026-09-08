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
    private static final long ANALYSIS_INTERVAL_MS = 80L;

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

        Bitmap metadata = CardCropper.metadataRegion(centeredCard);
        recognizer.process(InputImage.fromBitmap(metadata, 0))
                .addOnSuccessListener(text -> {
                    OcrCardData metadataOcr = OcrCardParser.parse(text.getText());
                    if (metadataOcr.hasExactPrintingKeys()) {
                        resolveCandidate(metadataOcr, centeredCard);
                    } else {
                        processFullCard(centeredCard);
                    }
                })
                .addOnFailureListener(e -> processFullCard(centeredCard));
    }

    private void processFullCard(Bitmap centeredCard) {
        recognizer.process(InputImage.fromBitmap(centeredCard, 0))
                .addOnSuccessListener(text -> {
                    OcrCardData ocr = OcrCardParser.parse(text.getText());
                    if (ocr.nameCandidate == null && !ocr.hasExactPrintingKeys()) {
                        cardGapObserved = true;
                        busy.set(false);
                        return;
                    }
                    resolveCandidate(ocr, centeredCard);
                })
                .addOnFailureListener(e -> {
                    busy.set(false);
                    listener.onRecognitionFailed("Text recognition failed. Try better lighting.");
                });
    }

    private void resolveCandidate(OcrCardData ocr, Bitmap centeredCard) {
        if (!cardGapObserved && appearsToBeLastCard(ocr)) {
            busy.set(false);
            return;
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
            }

            @Override
            public void onFailure(String reason) {
                busy.set(false);
                listener.onRecognitionFailed(reason);
            }
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
