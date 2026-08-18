# MTG Card Library — Verified Project State

Status date: 2026-08-18
Authority: operational state only. `docs/MASTER_SPEC.md` remains the authoritative product specification.

## Current phase

**Phase 1 — usable foundation: IN PROGRESS**

Do not mark Phase 1 complete merely because the UI renders or tests pass. Completion remains governed by `MASTER_SPEC.md`.

## Last verified checkpoint

Active branch: `main`
Active UI entry: `index.html` -> `src/app-v2.js`
Activation commit: `a2d3dd5237e0de52654d42ec76c2a5883884161d`

Local verification at activation:

- JavaScript syntax check: PASS
- Node test suite: **15/15 PASS**
- Static build verification: **9 required assets PASS**
- GitHub branch head verified after activation
- GitHub combined-status endpoint returned no status contexts at verification time, so remote CI is not independently claimed as green

## Completed and verified at this checkpoint

### Catalog / provider foundation

- Provider-normalized exact-printing model
- Scryfall-compatible external identifier mapping
- Null-safe price fields
- Paper-card bulk sync command: `npm run sync:scryfall`
- Local catalog loader with development seed fallback
- Catalog source/date/count visibility in Settings
- User collection metadata remains separate from provider refresh data

### Search

- Search over name, rules text, printed text where present, type, set, collector number, artist, flavor text, and keywords
- Exact or inclusive color-identity filtering
- Mono-color, all ten guild pairs, all shards/wedges, five-color, and colorless structured choices
- Rarity filtering
- Set filtering
- Type/subtype filtering
- Format-legality filtering
- Mana-value range filtering
- Maximum USD price filtering
- Visible terminology normalization
- Extended terminology dictionary including EDH/Commander, CMC/MV, ETB/LTB, blink/flicker, tutor, ramp, mana rock/dork, draw/cantrip, removal/wipes, graveyard concepts, stax/prison/tax, token/resource terms, protection keywords, infect/poison/toxic/proliferate, aura/equipment/Voltron, and common combat keywords
- Search rendering capped to the first 100 matches to prevent unbounded DOM rendering

### Card / printing detail

- Exact printing identity
- Card image
- Mana cost, type line, Oracle text
- Set/code/collector number
- Rarity, language, finishes
- Artist and release date when present
- Legal format list
- Exact-printing owned quantity
- Available USD/EUR/MTGO price fields

### Collection

- Exact-printing stacks
- Non-negative integer quantity enforcement
- Condition editing
- Finish editing
- Language editing
- Named collection locations
- Location selection per stack
- Remove stack
- Value summaries with unknown-price handling
- Extended stored metadata model for acquisition data, tags, notes, status, loan state, altered/misprint/signed flags, and custom attributes

### Sets

- Set search by name/code
- Set completion counts/percentages
- Add missing set printings to Buy List

### Deck / purchase foundation

These features arrived early but do not supersede Phase 1 delivery order:

- Saved deck record
- Deck text import
- Exact vs alternate-printing ownership calculation
- Missing-copy calculation
- Add all missing deck cards to Buy List
- Buy-list price estimate

### Import / export

- Full application JSON backup/export
- JSON restore
- Collection CSV export
- Collection CSV parse/preview with row diagnostics
- Common collection-column aliases
- Exact set+collector matching, name+set fallback, then name fallback
- Valid-row partial import while preserving invalid-row counts
- Deck-text parse/preview/import

## Perfection-gate items still open before Phase 1 can be called complete

1. **Production-scale catalog delivery must be exercised and verified.** The current bulk sync produces a local catalog file, but a full all-printings catalog must not require the browser to render or repeatedly parse an impractically large monolithic dataset.
2. Add a production-appropriate indexed search/delivery path with last-known-good catalog rollback semantics.
3. Exercise an actual bulk catalog sync end-to-end and verify printing counts, search behavior, memory/performance, and card-image/detail behavior.
4. Finish basic location management beyond creation/assignment, including rename and safe movement workflows required by the master spec.
5. Expand set checklist behavior toward base-vs-variant views and ownership filtering.
6. Expand import/export coverage and round-trip tests for the important collection metadata fields.
7. Add browser-level interaction verification. Current verification is logic/build based, not a full browser automation pass.
8. Independently confirm GitHub Actions execution rather than inferring success from local tests.

## Next controlled step

Build the production-scale catalog index/delivery layer while keeping the final backend framework, database, hosting, authentication, and cloud-storage decisions **unlocked**. The implementation may use a provisional local indexing mechanism for validation, but that experiment does not become an approved architecture choice merely by existing.

## Locked preservation rules

- `MASTER_SPEC.md` remains the Source of Truth.
- Exact-printing identity stays distinct from Oracle/card identity.
- Missing prices remain unknown, never invented as zero.
- Provider refreshes may not overwrite user collection metadata.
- No feature already required by the master spec is removed merely because it is not present in the current checkpoint.
- Scanner remains required as a later planned capability.
