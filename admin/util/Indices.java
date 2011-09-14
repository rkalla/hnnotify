package util;

import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class Indices {
	public static void main(String[] args) {
		Mongo m;
		DB db;

		try {
			m = new Mongo();
			db = m.getDB("hnnotify");
			
			DBCollection col = db.getCollection("users");
			List<DBObject> list = col.getIndexInfo();
			
			System.out.println("Index info for 'users'");
			
			for(DBObject o : list)
				System.out.println(o);
			
			col = db.getCollection("authors");
			list = col.getIndexInfo();
			
			System.out.println("Index info for 'authors'");
			
			for(DBObject o : list)
				System.out.println(o);
			
			col = db.getCollection("comments");
			list = col.getIndexInfo();
			
			System.out.println("Index info for 'comments'");
			
			for(DBObject o : list)
				System.out.println(o);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}