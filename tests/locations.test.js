import test from "node:test";
import assert from "node:assert/strict";
import { renameLocation } from "../src/core/locations.js";
test("renaming a location preserves card quantities and moves references",()=>{
 const c=[{id:"1",quantity:3,location:"Binder A"}],x=renameLocation(["Unsorted","Binder A"],"Binder A","Binder B",c);
 assert.equal(x.collection[0].quantity,3);assert.equal(x.collection[0].location,"Binder B");assert.ok(x.locations.includes("Binder B"));
});
