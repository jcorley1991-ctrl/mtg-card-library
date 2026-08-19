import { cp, mkdir, rm, stat } from "node:fs/promises";
import { resolve } from "node:path";
const root=resolve(new URL("..",import.meta.url).pathname),dist=resolve(root,"dist");
await rm(dist,{recursive:true,force:true});await mkdir(dist,{recursive:true});
await cp(resolve(root,"index.html"),resolve(dist,"index.html"));
await cp(resolve(root,"src"),resolve(dist,"src"),{recursive:true});
try{await stat(resolve(root,"data/catalog.json"));await mkdir(resolve(dist,"data"),{recursive:true});await cp(resolve(root,"data/catalog.json"),resolve(dist,"data/catalog.json"));}catch{}
const required=["index.html","src/app-v3.js","src/styles.css","src/core/search-v2.js","src/core/terminology.js","src/core/collection.js","src/core/decks.js","src/core/catalog.js","src/core/importers.js","src/core/online-search.js","src/v3/state.js","src/v3/cards.js","src/v3/search-page.js","src/v3/library-pages.js","src/v3/utility-pages.js"];
for(const file of required)await stat(resolve(dist,file));
console.log(`Build complete: ${required.length} required assets verified.`);
