package com.tcgscanner.app;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public final class CardCropper {
    private static final float CARD_ASPECT = 2.5f / 3.5f;

    private CardCropper() {}

    public static Bitmap rotate(Bitmap source, int degrees) {
        if (degrees == 0) return source;
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap centeredCard(Bitmap frame) {
        int frameW = frame.getWidth();
        int frameH = frame.getHeight();
        int cropW = Math.round(frameW * 0.78f);
        int cropH = Math.round(cropW / CARD_ASPECT);
        if (cropH > Math.round(frameH * 0.78f)) {
            cropH = Math.round(frameH * 0.78f);
            cropW = Math.round(cropH * CARD_ASPECT);
        }
        int left = Math.max(0, (frameW - cropW) / 2);
        int top = Math.max(0, (frameH - cropH) / 2);
        return Bitmap.createBitmap(frame, left, top, Math.min(cropW, frameW - left), Math.min(cropH, frameH - top));
    }

    public static Bitmap artworkRegion(Bitmap card) {
        int left = Math.round(card.getWidth() * 0.06f);
        int right = Math.round(card.getWidth() * 0.94f);
        int top = Math.round(card.getHeight() * 0.13f);
        int bottom = Math.round(card.getHeight() * 0.57f);
        return Bitmap.createBitmap(card, left, top, right - left, bottom - top);
    }
}
