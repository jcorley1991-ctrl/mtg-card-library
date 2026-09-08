package com.tcgscanner.app;

public class ScryfallCard {
    public final String id;
    public final String name;
    public final String set;
    public final String setName;
    public final String collectorNumber;
    public final String lang;
    public final String rarity;
    public final String oracleText;
    public final String imageUrl;
    public final String artworkUrl;
    public final String usd;
    public final String usdFoil;

    public ScryfallCard(
            String id,
            String name,
            String set,
            String setName,
            String collectorNumber,
            String lang,
            String rarity,
            String oracleText,
            String imageUrl,
            String artworkUrl,
            String usd,
            String usdFoil
    ) {
        this.id = id;
        this.name = name;
        this.set = set;
        this.setName = setName;
        this.collectorNumber = collectorNumber;
        this.lang = lang;
        this.rarity = rarity;
        this.oracleText = oracleText;
        this.imageUrl = imageUrl;
        this.artworkUrl = artworkUrl;
        this.usd = usd;
        this.usdFoil = usdFoil;
    }
}
