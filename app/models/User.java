package models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class User implements IModel {
	private static final long serialVersionUID = -5794834367247667306L;

	private static final String USERNAME_FIELD = "username";
	private static final String EMAIL_FIELD = "email";
	private static final String DATE_JOINED = "dateJoined";

	public String username;
	public String email;
	public Long dateJoined;

	public User(String username, String email, Long dateJoined) {
		this.username = username;
		this.email = email;
		this.dateJoined = dateJoined;
	}

	public User(DBObject obj) {
		username = (String) obj.get(USERNAME_FIELD);
		email = (String) obj.get(EMAIL_FIELD);
		dateJoined = (Long) obj.get(DATE_JOINED);
	}

	@Override
	public String toString() {
		return User.class.getName() + "@" + hashCode() + "\t[username="
				+ username + ", email=" + email + ", dateJoined=" + dateJoined
				+ "]";
	}

	public DBObject toDBObject() {
		DBObject obj = new BasicDBObject();

		if (username != null)
			obj.put(USERNAME_FIELD, username);

		if (email != null)
			obj.put(EMAIL_FIELD, email);

		if (dateJoined != null)
			obj.put(DATE_JOINED, dateJoined);

		return obj;
	}
}