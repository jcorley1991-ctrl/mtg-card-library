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
    private static final long ANALYSIS_INTERVAL_MS = 1200L;

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
    private String lastCardId = null;
    private long lastCardAt = 0L;

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
                        busy.set(false);
                        listener.onRecognitionFailed("Hold steady. I need the card name or bottom printing line.");
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
                            long resolvedAt = System.currentTimeMillis();
                            boolean duplicate = card.id != null
                                    && card.id.equals(lastCardId)
                                    && resolvedAt - lastCardAt < 5000L;
                            lastCardId = card.id;
                            lastCardAt = resolvedAt;
                            busy.set(false);

                            if (!duplicate) {
                                listener.onCardRecognized(card, confidence);
                            }
                            if (confidence == RecognitionConfidence.NAME_ONLY) {
                                listener.onStatus("Card found, but exact printing is not verified. Keep it centered for another scan.");
                            } else {
                                listener.onStatus("Exact printing identified: " + card.set.toUpperCase() + " #" + card.collectorNumber);
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

    public void close() {
        recognizer.close();
        scryfallClient.close();
    }
}
