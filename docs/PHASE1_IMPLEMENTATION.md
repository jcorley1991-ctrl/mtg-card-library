# Phase 1 Implementation Plan

Status: **Active implementation plan**
Authority: subordinate to `docs/MASTER_SPEC.md`

## Scope

Phase 1 implements the first usable catalog foundation without locking long-term hosting, authentication, or storage infrastructure.

### Deliverables

1. Responsive web application shell with first-class navigation for Search, Collection, Sets, Decks, Buy Lists, Pricing, Import/Export, Scanner, and Settings.
2. Provider abstraction for card catalog/search/pricing data.
3. Scryfall-compatible search query construction with MTG terminology normalization.
4. Exact-printing card model preserving set, collector number, finish, language, rarity, legalities, images, and provider IDs.
5. Collection inventory model with quantity, condition, finish, language, location, acquisition data, tags, and status fields.
6. Deck model with sections, commander metadata, ownership matching, and missing-card calculation contracts.
7. Buy-list model supporting missing cards, preferred printing, quantity, finish, target price, and priority.
8. Local development seed data so the interface works before bulk catalog ingestion is wired.
9. Tests for terminology normalization, color aliases, collection quantity safety, and ownership matching.

## Explicitly provisional choices

The implementation may use a lightweight TypeScript web stack for Phase 1. This does **not** lock the final frontend framework, database, hosting, authentication, or sync architecture.

## Data-source rules

- Catalog-scale operations must be designed for bulk/local indexing.
- Live API lookups are adapters, not the permanent search engine.
- User-owned metadata must never be overwritten by provider refreshes.
- A missing price is `null`/unknown, never `$0`.
- Exact printing identity is preserved independently from Oracle-card identity.

## Search acceptance criteria

The query layer must support both direct structured filters and friendly MTG terms. Initial terminology includes:

- Guilds: Azorius, Dimir, Rakdos, Gruul, Selesnya, Orzhov, Izzet, Golgari, Boros, Simic.
- Shards: Bant, Esper, Grixis, Jund, Naya.
- Wedges: Abzan, Jeskai, Sultai, Mardu, Temur.
- Common concepts: ETB, dies, cast, attack, combat damage, draw, discard, tutor, ramp, mana rock, removal, board wipe, counterspell, sacrifice, aristocrats, blink/flicker, reanimate, mill, treasure, token, lifegain, drain, stax, tax, protection, hexproof, indestructible, deathtouch, lifelink, menace, vigilance, trample, haste, flying, reach.

The terminology dictionary must be extensible and must expose the normalized query to the user instead of silently changing intent.

## Verification gate

Phase 1 is not considered complete until:

- application builds successfully;
- tests pass;
- all top-level modules render;
- search normalization is deterministic;
- collection quantities cannot become negative;
- missing-card calculations distinguish exact printing, alternate printing, and genuinely missing copies;
- generated code has been compared against `MASTER_SPEC.md` for drift.
