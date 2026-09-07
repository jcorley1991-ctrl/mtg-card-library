package com.tcgscanner.app;

public class OcrCardData {
    public final String nameCandidate;
    public final String setCode;
    public final String collectorNumber;
    public final String language;
    public final String rawText;

    public OcrCardData(String nameCandidate, String setCode, String collectorNumber, String language, String rawText) {
        this.nameCandidate = nameCandidate;
        this.setCode = setCode;
        this.collectorNumber = collectorNumber;
        this.language = language;
        this.rawText = rawText;
    }

    public boolean hasExactPrintingKeys() {
        return setCode != null && collectorNumber != null;
    }
}
