# ABAP code review — checklist and rewrites

How to review ABAP without burying real issues under nits, plus the most common anti-patterns with concrete rewrites.

## Triage framework

Sort every finding into one of three buckets and lead with the highest-impact ones. Reviewers who dump everything in line order make critical issues invisible.

| Bucket | Examples | When to insist |
|---|---|---|
| **Correctness / safety** | Missing `SY-SUBRC`, unhandled exception, missing authority check, unguarded `FOR ALL ENTRIES`, wrong client | Always — these are bugs or security issues |
| **Performance** | `SELECT` in loop, `SELECT *`, nested SELECTs, table type mismatch, push-down opportunity | When data volume justifies it; flag for profiling otherwise |
| **Style / maintainability** | Naming, structure, modern syntax, comment quality | Suggest, don't insist; respect team conventions |

## Correctness anti-patterns

### Missing SY-SUBRC check

```abap
" bad
SELECT SINGLE * FROM zorder INTO ls_order WHERE id = lv_id.
process( ls_order ).   " ls_order may be partially or fully empty

" good
SELECT SINGLE * FROM zorder INTO @DATA(ls_order) WHERE id = @lv_id.
IF sy-subrc <> 0.
  RAISE EXCEPTION TYPE zcx_order_not_found EXPORTING iv_id = lv_id.
ENDIF.
process( ls_order ).
```

After `READ TABLE`, `SELECT SINGLE`, `CALL FUNCTION ... EXCEPTIONS`, dynamic `ASSIGN`, the only safe assumption is that `SY-SUBRC` matters. Check it.

### FOR ALL ENTRIES without empty check

```abap
" bad — empty driver table reads ALL rows
SELECT id total FROM zorder
  FOR ALL ENTRIES IN @lt_keys
  WHERE id = @lt_keys-id
  INTO TABLE @DATA(lt_orders).

" good
IF lt_keys IS NOT INITIAL.
  SELECT id total FROM zorder
    FOR ALL ENTRIES IN @lt_keys
    WHERE id = @lt_keys-id
    INTO TABLE @DATA(lt_orders).
ENDIF.
```

This is the single most common production bug in ABAP — and the most expensive, because it scales with table size.

Two more `FOR ALL ENTRIES` traps:
- **Duplicates in the driver table** are silently removed before the SELECT, which can produce fewer rows than expected. `SORT lt_keys BY ... ASCENDING. DELETE ADJACENT DUPLICATES ...` if your downstream logic assumes 1:1.
- **All key fields of the WHERE must be from the driver table or constants.** Mixing in other variables yields surprising plans.

### Unhandled / over-caught exceptions

```abap
" bad — swallows everything including programming errors
TRY.
    do_complicated_thing( ).
  CATCH cx_root.
    " ignore
ENDTRY.

" bad — propagates an internal exception out of a public API
" cx_sy_open_sql_db crosses persistence -> service -> UI layers

" good — catch what you can handle, wrap the rest
TRY.
    DATA(lv_total) = order_repo->total_for( lv_id ).
  CATCH zcx_order_not_found INTO DATA(lx_nf).
    notify_user( |Order { lv_id ALPHA = OUT } not found| ).
  CATCH zcx_persistence_failed INTO DATA(lx_pf).
    log( lx_pf ).
    RAISE EXCEPTION TYPE zcx_app_error EXPORTING previous = lx_pf.
ENDTRY.
```

### Missing authority check

```abap
" bad
SELECT * FROM zsensitive INTO TABLE @DATA(lt_data).

" good
AUTHORITY-CHECK OBJECT 'Z_SENS'
  ID 'ACTVT' FIELD '03'.
IF sy-subrc <> 0.
  RAISE EXCEPTION TYPE zcx_no_authority.
ENDIF.
SELECT * FROM zsensitive INTO TABLE @DATA(lt_data).
```

Place at the boundary, not deep inside helpers. For row-level security, check inside the loop using row content.

### Hard-coded client / language / sysid

```abap
" bad
SELECT * FROM zorder WHERE mandt = '100' AND langu = 'E'.

" good
SELECT * FROM zorder WHERE mandt = @sy-mandt AND langu = @sy-langu.
```

Even better: don't include `mandt` in client-dependent SELECTs at all — ABAP SQL adds it automatically for client-dependent tables.

### Modifying a table while looping

```abap
" risky — index-based DELETE inside LOOP can skip rows
LOOP AT lt_items INTO DATA(ls_item).
  IF ls_item-status = 'X'.
    DELETE lt_items INDEX sy-tabix.
  ENDIF.
ENDLOOP.

" good — DELETE WHERE outside the loop
DELETE lt_items WHERE status = 'X'.
```

## Performance anti-patterns

### SELECT inside LOOP

```abap
" bad — N round trips
LOOP AT lt_orders ASSIGNING FIELD-SYMBOL(<o>).
  SELECT SINGLE name FROM kna1 INTO @DATA(lv_name) WHERE kunnr = @<o>-customer.
  <o>-customer_name = lv_name.
ENDLOOP.

" good — one trip with FOR ALL ENTRIES (or JOIN)
DATA(lt_keys) = VALUE bapi_t_kunnr( FOR <o> IN lt_orders ( <o>-customer ) ).
SORT lt_keys. DELETE ADJACENT DUPLICATES FROM lt_keys.

IF lt_keys IS NOT INITIAL.
  SELECT kunnr name1 FROM kna1
    FOR ALL ENTRIES IN @lt_keys
    WHERE kunnr = @lt_keys-table_line
    INTO TABLE @DATA(lt_names).
ENDIF.

LOOP AT lt_orders ASSIGNING FIELD-SYMBOL(<o>).
  <o>-customer_name = VALUE #( lt_names[ kunnr = <o>-customer ]-name1 OPTIONAL ).
ENDLOOP.
```

For large data sets in HANA, push the join all the way down — write a CDS view that joins the two sources and select from the view.

### SELECT *

```abap
" bad — wide rows for two fields
SELECT * FROM zorder INTO TABLE @DATA(lt_all)
  WHERE created_on >= @lv_from.

" good
SELECT id total FROM zorder INTO TABLE @DATA(lt_orders)
  WHERE created_on >= @lv_from.
```

`SELECT *` also breaks when the table grows columns: any code that builds dependencies on field order falls over.

### INTO CORRESPONDING FIELDS

```abap
" bad — runtime field mapping, slower, hides bugs
SELECT * FROM zorder INTO CORRESPONDING FIELDS OF TABLE @lt_target ...

" good — explicit fields, space-separated (7.40 form)
SELECT id total created_on FROM zorder INTO TABLE @lt_target ...
```

Use `CORRESPONDING #( ... )` for structure-to-structure copy with mapping; avoid `INTO CORRESPONDING FIELDS` in SELECTs.

### Wrong table type for access pattern

| Access pattern | Right table type |
|---|---|
| Iterate front-to-back, occasional `READ TABLE WITH KEY` over small N | `STANDARD` |
| Frequent `READ TABLE WITH KEY` on a sortable key, ordered iteration acceptable | `SORTED` |
| Frequent unique-key lookup, order doesn't matter | `HASHED` |
| Both ordered iteration AND fast lookup | `STANDARD` with **secondary sorted/hashed key** |

```abap
" good — secondary key for the lookup pattern
TYPES tt_orders TYPE STANDARD TABLE OF zorder
  WITH NON-UNIQUE DEFAULT KEY
  WITH UNIQUE HASHED KEY by_id COMPONENTS id.

DATA(ls) = lt_orders[ KEY by_id id = lv_id ].
```

### Aggregations done in ABAP

```abap
" bad — pulls everything, sums in ABAP
SELECT amount FROM zorder INTO TABLE @DATA(lt_amts)
  WHERE customer = @lv_id.
DATA(lv_total) = REDUCE wrtbtr( INIT s = 0 FOR a IN lt_amts NEXT s = s + a-amount ).

" good — push-down
SELECT SUM( amount ) FROM zorder INTO @DATA(lv_total)
  WHERE customer = @lv_id.
```

For complex aggregations or joins in S/4HANA, write a CDS view and select from it.

### Wide INSERT in a loop

```abap
" bad
LOOP AT lt_input INTO DATA(ls).
  INSERT INTO ztable VALUES ls.
ENDLOOP.

" good — single set-based INSERT
INSERT ztable FROM TABLE @lt_input.
```

The same applies to `UPDATE` and `MODIFY`.

## Style / maintainability anti-patterns

### Header lines

Always remove when refactoring (see `classic-abap.md`).

### Long routines

`START-OF-SELECTION` blocks of 200+ lines, `FORM`s of 500+ lines, `class~method` of 100+ lines. Decompose into named methods. The first refactor is usually "extract method" — pick a coherent block, name what it does, move it.

### Magic numbers / literals

```abap
" bad
IF sy-subrc = 4.
IF lv_status = 'X'.
DATA(lv_due) = sy-datum + 30.

" good
IF sy-subrc = co_not_found.   " constant in a constants class
IF lv_status = if_status=>processed.
DATA(lv_due) = sy-datum + co_default_payment_term_days.
```

### Boolean as `TYPE c LENGTH 1`

```abap
" bad
DATA lv_active TYPE c LENGTH 1.
IF lv_active = 'X'. ... ENDIF.

" good
DATA lv_active TYPE abap_bool.
IF lv_active = abap_true. ... ENDIF.
```

### Commented-out code

Delete it. Version control remembers.

### Obsolete syntax

| Obsolete | Replacement |
|---|---|
| `MOVE a TO b.` | `b = a.` |
| `MOVE-CORRESPONDING ls1 TO ls2.` | `ls2 = CORRESPONDING #( ls1 ).` |
| `CONCATENATE a b INTO c SEPARATED BY space.` | `c = \|{ a } { b }\|.` |
| `DESCRIBE TABLE itab LINES n.` | `n = lines( itab ).` |
| `WRITE: / 'header'.` (in classes) | `cl_demo_output=>display( ... ).` or proper UI |
| `OCCURS 0`, header lines | Standard table + work area |

## A review template

When asked for a review, structure the response as:

```
Summary
- 2-3 sentences on overall state.

Correctness / safety  (highest priority)
1. [file:method:line] <issue>
   Why: <impact>
   Fix:
   <before>
   <after>

Performance
1. ...

Style / maintainability  (consider after the above)
1. ...

Things I couldn't verify
- <anything depending on data, runtime behavior, system release, etc.>
```

This makes it easy for the author to act on the high-impact items first and decide whether to apply the rest.
