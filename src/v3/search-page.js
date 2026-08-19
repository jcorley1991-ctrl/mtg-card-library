import { normalizeFriendlySearch, filterCards } from "../core/search-v2.js";
import { searchScryfall, buildScryfallQuery } from "../core/online-search.js";
import { $, h, catalog, registerCards } from "./state.js";
import { cardNode } from "./cards.js";

const LIMIT=100;
const COLORS=[["any","Any color"],["W","White"],["U","Blue"],["B","Black"],["R","Red"],["G","Green"],["WU","Azorius"],["UB","Dimir"],["BR","Rakdos"],["RG","Gruul"],["GW","Selesnya"],["WB","Orzhov"],["UR","Izzet"],["BG","Golgari"],["RW","Boros"],["GU","Simic"],["GWU","Bant"],["WUB","Esper"],["UBR","Grixis"],["BRG","Jund"],["RGW","Naya"],["WBG","Abzan"],["URW","Jeskai"],["BGU","Sultai"],["RWB","Mardu"],["GUR","Temur"],["WUBRG","Five-color"],["colorless","Colorless"]];

export function renderSearch(){
  $("#search").innerHTML=`<div class="panel"><div class="card-title-row"><div><h2>Deep Card Search</h2><p class="muted">Search the full Magic catalog by name, rules text, set code, type, price, legality, color identity, or player terms such as <span class="tag">Orzhov ETB</span>, <span class="tag">graveyard hate</span>, <span class="tag">mana dork</span>, or <span class="tag">Voltron</span>.</p></div><span class="chip">LIVE CATALOG</span></div><div class="search-grid"><input id="q" placeholder="Card name, rules phrase, artist, or MTG terminology"><select id="color">${COLORS.map(([v,l])=>`<option value="${v}">${l}</option>`).join("")}</select><select id="colorMode"><option value="exact">Exact identity</option><option value="inclusive">Includes colors</option></select><select id="rarity"><option value="any">Any rarity</option><option>common</option><option>uncommon</option><option>rare</option><option>mythic</option><option>special</option><option>bonus</option></select><input id="set" placeholder="Set code, e.g. CMM"><input id="type" placeholder="Type / subtype"><select id="format"><option value="any">Any legality</option><option value="commander">Commander</option><option value="standard">Standard</option><option value="modern">Modern</option><option value="pioneer">Pioneer</option><option value="legacy">Legacy</option><option value="vintage">Vintage</option><option value="pauper">Pauper</option></select><input id="mvMin" type="number" min="0" placeholder="Min MV"><input id="mvMax" type="number" min="0" placeholder="Max MV"><input id="priceMax" type="number" min="0" step=".01" placeholder="Max USD"></div><p><button id="searchNow" class="primary">Search full catalog</button></p><div id="normalized" class="normalized">Normalized query: <strong>none</strong></div></div><div id="resultsMeta" class="muted">Type a search to query the full catalog.</div><div id="results" class="cards"></div>`;

  const ids=["q","color","colorMode","rarity","set","type","format","mvMin","mvMax","priceMax"];
  let timer=null,controller=null,serial=0;
  const values=()=>({raw:$("#q").value,color:$("#color").value,colorMode:$("#colorMode").value,rarity:$("#rarity").value,set:$("#set").value,type:$("#type").value,format:$("#format").value,mvMin:$("#mvMin").value,mvMax:$("#mvMax").value,priceMax:$("#priceMax").value});
  const fallback=(f,n)=>{let text=f.raw;for(const a of n.applied)text=text.replace(new RegExp(a.term.replace(/[.*+?^${}()|[\]\\]/g,"\\$&"),"ig")," ");return filterCards(catalog,{text,friendly:n,color:f.color,colorMode:f.colorMode,rarity:f.rarity,set:f.set.trim().toUpperCase()||"any",type:f.type,format:f.format,mvMin:f.mvMin,mvMax:f.mvMax,priceMax:f.priceMax});};

  const run=async()=>{
    const f=values(),friendly=normalizeFriendlySearch(f.raw),query=buildScryfallQuery({...f,friendly});
    $("#normalized").innerHTML=`Normalized query: <strong>${h(friendly.normalized||"none")}</strong>${friendly.applied.length?`<br><small>${friendly.applied.map(x=>`${h(x.term)} → ${h(x.clause)}`).join(" · ")}</small>`:""}${query?`<br><small>Full-catalog query: ${h(query)}</small>`:""}`;
    if(!query){$("#resultsMeta").textContent="Type a card name, rules phrase, set code, or MTG term to search the full catalog.";$("#results").replaceChildren(...catalog.slice(0,12).map(cardNode));return;}
    if(controller)controller.abort();controller=new AbortController();const mine=++serial;$("#resultsMeta").textContent="Searching full Magic catalog…";
    try{
      const result=await searchScryfall({...f,friendly},{signal:controller.signal});if(mine!==serial)return;
      registerCards(result.cards);const shown=result.cards.slice(0,LIMIT);
      $("#resultsMeta").textContent=`${result.totalCards} printings found · showing ${shown.length}${result.hasMore?" · refine search for additional results":""} · live Scryfall`;
      $("#results").replaceChildren(...shown.map(cardNode));
    }catch(error){
      if(error?.name==="AbortError"||mine!==serial)return;
      const local=fallback(f,friendly).slice(0,LIMIT);
      $("#resultsMeta").textContent=`Online search unavailable: ${error.message}. Showing ${local.length} cached/offline result${local.length===1?"":"s"}.`;
      $("#results").replaceChildren(...local.map(cardNode));
    }
  };
  const schedule=()=>{clearTimeout(timer);timer=setTimeout(run,500);};
  ids.forEach(id=>$("#"+id).addEventListener("input",schedule));
  $("#searchNow").onclick=()=>{clearTimeout(timer);run();};
  $("#q").addEventListener("keydown",event=>{if(event.key==="Enter"){event.preventDefault();clearTimeout(timer);run();}});
  $("#results").replaceChildren(...catalog.slice(0,12).map(cardNode));
}
