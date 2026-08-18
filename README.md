# MTG Card Library

A searchable Magic: The Gathering card catalog, collection manager, deck builder, set tracker, pricing dashboard, wishlist/buylist system, and purchase-planning tool.

## Project status

The repository has been initialized with a research-backed product specification. Implementation must follow `docs/MASTER_SPEC.md` as the current Source of Truth.

## Core product areas

- **Search** — deep structured search plus friendly terminology and natural-language aliases.
- **Collection** — exact-printing inventory, quantities, condition, finish, language, storage location, acquisition data, tags, and value tracking.
- **Sets** — browse every set/printing and track set completion and missing cards.
- **Decks** — build and save decks, validate legality, match against owned cards, calculate missing cards, and track deck value.
- **Buy Lists** — turn missing cards into persistent purchase lists with printing choices, quantities, prices, and marketplace links.
- **Pricing** — retail/buylist pricing, normal/foil/etched support where available, and price history.
- **Import / Export** — portable CSV/text backups and migration from other card-management tools.
- **Scanner** — camera-assisted card entry is a required planned capability after the core catalog and collection workflow is stable.

## Data strategy

The project will use a provider abstraction rather than hard-wiring the application to one external service. Scryfall-compatible identifiers/search semantics and MTGJSON printings/pricing data are the initial research-backed sources. Bulk/local indexing is preferred for catalog-scale operations.

## Source of Truth

See [`docs/MASTER_SPEC.md`](docs/MASTER_SPEC.md).
