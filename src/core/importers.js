function parseCsvLine(line) {
  const out=[]; let value=""; let quoted=false;
  for(let i=0;i<line.length;i++){
    const ch=line[i];
    if(ch === '"'){
      if(quoted && line[i+1] === '"'){ value+='"'; i++; }
      else quoted=!quoted;
    } else if(ch === "," && !quoted){ out.push(value); value=""; }
    else value+=ch;
  }
  out.push(value);
  return out;
}

const aliases = {
  name:["name","card name","card","cardname"],
  setCode:["set code","setcode","set"],
  collectorNumber:["collector number","collectornumber","number","#"],
  quantity:["quantity","qty","count"],
  finish:["finish","foil"],
  language:["language","lang"],
  condition:["condition"],
  location:["location","binder","box"],
  acquisitionPrice:["purchase price","price paid","acquisition price"],
  purchaseCurrency:["purchase currency","currency"],
  acquisitionDate:["purchase date","date"],
  source:["purchase source","vendor","source"],
  notes:["notes","note"],
  tags:["tags","tag"]
};

function mapHeaders(headers) {
  const normalized=headers.map(h=>h.trim().toLowerCase());
  const map={};
  for(const [field,names] of Object.entries(aliases)){
    const idx=normalized.findIndex(h=>names.includes(h));
    if(idx>=0) map[field]=idx;
  }
  return map;
}

export function parseCollectionCsv(text) {
  const lines=text.replace(/^\uFEFF/,"").split(/\r?\n/).filter(x=>x.trim().length);
  if(lines.length<2) return {rows:[],errors:[{row:1,message:"CSV must include a header and at least one row"}]};
  const headers=parseCsvLine(lines[0]), map=mapHeaders(headers), rows=[], errors=[];
  if(map.name==null && (map.setCode==null || map.collectorNumber==null)){
    errors.push({row:1,message:"Need card name or set code + collector number columns"});
    return {rows,errors};
  }
  for(let i=1;i<lines.length;i++){
    const cols=parseCsvLine(lines[i]);
    const get=f=>map[f]==null?"":(cols[map[f]]??"").trim();
    const qty=Number(get("quantity")||1);
    if(!Number.isFinite(qty)||qty<=0){errors.push({row:i+1,message:"Quantity must be greater than zero"});continue;}
    rows.push({
      sourceRow:i+1,name:get("name"),setCode:get("setCode").toUpperCase(),collectorNumber:get("collectorNumber"),
      quantity:Math.floor(qty),finish:normalizeFinish(get("finish")),language:get("language")||"en",
      condition:(get("condition")||"NM").toUpperCase(),location:get("location")||"Unsorted",
      acquisitionPrice:get("acquisitionPrice")===""?null:Number(get("acquisitionPrice")),
      purchaseCurrency:get("purchaseCurrency")||"USD",acquisitionDate:get("acquisitionDate")||null,
      source:get("source"),notes:get("notes"),tags:get("tags").split(/[;|]/).map(x=>x.trim()).filter(Boolean)
    });
  }
  return {rows,errors};
}

function normalizeFinish(value){
  const x=value.toLowerCase();
  if(["true","yes","foil","f"].includes(x))return"foil";
  if(["etched","etched foil"].includes(x))return"etched";
  return"nonfoil";
}

export function parseDeckText(text){
  const rows=[],errors=[];
  for(const [i,line] of text.split(/\r?\n/).entries()){
    const s=line.trim(); if(!s||s.startsWith("#")||s.startsWith("//"))continue;
    const m=s.match(/^(\d+)\s+(.+?)(?:\s+\[([A-Za-z0-9]+)\]\s+([A-Za-z0-9-]+))?$/);
    if(!m){errors.push({row:i+1,message:"Could not parse deck line"});continue;}
    rows.push({quantity:Number(m[1]),name:m[2].trim(),setCode:(m[3]||"").toUpperCase(),collectorNumber:m[4]||""});
  }
  return {rows,errors};
}
