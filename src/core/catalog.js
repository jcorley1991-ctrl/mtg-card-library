export function normalizeScryfallCard(card) {
  const image = card.image_uris?.normal || card.card_faces?.find(f=>f.image_uris)?.image_uris?.normal || "";
  return {
    id: `scryfall:${card.id}`,
    oracleId: card.oracle_id ? `scryfall-oracle:${card.oracle_id}` : `scryfall:${card.id}`,
    externalIds: {
      scryfall: card.id,
      oracle: card.oracle_id || null,
      mtgo: card.mtgo_id || null,
      arena: card.arena_id || null,
      tcgplayer: card.tcgplayer_id || null,
      cardmarket: card.cardmarket_id || null
    },
    name: card.name,
    setCode: (card.set || "").toUpperCase(),
    setName: card.set_name || "",
    setType: card.set_type || "",
    collectorNumber: card.collector_number || "",
    releasedAt: card.released_at || null,
    colors: card.colors || [],
    colorIdentity: card.color_identity || [],
    rarity: card.rarity || "unknown",
    manaValue: card.cmc ?? null,
    manaCost: card.mana_cost || "",
    typeLine: card.type_line || "",
    oracleText: card.oracle_text || card.card_faces?.map(f=>f.oracle_text||"").filter(Boolean).join("\n//\n") || "",
    printedText: card.printed_text || "",
    flavorText: card.flavor_text || "",
    artist: card.artist || "",
    power: card.power ?? null,
    toughness: card.toughness ?? null,
    loyalty: card.loyalty ?? null,
    defense: card.defense ?? null,
    keywords: card.keywords || [],
    legalities: card.legalities || {},
    finishes: card.finishes || [],
    language: card.lang || "en",
    layout: card.layout || "",
    borderColor: card.border_color || "",
    frame: card.frame || "",
    fullArt: !!card.full_art,
    borderless: !!card.borderless,
    promo: !!card.promo,
    reserved: !!card.reserved,
    digital: !!card.digital,
    prices: {
      usd: toNumber(card.prices?.usd),
      usdFoil: toNumber(card.prices?.usd_foil),
      usdEtched: toNumber(card.prices?.usd_etched),
      eur: toNumber(card.prices?.eur),
      eurFoil: toNumber(card.prices?.eur_foil),
      tix: toNumber(card.prices?.tix)
    },
    purchaseUris: card.purchase_uris || {},
    relatedUris: card.related_uris || {},
    image,
    sourceUpdatedAt: card.updated_at || null
  };
}

function toNumber(value) {
  if (value == null || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

export async function loadCatalog(fetchImpl=fetch) {
  try {
    const res=await fetchImpl("./data/catalog.json",{cache:"no-store"});
    if(!res.ok)throw new Error(`catalog ${res.status}`);
    const payload=await res.json();
    if(!Array.isArray(payload.cards))throw new Error("catalog cards missing");
    return {cards:payload.cards,meta:payload.meta||{source:"local"}};
  } catch {
    const {seedCards}=await import("./data.js");
    return {cards:seedCards,meta:{source:"development-seed",generatedAt:null,fallback:true}};
  }
}
