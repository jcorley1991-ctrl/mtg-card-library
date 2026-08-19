import test from "node:test";
import assert from "node:assert/strict";
import { normalizeFriendlySearch } from "../src/core/search-v2.js";
import { buildScryfallQuery, searchScryfall } from "../src/core/online-search.js";

test("friendly Orzhov ETB terms become Scryfall clauses",()=>{
  const friendly=normalizeFriendlySearch("Orzhov ETB");
  const q=buildScryfallQuery({raw:"Orzhov ETB",friendly,color:"any",rarity:"any",format:"any"});
  assert.match(q,/id=WB/i);
  assert.match(q,/o:"enters"/i);
  assert.match(q,/game:paper/i);
});

test("structured filters are included in live search",()=>{
  const q=buildScryfallQuery({raw:"Sol Ring",friendly:normalizeFriendlySearch("Sol Ring"),color:"colorless",colorMode:"exact",rarity:"uncommon",set:"CMM",type:"artifact",format:"commander",mvMin:"1",mvMax:"2",priceMax:"5"});
  assert.match(q,/id=c/);
  assert.match(q,/r:uncommon/);
  assert.match(q,/set:cmm/);
  assert.match(q,/t:"artifact"/);
  assert.match(q,/f:commander/);
  assert.match(q,/mv>=1/);
  assert.match(q,/usd<=5/);
});

test("live result is normalized into app printing model",async()=>{
  const fake={id:"abc",oracle_id:"oracle",name:"Test Card",set:"tst",set_name:"Test Set",collector_number:"1",colors:["W"],color_identity:["W"],rarity:"rare",cmc:2,mana_cost:"{1}{W}",type_line:"Creature — Test",oracle_text:"Draw a card.",keywords:[],legalities:{commander:"legal"},finishes:["nonfoil"],lang:"en",prices:{usd:"1.25"},image_uris:{normal:"https://cards.scryfall.io/test.jpg"}};
  const fetchImpl=async()=>({ok:true,status:200,json:async()=>({data:[fake],total_cards:1,has_more:false})});
  const result=await searchScryfall({raw:"Test Card",friendly:normalizeFriendlySearch("Test Card"),color:"any",rarity:"any",format:"any"},{fetchImpl,noCache:true});
  assert.equal(result.cards[0].id,"scryfall:abc");
  assert.equal(result.cards[0].prices.usd,1.25);
});
