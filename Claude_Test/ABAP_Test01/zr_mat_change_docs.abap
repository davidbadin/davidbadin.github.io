*&---------------------------------------------------------------------*
*& Report  ZR_MAT_CHANGE_DOCS
*&---------------------------------------------------------------------*
*& Display change documents (CDHDR / CDPOS) for a given material,
*& restricted to tables MARA, MARC and MARD. Result is shown in ALV.
*&---------------------------------------------------------------------*
REPORT zr_mat_change_docs.

*----------------------------------------------------------------------*
*  Types
*----------------------------------------------------------------------*
TYPES: BEGIN OF ty_change,
         matnr     TYPE matnr,
         tabname   TYPE cdtabname,
         tabkey    TYPE cdtabkey,
         fname     TYPE fieldname,
         chngind   TYPE cdchngind,
         value_old TYPE cdoldval,
         value_new TYPE cdnewval,
         udate     TYPE cdudate,
         utime     TYPE cdutime,
         username  TYPE cduser,
       END OF ty_change,
       tt_change TYPE STANDARD TABLE OF ty_change WITH EMPTY KEY.

*----------------------------------------------------------------------*
*  Local class - main worker
*----------------------------------------------------------------------*
CLASS lcl_mat_changes DEFINITION FINAL CREATE PUBLIC.

  PUBLIC SECTION.
    METHODS:
      run IMPORTING iv_matnr TYPE matnr.

  PRIVATE SECTION.
    CONSTANTS:
      gc_objectclas TYPE cdobjectcl VALUE 'MATERIAL'.

    DATA:
      gt_changes TYPE tt_change.

    METHODS:
      get_data    IMPORTING iv_matnr TYPE matnr,
      display_alv.

ENDCLASS.

CLASS lcl_mat_changes IMPLEMENTATION.

*----------------------------------------------------------------------*
  METHOD run.

    " guard - empty input
    IF iv_matnr IS INITIAL.
      MESSAGE 'Please enter a material number' TYPE 'S' DISPLAY LIKE 'E'.
      RETURN.
    ENDIF.

    get_data( iv_matnr ).

    IF gt_changes IS INITIAL.
      MESSAGE 'No change documents found for this material' TYPE 'S'.
      RETURN.
    ENDIF.

    display_alv( ).

  ENDMETHOD.

*----------------------------------------------------------------------*
  METHOD get_data.

    DATA: lv_matnr_int TYPE matnr,
          lv_matnr_ext TYPE matnr,
          lv_objectid  TYPE cdobjectv.

    " convert input to internal MATNR format (with leading zeros) -
    " CDHDR-OBJECTID stores it that way for OBJECTCLAS = 'MATERIAL'
    CALL FUNCTION 'CONVERSION_EXIT_MATN1_INPUT'
      EXPORTING
        input        = iv_matnr
      IMPORTING
        output       = lv_matnr_int
      EXCEPTIONS
        length_error = 1
        OTHERS       = 2.
    IF sy-subrc <> 0.
      MESSAGE 'Invalid material number' TYPE 'S' DISPLAY LIKE 'E'.
      RETURN.
    ENDIF.

    lv_objectid = lv_matnr_int.

    " external (display) format
    CALL FUNCTION 'CONVERSION_EXIT_MATN1_OUTPUT'
      EXPORTING
        input  = lv_matnr_int
      IMPORTING
        output = lv_matnr_ext.

    " ----- read change document headers ---------------------------------
    SELECT objectclas,
           objectid,
           changenr,
           username,
           udate,
           utime
      FROM cdhdr
      INTO TABLE @DATA(lt_cdhdr)
      WHERE objectclas = @gc_objectclas
        AND objectid   = @lv_objectid.

    IF lt_cdhdr IS INITIAL.
      RETURN.
    ENDIF.

    " sort for BINARY SEARCH and to give FOR ALL ENTRIES a clean driver
    SORT lt_cdhdr BY objectclas objectid changenr.

    " ----- read change document positions for MARA / MARC / MARD --------
    SELECT objectclas,
           objectid,
           changenr,
           tabname,
           tabkey,
           fname,
           chngind,
           value_new,
           value_old
      FROM cdpos
      FOR ALL ENTRIES IN @lt_cdhdr
      INTO TABLE @DATA(lt_cdpos)
      WHERE objectclas = @lt_cdhdr-objectclas
        AND objectid   = @lt_cdhdr-objectid
        AND changenr   = @lt_cdhdr-changenr
        AND tabname    IN ( 'MARA', 'MARC', 'MARD' ).

    IF lt_cdpos IS INITIAL.
      RETURN.
    ENDIF.

    " ----- merge header + position into output table --------------------
    LOOP AT lt_cdpos ASSIGNING FIELD-SYMBOL(<ls_pos>).

      READ TABLE lt_cdhdr ASSIGNING FIELD-SYMBOL(<ls_hdr>)
        WITH KEY objectclas = <ls_pos>-objectclas
                 objectid   = <ls_pos>-objectid
                 changenr   = <ls_pos>-changenr
        BINARY SEARCH.
      IF sy-subrc <> 0.
        CONTINUE.
      ENDIF.

      APPEND INITIAL LINE TO gt_changes ASSIGNING FIELD-SYMBOL(<ls_out>).
      <ls_out>-matnr     = lv_matnr_ext.
      <ls_out>-tabname   = <ls_pos>-tabname.
      <ls_out>-tabkey    = <ls_pos>-tabkey.
      <ls_out>-fname     = <ls_pos>-fname.
      <ls_out>-chngind   = <ls_pos>-chngind.
      <ls_out>-value_old = <ls_pos>-value_old.
      <ls_out>-value_new = <ls_pos>-value_new.
      <ls_out>-udate     = <ls_hdr>-udate.
      <ls_out>-utime     = <ls_hdr>-utime.
      <ls_out>-username  = <ls_hdr>-username.

    ENDLOOP.

    " newest changes first
    SORT gt_changes BY udate DESCENDING
                       utime DESCENDING
                       tabname
                       fname.

  ENDMETHOD.

*----------------------------------------------------------------------*
  METHOD display_alv.

    DATA: lo_alv TYPE REF TO cl_salv_table.

    TRY.
        cl_salv_table=>factory(
          IMPORTING
            r_salv_table = lo_alv
          CHANGING
            t_table      = gt_changes ).

        " enable standard ALV functions
        lo_alv->get_functions( )->set_all( abap_true ).

        " --- column tuning ------------------------------------------
        DATA(lo_cols) = lo_alv->get_columns( ).
        lo_cols->set_optimize( abap_true ).

        TRY.
            lo_cols->get_column( 'MATNR'     )->set_short_text( 'Material' ).
            lo_cols->get_column( 'TABNAME'   )->set_short_text( 'Table'    ).
            lo_cols->get_column( 'TABKEY'    )->set_short_text( 'Key'      ).
            lo_cols->get_column( 'FNAME'     )->set_short_text( 'Field'    ).
            lo_cols->get_column( 'CHNGIND'   )->set_short_text( 'Ind.'     ).
            lo_cols->get_column( 'VALUE_OLD' )->set_short_text( 'Old val.' ).
            lo_cols->get_column( 'VALUE_NEW' )->set_short_text( 'New val.' ).
            lo_cols->get_column( 'UDATE'     )->set_short_text( 'Date'     ).
            lo_cols->get_column( 'UTIME'     )->set_short_text( 'Time'     ).
            lo_cols->get_column( 'USERNAME'  )->set_short_text( 'User'     ).
          CATCH cx_salv_not_found.
        ENDTRY.

        " --- default sort -------------------------------------------
        DATA(lo_sorts) = lo_alv->get_sorts( ).
        lo_sorts->add_sort(
          columnname = 'UDATE'
          position   = 1
          sequence   = if_salv_c_sort=>sort_down ).
        lo_sorts->add_sort(
          columnname = 'UTIME'
          position   = 2
          sequence   = if_salv_c_sort=>sort_down ).

        " --- layout (save / restore) --------------------------------
        DATA(lo_layout) = lo_alv->get_layout( ).
        lo_layout->set_key( VALUE salv_s_layout_key( report = sy-repid ) ).
        lo_layout->set_save_restriction( cl_salv_layout=>restrict_none ).

        " --- header / display options -------------------------------
        DATA(lo_disp) = lo_alv->get_display_settings( ).
        lo_disp->set_list_header( 'Material change documents (MARA/MARC/MARD)' ).
        lo_disp->set_striped_pattern( abap_true ).

        lo_alv->display( ).

      CATCH cx_salv_msg INTO DATA(lx_msg).
        MESSAGE lx_msg TYPE 'E'.
    ENDTRY.

  ENDMETHOD.

ENDCLASS.

*----------------------------------------------------------------------*
*  Selection screen
*----------------------------------------------------------------------*
PARAMETERS: p_matnr TYPE mara-matnr OBLIGATORY.

*----------------------------------------------------------------------*
*  Main
*----------------------------------------------------------------------*
START-OF-SELECTION.

  DATA(go_app) = NEW lcl_mat_changes( ).
  go_app->run( iv_matnr = p_matnr ).
