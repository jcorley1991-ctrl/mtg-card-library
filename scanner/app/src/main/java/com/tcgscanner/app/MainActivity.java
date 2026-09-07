package com.tcgscanner.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends ComponentActivity implements MtgCardAnalyzer.Listener {
    private static final int CAMERA_PERMISSION_REQUEST = 41;

    private PreviewView previewView;
    private TextView statusText;
    private ScrollView resultPanel;
    private ImageView cardImage;
    private TextView cardName;
    private TextView cardMeta;
    private TextView cardPrice;
    private TextView cardOracle;
    private Button catalogButton;

    private ExecutorService cameraExecutor;
    private ExecutorService catalogExecutor;
    private MtgCardAnalyzer analyzer;
    private CatalogDatabase catalogDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);
        resultPanel = findViewById(R.id.resultPanel);
        cardImage = findViewById(R.id.cardImage);
        cardName = findViewById(R.id.cardName);
        cardMeta = findViewById(R.id.cardMeta);
        cardPrice = findViewById(R.id.cardPrice);
        cardOracle = findViewById(R.id.cardOracle);
        catalogButton = findViewById(R.id.catalogButton);

        cameraExecutor = Executors.newSingleThreadExecutor();
        catalogExecutor = Executors.newSingleThreadExecutor();
        analyzer = new MtgCardAnalyzer(this);
        catalogDatabase = new CatalogDatabase(getApplicationContext());

        catalogButton.setOnClickListener(v -> startActivity(new Intent(this, CatalogActivity.class)));
        refreshCatalogCount();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (catalogDatabase != null && catalogExecutor != null) refreshCatalogCount();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = providerFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analysis.setAnalyzer(cameraExecutor, analyzer);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                );
                onStatus("Center one MTG card in the frame");
            } catch (Exception e) {
                onStatus("Camera failed: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onStatus(String message) {
        runOnUiThread(() -> statusText.setText(message));
    }

    @Override
    public void onCardRecognized(ScryfallCard card, RecognitionConfidence confidence) {
        saveScanInBackground(card, confidence);

        runOnUiThread(() -> {
            flashScanConfirmed();
            resultPanel.setVisibility(View.VISIBLE);
            cardName.setText(card.name);

            String confidenceText;
            switch (confidence) {
                case EXACT_METADATA:
                    confidenceText = "Exact printing verified from set + collector number";
                    break;
                case ARTWORK_MATCH:
                    confidenceText = "Exact printing selected by artwork match";
                    break;
                default:
                    confidenceText = "Printing not yet verified; saved as unverified";
                    break;
            }

            String meta = card.setName + " (" + card.set.toUpperCase(Locale.US) + ")"
                    + "  •  #" + card.collectorNumber
                    + "\n" + capitalize(card.rarity)
                    + "  •  " + card.lang.toUpperCase(Locale.US)
                    + "\n" + confidenceText;
            cardMeta.setText(meta);

            String price = card.usd != null ? "$" + card.usd : "Unavailable";
            String foilPrice = card.usdFoil != null ? "  •  Foil $" + card.usdFoil : "";
            cardPrice.setText("Market: " + price + foilPrice + "\nScryfall fallback price");
            cardOracle.setText(card.oracleText == null || card.oracleText.isEmpty()
                    ? "No Oracle text returned."
                    : card.oracleText);

            cardImage.setImageDrawable(null);
            if (card.imageUrl != null) {
                SimpleImageLoader.load(card.imageUrl, bitmap -> runOnUiThread(() -> cardImage.setImageBitmap(bitmap)));
            }
        });
    }

    private void saveScanInBackground(ScryfallCard card, RecognitionConfidence confidence) {
        catalogExecutor.execute(() -> {
            try {
                CatalogDatabase.SaveResult saved = catalogDatabase.saveScan(card, confidence);
                runOnUiThread(() -> catalogButton.setText("Catalog • " + saved.totalCards));
            } catch (Exception e) {
                onStatus("Card recognized, but catalog save failed: " + e.getMessage());
            }
        });
    }

    private void refreshCatalogCount() {
        catalogExecutor.execute(() -> {
            int total = catalogDatabase.getTotalCount();
            runOnUiThread(() -> catalogButton.setText("Catalog • " + total));
        });
    }

    private void flashScanConfirmed() {
        ViewGroup root = findViewById(android.R.id.content);
        View flash = new View(this);
        flash.setBackgroundColor(0xFFFFFFFF);
        flash.setAlpha(0f);
        root.addView(flash, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        flash.animate()
                .alpha(0.78f)
                .setDuration(70L)
                .withEndAction(() -> flash.animate()
                        .alpha(0f)
                        .setDuration(130L)
                        .withEndAction(() -> root.removeView(flash))
                        .start())
                .start();
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) return "Unknown rarity";
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    @Override
    public void onRecognitionFailed(String reason) {
        onStatus(reason);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (requestCode == CAMERA_PERMISSION_REQUEST) {
            onStatus("Camera permission is required to scan cards.");
        }
    }

    @Override
    protected void onDestroy() {
        if (analyzer != null) analyzer.close();
        if (cameraExecutor != null) cameraExecutor.shutdownNow();
        if (catalogExecutor != null) catalogExecutor.shutdownNow();
        if (catalogDatabase != null) catalogDatabase.close();
        super.onDestroy();
    }
}
