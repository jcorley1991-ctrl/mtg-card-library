const scryfallImage=(setCode,collectorNumber)=>`https://api.scryfall.com/cards/${setCode.toLowerCase()}/${encodeURIComponent(collectorNumber)}?format=image&version=normal`;

export const seedCards = [
  {
    id:"c1", oracleId:"o1", name:"Swords to Plowshares", setCode:"STA", setName:"Strixhaven Mystical Archive", collectorNumber:"10",
    colors:["W"], colorIdentity:["W"], rarity:"rare", manaValue:1, manaCost:"{W}", typeLine:"Instant",
    oracleText:"Exile target creature. Its controller gains life equal to its power.",
    keywords:[], legalities:{commander:"legal",modern:"not_legal",legacy:"legal"}, finishes:["nonfoil","foil"],
    language:"en", prices:{usd:2.15,usdFoil:3.10,usdEtched:null}, image:scryfallImage("STA","10")
  },
  {
    id:"c2", oracleId:"o2", name:"Sol Ring", setCode:"CMM", setName:"Commander Masters", collectorNumber:"410",
    colors:[], colorIdentity:[], rarity:"uncommon", manaValue:1, manaCost:"{1}", typeLine:"Artifact",
    oracleText:"{T}: Add {C}{C}.", keywords:[], legalities:{commander:"legal",modern:"not_legal",legacy:"legal"}, finishes:["nonfoil","foil"],
    language:"en", prices:{usd:1.35,usdFoil:2.20,usdEtched:null}, image:scryfallImage("CMM","410")
  },
  {
    id:"c3", oracleId:"o3", name:"Vindicate", setCode:"MH2", setName:"Modern Horizons 2", collectorNumber:"322",
    colors:["W","B"], colorIdentity:["W","B"], rarity:"rare", manaValue:3, manaCost:"{1}{W}{B}", typeLine:"Sorcery",
    oracleText:"Destroy target permanent.", keywords:[], legalities:{commander:"legal",modern:"legal",legacy:"legal"}, finishes:["nonfoil","foil"],
    language:"en", prices:{usd:0.72,usdFoil:1.40,usdEtched:null}, image:scryfallImage("MH2","322")
  },
  {
    id:"c4", oracleId:"o4", name:"Grim Guardian", setCode:"JOU", setName:"Journey into Nyx", collectorNumber:"73",
    colors:["B"], colorIdentity:["B"], rarity:"common", manaValue:3, manaCost:"{2}{B}", typeLine:"Enchantment Creature — Zombie",
    oracleText:"Constellation — Whenever Grim Guardian or another enchantment enters the battlefield under your control, each opponent loses 1 life.",
    keywords:["Constellation"], legalities:{commander:"legal",modern:"legal",legacy:"legal"}, finishes:["nonfoil","foil"],
    language:"en", prices:{usd:0.24,usdFoil:0.85,usdEtched:null}, image:scryfallImage("JOU","73")
  },
  {
    id:"c5", oracleId:"o5", name:"Demonic Tutor", setCode:"STA", setName:"Strixhaven Mystical Archive", collectorNumber:"27",
    colors:["B"], colorIdentity:["B"], rarity:"mythic", manaValue:2, manaCost:"{1}{B}", typeLine:"Sorcery",
    oracleText:"Search your library for a card, put that card into your hand, then shuffle.",
    keywords:[], legalities:{commander:"legal",modern:"not_legal",legacy:"banned",vintage:"restricted"}, finishes:["nonfoil","foil","etched"],
    language:"en", prices:{usd:34.50,usdFoil:44.00,usdEtched:null}, image:scryfallImage("STA","27")
  },
  {
    id:"c6", oracleId:"o6", name:"Rhystic Study", setCode:"WOT", setName:"Wilds of Eldraine: Enchanting Tales", collectorNumber:"25",
    colors:["U"], colorIdentity:["U"], rarity:"mythic", manaValue:3, manaCost:"{2}{U}", typeLine:"Enchantment",
    oracleText:"Whenever an opponent casts a spell, you may draw a card unless that player pays {1}.",
    keywords:[], legalities:{commander:"legal",modern:"not_legal",legacy:"legal"}, finishes:["nonfoil","foil"],
    language:"en", prices:{usd:34.10,usdFoil:39.00,usdEtched:null}, image:scryfallImage("WOT","25")
  }
];

export const colorAliases = {
  azorius:["W","U"], dimir:["U","B"], rakdos:["B","R"], gruul:["R","G"], selesnya:["G","W"],
  orzhov:["W","B"], izzet:["U","R"], golgari:["B","G"], boros:["R","W"], simic:["G","U"],
  bant:["G","W","U"], esper:["W","U","B"], grixis:["U","B","R"], jund:["B","R","G"], naya:["R","G","W"],
  abzan:["W","B","G"], jeskai:["U","R","W"], sultai:["B","G","U"], mardu:["R","W","B"], temur:["G","U","R"]
};

export let catalogMeta={source:"development-seed",generatedAt:null,fallback:true};
if(typeof window!=="undefined"){
  try{
    const response=await fetch("./data/catalog.json",{cache:"no-store"});
    if(response.ok){
      const payload=await response.json();
      if(Array.isArray(payload.cards)&&payload.cards.length){
        seedCards.splice(0,seedCards.length,...payload.cards);
        catalogMeta=payload.meta||{source:"local-bulk"};
      }
    }
  }catch{
    // Deliberate fallback: retain last bundled development catalog.
  }
}
