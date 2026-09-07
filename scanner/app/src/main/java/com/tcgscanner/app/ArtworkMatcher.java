package com.tcgscanner.app;

import android.graphics.Bitmap;
import android.graphics.Color;

public final class ArtworkMatcher {
    private ArtworkMatcher() {}

    public static double similarity(Bitmap cameraArtwork, Bitmap candidateCardImage) {
        long a = averageHash(cameraArtwork);
        long b = averageHash(candidateCardImage);
        int distance = Long.bitCount(a ^ b);
        return 1.0 - (distance / 64.0);
    }

    private static long averageHash(Bitmap source) {
        Bitmap scaled = Bitmap.createScaledBitmap(source, 8, 8, true);
        int[] gray = new int[64];
        long total = 0;
        int p = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int color = scaled.getPixel(x, y);
                int value = (Color.red(color) * 30 + Color.green(color) * 59 + Color.blue(color) * 11) / 100;
                gray[p++] = value;
                total += value;
            }
        }
        int average = (int) (total / 64L);
        long hash = 0L;
        for (int i = 0; i < 64; i++) {
            if (gray[i] >= average) hash |= (1L << i);
        }
        return hash;
    }
}
