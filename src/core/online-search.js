import { normalizeScryfallCard } from "./catalog.js";

const DIRECT_ENDPOINT = "https://api.scryfall.com/cards/search";
const memoryCache = new Map();
const CACHE_TTL_MS = 2 * 60 * 1000;
const MAX_CACHE_ENTRIES = 40;

const quote = value => `"${String(value ?? "").replace(/\\/g, "\\\\").replace(/"/g, '\\"')}"`;
const escapeRegex = value => String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

function stripFriendlyTerms(raw, friendly) {
  let text = String(raw || "");
  for (const applied of friendly?.applied || []) {
    text = text.replace(new RegExp(`(^|\\s)${escapeRegex(applied.term)}(?=\\s|$)`, "ig"), " ");
  }
  return text.replace(/\s+/g, " ").trim();
}

function looksLikeScryfallSyntax(text) {
  return /(?:^|\\s)(?:name|n|oracle|o|type|t|color|c|identity|id|rarity|r|set|s|edition|e|format|f|game|mana|m|mv|cmc|power|pow|toughness|tou|loyalty|loy|artist|a|flavor|ft|lang|language|is|not|usd|eur|date|year|keyword|kw):|(?:^|\\s)(?:id|c|mv|cmc|pow|tou|usd|eur)[<>]=?/i.test(text);
}

function broadTextClause(text) {
  const value = String(text || "").trim();
  if (!value) return "";
  if (looksLikeScryfallSyntax(value)) return value;
  const q = quote(value);
  return `(${q} or o:${q} or t:${q} or flavor:${q} or artist:${q})`;
}

export function buildScryfallQuery(filters = {}) {
  const friendly = filters.friendly || { applied: [] };
  const clauses = [];
  const leftover = stripFriendlyTerms(filters.raw || "", friendly);
  const broad = broadTextClause(leftover);
  if (broad) clauses.push(broad);
  for (const applied of friendly.applied || []) clauses.push(applied.clause);

  const color = filters.color;
  if (color && color !== "any") {
    if (color === "colorless") clauses.push("id=c");
    else clauses.push(`id${filters.colorMode === "inclusive" ? ">=" : "="}${String(color).toUpperCase()}`);
  }
  if (filters.rarity && filters.rarity !== "any") clauses.push(`r:${filters.rarity}`);
  if (filters.set && String(filters.set).trim()) clauses.push(`set:${String(filters.set).trim().toLowerCase()}`);
  if (filters.type && String(filters.type).trim()) clauses.push(`t:${quote(String(filters.type).trim())}`);
  if (filters.format && filters.format !== "any") clauses.push(`f:${filters.format}`);
  if (filters.mvMin !== "" && filters.mvMin != null) clauses.push(`mv>=${Number(filters.mvMin)}`);
  if (filters.mvMax !== "" && filters.mvMax != null) clauses.push(`mv<=${Number(filters.mvMax)}`);
  if (filters.priceMax !== "" && filters.priceMax != null) clauses.push(`usd<=${Number(filters.priceMax)}`);

  if (!clauses.length) return "";
  clauses.push("game:paper");
  return clauses.join(" ");
}

function endpointForRuntime() {
  if (typeof location !== "undefined" && location.hostname === "appassets.androidplatform.net") {
    return `${location.origin}/api/scryfall/search`;
  }
  return DIRECT_ENDPOINT;
}

function trimCache() {
  while (memoryCache.size > MAX_CACHE_ENTRIES) memoryCache.delete(memoryCache.keys().next().value);
}

export async function searchScryfall(filters = {}, options = {}) {
  const query = buildScryfallQuery(filters);
  if (!query) return { query: "", cards: [], totalCards: 0, hasMore: false, source: "idle" };

  const key = query;
  const cached = memoryCache.get(key);
  if (!options.noCache && cached && Date.now() - cached.at < CACHE_TTL_MS) return cached.value;

  const params = new URLSearchParams({
    q: query,
    unique: "prints",
    order: "name",
    include_extras: "true"
  });
  const fetchImpl = options.fetchImpl || fetch;
  const response = await fetchImpl(`${endpointForRuntime()}?${params}`, {
    signal: options.signal,
    cache: "no-store",
    headers: { Accept: "application/json;q=0.9,*/*;q=0.8" }
  });

  let payload = null;
  try { payload = await response.json(); } catch { payload = null; }
  if (response.status === 404) return { query, cards: [], totalCards: 0, hasMore: false, source: "scryfall-live" };
  if (!response.ok) throw new Error(payload?.details || `Scryfall search failed (${response.status})`);
  if (!Array.isArray(payload?.data)) throw new Error("Scryfall returned an invalid card list");

  const value = {
    query,
    cards: payload.data.map(normalizeScryfallCard),
    totalCards: Number(payload.total_cards ?? payload.data.length),
    hasMore: !!payload.has_more,
    source: "scryfall-live"
  };
  memoryCache.set(key, { at: Date.now(), value });
  trimCache();
  return value;
}
