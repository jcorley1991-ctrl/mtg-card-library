export const CONDITIONS=["NM","LP","MP","HP","DMG"];
export function createCollectionItem(card,overrides={}){
  const quantity=Math.max(0,Number(overrides.quantity??1));
  return {
    id:overrides.id||crypto.randomUUID(),cardId:card.id,oracleId:card.oracleId,externalIds:card.externalIds||{},
    name:card.name,setCode:card.setCode,collectorNumber:card.collectorNumber,quantity,
    condition:CONDITIONS.includes(overrides.condition)?overrides.condition:"NM",
    finish:overrides.finish||"nonfoil",language:overrides.language||card.language||"en",location:overrides.location||"Unsorted",
    acquisitionPrice:overrides.acquisitionPrice??null,purchaseCurrency:overrides.purchaseCurrency||"USD",
    acquisitionDate:overrides.acquisitionDate??null,source:overrides.source||"",tags:Array.isArray(overrides.tags)?overrides.tags:[],
    notes:overrides.notes||"",status:overrides.status||"keep",loaned:!!overrides.loaned,loanNote:overrides.loanNote||"",
    altered:!!overrides.altered,misprint:!!overrides.misprint,signed:!!overrides.signed,customAttributes:overrides.customAttributes||{}
  };
}
export function setQuantity(item,quantity){const next=Number(quantity);if(!Number.isFinite(next))throw new TypeError("Quantity must be a finite number");return{...item,quantity:Math.max(0,Math.floor(next))};}
export function collectionValue(items,cardMap){return items.reduce((sum,item)=>{const card=cardMap.get(item.cardId);if(!card)return sum;const price=item.finish==="foil"?card.prices.usdFoil:item.finish==="etched"?card.prices.usdEtched:card.prices.usd;return sum+(price??0)*item.quantity;},0);}
export function totalOwned(items){return items.reduce((n,item)=>n+item.quantity,0);}
