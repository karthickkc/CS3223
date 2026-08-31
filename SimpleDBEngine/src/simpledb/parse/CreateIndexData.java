package simpledb.parse;

/**
 * The parser for the <i>create index</i> statement.
 * @author Edward Sciore
 */
public class CreateIndexData {
   private String idxname, tblname, fldname, idxtype;

   /**
    * Saves the table, field, and type of the specified index.
    */
   public CreateIndexData(String idxname, String tblname, String fldname, String idxtype) {
      this.idxname = idxname;
      this.tblname = tblname;
      this.fldname = fldname;
      this.idxtype = idxtype;
   }

   /**
    * Returns the name of the index.
    * @return the name of the index
    */
   public String indexName() {
      return idxname;
   }

   /**
    * Returns the name of the indexed table.
    * @return the name of the indexed table
    */
   public String tableName() {
      return tblname;
   }

   /**
    * Returns the name of the indexed field.
    * @return the name of the indexed field
    */
   public String fieldName() {
      return fldname;
   }

   /**
    * Returns the requested index type ("hash" or "btree").
    * @return the requested index type
    */
   public String indexType() {
      return idxtype;
   }
}
