package com.tcgscanner.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OcrCardParser {
    private static final Pattern COLLECTOR_FRACTION = Pattern.compile("\\b([0-9]{1,4}[A-Za-z]?)\\s*/\\s*[0-9]{1,4}\\b");
    private static final Pattern SET_LANG = Pattern.compile("\\b([A-Z0-9]{3,6})\\s*[•·\\-]\\s*(EN|DE|ES|FR|IT|JA|JP|KO|PT|RU|ZHS|ZHT)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOOSE_SET_LANG = Pattern.compile("\\b([A-Z0-9]{3,6})\\s+(EN|DE|ES|FR|IT|JA|JP|KO|PT|RU|ZHS|ZHT)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MANA_ONLY = Pattern.compile("^[WUBRGCXYZ0-9+/{}()\\s]+$");

    private OcrCardParser() {}

    public static OcrCardData parse(String rawText) {
        if (rawText == null) rawText = "";
        String[] rawLines = rawText.split("\\r?\\n");
        List<String> lines = new ArrayList<>();
        for (String raw : rawLines) {
            String line = raw.trim().replaceAll("\\s+", " ");
            if (!line.isEmpty()) lines.add(line);
        }

        String collector = null;
        String set = null;
        String lang = "en";

        Matcher collectorMatcher = COLLECTOR_FRACTION.matcher(rawText);
        if (collectorMatcher.find()) {
            collector = collectorMatcher.group(1);
        }

        Matcher setLang = SET_LANG.matcher(rawText.toUpperCase(Locale.US));
        if (!setLang.find()) {
            setLang = LOOSE_SET_LANG.matcher(rawText.toUpperCase(Locale.US));
        }
        if (setLang.find(0)) {
            set = setLang.group(1).toLowerCase(Locale.US);
            lang = normalizeLanguage(setLang.group(2));
        }

        String name = findNameCandidate(lines);
        return new OcrCardData(name, set, collector, lang, rawText);
    }

    private static String findNameCandidate(List<String> lines) {
        int limit = Math.min(lines.size(), 6);
        for (int i = 0; i < limit; i++) {
            String line = lines.get(i);
            if (line.length() < 2 || line.length() > 55) continue;
            if (COLLECTOR_FRACTION.matcher(line).find()) continue;
            if (MANA_ONLY.matcher(line.toUpperCase(Locale.US)).matches()) continue;
            if (line.matches(".*\\b(CREATURE|INSTANT|SORCERY|ENCHANTMENT|ARTIFACT|LAND|PLANESWALKER|BATTLE)\\b.*")) continue;
            if (line.matches(".*\\d{3,}.*")) continue;
            return line.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9'’,.\\- ]+$", "").trim();
        }
        return null;
    }

    private static String normalizeLanguage(String value) {
        String v = value.toLowerCase(Locale.US);
        if ("jp".equals(v)) return "ja";
        return v;
    }
}
