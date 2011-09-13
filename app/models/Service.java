package models;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import common.Constants;

// TODO: Don't need this class.
public class Service implements Serializable {
	private static final String LAST_COMMENT_ID_FIELD = "lastCommentID";

	public Integer lastCommentID;

	public Service(DBObject obj) {
		lastCommentID = (Integer) obj.get(LAST_COMMENT_ID_FIELD);
	}

	@Override
	public String toString() {
		return Service.class.getName() + "@" + hashCode() + "\t[lastCommentID="
				+ lastCommentID + "]";
	}

	public DBObject toDBObject() {
		DBObject obj = new BasicDBObject();

		if (lastCommentID != null)
			obj.put(LAST_COMMENT_ID_FIELD, lastCommentID);

		return obj;
	}
}