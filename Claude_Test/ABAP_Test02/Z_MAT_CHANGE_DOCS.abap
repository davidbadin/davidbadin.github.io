*&---------------------------------------------------------------------*
*& Report       : Z_MAT_CHANGE_DOCS
*& Description  : Material Change Documents Viewer (CDHDR / CDPOS)
*& ABAP Version : 7.40+
*&---------------------------------------------------------------------*
*& Text Elements:
*&   TEXT-001 = 'Selection Criteria'
*&   TEXT-002 = 'Material Change Documents'
*&---------------------------------------------------------------------*
REPORT z_mat_change_docs.

TABLES: mara.

"--- Constants ---
CONSTANTS c_objclas TYPE cdhdr-objectclas VALUE 'MATERIAL'.

"--- Types ---
TYPES: BEGIN OF ty_chng_doc,
         objectid   TYPE cdhdr-objectid,    " Material number
         changenr   TYPE cdhdr-changenr,    " Change document number
         udate      TYPE cdhdr-udate,       " Change date
         utime      TYPE cdhdr-utime,       " Change time
         username   TYPE cdhdr-username,    " Changed by
         tcode      TYPE cdhdr-tcode,       " Transaction code
         change_ind TYPE cdhdr-change_ind,  " Header change indicator (I/U/D)
         tabname    TYPE cdpos-tabname,     " Changed table
         fname      TYPE cdpos-fname,       " Changed field
         chngind    TYPE cdpos-chngind,     " Field change indicator (I/U/D/E)
         value_old  TYPE cdpos-value_old,   " Old value
         value_new  TYPE cdpos-value_new,   " New value
         text_old   TYPE cdpos-text_old,    " Old text (for long values)
         text_new   TYPE cdpos-text_new,    " New text (for long values)
       END OF ty_chng_doc.

"--- Global Data ---
DATA gt_chng_docs TYPE STANDARD TABLE OF ty_chng_doc.

"--- Selection Screen ---
SELECTION-SCREEN BEGIN OF BLOCK b1 WITH FRAME TITLE TEXT-001.
  SELECT-OPTIONS: s_matnr FOR mara-matnr OBLIGATORY,
                  s_udate FOR sy-datum,
                  s_uname FOR sy-uname.
SELECTION-SCREEN END OF BLOCK b1.

"--- Pre-fill: last 90 days as default date range ---
INITIALIZATION.
  s_udate-sign   = 'I'.
  s_udate-option = 'BT'.
  s_udate-low    = sy-datum - 90.
  s_udate-high   = sy-datum.
  APPEND s_udate TO s_udate.

"--- Start of Selection ---
START-OF-SELECTION.
  PERFORM fetch_data.
  IF gt_chng_docs IS NOT INITIAL.
    PERFORM display_alv.
  ELSE.
    MESSAGE 'No change documents found for the selected criteria.' TYPE 'S'
            DISPLAY LIKE 'W'.
  ENDIF.

*&---------------------------------------------------------------------*
*& Form FETCH_DATA
*& Reads CDHDR + CDPOS via JOIN and fills gt_chng_docs
*&---------------------------------------------------------------------*
FORM fetch_data.

  DATA: lt_objid TYPE RANGE OF cdhdr-objectid,
        lv_low   TYPE cdhdr-objectid,
        lv_high  TYPE cdhdr-objectid.

  " Convert material numbers to internal OBJECTID format (leading zeros)
  LOOP AT s_matnr ASSIGNING FIELD-SYMBOL(<s_sel>).
    CLEAR: lv_low, lv_high.

    CALL FUNCTION 'CONVERSION_EXIT_ALPHA_INPUT'
      EXPORTING input  = <s_sel>-low
      IMPORTING output = lv_low.

    IF <s_sel>-high IS NOT INITIAL.
      CALL FUNCTION 'CONVERSION_EXIT_ALPHA_INPUT'
        EXPORTING input  = <s_sel>-high
        IMPORTING output = lv_high.
    ENDIF.

    APPEND VALUE #(
      sign   = <s_sel>-sign
      option = <s_sel>-option
      low    = lv_low
      high   = lv_high
    ) TO lt_objid.
  ENDLOOP.

  " Read header + position data in one JOIN
  SELECT h~objectid,  h~changenr,   h~udate,      h~utime,
         h~username,  h~tcode,      h~change_ind,
         p~tabname,   p~fname,      p~chngind,
         p~value_old, p~value_new,  p~text_old,   p~text_new
    FROM cdhdr AS h
    INNER JOIN cdpos AS p
      ON  p~objectclas = h~objectclas
      AND p~objectid   = h~objectid
      AND p~changenr   = h~changenr
    INTO TABLE @gt_chng_docs
    WHERE h~objectclas = @c_objclas
      AND h~objectid   IN @lt_objid
      AND h~udate      IN @s_udate
      AND h~username   IN @s_uname
    ORDER BY h~objectid, h~udate DESCENDING, h~utime DESCENDING.

ENDFORM.

*&---------------------------------------------------------------------*
*& Form DISPLAY_ALV
*& Builds and launches the CL_SALV_TABLE output
*&---------------------------------------------------------------------*
FORM display_alv.

  DATA lo_alv TYPE REF TO cl_salv_table.

  TRY.
      "--- Create ALV instance ---
      cl_salv_table=>factory(
        IMPORTING r_salv_table = lo_alv
        CHANGING  t_table      = gt_chng_docs
      ).

      "--- Display settings ---
      DATA(lo_disp) = lo_alv->get_display_settings( ).
      lo_disp->set_striped_pattern( abap_true ).
      lo_disp->set_list_header(
        |Material Change Documents  –  { lines( gt_chng_docs ) } record(s)| ).

      "--- Full toolbar (sort / filter / export / layout) ---
      lo_alv->get_functions( )->set_all( abap_true ).

      "--- Column settings ---
      DATA(lo_cols) = lo_alv->get_columns( ).
      lo_cols->set_optimize( abap_true ).

      PERFORM set_column_texts USING lo_cols.

      "--- Default sort: material → date ↓ → time ↓ ---
      PERFORM set_sort_criteria USING lo_alv.

      lo_alv->display( ).

    CATCH cx_salv_msg INTO DATA(lx_msg).
      MESSAGE lx_msg->get_text( ) TYPE 'E'.
  ENDTRY.

ENDFORM.

*&---------------------------------------------------------------------*
*& Form SET_COLUMN_TEXTS
*& Assigns readable labels to every output column
*&---------------------------------------------------------------------*
FORM set_column_texts USING io_cols TYPE REF TO cl_salv_columns_table.

  " Helper macro: silently ignore unknown column names
  DEFINE _set_col.
    TRY.
        CAST cl_salv_column_table(
          io_cols->get_column( &1 )
        )->set_long_text( &2 ).
        CAST cl_salv_column_table(
          io_cols->get_column( &1 )
        )->set_medium_text( &3 ).
        CAST cl_salv_column_table(
          io_cols->get_column( &1 )
        )->set_short_text( &4 ).
      CATCH cx_salv_not_found.
    ENDTRY.
  END-OF-DEFINITION.

  _set_col 'OBJECTID'   'Material Number'           'Material'     'Material'.
  _set_col 'CHANGENR'   'Change Document Number'    'Change No.'   'ChgNr'.
  _set_col 'UDATE'      'Change Date'               'Date'         'Date'.
  _set_col 'UTIME'      'Change Time'               'Time'         'Time'.
  _set_col 'USERNAME'   'Changed By'                'User'         'User'.
  _set_col 'TCODE'      'Transaction Code'          'Trans.'       'TCode'.
  _set_col 'CHANGE_IND' 'Header Change Type (I/U/D)' 'Hdr Chg'    'HChg'.
  _set_col 'TABNAME'    'Changed Table'             'Table'        'Table'.
  _set_col 'FNAME'      'Changed Field'             'Field'        'Field'.
  _set_col 'CHNGIND'    'Field Change Type (I/U/D/E)' 'Fld Chg'   'FChg'.
  _set_col 'VALUE_OLD'  'Old Value'                 'Old Value'    'OldVal'.
  _set_col 'VALUE_NEW'  'New Value'                 'New Value'    'NewVal'.
  _set_col 'TEXT_OLD'   'Old Text Value'            'Old Text'     'OldTxt'.
  _set_col 'TEXT_NEW'   'New Text Value'            'New Text'     'NewTxt'.

ENDFORM.

*&---------------------------------------------------------------------*
*& Form SET_SORT_CRITERIA
*& Adds default sort: material → date (desc) → time (desc)
*&---------------------------------------------------------------------*
FORM set_sort_criteria USING io_alv TYPE REF TO cl_salv_table.

  DATA(lo_sorts) = io_alv->get_sorts( ).

  TRY.
      lo_sorts->add_sort(
        columnname = 'OBJECTID'
        position   = 1
        order      = if_salv_c_sort_order=>ascending
        subtotal   = abap_true ).

      lo_sorts->add_sort(
        columnname = 'UDATE'
        position   = 2
        order      = if_salv_c_sort_order=>descending ).

      lo_sorts->add_sort(
        columnname = 'UTIME'
        position   = 3
        order      = if_salv_c_sort_order=>descending ).

    CATCH cx_salv_not_found
          cx_salv_existing
          cx_salv_data_error.
      " Sort criteria not critical – continue without them
  ENDTRY.

ENDFORM.
