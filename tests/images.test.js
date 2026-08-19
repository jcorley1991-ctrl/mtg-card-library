import test from "node:test";
import assert from "node:assert/strict";
import { seedCards } from "../src/core/data.js";

test("seed cards use static Scryfall image URLs",()=>{
  assert.ok(seedCards.length>0);
  for(const card of seedCards){
    assert.match(card.image,/^https:\/\/cards\.scryfall\.io\/normal\/front\/[0-9a-f]\/[0-9a-f]\/[0-9a-f-]{36}\.jpg$/i,`${card.name} has an invalid seed image URL`);
  }
});

test("seed printing coordinates match corrected Sol Ring printing",()=>{
  const sol=seedCards.find(card=>card.name==="Sol Ring");
  assert.equal(sol?.setCode,"CMM");
  assert.equal(sol?.collectorNumber,"410");
});
