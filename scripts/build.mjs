import { cp, mkdir, rm, stat } from "node:fs/promises";
import { resolve } from "node:path";
const root=resolve(new URL("..",import.meta.url).pathname),dist=resolve(root,"dist");
await rm(dist,{recursive:true,force:true});await mkdir(dist,{recursive:true});
await cp(resolve(root,"index.html"),resolve(dist,"index.html"));
await cp(resolve(root,"src"),resolve(dist,"src"),{recursive:true});
try{await stat(resolve(root,"data/catalog.json"));await mkdir(resolve(dist,"data"),{recursive:true});await cp(resolve(root,"data/catalog.json"),resolve(dist,"data/catalog.json"));}catch{}
const required=["index.html","src/app.js","src/styles.css","src/core/search.js","src/core/collection.js","src/core/decks.js","src/core/catalog.js","src/core/importers.js"];
for(const file of required)await stat(resolve(dist,file));
console.log(`Build complete: ${required.length} required assets verified.`);
