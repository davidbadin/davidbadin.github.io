@AbapCatalog.sqlViewName: 'ZIMATCHGDOC'
@AbapCatalog.compiler.compareFilter: true
@AbapCatalog.preserveKey: true
@AccessControl.authorizationCheck: #NOT_REQUIRED
@EndUserText.label: 'Mat. change docs - basic interface'

/*
  Basic interface view that joins CDHDR and CDPOS for change document
  object MATERIAL. Restricts CDPOS to the three master tables we care
  about: MARA, MARC, MARD.

  This view is NOT exposed via OData - it is the technical foundation
  layer that both the header (aggregated) and the item (line-by-line)
  consumption views build on top of.
*/
define view ZI_MatChangeDoc
  as select from cdhdr
    inner join cdpos on  cdpos.objectclas = cdhdr.objectclas
                     and cdpos.objectid   = cdhdr.objectid
                     and cdpos.changenr   = cdhdr.changenr
{
  key cdhdr.objectclas                    as ObjectClass,
  key cdhdr.objectid                      as ObjectId,
  key cdhdr.changenr                      as ChangeNumber,
  key cdpos.tabname                       as TableName,
  key cdpos.tabkey                        as TableKey,
  key cdpos.fname                         as FieldName,

      // OBJECTID is CHAR50 - cast to MATNR (CHAR18) so the conversion
      // exit MATN1 is applied automatically in the Fiori UI.
      cast( cdhdr.objectid as matnr )     as Material,

      cdpos.chngind                       as ChangeIndicator,
      cdpos.value_old                     as OldValue,
      cdpos.value_new                     as NewValue,
      cdhdr.udate                         as ChangeDate,
      cdhdr.utime                         as ChangeTime,
      cdhdr.username                      as UserName,
      cdhdr.tcode                         as TransactionCode
}
where cdhdr.objectclas =  'MATERIAL'
  and cdpos.tabname    in ( 'MARA', 'MARC', 'MARD' )
