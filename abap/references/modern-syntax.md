# Modern ABAP syntax — old vs new (7.40 target)

Side-by-side rewrites for the constructs that show up most often. **This skill targets ABAP 7.40.** Each section names the SP where the form became available; if the user's SP is older, fall back to the "old" form. Do **not** use 7.50+ syntax (notably the `SELECT FROM ... FIELDS ..., ...` comma-separated form).

## Inline declarations (7.40)

```abap
" old
DATA: lt_orders TYPE STANDARD TABLE OF zorder.
DATA: ls_order  TYPE zorder.
SELECT * FROM zorder INTO TABLE lt_orders WHERE active = 'X'.
LOOP AT lt_orders INTO ls_order.
  ...
ENDLOOP.

" new
SELECT * FROM zorder INTO TABLE @DATA(lt_orders) WHERE active = @abap_true.
LOOP AT lt_orders INTO DATA(ls_order).
  ...
ENDLOOP.
```

`FIELD-SYMBOL(<fs>)` works the same way for assignments:

```abap
LOOP AT lt_orders ASSIGNING FIELD-SYMBOL(<order>).
  <order>-processed = abap_true.
ENDLOOP.
```

When using `@DATA(...)` in `SELECT`, the `@` is required for host variables.

## VALUE — structures and tables (7.40)

```abap
" old
DATA ls_addr TYPE zaddress.
ls_addr-street = 'Main 1'.
ls_addr-city   = 'Bratislava'.

" new
DATA(ls_addr) = VALUE zaddress( street = 'Main 1' city = 'Bratislava' ).

" old (table)
DATA lt_status TYPE tt_status.
DATA ls TYPE status.
ls-code = 'A'. ls-text = 'Active'.   APPEND ls TO lt_status.
ls-code = 'I'. ls-text = 'Inactive'. APPEND ls TO lt_status.

" new
DATA(lt_status) = VALUE tt_status(
  ( code = 'A' text = 'Active'   )
  ( code = 'I' text = 'Inactive' ) ).
```

`VALUE #( )` uses the operand type from context — useful for parameters and `RETURNING`:

```abap
result = VALUE #( ( id = 1 ) ( id = 2 ) ).
```

## NEW — object construction (7.40)

```abap
" old
DATA lo_handler TYPE REF TO cl_order_handler.
CREATE OBJECT lo_handler EXPORTING iv_id = lv_id.

" new
DATA(lo_handler) = NEW cl_order_handler( iv_id = lv_id ).

" anonymous
go_handler = NEW #( iv_id = lv_id ).
```

## Table expressions (7.40)

```abap
" old
READ TABLE lt_orders WITH KEY id = lv_id INTO ls_order.
IF sy-subrc <> 0. RAISE EXCEPTION TYPE zcx_not_found. ENDIF.

" new — raises CX_SY_ITAB_LINE_NOT_FOUND if missing
DATA(ls_order) = lt_orders[ id = lv_id ].

" with default
DATA(ls_order) = VALUE #( lt_orders[ id = lv_id ] DEFAULT VALUE #( ) ).

" check existence without read
IF line_exists( lt_orders[ id = lv_id ] ).
  ...
ENDIF.

" line index
DATA(idx) = line_index( lt_orders[ id = lv_id ] ).
```

Behavior difference: `READ TABLE ... WITH KEY` sets `SY-SUBRC`; the table expression raises an exception. Pick based on whether "not found" is exceptional.

## COND / SWITCH (7.40)

```abap
" old
DATA lv_label TYPE string.
IF lv_count = 0.
  lv_label = 'empty'.
ELSEIF lv_count = 1.
  lv_label = 'one'.
ELSE.
  CONCATENATE lv_count 'items' INTO lv_label SEPARATED BY space.
ENDIF.

" new
DATA(lv_label) = COND string( WHEN lv_count = 0 THEN 'empty'
                              WHEN lv_count = 1 THEN 'one'
                              ELSE                   |{ lv_count } items| ).

" switch — discrete values
DATA(lv_kind) = SWITCH string( ls_doc-type
                               WHEN 'INV' THEN 'Invoice'
                               WHEN 'CRN' THEN 'Credit note'
                               ELSE           'Other' ).
```

## FOR — table comprehensions (7.40)

```abap
" map: extract one field
DATA(lt_ids) = VALUE id_tab( FOR <o> IN lt_orders ( <o>-id ) ).

" filter inside FOR
DATA(lt_open_ids) = VALUE id_tab(
  FOR <o> IN lt_orders WHERE ( status = 'O' ) ( <o>-id ) ).

" cross-join over two tables
DATA(lt_pairs) = VALUE pair_tab(
  FOR <a> IN lt_a
  FOR <b> IN lt_b WHERE ( key = <a>-key )
    ( a = <a>-value b = <b>-value ) ).
```

## REDUCE — fold (7.40)

```abap
DATA(lv_total) = REDUCE i( INIT s = 0
                          FOR <o> IN lt_orders
                          NEXT s = s + <o>-amount ).
```

## FILTER — table filtering (7.40)

```abap
" itab must be SORTED or have a sorted secondary key on the filter field
DATA(lt_open) = FILTER #( lt_orders WHERE status = 'O' ).

" exclude form
DATA(lt_other) = FILTER #( lt_orders EXCEPT WHERE status = 'O' ).
```

## CORRESPONDING — structure copy with mapping (7.40)

```abap
" copy matching fields
ls_target = CORRESPONDING #( ls_source ).

" with explicit mapping
ls_target = CORRESPONDING #( ls_source MAPPING new_field = old_field ).

" deep copy with table
ls_target = CORRESPONDING #( ls_source EXCEPT created_on ).
```

## String templates (7.02 / 7.40)

```abap
" old
CONCATENATE 'Order' lv_id 'total' lv_total INTO lv_msg SEPARATED BY space.

" new
DATA(lv_msg) = |Order { lv_id ALPHA = OUT } total { lv_total NUMBER = USER }|.
```

Format options inside `{ }`:
- `ALPHA = IN | OUT` — leading-zero conversion
- `NUMBER = USER | RAW` — user-format numbers
- `DATE = USER | ISO`, `TIME = USER | ISO`
- `WIDTH = n PAD = '0' ALIGN = RIGHT`
- `CASE = UPPER | LOWER`

## ABAP SQL — 7.40 form

In 7.40 SP05+ you get inline `@DATA(...)` and host variables escaped with `@`. The field list is **space-separated**; the comma-separated `FIELDS` form is 7.50+ and is out of scope here.

```abap
" old (pre-7.40 — pre-declared target, no host-variable escapes)
DATA lt_orders TYPE STANDARD TABLE OF zorder.
SELECT * FROM zorder INTO TABLE lt_orders WHERE created_on >= lv_from.

" 7.40 SP05+ — space-separated fields, @ host vars, inline @DATA
SELECT id customer total
  FROM zorder
  INTO TABLE @DATA(lt_orders)
  UP TO 100 ROWS
  WHERE created_on >= @lv_from
  ORDER BY created_on DESCENDING.
```

**Don't** write this — it's 7.50:

```abap
" 7.50+ — DO NOT USE (out of scope for 7.40)
SELECT FROM zorder
  FIELDS id, customer, total
  WHERE created_on >= @lv_from
  INTO TABLE @DATA(lt_orders).
```

Joins, sub-queries, aggregates, and unions all work in 7.40 ABAP SQL with the classic syntax:

```abap
SELECT a~id a~total b~name1
  FROM zorder AS a
  INNER JOIN kna1 AS b ON a~customer = b~kunnr
  INTO TABLE @DATA(lt_joined)
  WHERE a~created_on >= @lv_from.

SELECT customer SUM( total ) AS revenue
  FROM zorder
  INTO TABLE @DATA(lt_revenue)
  WHERE created_on >= @lv_from
  GROUP BY customer.
```

For aggregations over large tables in HANA, prefer CDS views (also available in 7.40 SP05) over inline aggregation.

## CONV — type conversion (7.40)

```abap
" old
DATA lv_int TYPE i.
lv_int = lv_string.   " implicit, sometimes won't compile

" new
DATA(lv_int) = CONV i( lv_string ).
```

Useful when passing a literal of one type to a parameter expecting another.

## REF — taking a reference (7.40)

```abap
DATA(lr_data) = REF #( ls_order ).   " REF TO zorder pointing at ls_order
```

## Functional method calls in expressions (7.02 / 7.40)

```abap
" chain
DATA(lv_total) = order_factory=>create_for_customer( lv_id )->calculate_total( ).

" used inside VALUE / table expression
DATA(lt_invoices) = invoice_repo=>list_for( lv_customer ).
```

A method must have at most one importing+returning signature with the right shape to be called this way (i.e., `RETURNING` and only optional importing parameters).

## Release reference (7.40 SP coverage)

In scope for this skill:

| Feature                          | Available from |
|----------------------------------|----------------|
| String templates `\|...\|`       | 7.02           |
| Inline `DATA(...)` / `FIELD-SYMBOL(<...>)` | 7.40 SP02 |
| `VALUE`, `NEW`, `REF`, `CONV`, `COND`, `SWITCH`, `CORRESPONDING #( )` | 7.40 SP02 |
| Table expressions `itab[ key = ... ]`, `line_exists( )`, `line_index( )` | 7.40 SP02 |
| `FOR ... IN ...`, `REDUCE`       | 7.40 SP05      |
| Inline `@DATA(...)` in `SELECT`, host-var escape `@` | 7.40 SP05 |
| `FILTER`                         | 7.40 SP08      |
| ABAP CDS views                   | 7.40 SP05      |
| AMDP (`BY DATABASE PROCEDURE`)   | 7.40 SP05      |

**Out of scope (7.50+):** the `SELECT FROM ... FIELDS ..., ...` comma-separated form, RAP (BDEFs), ABAP Cloud's released-API model, `xco_cp_*` factories. Mention they exist if the user asks, but don't generate them.

If the SP level is unknown, default to assuming 7.40 SP08+. If the surrounding code clearly predates SP02 (no `DATA(...)`, lots of `MOVE`, header lines everywhere), match that style instead.
