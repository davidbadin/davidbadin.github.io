# ABAP templates — ready to adapt

Skeletons for the artifacts you'll start from most often. Adjust namespaces, types, and exception classes to fit the project.

## Modern report with delegated logic

```abap
REPORT z_order_summary.

CLASS lcl_app DEFINITION FINAL.
  PUBLIC SECTION.
    METHODS run.
  PRIVATE SECTION.
    METHODS read_orders RETURNING VALUE(rt_orders) TYPE ztt_order
                       RAISING   zcx_app_error.
    METHODS show_alv    IMPORTING it_orders TYPE ztt_order.
ENDCLASS.

CLASS lcl_app IMPLEMENTATION.

  METHOD run.
    TRY.
        show_alv( read_orders( ) ).
      CATCH zcx_app_error INTO DATA(lx).
        MESSAGE lx TYPE 'E'.
    ENDTRY.
  ENDMETHOD.

  METHOD read_orders.
    SELECT id customer total created_on
      FROM zorder
      INTO TABLE @rt_orders
      WHERE created_on IN @s_date
        AND customer   IN @s_custmr.
  ENDMETHOD.

  METHOD show_alv.
    DATA lt_local LIKE it_orders.
    lt_local = it_orders.
    cl_salv_table=>factory(
      IMPORTING r_salv_table = DATA(lo_alv)
      CHANGING  t_table      = lt_local ).
    lo_alv->get_columns( )->set_optimize( abap_true ).
    lo_alv->get_functions( )->set_all( ).
    lo_alv->display( ).
  ENDMETHOD.

ENDCLASS.

TABLES: zorder.

SELECTION-SCREEN BEGIN OF BLOCK b1 WITH FRAME TITLE TEXT-001.
  SELECT-OPTIONS s_custmr FOR zorder-customer.
  SELECT-OPTIONS s_date   FOR zorder-created_on.
SELECTION-SCREEN END OF BLOCK b1.

INITIALIZATION.
  s_date-low    = sy-datum - 30.
  s_date-high   = sy-datum.
  s_date-sign   = 'I'.
  s_date-option = 'BT'.
  APPEND s_date.

START-OF-SELECTION.
  NEW lcl_app( )->run( ).
```

The pattern: the report is a thin shell around a class. The class is testable; the report is just plumbing.

## Global class with constructor and exceptions

```abap
CLASS zcl_order_calculator DEFINITION
  PUBLIC FINAL CREATE PUBLIC.

  PUBLIC SECTION.
    METHODS:
      constructor
        IMPORTING io_repo TYPE REF TO zif_order_repo,

      total_for
        IMPORTING iv_customer    TYPE kunnr
        RETURNING VALUE(rv_total) TYPE wrtbtr
        RAISING   zcx_calc_error.

  PRIVATE SECTION.
    DATA mo_repo TYPE REF TO zif_order_repo.

ENDCLASS.

CLASS zcl_order_calculator IMPLEMENTATION.

  METHOD constructor.
    mo_repo = io_repo.
  ENDMETHOD.

  METHOD total_for.
    TRY.
        DATA(lt_orders) = mo_repo->list_for( iv_customer ).
      CATCH zcx_repo_failed INTO DATA(lx).
        RAISE EXCEPTION TYPE zcx_calc_error EXPORTING previous = lx.
    ENDTRY.

    rv_total = REDUCE wrtbtr( INIT s = 0
                              FOR  <o> IN lt_orders
                              NEXT s = s + <o>-total ).
  ENDMETHOD.

ENDCLASS.
```

Constructor injection of `zif_order_repo` (an interface) means the class is unit-testable: tests pass a fake repo. Don't `cl_xyz=>singleton( )` from inside the class.

## Custom exception class

```abap
CLASS zcx_calc_error DEFINITION
  PUBLIC INHERITING FROM cx_static_check FINAL CREATE PUBLIC.

  PUBLIC SECTION.
    INTERFACES if_t100_message.

    CONSTANTS:
      BEGIN OF zcx_calc_error,
        msgid TYPE symsgid VALUE 'Z_CALC',
        msgno TYPE symsgno VALUE '001',
        attr1 TYPE scx_attrname VALUE 'CUSTOMER',
        attr2 TYPE scx_attrname VALUE '',
        attr3 TYPE scx_attrname VALUE '',
        attr4 TYPE scx_attrname VALUE '',
      END OF zcx_calc_error.

    DATA customer TYPE kunnr.

    METHODS constructor
      IMPORTING textid   LIKE if_t100_message=>t100key OPTIONAL
                previous LIKE previous OPTIONAL
                customer TYPE kunnr OPTIONAL.
ENDCLASS.

CLASS zcx_calc_error IMPLEMENTATION.
  METHOD constructor.
    super->constructor( previous = previous ).
    me->customer = customer.
    CLEAR me->textid.
    IF textid IS INITIAL.
      if_t100_message~t100key = zcx_calc_error.
    ELSE.
      if_t100_message~t100key = textid.
    ENDIF.
  ENDMETHOD.
ENDCLASS.
```

Use `cx_static_check` when callers should handle the exception, `cx_dynamic_check` for unrecoverable programmer errors, `cx_no_check` only as an escape hatch.

## ABAP Unit test class (local)

```abap
CLASS ltcl_order_calculator DEFINITION
  FOR TESTING DURATION SHORT RISK LEVEL HARMLESS FINAL.

  PRIVATE SECTION.
    DATA mo_repo_mock TYPE REF TO zif_order_repo.
    DATA mo_cut       TYPE REF TO zcl_order_calculator.

    METHODS setup.
    METHODS sums_orders             FOR TESTING RAISING cx_static_check.
    METHODS empty_returns_zero      FOR TESTING RAISING cx_static_check.
    METHODS repo_failure_propagates FOR TESTING.
ENDCLASS.

CLASS ltcl_order_calculator IMPLEMENTATION.

  METHOD setup.
    mo_repo_mock ?= cl_abap_testdouble=>create( 'ZIF_ORDER_REPO' ).
    mo_cut       = NEW #( io_repo = mo_repo_mock ).
  ENDMETHOD.

  METHOD sums_orders.
    DATA(lt_fake) = VALUE ztt_order(
      ( id = '1' total = '10.00' )
      ( id = '2' total = '32.50' ) ).
    cl_abap_testdouble=>configure_call( mo_repo_mock )->returning( lt_fake ).
    mo_repo_mock->list_for( '1000' ).

    DATA(lv_total) = mo_cut->total_for( '1000' ).

    cl_abap_unit_assert=>assert_equals( exp = '42.50' act = lv_total ).
  ENDMETHOD.

  METHOD empty_returns_zero.
    cl_abap_testdouble=>configure_call( mo_repo_mock )->returning( VALUE ztt_order( ) ).
    mo_repo_mock->list_for( '1000' ).

    cl_abap_unit_assert=>assert_equals( exp = 0 act = mo_cut->total_for( '1000' ) ).
  ENDMETHOD.

  METHOD repo_failure_propagates.
    cl_abap_testdouble=>configure_call( mo_repo_mock
      )->raise_exception( NEW zcx_repo_failed( ) ).
    mo_repo_mock->list_for( '1000' ).

    TRY.
        mo_cut->total_for( '1000' ).
        cl_abap_unit_assert=>fail( 'expected zcx_calc_error' ).
      CATCH zcx_calc_error.
        " expected
    ENDTRY.
  ENDMETHOD.

ENDCLASS.
```

## Function module (RFC-enabled wrapper)

```abap
FUNCTION z_order_total_rfc.
*"----------------------------------------------------------------------
*"*"Local Interface:
*"  IMPORTING
*"     VALUE(IV_CUSTOMER) TYPE  KUNNR
*"  EXPORTING
*"     VALUE(EV_TOTAL)    TYPE  WRTBTR
*"     VALUE(ET_RETURN)   TYPE  BAPIRETTAB
*"----------------------------------------------------------------------

  TRY.
      DATA(lo_calc) = NEW zcl_order_calculator(
        io_repo = NEW zcl_order_repo( ) ).
      ev_total = lo_calc->total_for( iv_customer ).

    CATCH zcx_calc_error INTO DATA(lx).
      APPEND VALUE bapiret2(
        type = 'E'
        id   = 'Z_CALC'
        number = '001'
        message_v1 = iv_customer
        message    = lx->get_text( ) ) TO et_return.
  ENDTRY.
ENDFUNCTION.
```

Returning a `BAPIRETTAB` instead of using `EXCEPTIONS` is the convention for RFC-callable APIs — exceptions don't cross RFC boundaries cleanly.

## Set-based UPDATE pattern

```abap
" applying a derived total back to the database
DATA(lt_updates) = VALUE ztt_order(
  FOR <o> IN lt_orders ( id = <o>-id total = <o>-total ) ).

UPDATE zorder FROM TABLE @lt_updates.
COMMIT WORK.
```

Avoid update-in-loop. The set-based form is one round trip and lets the database optimize.

## Repository interface (for testability)

```abap
INTERFACE zif_order_repo PUBLIC.
  METHODS:
    list_for
      IMPORTING iv_customer    TYPE kunnr
      RETURNING VALUE(rt_orders) TYPE ztt_order
      RAISING   zcx_repo_failed,

    save
      IMPORTING is_order TYPE zorder
      RAISING   zcx_repo_failed.
ENDINTERFACE.
```

The implementing class talks to the database; the interface lets tests substitute a fake. This single pattern is the difference between testable ABAP and untestable ABAP.
