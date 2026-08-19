import { loadCatalog } from "../core/catalog.js";
import { loadState, saveState } from "../core/storage.js";
import { collectionValue, totalOwned } from "../core/collection.js";

const SEARCH_CACHE_KEY="mtg-card-library-search-cache-v1";

function loadSearchCache(){
  try{const value=JSON.parse(localStorage.getItem(SEARCH_CACHE_KEY)||"[]");return Array.isArray(value)?value:[];}catch{return[];}
}
function saveSearchCache(cards){
  try{
    const map=new Map(loadSearchCache().map(card=>[card.id,card]));
    for(const card of cards)map.set(card.id,card);
    localStorage.setItem(SEARCH_CACHE_KEY,JSON.stringify([...map.values()].slice(-500)));
  }catch{}
}

const loaded=await loadCatalog();
export const catalog=loaded.cards;
export const catalogMeta=loaded.meta;
for(const card of loadSearchCache())if(!catalog.some(x=>x.id===card.id))catalog.push(card);

export const cardMap=new Map();
export const printingIndex=new Map();
export const nameSetIndex=new Map();
export const nameIndex=new Map();

function indexCard(card){
  cardMap.set(card.id,card);
  printingIndex.set(`${card.setCode}|${card.collectorNumber}`,card);
  nameSetIndex.set(`${card.setCode}|${card.name.toLowerCase()}`,card);
  if(!nameIndex.has(card.name.toLowerCase()))nameIndex.set(card.name.toLowerCase(),card);
}
for(const card of catalog)indexCard(card);

export function registerCards(cards){
  for(const card of cards){if(!cardMap.has(card.id))catalog.push(card);indexCard(card);}
  saveSearchCache(cards);
}

const saved=loadState()||{};
export const state={
  collection:saved.collection||[],
  buyList:saved.buyList||[],
  decks:saved.decks||[],
  locations:saved.locations?.length?saved.locations:["Unsorted"]
};
if(!state.decks.length)state.decks=[{id:"deck-1",name:"My First Deck",format:"commander",entries:[]}];

export const $=selector=>document.querySelector(selector);
export const h=value=>String(value??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
export const money=value=>value==null?"Unknown":new Intl.NumberFormat("en-US",{style:"currency",currency:"USD"}).format(value);

export function header(){
  $("#ownedCount").textContent=`${totalOwned(state.collection)} cards owned`;
  $("#collectionValue").textContent=`${money(collectionValue(state.collection,cardMap))} collection`;
}
export function persist(){saveState(state);header();}

export function match(row){
  const name=(row.name||"").trim().toLowerCase();
  const set=(row.setCode||"").trim().toUpperCase();
  const number=String(row.collectorNumber||"").trim();
  return (set&&number&&printingIndex.get(`${set}|${number}`))
    ||(name&&set&&nameSetIndex.get(`${set}|${name}`))
    ||(name&&nameIndex.get(name))||null;
}
