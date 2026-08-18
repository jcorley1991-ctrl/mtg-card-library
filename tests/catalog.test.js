import test from "node:test";
import assert from "node:assert/strict";
import { normalizeScryfallCard } from "../src/core/catalog.js";
test("Scryfall normalization preserves printing identity and unknown prices",()=>{
  const c=normalizeScryfallCard({id:"p1",oracle_id:"o1",name:"Test",set:"abc",set_name:"Alpha Beta",collector_number:"7",colors:["W"],color_identity:["W"],rarity:"rare",cmc:2,type_line:"Creature",oracle_text:"Text",keywords:[],legalities:{commander:"legal"},finishes:["nonfoil"],lang:"en",prices:{usd:null,usd_foil:"1.25"},games:["paper"]});
  assert.equal(c.id,"scryfall:p1");assert.equal(c.oracleId,"scryfall-oracle:o1");assert.equal(c.setCode,"ABC");assert.equal(c.prices.usd,null);assert.equal(c.prices.usdFoil,1.25);
});
