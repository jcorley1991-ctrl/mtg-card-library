package com.tcgscanner.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public final class CatalogDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "tcg_catalog.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "catalog";

    public CatalogDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "printing_id TEXT PRIMARY KEY,"
                + "name TEXT NOT NULL,"
                + "set_code TEXT,"
                + "set_name TEXT,"
                + "collector_number TEXT,"
                + "lang TEXT,"
                + "rarity TEXT,"
                + "oracle_text TEXT,"
                + "image_url TEXT,"
                + "usd TEXT,"
                + "usd_foil TEXT,"
                + "confidence TEXT,"
                + "quantity INTEGER NOT NULL DEFAULT 1,"
                + "first_scanned_at INTEGER NOT NULL,"
                + "last_scanned_at INTEGER NOT NULL"
                + ")");
        db.execSQL("CREATE INDEX idx_catalog_last_scanned ON " + TABLE + "(last_scanned_at DESC)");
        db.execSQL("CREATE INDEX idx_catalog_name ON " + TABLE + "(name COLLATE NOCASE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 only. Future migrations must preserve catalog data.
    }

    public SaveResult saveScan(ScryfallCard card, RecognitionConfidence confidence) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        String key = stableKey(card);
        int quantity;

        db.beginTransaction();
        try {
            int existingQuantity = 0;
            try (Cursor cursor = db.rawQuery(
                    "SELECT quantity FROM " + TABLE + " WHERE printing_id = ?",
                    new String[]{key})) {
                if (cursor.moveToFirst()) existingQuantity = cursor.getInt(0);
            }

            ContentValues values = valuesFor(card, confidence, now);
            if (existingQuantity > 0) {
                quantity = existingQuantity + 1;
                values.put("quantity", quantity);
                db.update(TABLE, values, "printing_id = ?", new String[]{key});
            } else {
                quantity = 1;
                values.put("printing_id", key);
                values.put("quantity", quantity);
                values.put("first_scanned_at", now);
                db.insertOrThrow(TABLE, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return new SaveResult(quantity, getTotalCount(), getUniqueCount());
    }

    private ContentValues valuesFor(ScryfallCard card, RecognitionConfidence confidence, long now) {
        ContentValues values = new ContentValues();
        values.put("name", safe(card.name));
        values.put("set_code", safe(card.set));
        values.put("set_name", safe(card.setName));
        values.put("collector_number", safe(card.collectorNumber));
        values.put("lang", safe(card.lang));
        values.put("rarity", safe(card.rarity));
        values.put("oracle_text", safe(card.oracleText));
        values.put("image_url", safe(card.imageUrl));
        values.put("usd", safe(card.usd));
        values.put("usd_foil", safe(card.usdFoil));
        values.put("confidence", confidence.name());
        values.put("last_scanned_at", now);
        return values;
    }

    public int getTotalCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(quantity), 0) FROM " + TABLE, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public int getUniqueCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public List<CatalogRow> getRecentRows(int limit) {
        List<CatalogRow> rows = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT name,set_code,set_name,collector_number,lang,usd,usd_foil,confidence,quantity "
                + "FROM " + TABLE + " ORDER BY last_scanned_at DESC LIMIT ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(limit)})) {
            while (cursor.moveToNext()) {
                rows.add(new CatalogRow(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getInt(8)
                ));
            }
        }
        return rows;
    }

    private static String stableKey(ScryfallCard card) {
        if (card.id != null && !card.id.trim().isEmpty()) return card.id;
        return safe(card.name) + "|" + safe(card.set) + "|" + safe(card.collectorNumber) + "|" + safe(card.lang);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static final class SaveResult {
        public final int printingQuantity;
        public final int totalCards;
        public final int uniquePrintings;

        SaveResult(int printingQuantity, int totalCards, int uniquePrintings) {
            this.printingQuantity = printingQuantity;
            this.totalCards = totalCards;
            this.uniquePrintings = uniquePrintings;
        }
    }

    public static final class CatalogRow {
        public final String name;
        public final String setCode;
        public final String setName;
        public final String collectorNumber;
        public final String lang;
        public final String usd;
        public final String usdFoil;
        public final String confidence;
        public final int quantity;

        CatalogRow(String name, String setCode, String setName, String collectorNumber,
                   String lang, String usd, String usdFoil, String confidence, int quantity) {
            this.name = name;
            this.setCode = setCode;
            this.setName = setName;
            this.collectorNumber = collectorNumber;
            this.lang = lang;
            this.usd = usd;
            this.usdFoil = usdFoil;
            this.confidence = confidence;
            this.quantity = quantity;
        }
    }
}
