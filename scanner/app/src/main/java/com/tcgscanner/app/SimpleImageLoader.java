package com.tcgscanner.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SimpleImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    public interface Callback {
        void onLoaded(Bitmap bitmap);
    }

    private SimpleImageLoader() {}

    public static void load(String url, Callback callback) {
        EXECUTOR.execute(() -> {
            Bitmap bitmap = download(url);
            if (bitmap != null) callback.onLoaded(bitmap);
        });
    }

    public static Bitmap download(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(7000);
            connection.setRequestProperty("User-Agent", "TCGScanner/0.1 Android");
            connection.connect();
            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
