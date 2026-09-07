package com.tcgscanner.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CatalogActivity extends ComponentActivity {
    private TextView summaryText;
    private ListView catalogList;
    private CatalogDatabase database;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        summaryText = findViewById(R.id.catalogSummary);
        catalogList = findViewById(R.id.catalogList);
        database = new CatalogDatabase(this);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCatalog();
    }

    private void loadCatalog() {
        executor.execute(() -> {
            int total = database.getTotalCount();
            int unique = database.getUniqueCount();
            List<CatalogDatabase.CatalogRow> rows = database.getRecentRows(2000);
            List<String> display = new ArrayList<>(rows.size());

            for (CatalogDatabase.CatalogRow row : rows) {
                String set = row.setCode == null ? "?" : row.setCode.toUpperCase(Locale.US);
                String verified = "NAME_ONLY".equals(row.confidence) ? " • printing unverified" : "";
                String price = row.usd == null || row.usd.isEmpty() ? "" : " • $" + row.usd;
                display.add(row.name + "  ×" + row.quantity
                        + "\n" + set + " #" + row.collectorNumber
                        + " • " + row.lang.toUpperCase(Locale.US)
                        + price + verified);
            }

            runOnUiThread(() -> {
                summaryText.setText(total + " cards • " + unique + " unique printings");
                catalogList.setAdapter(new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        display
                ));
            });
        });
    }

    @Override
    protected void onDestroy() {
        if (executor != null) executor.shutdownNow();
        if (database != null) database.close();
        super.onDestroy();
    }
}
