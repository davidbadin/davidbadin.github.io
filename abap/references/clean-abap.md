# Clean ABAP — cheat sheet

Distilled from SAP's clean-ABAP style guide and what holds up in real projects. Use this as the default when writing new code; respect existing conventions when editing.

## Naming

- Use **descriptive, pronounceable** names. `customer_orders`, not `cust_ord_t` or `tab1`.
- **Solution-domain over problem-domain** — name things by what the code does, not by SAP jargon, when there's a choice.
- Avoid abbreviations except universally understood ones (`id`, `url`, `db`).
- **Plural for collections**: `orders`, not `order_table` or `t_order`.
- **Verb phrases for methods**: `calculate_total`, `is_valid`, `has_open_items`, `get_customer`. Boolean methods read as predicates: `IF order->is_valid( ).`
- **Noun phrases for classes**: `cl_order_validator`, not `cl_validate_order`.
- The `Z`/`Y` namespace prefix on repository objects is non-negotiable in customer code; that's not Hungarian, it's how SAP separates namespaces.

### Hungarian prefixes — when to use them

Clean ABAP recommends dropping `lv_/lt_/ls_/lo_/...`. In practice:

- **Greenfield, modern team, clean-ABAP enforced**: drop them.
- **Existing project with Hungarian everywhere**: keep them. Consistency beats personal preference.
- **Mixed file you're editing**: match the file. If the file is itself mixed, ask the user which way to go.

If you keep them, the conventional set:

| Prefix | Meaning                              |
|--------|--------------------------------------|
| `lv_`  | local variable (elementary)          |
| `lt_`  | local internal table                 |
| `ls_`  | local structure                      |
| `lo_`  | local object reference               |
| `lr_`  | local data reference (`REF TO data`) |
| `gv_/gt_/gs_/go_/gr_` | global (program-level)  |
| `mv_/mt_/ms_/mo_/mr_` | class member (instance) |
| `sv_/st_/...` | static class member            |
| `iv_/it_/is_/io_/ir_` | importing parameter     |
| `ev_/et_/es_/eo_/er_` | exporting parameter     |
| `cv_/ct_/cs_/co_/cr_` | changing parameter      |
| `rv_/rt_/rs_/ro_/rr_` | returning parameter     |

## Statements

- **One statement per line.** Avoid `DATA: a, b, c.` chains for unrelated names — they hide diffs and make refactors harder.
- **Inline declarations** at first use (`DATA(...)`, `FIELD-SYMBOL(<...>)`). Cuts noise and limits scope.
- **No `EXIT` to leave a method.** Use `RETURN` (or `CHECK` with intent) so the reader sees method-level control flow.
- **No `EXIT` from a loop just to skip an iteration.** Use `CONTINUE`, or invert the condition.

## Methods

- **Short**: aim for under 20 lines of body. Long methods are usually doing more than one thing.
- **Few parameters**: 0–3 is comfortable, 4–5 is the ceiling, more is a code smell — pass a structure or split the method.
- **Do one thing.** A method called `update_order` should not also send emails. Caller composes; the method does its job.
- **`RETURNING` for single results.** Lets callers chain: `DATA(total) = order->calculate_total( ).` Use `EXPORTING` only when there are multiple meaningfully distinct outputs.
- **Constructors stay simple.** Validate inputs and store them. Do real work in factory methods (`create_for_customer( )`) or in named methods called after construction.
- **Don't `RAISE` from a getter.** Make it return a defaultable value or expose a separate `is_available( )`.

## Classes

- **Final by default.** `CLASS ... DEFINITION FINAL.` Open for extension only when subclassing is part of the design.
- **Public attributes are read-only or constants.** Anything mutable goes through methods.
- **Prefer composition over inheritance.** Inheritance for "is-a" only — and even then, an interface is usually better.
- **Interfaces describe roles**, not "everything the class can do". Small focused interfaces beat one fat one.

## Error handling

- **Class-based exceptions** (`cx_static_check`, `cx_dynamic_check`, `cx_no_check`). Avoid old-style `EXCEPTIONS` lists and `MESSAGE ... RAISING` for new code.
- **Static check** for things callers should handle. **Dynamic check** for programmer errors that shouldn't normally occur.
- **Catch what you can handle, propagate the rest.** Bare `CATCH cx_root` that swallows is the worst pattern in ABAP review.
- **Wrap when crossing a layer.** Don't leak `cx_sy_open_sql_db` to the UI; catch it, log it, and raise your own `zcx_order_persistence_failed`.
- **Set `previous = exception_object`** when wrapping so the chain is preserved.
- **Messages**: use message classes (`MESSAGE` with text symbols / message numbers) for anything user-facing. Hard-coded English literals don't translate.

## Internal tables

- **Pick the right table type.** `STANDARD` for ordered processing, `SORTED` for ordered access by key, `HASHED` for `O(1)` lookups by full unique key. The default `STANDARD` with frequent `READ TABLE WITH KEY` over a large table is a common performance bug.
- **Define secondary keys** when you need multiple access paths.
- **`ASSIGNING <fs>`** rather than `INTO ls_` when looping for write-back or to avoid copying. Use `READ TABLE ... ASSIGNING` likewise.
- **Initialize with `VALUE #( ... )`** rather than building line-by-line with `APPEND`.

## ABAP SQL

- **Explicit field list**. `SELECT id customer total` (space-separated, 7.40 form) over `SELECT *`. The 7.50 comma form (`SELECT FROM zorder FIELDS id, customer, total`) is out of scope here.
- **No `SELECT` inside `LOOP`.** Pull the data once. If you need filtered data per item, use `FOR ALL ENTRIES` (with empty-check) or a JOIN.
- **`FOR ALL ENTRIES` empty-check is mandatory.**
  ```abap
  IF lt_keys IS NOT INITIAL.
    SELECT ... FOR ALL ENTRIES IN @lt_keys WHERE ... INTO TABLE @DATA(lt_data).
  ENDIF.
  ```
- **`UP TO n ROWS`** to bound result sets when you only need a few.
- **Host variables**: prefix with `@` (`WHERE id = @lv_id`). Without `@`, the meaning differs.
- **Order is not guaranteed** without `ORDER BY`. Don't rely on insertion order.
- **HANA: prefer push-down**. CDS views and AMDP let the database do the work; in S/4HANA this is usually the right place for heavy aggregation.

## Comments

- **Comment why, not what.** `* increment counter` next to `lv_count = lv_count + 1.` is noise.
- **Use ABAP Doc** (`"!`) for public method headers — type info, parameters, raised exceptions.
- **No commented-out code.** Version control exists for that.
- **TODO/FIXME with context**: `" TODO 2026-04-30 alice: revisit when SLA changes`. Bare TODOs rot.

## Testing

- **ABAP Unit** for everything testable. `CLASS ltcl_xyz DEFINITION FOR TESTING DURATION SHORT RISK LEVEL HARMLESS.`
- **Local test classes in the same include** — they have access to private methods via `FRIENDS`.
- **Test doubles**: pass interfaces, not concrete classes, into your code under test. `cl_abap_testdouble=>create( )` for quick mocks.
- **Don't test the database.** Abstract DB access behind a repository interface; test the logic, mock the repo.

## Dependencies and seams

- **Constructor injection** for required collaborators. Pass them in; don't `cl_xyz=>singleton( )` from inside.
- **Factory method** when construction logic is non-trivial.
- **Avoid `CREATE OBJECT lo_helper TYPE cl_specific_helper`** inside business code — that's hidden coupling. Inject an interface.
