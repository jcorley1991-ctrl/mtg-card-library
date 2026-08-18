import { mkdir, writeFile } from "node:fs/promises";
import { normalizeScryfallCard } from "../src/core/catalog.js";

const API="https://api.scryfall.com/bulk-data/default-cards";
const headers={"User-Agent":"mtg-card-library/0.1 (catalog sync)","Accept":"application/json;q=0.9,*/*;q=0.8"};

console.log("Fetching Scryfall bulk manifest...");
const manifestResponse=await fetch(API,{headers});
if(!manifestResponse.ok)throw new Error(`Scryfall bulk manifest failed: ${manifestResponse.status}`);
const manifest=await manifestResponse.json();
if(!manifest.download_uri)throw new Error("Scryfall bulk manifest did not include download_uri");

console.log(`Downloading ${manifest.name || manifest.type} bulk data...`);
const dataResponse=await fetch(manifest.download_uri,{headers});
if(!dataResponse.ok)throw new Error(`Scryfall bulk download failed: ${dataResponse.status}`);
const raw=await dataResponse.json();
if(!Array.isArray(raw))throw new Error("Scryfall bulk payload was not an array");

const cards=raw.filter(card=>card.games?.includes("paper")).map(normalizeScryfallCard);
const payload={meta:{source:"Scryfall default_cards",sourceUpdatedAt:manifest.updated_at||null,generatedAt:new Date().toISOString(),count:cards.length},cards};
await mkdir(new URL("../data/",import.meta.url),{recursive:true});
await writeFile(new URL("../data/catalog.json",import.meta.url),JSON.stringify(payload));
console.log(`Wrote ${cards.length} paper printings to data/catalog.json`);
