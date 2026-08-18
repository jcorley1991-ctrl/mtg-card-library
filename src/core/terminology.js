export const colorTerms={
  white:["W"],blue:["U"],black:["B"],red:["R"],green:["G"],
  azorius:["W","U"],dimir:["U","B"],rakdos:["B","R"],gruul:["R","G"],selesnya:["G","W"],
  orzhov:["W","B"],izzet:["U","R"],golgari:["B","G"],boros:["R","W"],simic:["G","U"],
  bant:["G","W","U"],esper:["W","U","B"],grixis:["U","B","R"],jund:["B","R","G"],naya:["R","G","W"],
  abzan:["W","B","G"],jeskai:["U","R","W"],sultai:["B","G","U"],mardu:["R","W","B"],temur:["G","U","R"],
  "five color":["W","U","B","R","G"],"five-color":["W","U","B","R","G"],wubrg:["W","U","B","R","G"],colorless:[]
};

const rule=(query,opts={})=>({query,...opts});
export const conceptTerms={
  edh:rule("f:commander",{format:"commander"}),commander:rule("f:commander",{format:"commander"}),
  cmc:rule("mv:*"),"mana value":rule("mv:*"),mv:rule("mv:*"),
  etb:rule('o:"enters"',{any:["enters the battlefield","enters"]}),"enters the battlefield":rule('o:"enters"',{any:["enters the battlefield","enters"]}),
  ltb:rule('o:"leaves the battlefield"',{any:["leaves the battlefield"]}),dies:rule('o:"dies"',{any:["dies"]}),
  blink:rule('o:"exile" o:"return"',{all:["exile","return"]}),flicker:rule('o:"exile" o:"return"',{all:["exile","return"]}),
  tutor:rule('o:"search your library"',{any:["search your library"]}),ramp:rule('(o:"add" or o:"land")',{any:["add ","land card"]}),
  "mana rock":rule('t:artifact o:"add"',{type:"artifact",any:["add "]}),"mana dork":rule('t:creature o:"add"',{type:"creature",any:["add "]}),dork:rule('t:creature o:"add"',{type:"creature",any:["add "]}),
  draw:rule('o:"draw"',{any:["draw"]}),"draw engine":rule('o:"draw"',{any:["draw"]}),cantrip:rule('o:"draw a card"',{any:["draw a card"]}),
  removal:rule('(o:"destroy target" or o:"exile target")',{any:["destroy target","exile target"]}),"spot removal":rule('(o:"destroy target" or o:"exile target")',{any:["destroy target","exile target"]}),
  "exile removal":rule('o:"exile target"',{any:["exile target"]}),"board wipe":rule('(o:"destroy all" or o:"exile all")',{any:["destroy all","exile all"]}),wrath:rule('(o:"destroy all" or o:"exile all")',{any:["destroy all","exile all"]}),
  counterspell:rule('o:"counter target spell"',{any:["counter target spell"]}),discard:rule('o:"discard"',{any:["discard"]}),mill:rule('o:"mill"',{any:["mill"]}),
  drain:rule('o:"loses" o:"life"',{all:["loses","life"]}),lifegain:rule('o:"gain" o:"life"',{all:["gain","life"]}),
  aristocrats:rule('(o:"dies" or o:"sacrifice")',{any:["dies","sacrifice"]}),"sac outlet":rule('o:"sacrifice"',{any:["sacrifice"]}),"sacrifice outlet":rule('o:"sacrifice"',{any:["sacrifice"]}),sacrifice:rule('o:"sacrifice"',{any:["sacrifice"]}),
  anthem:rule('o:"creatures you control get"',{any:["creatures you control get"]}),lord:rule('o:"other" o:"get +"',{all:["other","get +"]}),
  token:rule('o:"token"',{any:["token"]}),tokens:rule('o:"token"',{any:["token"]}),"token maker":rule('o:"create" o:"token"',{all:["create","token"]}),
  stax:rule('(o:"can’t" or o:"can\'t" or o:"unless")',{any:["can't","can’t","unless"]}),prison:rule('(o:"can’t" or o:"can\'t" or o:"doesn’t untap")',{any:["can't","can’t","doesn’t untap","doesn't untap"]}),
  hatebear:rule('t:creature (o:"can’t" or o:"unless")',{type:"creature",any:["can't","can’t","unless"]}),tax:rule('o:"unless" o:"pay"',{all:["unless","pay"]}),
  landfall:rule('o:"landfall"',{any:["landfall"]}),recursion:rule('o:"graveyard" o:"return"',{all:["graveyard","return"]}),reanimate:rule('o:"graveyard" o:"battlefield"',{all:["graveyard","battlefield"]}),
  "graveyard hate":rule('(o:"exile" o:"graveyard")',{all:["exile","graveyard"]}),
  protection:rule("kw:protection",{keyword:"protection"}),hexproof:rule("kw:hexproof",{keyword:"hexproof"}),ward:rule("kw:ward",{keyword:"ward"}),shroud:rule("kw:shroud",{keyword:"shroud"}),indestructible:rule("kw:indestructible",{keyword:"indestructible"}),
  infect:rule("kw:infect",{keyword:"infect"}),poison:rule('o:"poison counter"',{any:["poison counter"]}),toxic:rule("kw:toxic",{keyword:"toxic"}),proliferate:rule("kw:proliferate",{keyword:"proliferate"}),storm:rule("kw:storm",{keyword:"storm"}),
  treasure:rule('o:"Treasure"',{any:["treasure"]}),food:rule('o:"Food"',{any:["food"]}),clue:rule('o:"Clue"',{any:["clue"]}),blood:rule('o:"Blood token"',{any:["blood token"]}),
  aura:rule("t:aura",{type:"aura"}),equipment:rule("t:equipment",{type:"equipment"}),enchantress:rule('(t:enchantment o:"draw")',{any:["enchantment"],typeAny:["enchantment"]}),
  voltron:rule('(t:aura or t:equipment)',{typeAny:["aura","equipment"]}),"combo piece":rule('(o:"win the game" or o:"infinite")',{any:["win the game","infinite"]}),finisher:rule('(o:"win the game" or pow>=6)',{any:["win the game"],powerAtLeast:6}),
  deathtouch:rule("kw:deathtouch",{keyword:"deathtouch"}),lifelink:rule("kw:lifelink",{keyword:"lifelink"}),menace:rule("kw:menace",{keyword:"menace"}),vigilance:rule("kw:vigilance",{keyword:"vigilance"}),trample:rule("kw:trample",{keyword:"trample"}),haste:rule("kw:haste",{keyword:"haste"}),flying:rule("kw:flying",{keyword:"flying"}),reach:rule("kw:reach",{keyword:"reach"})
};

export const terminologyKeys=[...Object.keys(colorTerms),...Object.keys(conceptTerms)].sort((a,b)=>b.length-a.length);
