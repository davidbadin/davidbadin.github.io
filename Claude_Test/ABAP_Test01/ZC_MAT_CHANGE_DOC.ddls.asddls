@AbapCatalog.sqlViewName: 'ZCMATCHGDOC'
@AbapCatalog.compiler.compareFilter: true
@AbapCatalog.preserveKey: true
@AccessControl.authorizationCheck: #NOT_REQUIRED
@EndUserText.label: 'Material change documents - List Report'

@OData.publish: true

@Search.searchable: true

/*
  Consumption view bound to the Fiori Elements List Report.

  - One row per FIELD change in MARA / MARC / MARD.
  - No aggregation, no parent/child structure - the user wants to see
    every change individually.
  - The MATERIAL column is annotated with @Consumption.semanticObject
    so the smart template renders it as a hyperlink that triggers
    Intent-Based Navigation through the Fiori Launchpad to whatever
    target mapping is configured for semantic object "Material"
    (e.g. the Fiori app "Display Material" / "Manage Product Master Data",
    which is the MM03 equivalent on Fiori).
  - Default sort: newest changes first.
*/

@UI.headerInfo: {
  typeName:        'Change document',
  typeNamePlural:  'Change documents',
  title:           { value: 'FieldName' },
  description:     { value: 'Material' }
}

@UI.presentationVariant: [{
  sortOrder:       [
    { by: 'ChangeDate', direction: #DESC },
    { by: 'ChangeTime', direction: #DESC }
  ],
  visualizations:  [{ type: #AS_LINEITEM_REFERENCE, qualifier: '' }]
}]

define view ZC_MatChangeDoc
  as select from ZI_MatChangeDoc
{
  /* ---- Material - hyperlink to external Fiori app ---------------- */
  @UI.lineItem:               [{ position: 10, label: 'Material',  importance: #HIGH }]
  @UI.selectionField:         [{ position: 10 }]
  @UI.identification:         [{ position: 10, label: 'Material' }]
  @Consumption.semanticObject: 'Material'
  @Search.defaultSearchElement: true
  @Search.fuzzinessThreshold:   0.7
  key Material,

  /* ---- Table (MARA / MARC / MARD) -------------------------------- */
  @UI.lineItem:       [{ position: 20, label: 'Table',     importance: #HIGH }]
  @UI.selectionField: [{ position: 20 }]
  @UI.identification: [{ position: 20, label: 'Table' }]
  key TableName,

  /* ---- Field that changed ---------------------------------------- */
  @UI.lineItem:       [{ position: 30, label: 'Field',     importance: #HIGH }]
  @UI.selectionField: [{ position: 30 }]
  @UI.identification: [{ position: 30, label: 'Field' }]
  key FieldName,

  /* ---- I = insert / U = update / D = delete ---------------------- */
  @UI.lineItem:       [{ position: 35, label: 'Indicator' }]
  @UI.identification: [{ position: 35, label: 'Indicator' }]
      ChangeIndicator,

  /* ---- Old value ------------------------------------------------- */
  @UI.lineItem:       [{ position: 40, label: 'Old value' }]
  @UI.identification: [{ position: 40, label: 'Old value' }]
      OldValue,

  /* ---- New value ------------------------------------------------- */
  @UI.lineItem:       [{ position: 50, label: 'New value', importance: #HIGH }]
  @UI.identification: [{ position: 50, label: 'New value' }]
      NewValue,

  /* ---- Date ------------------------------------------------------ */
  @UI.lineItem:       [{ position: 60, label: 'Date',      importance: #HIGH }]
  @UI.selectionField: [{ position: 40 }]
  @UI.identification: [{ position: 60, label: 'Date' }]
      ChangeDate,

  /* ---- Time ------------------------------------------------------ */
  @UI.lineItem:       [{ position: 70, label: 'Time' }]
  @UI.identification: [{ position: 70, label: 'Time' }]
      ChangeTime,

  /* ---- User ------------------------------------------------------ */
  @UI.lineItem:       [{ position: 80, label: 'User',      importance: #HIGH }]
  @UI.selectionField: [{ position: 50 }]
  @UI.identification: [{ position: 80, label: 'User' }]
      UserName,

  /* ---- Transaction code ------------------------------------------ */
  @UI.lineItem:       [{ position: 90, label: 'TCode' }]
  @UI.selectionField: [{ position: 60 }]
  @UI.identification: [{ position: 90, label: 'TCode' }]
      TransactionCode,

  /* ---- technical key fields - hidden from default UI ------------- */
  @UI.hidden: true
  key ObjectClass,
  @UI.hidden: true
  key ObjectId,
  @UI.hidden: true
  key ChangeNumber,
  @UI.hidden: true
  key TableKey
}
