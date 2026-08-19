package com.jcorley.mtgcardlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private static final String APP_ORIGIN = "https://appassets.androidplatform.net";
    private static final String APP_HOST = "appassets.androidplatform.net";
    private static final String SCRYFALL_SEARCH_PATH = "/api/scryfall/search";
    private WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        settings.setBlockNetworkLoads(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull WebResourceRequest request) {
                Uri uri = request.getUrl();
                WebResourceResponse liveSearch = proxyScryfall(uri);
                return liveSearch != null ? liveSearch : assetLoader.shouldInterceptRequest(uri);
            }

            @Override
            @SuppressWarnings("deprecation")
            public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull String url) {
                Uri uri = Uri.parse(url);
                WebResourceResponse liveSearch = proxyScryfall(uri);
                return liveSearch != null ? liveSearch : assetLoader.shouldInterceptRequest(uri);
            }

            @Override
            public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (APP_ORIGIN.equals(uri.getScheme() + "://" + uri.getHost())) {
                    return false;
                }
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            }
        });

        if (savedInstanceState == null) {
            webView.loadUrl(APP_ORIGIN + "/assets/www/index.html");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private WebResourceResponse proxyScryfall(Uri uri) {
        if (!APP_HOST.equals(uri.getHost()) || !SCRYFALL_SEARCH_PATH.equals(uri.getPath())) {
            return null;
        }

        HttpURLConnection connection = null;
        try {
            String encodedQuery = uri.getEncodedQuery();
            String upstreamUrl = "https://api.scryfall.com/cards/search"
                    + (encodedQuery == null || encodedQuery.isEmpty() ? "" : "?" + encodedQuery);
            connection = (HttpURLConnection) new URL(upstreamUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestProperty("User-Agent", "MTGCardLibrary/0.2 Android (+https://github.com/jcorley1991-ctrl/mtg-card-library)");
            connection.setRequestProperty("Accept", "application/json;q=0.9,*/*;q=0.8");

            int status = connection.getResponseCode();
            InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            byte[] body = readAll(input);
            String reason = connection.getResponseMessage();
            if (reason == null || reason.isEmpty()) reason = status < 400 ? "OK" : "Error";

            Map<String, String> headers = new HashMap<>();
            headers.put("Cache-Control", "no-store");
            return new WebResourceResponse(
                    "application/json",
                    "UTF-8",
                    status,
                    reason,
                    headers,
                    new ByteArrayInputStream(body)
            );
        } catch (Exception ignored) {
            byte[] body = "{\"object\":\"error\",\"details\":\"Android live search network failure\"}"
                    .getBytes(StandardCharsets.UTF_8);
            Map<String, String> headers = new HashMap<>();
            headers.put("Cache-Control", "no-store");
            return new WebResourceResponse(
                    "application/json",
                    "UTF-8",
                    502,
                    "Bad Gateway",
                    headers,
                    new ByteArrayInputStream(body)
            );
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static byte[] readAll(InputStream input) throws Exception {
        if (input == null) return new byte[0];
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            return out.toByteArray();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
