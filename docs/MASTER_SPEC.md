# MTG Card Library — MASTER SPEC

Status: **Authoritative product specification**

This file is the single Source of Truth for the project until the user explicitly changes or supersedes a requirement.

## 1. Product goal

Build a fast, visual Magic: The Gathering catalog where a user can:

1. Search the complete card/printing catalog using both exact structured filters and ordinary MTG terminology.
2. Save and organize a physical card collection at exact-printing granularity.
3. Browse by set and track set completion.
4. Build, save, duplicate, revise, and analyze decks.
5. Match decks against the owned collection.
6. Generate persistent missing-card / buy lists.
7. See current prices and available price history for cards, printings, collections, decks, and buy lists.
8. Import and export data without being locked into this application.

The application must be usable on desktop and mobile. Exact implementation technology remains changeable until explicitly approved.

---

## 2. Locked top-level modules

The product must contain these first-class areas:

- Search
- Collection
- Sets
- Decks
- Buy Lists / Wishlist
- Pricing / Value
- Import / Export
- Scanner
- Settings / Data Sync

Scanner is a required planned capability, but it may follow the stable core catalog/collection release rather than block the first usable build.

---

## 3. Search requirements

Search is a primary product feature and must not be limited to card names.

### 3.1 Search modes

Support all three simultaneously:

1. **Quick search** — name, partial name, typo-tolerant/fuzzy matching, collector number, set code.
2. **Structured advanced search** — explicit filters and operators.
3. **Friendly terminology search** — common player vocabulary and aliases normalized into structured/card-text queries.

Unknown terminology should fall back to indexed full-text search rather than silently returning nonsense.

### 3.2 Searchable card concepts

The search index and UI must support, where underlying card data exposes the property:

- Card name
- Exact name / partial name / fuzzy name
- Oracle/rules text
- Printed text when available
- Type line
- Supertypes
- Card types
- Subtypes / creature types
- Keywords
- Ability/action terminology
- Mana cost
- Mana value / CMC
- Colored mana symbols and pip counts where derivable
- Color
- Color identity
- Color count
- Mono-color / multicolor / colorless
- Exact, subset, superset, and inclusive color matching
- Rarity: common, uncommon, rare, mythic, special/bonus where represented
- Set name
- Set code
- Set type/product family
- Printing
- Collector number
- Release date and year
- Artist
- Flavor text where available
- Language
- Finish/treatment: normal, foil, etched, and other represented finishes
- Layout/frame treatment when available
- Borderless/full-art/showcase/extended-art/promo-style flags where represented
- Watermark where available
- Reserved List status
- Reprint/new-printing status where derivable
- Paper/digital availability
- Format legality
- Commander/leadership eligibility where available
- Power
- Toughness
- Loyalty
- Defense
- Price and price ranges
- Retail vs buylist where available
- Marketplace/provider
- Token/related-card relationships where represented
- Promo identifiers / special printing properties where represented

### 3.3 MTG terminology alias layer

The application must recognize common shorthand and archetype vocabulary. The alias dictionary must be data-driven and extensible rather than hard-coded into UI components.

Initial required examples include:

- EDH = Commander
- CMC = mana value / MV
- ETB = enters / enters the battlefield terminology
- LTB = leaves the battlefield terminology
- dies = creature death terminology
- blink / flicker
- tutor
- ramp
- mana rock
- dork / mana dork
- draw engine
- cantrip
- removal
- spot removal
- board wipe / wrath
- counterspell
- discard
- mill
- drain
- lifegain
- aristocrats
- sac outlet / sacrifice outlet
- anthem
- lord
- tokens / token maker
- stax
- prison
- hatebear
- landfall
- recursion / reanimate
- graveyard hate
- exile removal
- protection
- hexproof / ward / shroud
- infect / poison / toxic
- proliferate
- storm
- treasure / food / clue / blood
- aura / equipment / enchantress
- Voltron
- combo piece
- finisher

Color terminology must include:

- W/U/B/R/G and full color names
- Azorius, Dimir, Rakdos, Gruul, Selesnya
- Orzhov, Izzet, Golgari, Boros, Simic
- Bant, Esper, Grixis, Jund, Naya
- Abzan, Jeskai, Sultai, Mardu, Temur
- five-color / WUBRG
- colorless

The search parser must make the normalized interpretation visible to the user so a friendly-language search can be audited and corrected.

### 3.4 Search interactions

- Card-image grid view
- Compact list/table view
- Sort by name, set, release date, rarity, mana value, price, color, collector number
- Multi-sort where practical
- Save search
- Recent searches
- Pin/favorite cards
- Search only owned cards
- Search only missing cards from a deck or set
- Search within a selected set
- Search within a selected collection location/binder
- Search results must show owned quantity and deck usage without requiring a second lookup when performance allows
- Quick actions from results: add to collection, add to deck, add to buy list, choose printing

---

## 4. Card and printing detail

Every Oracle-equivalent card identity and every physical printing must be distinguishable.

A card detail view must expose:

- Card image(s)
- Name
- Mana cost
- Rules text
- Type line
- Power/toughness/loyalty/defense as applicable
- Colors and color identity
- Rarity
- Set and set code
- Collector number
- Artist
- Language
- Available finishes
- Legalities
- Keywords
- Related cards/tokens where available
- All printings/versions
- Current prices by available provider
- Price history when available
- Buylist values when available
- Marketplace purchase links when available
- Owned quantity by printing
- Storage location(s)
- Decks containing the card
- Wishlist/buy-list quantity

The UI must clearly distinguish **card identity** from **specific printing**.

---

## 5. Collection management

### 5.1 Inventory granularity

Collection records must support exact printing and multiple stacks of the same printing when metadata differs.

Required fields:

- Internal collection-entry ID
- Internal card/printing ID
- External identifiers for data-source reconciliation
- Quantity
- Set / printing
- Collector number
- Finish
- Language
- Condition
- Storage location
- Tags
- Notes
- Purchase price
- Purchase currency
- Purchase date
- Purchase source/vendor
- Trade status
- Sell status
- Keep/not-for-trade status
- Loaned status and optional note
- Altered flag
- Misprint flag
- Signed flag
- Optional custom attributes without schema redesign

### 5.2 Physical organization

Support named locations that mirror the real collection, for example:

- Binder
- Box
- Deck
- Shelf
- Trade binder
- Bulk box
- Custom location

Locations must be user-created, renameable, moveable, and searchable.

### 5.3 Collection operations

- Add one or many copies
- Remove quantity
- Move copies between locations
- Split a stack
- Merge compatible stacks
- Bulk edit
- Bulk move
- Bulk tag
- Bulk condition/finish/language changes
- Duplicate detection
- Find unassigned cards
- Find cards currently committed to physical decks
- Undo destructive bulk operations where feasible

### 5.4 Collection analytics

Dashboard metrics must include:

- Total physical cards
- Unique card identities
- Unique printings
- Total estimated collection value
- Value by provider
- Value by set
- Value by rarity
- Value by color/color identity
- Most valuable cards
- Largest price movers within available history
- Duplicate counts
- Set completion percentages
- Missing cards for tracked sets

No value may be invented when a provider has no price. Missing price data must be visibly marked.

---

## 6. Set catalog and completion

The application must provide a dedicated set browser.

Required capabilities:

- Browse all known sets
- Search set by name/code
- Sort by release date, name, set type
- Open a set checklist
- View card-number ordering
- View all printing variants represented in the source data
- Toggle between base checklist and all variants when possible
- Show quantity owned for each card/printing
- Show missing cards
- Show percentage complete
- Show value owned and estimated value missing when prices exist
- Add missing cards to a buy list in bulk
- Filter a set checklist by rarity, color, type, finish, ownership, price
- Track selected sets as collection goals

---

## 7. Deck builder

### 7.1 Core deck functions

- Create and save unlimited local/project-supported deck records
- Name and description
- Format selection
- Commander selection where relevant
- Partner/multi-commander support where rules/data permit
- Mainboard
- Sideboard
- Maybeboard / considering board
- Custom categories/tags
- Quantity editing
- Printing selection
- Drag/move between boards/categories where UI supports it
- Clone deck
- Archive deck
- Revision/version snapshots
- Compare revisions
- Import deck list
- Export/share deck list

### 7.2 Deck legality

Where card-data sources provide legality information, validate:

- Format legality
- Deck size
- Copy limits
- Commander/color-identity restrictions
- Commander eligibility
- Sideboard rules where applicable

The application must display the specific reason for a legality failure rather than only showing "illegal".

### 7.3 Collection integration

The deck builder must distinguish:

- Exact requested printing owned
- Different printing owned
- Partially owned quantity
- Missing
- Copy exists but is assigned to another physical deck

Required actions:

- Build from collection
- Prefer exact versions
- Allow alternate owned printing
- Move/reserve physical copies into a registered physical deck
- Disassemble a physical deck back to a chosen location
- Reconcile a changed deck list against the physical deck
- Add all missing cards to a buy list

### 7.4 Deck statistics

At minimum:

- Mana-value curve
- Color/mana-pip distribution
- Card-type distribution
- Land count
- Creature count
- Noncreature count
- Total price
- Owned vs missing value
- Tokens/related pieces where derivable

Additional useful metrics should include customizable tags for categories such as draw, ramp, removal, board wipes, tutors, protection, recursion, finishers, and combo pieces.

### 7.5 Deck testing

A lightweight goldfish simulator is a required planned feature:

- Shuffle
- Draw opening hand
- Mulligan/reset
- Draw card
- Move cards among hand/library/graveyard/battlefield/exile for testing

It does not need to become a full rules engine.

---

## 8. Buy lists, wishlist, and purchase planning

Support persistent lists for cards not necessarily owned.

Required list types/capabilities:

- Wishlist
- Deck missing-cards list
- Set completion list
- General buy list
- Trade/sell candidate list
- Custom list

Each item can track:

- Card identity
- Preferred printing or "any printing"
- Quantity wanted
- Preferred finish
- Maximum target price
- Notes/tags
- Priority

### 8.1 Buy all workflow

For a deck/set/list:

1. Calculate missing quantity against collection.
2. Allow exact printing or cheapest/alternate printing selection.
3. Show available retail prices by provider.
4. Show list total by provider where enough price data exists.
5. Provide marketplace purchase links where data provides them.
6. Export the buy list as text/CSV.

The application must not pretend it can complete checkout if the selected marketplace does not expose a supported checkout/cart integration.

### 8.2 Price targets

Support optional target-price alerts/watch state at the data-model level. Notification delivery can be implemented after the core application if infrastructure is not yet selected.

---

## 9. Pricing

Pricing is required throughout the application, not isolated to one page.

Display, where data exists:

- Current retail price
- Buylist price
- Normal price
- Foil price
- Etched price
- Provider name
- Currency
- Price date/timestamp
- Historical chart/data

Price totals must be available for:

- Single printing
- Collection
- Collection subset/location
- Deck
- Missing cards
- Buy list
- Set owned cards
- Set missing cards

Users must be able to select a preferred provider and fallback provider order.

Price data must be timestamped and never represented as a guaranteed transaction price.

---

## 10. Import / export / backup

Data portability is required.

### 10.1 Import

Support:

- CSV collection import
- Text/MTGA-style card list import
- Deck-list import
- Mapping common column names from other collection tools
- Scryfall IDs where supplied
- Set code + collector number matching
- Card name + set matching fallback
- Import preview
- Validation errors by row
- Partial successful import without losing failed-row diagnostics

Useful collection import columns include:

- Card name
- Set code/name
- Collector number
- Quantity
- Finish/foil
- Language
- Condition
- Purchase price
- Purchase currency
- Altered
- Misprint
- Signed
- External IDs
- Location/binder
- Tags

### 10.2 Export

Support:

- Full collection CSV
- Selected collection/location CSV
- Deck text/CSV
- Missing cards
- Buy list
- Backup file containing application-specific metadata

A user must be able to leave the application with their collection data intact.

---

## 11. Scanner

Camera-assisted card entry is required as a planned capability.

Target behavior:

- Scan card artwork/name
- Suggest likely printing
- Confirm ambiguous printing before save
- Set lock/filter during scanning
- Normal/foil selection
- Language
- Condition
- Quantity
- Purchase price
- Storage destination
- Review scanned batch before committing
- Bulk edit scanned batch
- Show already-owned quantity during review

Quick mode may choose the best likely match, but strict mode must require confirmation for ambiguous printings.

Scanner output must enter the same collection model as manual/imported cards. No separate incompatible inventory path.

---

## 12. UI/UX requirements

- Responsive mobile and desktop layout
- Card images are first-class, not hidden behind text-only tables
- Fast quick-view panel/modal
- Grid and list modes
- Persistent filters while browsing
- Clear owned/missing badges
- Clear normal/foil/etched badges
- Clear exact-printing vs alternate-printing distinction
- Bulk-selection mode
- Keyboard-friendly desktop search
- Touch-friendly mobile controls
- Accessible contrast and scalable text
- No destructive action without a clear recovery/confirmation strategy

Primary navigation should keep Search, Collection, Sets, Decks, and Buy Lists directly reachable.

---

## 13. Data architecture guardrails

### 13.1 Provider abstraction

Do not make database records depend on one provider's identifier as the application's sole primary key.

Use:

- Internal stable IDs
- External identifier mapping table
- Printing-level records
- Card-identity-level records
- Provider-specific price records

Initial external identifiers should preserve Scryfall-compatible IDs and MTGJSON UUIDs where available.

### 13.2 Catalog sync

Catalog-scale search should use bulk/downloaded datasets or a locally indexed backend dataset instead of issuing one live API request per result/card.

The sync system must support:

- Dataset version/date
- Last successful sync
- Atomic replacement/update strategy
- Failure rollback to last good catalog
- Price sync independent from card-text sync

### 13.3 Search indexing

Search should index normalized fields rather than repeatedly parsing raw JSON at query time.

Index must support:

- Prefix/name search
- Full-text rules/type search
- Exact filters
- Numeric ranges
- Boolean flags
- Aliases/synonyms
- Color set operations
- Price ranges

---

## 14. Initial research-backed data sources

### Scryfall

Use cases:

- Card identifiers and card-image ecosystem
- Search semantics/reference behavior
- Bulk card data where appropriate
- Cross-reference IDs

Guardrail: favor bulk data for catalog-scale work and throttle live API requests.

### MTGJSON

Use cases:

- All printings and set-level data
- Cross-provider identifiers
- Legalities, keywords, printings, languages, finishes and descriptive flags where available
- Current prices and available historical prices
- Retail/buylist separation where available
- Marketplace purchase URLs where available

The application must isolate source-specific parsing behind adapters so either source can change without rewriting collection/deck logic.

---

## 15. Legal / attribution guardrails

Before public deployment:

- Include an unofficial fan-content disclaimer appropriate to Wizards of the Coast policy.
- Do not present the product as endorsed by Wizards.
- Do not use Wizards logos as application branding.
- Preserve required notices/attribution associated with third-party data/image sources.
- Review current provider terms before commercializing or adding paid access.

This project is a catalog/collection/deck tool, not a card-counterfeiting or proxy reproduction system.

---

## 16. Performance and reliability requirements

- Search should feel immediate after the catalog is indexed.
- Large collections must not require loading every deck relationship for every visible card unless efficiently precomputed.
- Paginate/virtualize large lists.
- Cache card metadata and images responsibly.
- Avoid redundant live API calls.
- Preserve last-known-good catalog on sync failure.
- Collection writes must be transactional/consistent.
- Avoid silent quantity loss during import, deck allocation, or location movement.

---

## 17. Data integrity rules

1. Quantity can never silently become negative.
2. Moving physical cards between locations must preserve total quantity.
3. Deck allocation must never fabricate owned copies.
4. Exact printing and alternate printing must remain distinguishable.
5. A missing price is `unknown`, not zero.
6. Import failures must never silently discard rows.
7. External data refresh must not overwrite user-owned metadata such as condition, purchase price, location, tags, or notes.
8. A data-source ID change must be handled through identifier reconciliation rather than creating duplicate physical collection entries.

---

## 18. Delivery order

### Phase 1 — usable foundation

- Catalog sync/data adapter
- Search and filters
- Card/printing detail
- Collection add/edit/remove
- Locations/binders
- Sets browser
- Set completion
- Basic pricing
- CSV/text import/export

### Phase 2 — deck and purchasing workflow

- Deck builder
- Legality
- Collection ownership matching
- Missing-card calculations
- Buy lists
- Marketplace links
- Deck statistics
- Price history views

### Phase 3 — speed and collector tooling

- Camera scanner
- Bulk collection workflows
- Saved searches
- Price targets/watchlists
- Enhanced analytics
- Goldfish simulator
- More import adapters

Delivery order may change by explicit user instruction, but features listed above remain required unless explicitly removed.

---

## 19. Explicitly not yet locked

The following are implementation decisions, not approved product truths yet:

- Exact frontend framework
- Exact backend framework
- Cloud hosting provider
- Authentication provider
- Whether collection storage is local-first, cloud-first, or hybrid
- Database engine
- Notification provider
- Whether a native Android/iOS shell is added after the responsive web build

These choices must not be treated as approved merely because they appear in an experiment or prototype.

---

## 20. Perfection gate for implementation

Before a feature is accepted as complete, verify:

- It matches this MASTER SPEC or a newer explicit user change.
- Search/filter semantics are correct and inspectable.
- No collection quantity drift occurs.
- Exact printings stay exact.
- Prices are source/date labeled and missing data is not invented.
- Set/deck missing-card calculations respect owned quantities.
- Import/export round-trips important collection metadata.
- Mobile and desktop primary workflows remain usable.
- No unrelated module was redesigned as a side effect.

Generated code or UI is not automatically approved. Only verified output becomes the next project state.
