export function normalizeLocations(locations){
  const clean=[...new Set((locations||[]).map(x=>String(x).trim()).filter(Boolean))];
  return clean.length ? clean : ["Unsorted"];
}
export function addLocation(locations,name){
  const clean=String(name||"").trim();
  if(!clean)return normalizeLocations(locations);
  return normalizeLocations([...(locations||[]),clean]);
}
export function renameLocation(locations,from,to,collection){
  const clean=String(to||"").trim();
  if(!clean)throw new Error("Location name is required");
  const next=normalizeLocations((locations||[]).map(x=>x===from?clean:x));
  const updated=(collection||[]).map(item=>item.location===from?{...item,location:clean}:item);
  return {locations:next,collection:updated};
}
