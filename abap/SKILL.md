---
name: abap
description: Write, review, and refactor SAP ABAP code — modern object-oriented ABAP and clean-ABAP style alongside classic procedural reports, function modules, and dialog programs. Use this skill whenever the user mentions ABAP, SAP development, Z-programs/Y-programs, reports, function modules, ABAP classes, BAPIs, internal tables, ALV, SELECT statements against SAP tables, or asks to write, review, refactor, modernize, or debug any ABAP code — even if they don't say the word "skill" or specify the SAP version. Also trigger when reviewing code that uses ABAP-specific keywords like REPORT, DATA:, LOOP AT, READ TABLE, SELECT ... INTO TABLE, CALL FUNCTION, AUTHORITY-CHECK, PERFORM/FORM, or when files have extensions like .abap, .prog, or look like SAP transport content.
---

# ABAP

This skill helps you write, review, and refactor ABAP — SAP's primary language for business applications. ABAP code lives a long time and runs in environments where reliability, performance against large tables, and SAP-specific idioms matter more than language fashion. This skill targets two things you'll be asked to do constantly: producing new ABAP that follows modern, clean conventions, and improving existing ABAP without breaking the world it runs in.

## Target release

**This skill targets ABAP 7.40** (assuming a late support pack — SP05 for `FOR` / `REDUCE` / inline `@DATA(...)` in `SELECT`, SP08 for `FILTER`). Do **not** generate 7.50+ syntax. In particular:

- Use the **classic field list** in `SELECT`: space-separated, no commas, no `FIELDS` keyword.
  - `SELECT id customer total FROM zorder INTO TABLE @DATA(lt) WHERE ...` — yes
  - `SELECT FROM zorder FIELDS id, customer, total WHERE ... INTO TABLE @DATA(lt)` — no, that's 7.50
- No RAP (BDEFs, `MANAGED` / `UNMANAGED` behavior) — that's 7.54+ / S/4HANA 1909+.
- No ABAP Cloud / Steampunk-only constructs (`xco_cp_*`, released-API restrictions).
- CDS views exist in 7.40 SP05 and are usable; AMDP exists in 7.40. Mention them when relevant but lean toward classic ABAP SQL for examples.

If the user is clearly on a system newer than 7.40 and would benefit from a 7.50+ feature, you can mention the existence of the newer form, but the primary recommendation must be 7.40-compatible.

## Philosophy

Two ideas drive the recommendations here:

**Prefer modern, expression-oriented ABAP — within 7.40.** ABAP 7.40 introduced constructor expressions (`VALUE`, `NEW`, `REF`, `CONV`, `COND`, `SWITCH`), inline declarations (`DATA(...)`, `FIELD-SYMBOL(<...>)`), table expressions (`itab[ key = ... ]`), iteration expressions (`FOR`, `REDUCE`, `FILTER`), and string templates (`|...|`). These reduce ceremony, eliminate whole categories of bugs (uninitialized variables, copy-paste errors), and make intent visible. Use them.

**Respect the system you're in.** Even within 7.40, some shops are still on early support packs, where `FOR` / `REDUCE` / `FILTER` aren't available. Some code bases are large and use header lines, `PERFORM`/`FORM`, and procedural reports throughout. A skill that only emits modern syntax fails there. Where a feature requires a specific SP, say so and provide a fallback.

If the SP level is unknown, ask. If the user shows code, read it: classic syntax (PERFORM, header lines, MOVE-CORRESPONDING from a structure with header line) usually means an older code base or an older convention that they'll keep using for consistency. Match the surrounding style unless they explicitly want modernization.

## Workflow: writing new code

1. **Clarify the target.** Before writing, confirm:
   - SP level on 7.40 if you're going to use `FOR` / `REDUCE` / `FILTER` (assume SP08+ unless told otherwise).
   - Whether this is on-stack or in customer namespace (Z*/Y*).
   - The deliverable: standalone report, class, function module, BAdI implementation, etc. (RAP/Fiori is out of scope for 7.40.)
   - Any team conventions (Hungarian-style prefixes vs. clean-ABAP "no prefix" stance, error handling pattern, logging framework).

2. **Pick the right artifact type.** Don't default to a `REPORT`. A small piece of reusable logic belongs in a class. A standalone executable belongs in a report that delegates to a class. A web/UI service belongs in RAP. A reusable callable from outside ABAP belongs in a function module (RFC-enabled if needed).

3. **Write the public surface first.** Define class signatures, exception classes, DDIC types, and public method contracts before filling in implementations. This forces you to name things well and surfaces design problems early.

4. **Implement small, named methods.** A method should fit on a screen and do one thing. Long `START-OF-SELECTION` blocks and 200-line `PERFORM` routines are the most common ABAP smell. Decompose.

5. **Handle errors with class-based exceptions.** `RAISE EXCEPTION TYPE` with subclasses of `cx_static_check` (or `cx_dynamic_check`) and meaningful messages. Avoid `MESSAGE ... RAISING` for new code; avoid `SY-SUBRC` propagation across multiple call layers.

6. **Test where possible.** ABAP Unit (`CLASS ... FOR TESTING`) is well-supported. For new code, write at least one positive and one error-path test. For database-bound logic, prefer test doubles via interfaces over mocking the DB.

7. **Document intent, not syntax.** Comments should explain *why*, not restate `lv_total = lv_a + lv_b.`. Method-level docs (`"! ...`) feed the ABAP Doc tool and help downstream readers.

## Workflow: reviewing or refactoring code

1. **Read the surrounding code first.** Don't propose modernizations that break the file's style. If everything else uses `lv_`/`lt_` prefixes and `PERFORM`, your refactor should fit in.

2. **Triage by impact.** Group findings into three buckets:
   - **Correctness / safety**: missing `SY-SUBRC` checks, `FOR ALL ENTRIES` without empty-check, unhandled exceptions, authority checks missing, hard-coded clients.
   - **Performance**: SELECT inside LOOP, SELECT * with INTO CORRESPONDING, nested SELECTs, missing indexes / table types, PARALLEL CURSOR opportunities.
   - **Style / maintainability**: header lines, magic numbers, long routines, dead code, obsolete syntax.
   The first two are non-negotiable; the third is taste and should respect the team's conventions.

3. **Show before/after for each non-trivial change.** Don't just say "use VALUE". Show the original and the rewrite, and explain the win.

4. **Preserve behavior.** Modernizing syntax must not change semantics. `READ TABLE itab WITH KEY ... ASSIGNING` and `itab[ ... ]` differ in how they signal "not found" — the second raises `CX_SY_ITAB_LINE_NOT_FOUND`, the first sets `SY-SUBRC = 4`. Pick the right one.

5. **Call out things you can't verify.** If you don't have visibility into a custom table's contents, the surrounding caller, or the system release, say so explicitly rather than assuming.

6. **Order suggestions by ROI.** A reviewer who lists every nit alongside a real bug buries the bug. Lead with the things that matter.

See `references/code-review.md` for the full review checklist and the most common anti-patterns with rewrites.

## Core conventions

Naming is the most contested topic in ABAP style. Two camps exist and you should follow whichever the project uses:

- **Hungarian prefixes (traditional).** `lv_count` (local var), `lt_orders` (local table), `ls_order` (local structure), `lo_handler` (local object ref), `lr_data` (local data ref), `gv_/gt_/...` for globals, `mv_/mt_/...` for class members, `iv_/it_/.../ev_/et_/.../cv_/.../rv_/...` for parameters by direction (importing/exporting/changing/returning). `Z`/`Y` namespace prefix on customer objects.
- **Clean ABAP (no scope prefix).** Names describe intent (`order_count`, `pending_invoices`); scope is conveyed by where the variable is declared. `Z`/`Y` prefix still applies to repository objects.

Default to clean-ABAP style for greenfield code unless you can see the team uses Hungarian. Never mix the two within a file.

Other defaults:
- One statement per line. No chained `DATA: a, b, c.` for unrelated names.
- `RETURNING` parameters are preferred for single-output methods — they enable functional chaining and `DATA(result) = obj->compute( )`.
- Use `abap_bool` (`abap_true` / `abap_false`), not `TYPE c LENGTH 1`.
- Use `cl_abap_*` and standard utility classes before writing your own (e.g., `cl_abap_timefm` for time, `cl_abap_codepage` for encoding, `xco_cp_*` in ABAP Cloud).
- Avoid `SY-*` outside of the statement that produced it. Capture into a local variable immediately if you need it later.

For the full clean-ABAP cheat sheet, see `references/clean-abap.md`.

## Modern syntax — the high-leverage subset

These are the constructs you should reach for first when writing or modernizing ABAP. Side-by-side comparisons live in `references/modern-syntax.md`; below is the short list.

**Inline declarations.**
```abap
" old
DATA: lt_orders TYPE STANDARD TABLE OF zorder.
SELECT * FROM zorder INTO TABLE lt_orders WHERE customer = lv_id.

" new
SELECT * FROM zorder INTO TABLE @DATA(lt_orders) WHERE customer = @lv_id.
```

**Constructor expressions.**
```abap
" build a structure
DATA(ls_addr) = VALUE zaddress( street = 'Main 1' city = 'Bratislava' ).

" build a table
DATA(lt_status) = VALUE tt_status(
  ( code = 'A' text = 'Active'  )
  ( code = 'I' text = 'Inactive' ) ).

" conditional value
DATA(lv_label) = COND string( WHEN lv_count = 0 THEN 'empty'
                              WHEN lv_count = 1 THEN 'one'
                              ELSE                   |{ lv_count } items| ).
```

**Table expressions.**
```abap
" old
READ TABLE lt_orders WITH KEY id = lv_id INTO ls_order.
IF sy-subrc <> 0. ... ENDIF.

" new (raises CX_SY_ITAB_LINE_NOT_FOUND if absent)
DATA(ls_order) = lt_orders[ id = lv_id ].

" new, safe variant — defaults if not found
DATA(ls_order) = VALUE #( lt_orders[ id = lv_id ] DEFAULT VALUE #( ) ).
```

**FOR / REDUCE / FILTER.**
```abap
" map
DATA(lt_ids) = VALUE id_tab( FOR <o> IN lt_orders ( <o>-id ) ).

" filter
DATA(lt_open) = FILTER #( lt_orders WHERE status = 'O' ).

" reduce
DATA(lv_total) = REDUCE i( INIT s = 0
                           FOR <o> IN lt_orders
                           NEXT s = s + <o>-amount ).
```

**String templates.**
```abap
DATA(lv_msg) = |Order { ls_order-id ALPHA = OUT } total { ls_order-total NUMBER = USER }|.
```

**ABAP SQL — 7.40 form.** Space-separated field list, host variables escaped with `@`, inline `@DATA(...)` for the target table. **Do not** use the 7.50 `SELECT FROM ... FIELDS ..., ..., ...` comma form.

```abap
SELECT id customer total
  FROM zorder
  INTO TABLE @DATA(lt_orders)
  UP TO 100 ROWS
  WHERE created_on >= @lv_from
  ORDER BY created_on DESCENDING.
```

When the surrounding code is older than 7.40 SP05, fall back to a pre-declared target and no `@`:

```abap
DATA lt_orders TYPE STANDARD TABLE OF zorder.
SELECT id customer total
  FROM zorder
  INTO CORRESPONDING FIELDS OF TABLE lt_orders
  WHERE created_on >= lv_from.
```

## Anti-patterns to flag on sight

These come up so often they deserve top-level attention. Every one of these has a rewrite — see `references/code-review.md` for details.

- **`SELECT ... INSIDE LOOP`** over an internal table. Pull the data once outside the loop with `FOR ALL ENTRIES` or a join.
- **`FOR ALL ENTRIES` without empty-table check.** Empty driver table = full-table scan, not zero rows. Always guard.
- **`SELECT *` when only a few fields are needed.** Wastes I/O and breaks if the table grows columns.
- **`INTO CORRESPONDING FIELDS` as a default.** It's slower and hides field-mapping bugs. Use explicit field lists.
- **Header lines on internal tables.** Long deprecated. Use a separate work area or `ASSIGNING <fs>`.
- **Unchecked `SY-SUBRC`.** Especially after `READ TABLE`, `SELECT SINGLE`, `CALL FUNCTION` with `EXCEPTIONS`.
- **`MESSAGE 'hard-coded text' TYPE 'E'`** outside legitimate user-facing flows. Use message classes; for programmatic flow, use class-based exceptions.
- **Modifying a table you're looping over** without `LOOP ... ASSIGNING <fs>` plus careful `INSERT/DELETE` semantics.
- **Authority gaps.** Reports that read sensitive tables without `AUTHORITY-CHECK`.
- **Hard-coded clients, languages, or system IDs.** Use `sy-mandt`, `sy-langu`, environment-aware lookups.
- **Catch-all `CATCH cx_root.`** that swallows everything. Catch the narrowest exception you can handle.
- **`PERFORM` subroutines holding business logic** in code that could be class methods. Refactor when touching them.

## When to read the references

Reach for the reference files when the SKILL.md summary isn't enough:

- `references/clean-abap.md` — the full clean-ABAP cheat sheet: naming, structure, error handling, testing, comments. Read when the user asks about style, conventions, or "what's the right way to write X".
- `references/modern-syntax.md` — old-vs-new comparisons across the major 7.40+ constructs, plus release notes on what's available where. Read when modernizing legacy code or when you need to know whether a syntax form is safe in the user's release.
- `references/classic-abap.md` — patterns that still matter: classical reports, selection screens, dialog programs, function modules, BAPIs, ALV grids. Read when the user is on ECC/older releases or maintaining classic code.
- `references/code-review.md` — the review checklist: anti-patterns with full before/after rewrites, performance heuristics, and a triage framework. Read on any review or refactor request.
- `references/examples.md` — ready-to-adapt templates: a clean report skeleton, a class with constructor and exception class, a BAPI wrapper, an ALV display, a unit test class. Read when starting from a blank file.

## Output format

When **writing new code**, produce:
1. A brief summary of what you're building and any assumptions (release level, namespace, whether to write tests).
2. The code itself in fenced ABAP blocks. Keep DDIC dependencies named (don't invent table fields silently).
3. A short "what's next" section: how to activate, what's missing (tests, authority, transport), and any follow-ups.

When **reviewing or refactoring**, produce:
1. A triaged summary (Correctness / Performance / Style) with the highest-impact items first.
2. For each finding: location (file/method/line if available), the issue, why it matters, and a before/after rewrite.
3. Anything you can't verify, called out explicitly.
