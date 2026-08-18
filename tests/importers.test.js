import test from "node:test";
import assert from "node:assert/strict";
import { parseCollectionCsv, parseDeckText } from "../src/core/importers.js";
test("CSV parser maps common collection columns and keeps row errors",()=>{
 const x=parseCollectionCsv("Card Name,Set Code,Collector Number,Qty,Foil,Condition,Binder\nSol Ring,CMM,396,2,yes,LP,Box A\nBad,CMM,1,0,no,NM,Box A");
 assert.equal(x.rows.length,1);assert.equal(x.rows[0].finish,"foil");assert.equal(x.rows[0].location,"Box A");assert.equal(x.errors.length,1);assert.equal(x.errors[0].row,3);
});
test("deck text parser reads quantity, name, set and collector",()=>{
 const x=parseDeckText("2 Sol Ring [CMM] 396");
 assert.deepEqual(x.rows[0],{quantity:2,name:"Sol Ring",setCode:"CMM",collectorNumber:"396"});
});
