const KEY="mtg-card-library-v1";
export function loadState(){try{const raw=localStorage.getItem(KEY);return raw?JSON.parse(raw):null;}catch{return null;}}
export function saveState(state){localStorage.setItem(KEY,JSON.stringify(state));}
export function exportState(state){return JSON.stringify({schemaVersion:1,exportedAt:new Date().toISOString(),...state},null,2);}
export function importState(text){const parsed=JSON.parse(text);if(!parsed||typeof parsed!=="object")throw new Error("Invalid backup");if(parsed.schemaVersion!==1)throw new Error("Unsupported backup schema");return parsed;}
export function collectionToCsv(collection){const cols=["name","setCode","collectorNumber","quantity","condition","finish","language","location","acquisitionPrice","acquisitionDate","source","status"],escape=v=>`"${String(v??"").replaceAll('"','""')}"`;return[cols.join(","),...collection.map(row=>cols.map(c=>escape(row[c])).join(","))].join("\n");}
