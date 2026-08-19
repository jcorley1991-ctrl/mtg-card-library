import { $, header } from "./v3/state.js";
import { setRefreshHandler } from "./v3/cards.js";
import { renderSearch } from "./v3/search-page.js";
import { renderCollection, renderSets, renderDecks, renderBuy, renderPricing } from "./v3/library-pages.js";
import { renderImports, renderScanner, renderSettings } from "./v3/utility-pages.js";

const PAGES=[["search","Search"],["collection","Collection"],["sets","Sets"],["decks","Decks"],["buy","Buy Lists"],["pricing","Pricing"],["import","Import / Export"],["scanner","Scanner"],["settings","Settings / Sync"]];
const renderers={collection:renderCollection,sets:renderSets,decks:renderDecks,buy:renderBuy,pricing:renderPricing,import:renderImports,scanner:renderScanner,settings:renderSettings};

function renderAll(){for(const render of Object.values(renderers))render();header();}
function refresh(page){renderers[page]?.();header();}
setRefreshHandler(refresh);

function nav(){
  $("#nav").innerHTML=PAGES.map(([id,label],i)=>`<button data-page="${id}" class="${i?"":"active"}">${label}</button>`).join("");
  $("#nav").onclick=event=>{const button=event.target.closest("[data-page]");if(!button)return;document.querySelectorAll(".nav button").forEach(x=>x.classList.toggle("active",x===button));document.querySelectorAll(".page").forEach(x=>x.classList.toggle("active",x.id===button.dataset.page));renderers[button.dataset.page]?.();header();};
}

nav();renderSearch();renderAll();
