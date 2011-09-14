package script;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import dao.DAO;

/**
 * Script used to remove the unique indices on user accounts and allow
 * duplicates.
 */
public class S01_NonUniqueDBIndices {
	public static void main(String[] args) {
		Mongo m;
		DB db;

		try {
			m = new Mongo();
			db = m.getDB("hnnotify");
			
			DBCollection col = db.getCollection("users");
			// drop the old indexes
			col.dropIndexes();
			
			// re-ensure all the indices we want.
			DAO.ensureIndices();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}