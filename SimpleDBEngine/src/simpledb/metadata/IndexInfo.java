package simpledb.metadata;

import static java.sql.Types.INTEGER;

import simpledb.index.Index;
import simpledb.index.btree.BTreeIndex;
import simpledb.index.hash.HashIndex;
import simpledb.record.Layout;
import simpledb.record.Schema;
import simpledb.tx.Transaction;

/**
 * The information about an index. This information is used by the query planner
 * in order to estimate the costs of using the index, and to obtain the layout
 * of the index records. Its methods are essentially the same as those of Plan.
 *
 * @author Edward Sciore
 */
public class IndexInfo {
	private String idxname, fldname, idxtype;
	private Transaction tx;
	private Schema tblSchema;
	private Layout idxLayout;
	private StatInfo si;

	/**
	 * Create an IndexInfo object for the specified index.
	 *
	 * @param idxname   the name of the index
	 * @param fldname   the name of the indexed field
	 * @param tx        the calling transaction
	 * @param tblSchema the schema of the table
	 * @param si        the statistics for the table
	 * @param idxtype   the type of index ("hash" or "btree")
	 */
	public IndexInfo(String idxname, String fldname, Schema tblSchema, Transaction tx, StatInfo si, String idxtype) {
		this.idxname = idxname;
		this.fldname = fldname;
		this.tx = tx;
		this.tblSchema = tblSchema;
		this.idxLayout = createIdxLayout();
		this.si = si;
		this.idxtype = idxtype;
	}

	/**
	 * Open the index described by this object. The concrete index implementation
	 * used (hash or B-tree) is determined by the index's stored type.
	 *
	 * @return the Index object associated with this information
	 */
	public Index open() {
		if ("btree".equals(idxtype))
			return new BTreeIndex(tx, idxname, idxLayout);
		else
			return new HashIndex(tx, idxname, idxLayout);
	}

	/**
	 * Estimate the number of block accesses required to find all index records
	 * having a particular search key. The method uses the table's metadata to
	 * estimate the size of the index file and the number of index records per
	 * block. It then passes this information to the traversalCost method of the
	 * appropriate index type, which provides the estimate.
	 *
	 * @return the number of block accesses required to traverse the index
	 */
	public int blocksAccessed() {
		int rpb = tx.blockSize() / idxLayout.slotSize();
		int numblocks = si.recordsOutput() / rpb;
		if ("btree".equals(idxtype))
			return BTreeIndex.searchCost(numblocks, rpb);
		else
			return HashIndex.searchCost(numblocks, rpb);
	}

	/**
	 * Return the estimated number of records having a search key. This value is the
	 * same as doing a select query; that is, it is the number of records in the
	 * table divided by the number of distinct values of the indexed field.
	 * 
	 * @return the estimated number of records having a search key
	 */
	public int recordsOutput() {
		return si.recordsOutput() / si.distinctValues(fldname);
	}

	/**
	 * Return the distinct values for a specified field in the underlying table, or
	 * 1 for the indexed field.
	 * 
	 * @param fname the specified field
	 */
	public int distinctValues(String fname) {
		return fldname.equals(fname) ? 1 : si.distinctValues(fldname);
	}

	/**
	 * Return the layout of the index records. The schema consists of the dataRID
	 * (which is represented as two integers, the block number and the record ID)
	 * and the dataval (which is the indexed field). Schema information about the
	 * indexed field is obtained via the table's schema.
	 * 
	 * @return the layout of the index records
	 */
	private Layout createIdxLayout() {
		Schema sch = new Schema();
		sch.addIntField("block");
		sch.addIntField("id");
		if (tblSchema.type(fldname) == INTEGER)
			sch.addIntField("dataval");
		else {
			int fldlen = tblSchema.length(fldname);
			sch.addStringField("dataval", fldlen);
		}
		return new Layout(sch);
	}
}
