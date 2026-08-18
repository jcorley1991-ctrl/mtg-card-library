import{colorTerms,conceptTerms,terminologyKeys}from"./terminology.js";
const reEscape=s=>s.replace(/[.*+?^${}()|[\]\\]/g,"\\$&");
export function normalizeFriendlySearch(input){
  let working=String(input||"").trim();const applied=[],semantics={colorIdentity:null,concepts:[],format:null};
  for(const term of terminologyKeys){const rx=new RegExp(`(^|\\s)${reEscape(term)}(?=\\s|$)`,`i`);if(!rx.test(working))continue;
    if(term in colorTerms){const colors=colorTerms[term],clause=colors.length?`id=${colors.join("")}`:"id=c";working=working.replace(rx,(_,p)=>`${p}${clause}`);applied.push({term,clause,kind:"color"});semantics.colorIdentity=colors;}
    else{const r=conceptTerms[term],clause=r.query;working=working.replace(rx,(_,p)=>`${p}${clause}`);applied.push({term,clause,kind:"concept"});semantics.concepts.push(term);if(r.format)semantics.format=r.format;}
  }
  return{original:input,normalized:working.replace(/\s+/g," ").trim(),applied,semantics};
}
function conceptMatch(card,term){const r=conceptTerms[term];if(!r)return true;const oracle=(card.oracleText||"").toLowerCase(),type=(card.typeLine||"").toLowerCase(),keywords=(card.keywords||[]).map(x=>x.toLowerCase());
  if(r.format&&card.legalities?.[r.format]!=="legal")return false;if(r.type&&!type.includes(r.type))return false;if(r.typeAny&&!r.typeAny.some(x=>type.includes(x)))return false;if(r.keyword&&!keywords.includes(r.keyword)&&!oracle.includes(r.keyword))return false;if(r.all&&!r.all.every(x=>oracle.includes(x)))return false;if(r.any&&!r.any.some(x=>oracle.includes(x))){if(!(r.powerAtLeast!=null&&Number(card.power)>=r.powerAtLeast))return false;}if(r.powerAtLeast!=null&&!r.any&&!(Number(card.power)>=r.powerAtLeast))return false;return true;}
export function filterCards(cards,f={}){const q=(f.text||"").trim().toLowerCase();return cards.filter(card=>{
  if(q){const hay=[card.name,card.oracleText,card.printedText,card.typeLine,card.setName,card.setCode,card.collectorNumber,card.artist,card.flavorText,...(card.keywords||[])].join(" ").toLowerCase();if(!hay.includes(q))return false;}
  const friendly=f.friendly?.semantics;if(friendly?.colorIdentity){const w=friendly.colorIdentity;if(card.colorIdentity.length!==w.length||!w.every(c=>card.colorIdentity.includes(c)))return false;}for(const t of friendly?.concepts||[])if(!conceptMatch(card,t))return false;
  if(f.color&&f.color!=="any"){if(f.color==="colorless"){if(card.colorIdentity.length)return false;}else{const w=f.color.split("");if(!w.every(c=>card.colorIdentity.includes(c)))return false;if(f.colorMode==="exact"&&card.colorIdentity.length!==w.length)return false;}}
  if(f.rarity&&f.rarity!=="any"&&card.rarity!==f.rarity)return false;if(f.set&&f.set!=="any"&&card.setCode!==f.set)return false;if(f.type&&!card.typeLine.toLowerCase().includes(f.type.toLowerCase()))return false;
  if(f.mvMin!==""&&f.mvMin!=null&&(card.manaValue==null||card.manaValue<Number(f.mvMin)))return false;if(f.mvMax!==""&&f.mvMax!=null&&(card.manaValue==null||card.manaValue>Number(f.mvMax)))return false;
  if(f.priceMax!==""&&f.priceMax!=null&&(card.prices?.usd==null||card.prices.usd>Number(f.priceMax)))return false;if(f.format&&f.format!=="any"&&card.legalities?.[f.format]!=="legal")return false;return true;});}
