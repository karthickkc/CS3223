package simpledb.metadata;

import java.util.*;
import static simpledb.metadata.TableMgr.MAX_NAME;
import simpledb.tx.Transaction;
import simpledb.record.TableScan;
import simpledb.record.*;

/**
 * The index manager.
 * The index manager has similar functionality to the table manager.
 * @author Edward Sciore
 */
class IndexMgr {
   // The max characters an index type ("hash" or "btree") can have.
   public static final int MAX_TYPE_NAME = 8;

   private Layout layout;
   private TableMgr tblmgr;
   private StatMgr statmgr;

   /**
    * Create the index manager.
    * This constructor is called during system startup.
    * If the database is new, then the <i>idxcat</i> table is created.
    * @param isnew indicates whether this is a new database
    * @param tx the system startup transaction
    */
   public IndexMgr(boolean isnew, TableMgr tblmgr, StatMgr statmgr, Transaction tx) {
      if (isnew) {
         Schema sch = new Schema();
         sch.addStringField("indexname", MAX_NAME);
         sch.addStringField("tablename", MAX_NAME);
         sch.addStringField("fieldname", MAX_NAME);
         sch.addStringField("indextype", MAX_TYPE_NAME);
         tblmgr.createTable("idxcat", sch, tx);
      }
      this.tblmgr = tblmgr;
      this.statmgr = statmgr;
      layout = tblmgr.getLayout("idxcat", tx);
   }

   /**
    * Create a hash index for the specified field.
    * Equivalent to calling {@link #createIndex(String, String, String, String, Transaction)}
    * with an index type of "hash".
    * @param idxname the name of the index
    * @param tblname the name of the indexed table
    * @param fldname the name of the indexed field
    * @param tx the calling transaction
    */
   public void createIndex(String idxname, String tblname, String fldname, Transaction tx) {
      createIndex(idxname, tblname, fldname, "hash", tx);
   }

   /**
    * Create an index of the specified type for the specified field.
    * A unique ID is assigned to this index, and its information
    * is stored in the idxcat table.
    * @param idxname the name of the index
    * @param tblname the name of the indexed table
    * @param fldname the name of the indexed field
    * @param idxtype the type of index ("hash" or "btree")
    * @param tx the calling transaction
    */
   public void createIndex(String idxname, String tblname, String fldname, String idxtype, Transaction tx) {
      TableScan ts = new TableScan(tx, "idxcat", layout);
      ts.insert();
      ts.setString("indexname", idxname);
      ts.setString("tablename", tblname);
      ts.setString("fieldname", fldname);
      ts.setString("indextype", idxtype);
      ts.close();
   }

   /**
    * Return a map containing the index info for all indexes
    * on the specified table.
    * @param tblname the name of the table
    * @param tx the calling transaction
    * @return a map of IndexInfo objects, keyed by their field names
    */
   public Map<String,IndexInfo> getIndexInfo(String tblname, Transaction tx) {
      Map<String,IndexInfo> result = new HashMap<String,IndexInfo>();
      TableScan ts = new TableScan(tx, "idxcat", layout);
      while (ts.next())
         if (ts.getString("tablename").equals(tblname)) {
         String idxname = ts.getString("indexname");
         String fldname = ts.getString("fieldname");
         String idxtype = ts.getString("indextype");
         Layout tblLayout = tblmgr.getLayout(tblname, tx);
         StatInfo tblsi = statmgr.getStatInfo(tblname, tblLayout, tx);
         IndexInfo ii = new IndexInfo(idxname, fldname, tblLayout.schema(), tx, tblsi, idxtype);
         result.put(fldname, ii);
      }
      ts.close();
      return result;
   }
}
