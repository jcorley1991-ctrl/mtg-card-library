import { exportState, importState, collectionToCsv } from "../core/storage.js";
import { parseCollectionCsv, parseDeckText } from "../core/importers.js";
import { addLocation } from "../core/locations.js";
import { createCollectionItem } from "../core/collection.js";
import { $, h, state, catalog, catalogMeta, match, persist } from "./state.js";
import { addDeck } from "./cards.js";
import { renderCollection } from "./library-pages.js";

function download(name,text,type="application/json"){const a=document.createElement("a");a.href=URL.createObjectURL(new Blob([text],{type}));a.download=name;a.click();URL.revokeObjectURL(a.href);}

export function renderImports(){
  $("#import").innerHTML=`<div class="panel"><h2>Import / Export</h2><button id="backup">Export backup</button> <button id="csvOut">Export collection CSV</button></div><div class="cards"><div class="panel"><h3>Backup JSON</h3><textarea id="jsonIn" class="field" rows="8"></textarea><p><button id="jsonGo">Import backup</button></p><div id="jsonStatus" class="muted"></div></div><div class="panel"><h3>Collection CSV</h3><textarea id="csvIn" class="field" rows="8" placeholder="Card Name,Set Code,Collector Number,Qty,Condition,Binder"></textarea><p><button id="csvPreview">Preview</button> <button id="csvGo">Import valid rows</button></p><div id="csvStatus" class="muted"></div></div><div class="panel"><h3>Deck text</h3><textarea id="deckIn" class="field" rows="8" placeholder="1 Sol Ring [CMM] 410"></textarea><p><button id="deckPreview">Preview</button> <button id="deckGo">Add to deck</button></p><div id="deckStatus" class="muted"></div></div></div>`;
  $("#backup").onclick=()=>download("mtg-card-library-backup.json",exportState(state));$("#csvOut").onclick=()=>download("mtg-collection.csv",collectionToCsv(state.collection),"text/csv");
  $("#jsonGo").onclick=()=>{try{const x=importState($("#jsonIn").value);state.collection=x.collection||[];state.buyList=x.buyList||[];state.decks=x.decks?.length?x.decks:state.decks;state.locations=x.locations?.length?x.locations:["Unsorted"];persist();$("#jsonStatus").textContent="Import complete";renderCollection();}catch(error){$("#jsonStatus").textContent=error.message;}};
  let collectionPreview={rows:[],errors:[]},deckPreview={rows:[],errors:[]};
  const summary=(element,preview,word)=>{element.innerHTML=`${preview.rows.length} valid ${word}(s), ${preview.errors.length} error(s)${preview.errors.length?`<br>${preview.errors.slice(0,8).map(e=>`${word} ${e.row}: ${h(e.message)}`).join("<br>")}`:""}`;};
  $("#csvPreview").onclick=()=>{collectionPreview=parseCollectionCsv($("#csvIn").value);summary($("#csvStatus"),collectionPreview,"row");};
  $("#csvGo").onclick=()=>{$("#csvPreview").click();let added=0,miss=0;for(const row of collectionPreview.rows){const card=match(row);if(!card){miss++;continue;}state.locations=addLocation(state.locations,row.location);state.collection.push(createCollectionItem(card,row));added++;}persist();$("#csvStatus").textContent=`Imported ${added}; unmatched ${miss}; invalid ${collectionPreview.errors.length}`;renderCollection();};
  $("#deckPreview").onclick=()=>{deckPreview=parseDeckText($("#deckIn").value);summary($("#deckStatus"),deckPreview,"line");};
  $("#deckGo").onclick=()=>{$("#deckPreview").click();let added=0,miss=0;for(const row of deckPreview.rows){const card=match(row);if(!card){miss++;continue;}addDeck(card,row.quantity);added+=row.quantity;}$("#deckStatus").textContent=`Added ${added} copies; unmatched ${miss}; invalid ${deckPreview.errors.length}`;};
}

export function renderScanner(){$("#scanner").innerHTML='<div class="panel"><h2>Scanner</h2><p class="muted">Planned next-stage capability. Ambiguous printings must be confirmed before inventory changes.</p></div>';}
export function renderSettings(){$("#settings").innerHTML=`<div class="panel"><h2>Settings / Data Sync</h2><div class="metric-grid"><div class="metric"><span>Fallback catalog</span><strong>${h(catalogMeta.source||"development-seed")}</strong></div><div class="metric"><span>Cached/loaded printings</span><strong>${catalog.length}</strong></div><div class="metric"><span>Search provider</span><strong>Live Scryfall</strong></div></div><p>Search uses the live catalog when online and keeps recently returned printings cached locally. Collection metadata remains separate from provider refreshes.</p></div>`;}
