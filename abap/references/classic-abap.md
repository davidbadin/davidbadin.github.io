# Classic ABAP — patterns that still matter

Even in a modern shop you'll encounter classic procedural ABAP: legacy reports, function modules, dialog programs, BAPI calls, ALV grids. This file is the practical reference for working with them — without pretending modern syntax has replaced them everywhere.

## Classical report skeleton

```abap
REPORT z_order_summary.

TABLES: zorder.   " for SELECT-OPTIONS

SELECTION-SCREEN BEGIN OF BLOCK b1 WITH FRAME TITLE TEXT-001.
  SELECT-OPTIONS: s_custmr FOR zorder-customer,
                  s_date   FOR zorder-created_on.
  PARAMETERS:     p_active AS CHECKBOX DEFAULT 'X'.
SELECTION-SCREEN END OF BLOCK b1.

INITIALIZATION.
  s_date-low  = sy-datum - 30.
  s_date-high = sy-datum.
  s_date-sign = 'I'. s_date-option = 'BT'.
  APPEND s_date.

AT SELECTION-SCREEN ON s_date.
  IF s_date-low > s_date-high.
    MESSAGE 'Invalid date range' TYPE 'E'.
  ENDIF.

START-OF-SELECTION.
  DATA(lo_app) = NEW lcl_app( ).
  lo_app->run( ).
```

The pattern: keep `START-OF-SELECTION` as a thin shell that delegates to a class. Selection screen stays in the report; logic moves to a local or global class. This makes the logic testable and the report dumb.

### SELECT-OPTIONS — what to remember

- A `SELECT-OPTIONS` is a multi-row internal table behind the scenes. The fields are `SIGN` (I/E), `OPTION` (EQ/BT/CP/...), `LOW`, `HIGH`.
- Use directly in `WHERE` clauses: `... WHERE customer IN s_custmr.`
- For checks in code, use `IF lv_value IN s_custmr.`
- Programmatic population requires `APPEND` after setting all four fields.

### Selection-screen events

- `INITIALIZATION` — once at startup, set defaults.
- `AT SELECTION-SCREEN OUTPUT` — before each render, e.g. to grey out fields.
- `AT SELECTION-SCREEN ON <field>` — validate one field; `MESSAGE TYPE 'E'` re-prompts.
- `AT SELECTION-SCREEN` — global validation after all fields entered.
- `AT SELECTION-SCREEN ON VALUE-REQUEST FOR <field>` — F4 help.

## Function modules

Function modules predate classes and still anchor a lot of SAP. New code rarely needs them — *unless* you need RFC (callable from outside the system) or you're integrating with frameworks that demand them.

```abap
FUNCTION z_order_total.
*"----------------------------------------------------------------------
*"*"Local Interface:
*"  IMPORTING
*"     VALUE(IV_ORDER_ID) TYPE  ZORDER-ID
*"  EXPORTING
*"     VALUE(EV_TOTAL)    TYPE  ZORDER-TOTAL
*"  EXCEPTIONS
*"     NOT_FOUND
*"----------------------------------------------------------------------

  SELECT SINGLE total FROM zorder
    INTO ev_total
    WHERE id = iv_order_id.
  IF sy-subrc <> 0.
    RAISE not_found.
  ENDIF.
ENDFUNCTION.
```

Caller pattern:

```abap
CALL FUNCTION 'Z_ORDER_TOTAL'
  EXPORTING  iv_order_id = lv_id
  IMPORTING  ev_total    = lv_total
  EXCEPTIONS not_found   = 1
             OTHERS      = 2.
IF sy-subrc <> 0.
  " handle
ENDIF.
```

For new code, prefer a class method with `RAISE EXCEPTION TYPE` over `EXCEPTIONS` lists.

### RFC-enabled function modules

- Mark "Remote-Enabled Module" in the FM attributes.
- All parameters must be pass-by-value (no `REFERENCE`).
- Tables/structures must be DDIC-defined (no local types).
- Treat them like a public API: backwards compatibility matters, version carefully.

## BAPIs

BAPIs are RFC-enabled function modules with a standard interface contract. Calling one:

```abap
DATA: ls_header TYPE bapisdhd1,
      lt_items  TYPE STANDARD TABLE OF bapisditm,
      lt_return TYPE STANDARD TABLE OF bapiret2.

ls_header-doc_type = 'OR'.
ls_header-sales_org = '1000'.
"... fill required fields ...

CALL FUNCTION 'BAPI_SALESORDER_CREATEFROMDAT2'
  EXPORTING  order_header_in = ls_header
  IMPORTING  salesdocument   = DATA(lv_doc_id)
  TABLES     order_items_in  = lt_items
             return          = lt_return.

" Inspect return — BAPIs do NOT raise; they put errors in the return table
LOOP AT lt_return ASSIGNING FIELD-SYMBOL(<msg>) WHERE type CA 'EAX'.
  " handle error
ENDLOOP.

IF NOT line_exists( lt_return[ type = 'E' ] )
   AND NOT line_exists( lt_return[ type = 'A' ] )
   AND NOT line_exists( lt_return[ type = 'X' ] ).
  CALL FUNCTION 'BAPI_TRANSACTION_COMMIT' EXPORTING wait = 'X'.
ELSE.
  CALL FUNCTION 'BAPI_TRANSACTION_ROLLBACK'.
ENDIF.
```

Three rules to internalize:
1. **Always inspect the `RETURN` table.** Success = no E/A/X messages.
2. **You own the commit.** BAPIs do not commit. Call `BAPI_TRANSACTION_COMMIT` after success, `BAPI_TRANSACTION_ROLLBACK` after failure.
3. **`WAIT = 'X'`** on commit when subsequent reads need to see the write.

## Dialog programs (module pool)

Module pools are SAP's traditional UI runtime — dynpros (screens) plus a flow logic that runs `MODULE` calls. New work generally goes to Web Dynpro / Fiori, but maintenance still happens.

Anatomy:

- `Z_PROG` — top include with `PROGRAM` statement and globals.
- Screen flow logic — `PROCESS BEFORE OUTPUT` (PBO), `PROCESS AFTER INPUT` (PAI), `PROCESS ON HELP-REQUEST`, `PROCESS ON VALUE-REQUEST`.
- `MODULE` includes — implement `status_0100 OUTPUT`, `user_command_0100 INPUT`, etc.

Minimum sane pattern:

```abap
PROGRAM z_dialog_demo.

DATA: gv_ok_code TYPE sy-ucomm.

* in the screen's flow logic:
* PROCESS BEFORE OUTPUT.
*   MODULE status_0100.
* PROCESS AFTER INPUT.
*   MODULE user_command_0100.

MODULE status_0100 OUTPUT.
  SET PF-STATUS 'STATUS_0100'.
  SET TITLEBAR  'TITLE_0100'.
ENDMODULE.

MODULE user_command_0100 INPUT.
  CASE gv_ok_code.
    WHEN 'BACK' OR 'EXIT' OR 'CANC'.
      LEAVE TO SCREEN 0.
    WHEN 'SAVE'.
      PERFORM save_data.
  ENDCASE.
  CLEAR gv_ok_code.
ENDMODULE.
```

Modernizing tip: even in a module pool, the *logic* can live in a class. Modules are thin: they read the OK-code and call a method.

## ALV grid — the standard list output

For new code, use `cl_salv_table` (simpler) over `cl_gui_alv_grid` (more flexible but heavier).

```abap
DATA(lt_orders) = order_repo->list_recent( ).

cl_salv_table=>factory(
  IMPORTING r_salv_table = DATA(lo_alv)
  CHANGING  t_table      = lt_orders ).

lo_alv->get_columns( )->set_optimize( abap_true ).
lo_alv->get_functions( )->set_all( ).
lo_alv->display( ).
```

For column-level config — sums, hyperlinks, hotspots, color — use `lo_alv->get_columns( )->get_column( 'FIELD' )->...`.

## Authority checks

Every report or function that accesses sensitive data needs an authority check. The standard pattern:

```abap
AUTHORITY-CHECK OBJECT 'Z_ORDER'
  ID 'ACTVT' FIELD '03'
  ID 'BUKRS' FIELD ls_order-company_code.
IF sy-subrc <> 0.
  MESSAGE e045(00) WITH ls_order-company_code.   " no authorization
  " or for class-based: RAISE EXCEPTION TYPE zcx_auth_failed.
ENDIF.
```

Place checks at the boundary (start of the operation), not deep in helpers. Don't repeat the same check inside a loop unless the check depends on row content.

## Internal tables — classic operations to know

```abap
" loop with index
LOOP AT lt_orders ASSIGNING <fs>.
  IF sy-tabix MOD 100 = 0.
    " every 100th
  ENDIF.
ENDLOOP.

" delete by condition
DELETE lt_orders WHERE status = 'C'.

" sort
SORT lt_orders BY customer ASCENDING created_on DESCENDING.

" remove duplicates after sort
SORT lt_orders BY id.
DELETE ADJACENT DUPLICATES FROM lt_orders COMPARING id.

" parallel cursor pattern (when joining two sorted tables)
SORT lt_orders BY customer.
SORT lt_customers BY id.
DATA(lv_idx) = 1.
LOOP AT lt_orders ASSIGNING FIELD-SYMBOL(<o>).
  LOOP AT lt_customers ASSIGNING FIELD-SYMBOL(<c>) FROM lv_idx.
    IF <c>-id <> <o>-customer.
      lv_idx = sy-tabix.
      EXIT.
    ENDIF.
    " process
  ENDLOOP.
ENDLOOP.
```

## Header lines — the thing to delete

Header lines (`DATA: lt_orders TYPE ... WITH HEADER LINE.`) are deprecated and a frequent source of bugs (the structure shadows the table). When you see one in code you control, refactor:

```abap
" old
DATA: lt_orders LIKE zorder OCCURS 0 WITH HEADER LINE.
LOOP AT lt_orders.
  IF lt_orders-status = 'A'. ... ENDIF.
ENDLOOP.

" new
DATA lt_orders TYPE STANDARD TABLE OF zorder.
DATA ls_order  TYPE zorder.
LOOP AT lt_orders INTO ls_order.
  IF ls_order-status = 'A'. ... ENDIF.
ENDLOOP.

" or modern
LOOP AT lt_orders INTO DATA(ls_order).
  IF ls_order-status = 'A'. ... ENDIF.
ENDLOOP.
```

## Macros and FORM/PERFORM

- **Macros (`DEFINE ... END-OF-DEFINITION`)**: avoid. They're hard to debug, opaque to refactoring tools, and have no proper scope. Replace with methods.
- **FORM/PERFORM**: legitimate in old reports, but new code should use class methods. When refactoring, extracting a `FORM` to a method is usually safe and pays off the first time you need to test it.

## SmartForms / SAPscript

Print output is a world unto itself; this skill won't generate forms. When asked, point the user at:
- **SmartForms** (`SMARTFORMS` transaction) for most modern print output.
- **Adobe Forms** (`SFP`) for PDF-quality output, especially in S/4HANA.
- **SAPscript** (`SE71`) — legacy, still in use; avoid for new development.
