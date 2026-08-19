import { createCollectionItem } from "../core/collection.js";
import { $, h, money, state, cardMap, persist } from "./state.js";

let refresh=()=>{};
export function setRefreshHandler(fn){refresh=typeof fn==="function"?fn:()=>{};}

export function addDeck(card,quantity){
  if(!card||quantity<=0)return;
  const deck=state.decks[0];
  const found=deck.entries.find(x=>x.cardId===card.id&&x.finish==="nonfoil"&&x.language==="en");
  found?found.quantity+=quantity:deck.entries.push({cardId:card.id,oracleId:card.oracleId,name:card.name,quantity,finish:"nonfoil",language:"en",section:"main"});
  persist();refresh("decks");
}
export function addBuy(card,quantity){
  if(!card||quantity<=0)return;
  const found=state.buyList.find(x=>x.cardId===card.id);
  found?found.quantity+=quantity:state.buyList.push({cardId:card.id,oracleId:card.oracleId,name:card.name,quantity,finish:"nonfoil",targetPrice:null,priority:"normal"});
  persist();refresh("buy");
}

export function details(card){
  const dialog=$("#cardDialog");
  const owned=state.collection.filter(x=>x.cardId===card.id).reduce((n,x)=>n+x.quantity,0);
  const prices=[["USD",card.prices?.usd],["USD Foil",card.prices?.usdFoil],["USD Etched",card.prices?.usdEtched],["EUR",card.prices?.eur],["EUR Foil",card.prices?.eurFoil],["MTGO tix",card.prices?.tix]].filter(([,v])=>v!=null);
  dialog.querySelector(".dialog-content").innerHTML=`<div class="detail-grid"><img src="${h(card.image)}" alt="${h(card.name)}"><div><div class="card-title-row"><h2>${h(card.name)}</h2><button id="closeDialog">Close</button></div><p><strong>${h(card.manaCost||"")}</strong> · ${h(card.typeLine)}</p><p>${h(card.oracleText||"No Oracle text.")}</p><p><strong>Printing:</strong> ${h(card.setCode)} #${h(card.collectorNumber)} · ${h(card.setName)}</p><p><strong>Rarity:</strong> ${h(card.rarity)} · <strong>Language:</strong> ${h(card.language)} · <strong>Finishes:</strong> ${h((card.finishes||[]).join(", ")||"unknown")}</p><p><strong>Artist:</strong> ${h(card.artist||"unknown")} · <strong>Released:</strong> ${h(card.releasedAt||"unknown")}</p><p><strong>Owned exact printing:</strong> ${owned}</p><p><strong>Legal:</strong> ${h(Object.entries(card.legalities||{}).filter(([,v])=>v==="legal").map(([k])=>k).join(", ")||"none listed")}</p><div class="metric-grid">${prices.length?prices.map(([k,v])=>`<div class="metric"><span>${h(k)}</span><strong>${k==="MTGO tix"?h(v):money(v)}</strong></div>`).join(""):'<div class="metric"><span>Price</span><strong>Unknown</strong></div>'}</div></div></div>`;
  dialog.querySelector("#closeDialog").onclick=()=>dialog.close();
  dialog.showModal();
}

export function cardNode(card){
  const node=$("#cardTemplate").content.cloneNode(true);
  node.querySelector(".card-name").textContent=card.name;
  node.querySelector(".rarity").textContent=card.rarity;
  node.querySelector(".type-line").textContent=`${card.setCode} #${card.collectorNumber} · ${card.typeLine}`;
  node.querySelector(".oracle").textContent=card.oracleText;
  node.querySelector(".price").textContent=money(card.prices?.usd);
  const image=node.querySelector(".card-image");image.src=card.image;image.alt=`${card.name} card`;
  node.querySelector(".chips").innerHTML=[card.manaCost||"No mana cost",card.colorIdentity?.join("")||"Colorless",...(card.finishes||[])].map(x=>`<span class="chip">${h(x)}</span>`).join("");
  node.querySelector('[data-action="details"]').onclick=()=>details(card);
  node.querySelector('[data-action="add-collection"]').onclick=()=>{state.collection.push(createCollectionItem(card,{location:state.locations[0]}));persist();refresh("collection");};
  node.querySelector('[data-action="add-deck"]').onclick=()=>addDeck(card,1);
  node.querySelector('[data-action="add-buy"]').onclick=()=>addBuy(card,1);
  return node;
}
