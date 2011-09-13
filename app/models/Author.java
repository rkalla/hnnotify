package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Author implements IModel {
	private static final long serialVersionUID = 8756025960792005423L;
	
	private static final String PARENT_ID_FIELD = "parentID";
	private static final String USERNAME_FIELD = "username";

	public Integer parentID;
	public String username;

	public Author(Integer parentID, String username) {
		this.parentID = parentID;
		this.username = username;
	}

	public Author(DBObject obj) {
		parentID = (Integer) obj.get(PARENT_ID_FIELD);
		username = (String) obj.get(USERNAME_FIELD);
	}

	@Override
	public String toString() {
		return Author.class.getName() + "@" + hashCode() + "\t[parentID="
				+ parentID + ", username=" + username + "]";
	}

	public DBObject toDBObject() {
		DBObject obj = new BasicDBObject();

		if (parentID != null)
			obj.put(PARENT_ID_FIELD, parentID);

		if (username != null)
			obj.put(USERNAME_FIELD, username);

		return obj;
	}
}