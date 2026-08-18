# MTG Card Library

A searchable Magic: The Gathering card catalog, collection manager, set tracker, deck builder, pricing dashboard, wishlist/buy-list system, and purchase-planning tool.

## Current state

Phase 1 foundation is implemented as a dependency-free responsive web application so the product can be exercised before long-term framework, database, hosting, authentication, or cloud-sync choices are locked.

Run locally with any static file server. Logic verification uses Node 22:

```bash
npm test
npm run build
```

## Implemented Phase 1 behavior

- Deep catalog search across name/text/type/set/collector information in the development catalog.
- Friendly MTG terminology normalization, including guild/shard/wedge color names and common gameplay terms.
- Color, rarity, set, and maximum-price filters.
- Exact-printing collection inventory with quantity safety and value calculation.
- Set completion tracking and one-click add-missing-set-cards to Buy List.
- Saved deck foundation with exact-printing vs alternate-printing ownership matching.
- One-click add-all-missing-deck-cards to Buy List.
- Persistent browser-local collection, deck, and buy-list state.
- Collection/deck/buy-list pricing summaries with unknown-price handling.
- JSON backup/import and collection CSV export.
- Scanner and provider-sync areas preserved as explicit planned modules rather than silently omitted.
- Automated Node tests and GitHub Actions CI.

## Data strategy

The production catalog will use a provider abstraction rather than hard-wiring user data to one external service. Scryfall-compatible identifiers/search semantics and MTGJSON-compatible printing/pricing data are the initial research-backed sources. Bulk/local indexing is the required direction for catalog-scale operations.

Development seed prices and cards are placeholders only. They are deliberately labeled as such in the interface until real provider synchronization is connected.

## Source of Truth

- [`docs/MASTER_SPEC.md`](docs/MASTER_SPEC.md) — authoritative product requirements.
- [`docs/PHASE1_IMPLEMENTATION.md`](docs/PHASE1_IMPLEMENTATION.md) — active Phase 1 acceptance criteria, subordinate to the master spec.
