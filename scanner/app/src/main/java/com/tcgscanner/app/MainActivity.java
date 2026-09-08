package com.tcgscanner.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.widget.Button;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends ComponentActivity implements MtgCardAnalyzer.Listener {
    private static final int CAMERA_PERMISSION_REQUEST = 41;

    private PreviewView previewView;
    private TextView statusText;
    private Button catalogButton;

    private ExecutorService cameraExecutor;
    private ExecutorService catalogExecutor;
    private MtgCardAnalyzer analyzer;
    private CatalogDatabase catalogDatabase;
    private ToneGenerator scanTone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        statusText = findViewById(R.id.statusText);
        catalogButton = findViewById(R.id.catalogButton);

        cameraExecutor = Executors.newSingleThreadExecutor();
        catalogExecutor = Executors.newSingleThreadExecutor();
        analyzer = new MtgCardAnalyzer(this);
        catalogDatabase = new CatalogDatabase(getApplicationContext());
        scanTone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75);

        statusText.setText("Ready to scan");
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
                statusText.setText("Ready to scan");
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
        // Rapid scan mode: acceptance feedback is the tone. Do not download images,
        // render card details, or calculate display data on the scan screen.
        saveScanInBackground(card, confidence);
        runOnUiThread(this::playScanTone);
    }

    private void playScanTone() {
        if (scanTone != null) {
            scanTone.startTone(ToneGenerator.TONE_PROP_ACK, 85);
        }
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

    @Override
    public void onRecognitionFailed(String reason) {
        // Normal missed frames stay silent so the scan screen does not flicker with text.
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
        if (scanTone != null) scanTone.release();
        super.onDestroy();
    }
}
