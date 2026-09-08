package com.tcgscanner.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OcrCardParser {
    private static final String LANGUAGES = "EN|DE|ES|FR|IT|JA|JP|KO|PT|RU|ZHS|ZHT";

    private static final Pattern COLLECTOR_FRACTION = Pattern.compile(
            "(?<![A-Za-z0-9])([0-9]{1,4}[A-Za-z]?)\\s*[/|]\\s*[0-9]{1,4}(?![A-Za-z0-9])",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SET_LANG = Pattern.compile(
            "\\b([A-Z0-9]{2,6})\\b\\s*(?:[•·●*|/\\\\\\-–—.:]+\\s*)?(" + LANGUAGES + ")\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern LANG_SET = Pattern.compile(
            "\\b(" + LANGUAGES + ")\\b\\s*(?:[•·●*|/\\\\\\-–—.:]+\\s*)?([A-Z0-9]{2,6})\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MANA_ONLY = Pattern.compile("^[WUBRGCXYZ0-9+/{}()\\s]+$");

    private OcrCardParser() {}

    public static OcrCardData parse(String rawText) {
        if (rawText == null) rawText = "";
        String normalizedRaw = normalizeOcrPunctuation(rawText);
        String[] rawLines = normalizedRaw.split("\\r?\\n");
        List<String> lines = new ArrayList<>();
        for (String raw : rawLines) {
            String line = raw.trim().replaceAll("\\s+", " ");
            if (!line.isEmpty()) lines.add(line);
        }

        String collector = null;
        String set = null;
        String lang = "en";

        Matcher collectorMatcher = COLLECTOR_FRACTION.matcher(normalizedRaw);
        if (collectorMatcher.find()) {
            collector = collectorMatcher.group(1);
        }

        String upper = normalizedRaw.toUpperCase(Locale.US);
        Matcher setLang = SET_LANG.matcher(upper);
        if (setLang.find()) {
            set = cleanSetCode(setLang.group(1));
            lang = normalizeLanguage(setLang.group(2));
        } else {
            Matcher langSet = LANG_SET.matcher(upper);
            if (langSet.find()) {
                lang = normalizeLanguage(langSet.group(1));
                set = cleanSetCode(langSet.group(2));
            }
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
            if (line.toUpperCase(Locale.US).matches(
                    ".*\\b(CREATURE|INSTANT|SORCERY|ENCHANTMENT|ARTIFACT|LAND|PLANESWALKER|BATTLE)\\b.*")) continue;
            if (line.matches(".*\\d{3,}.*")) continue;
            String cleaned = line.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9'’,.\\- ]+$", "").trim();
            if (cleaned.length() >= 2) return cleaned;
        }
        return null;
    }

    private static String normalizeOcrPunctuation(String value) {
        return value
                .replace('∙', '•')
                .replace('⋅', '•')
                .replace('·', '•')
                .replace('—', '-')
                .replace('–', '-');
    }

    private static String cleanSetCode(String value) {
        if (value == null) return null;
        String cleaned = value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.US);
        return cleaned.length() >= 2 && cleaned.length() <= 6 ? cleaned : null;
    }

    private static String normalizeLanguage(String value) {
        String v = value.toLowerCase(Locale.US);
        if ("jp".equals(v)) return "ja";
        return v;
    }
}
