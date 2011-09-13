package models;

import java.io.Serializable;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import dao.DAO;

public class Comment implements IModel {
	public static void main(String[] args) {
		// models.Comment@2985549 [state=NEW, id=2985549, username=richo,
		// parentID=2985276, parentUsername=null, topicID=2985276,
		// topicTitle=Twat. A cli ruby gem for Twitter,
		// adoptionAttempts=0, deliveryAttempts=0, dateCreated=1315793283636,
		// dateLastUpdated=1315793283636]
		Comment c = new Comment();
		c.id = 2985549;
		c.username = "richo";
		c.parentID = 2985276;
		c.topicID = 2985276;
		c.topicTitle = "Twat. A cli ruby gem for Twitter";
		c.dateCreated = 1315793283636L;
		c.dateLastUpdated = 1315793283636L;

		DAO.saveComment(c);

		// Comment c1 = new Comment();
		// Comment c2 = new Comment();
		//
		// System.out.println(c1.hashCode());
		// System.out.println(c2.hashCode());
		// System.out.println(c1.equals(c2)); // false
		//
		// c1.id = 13;
		// System.out.println(c1.hashCode()); // 13
		// System.out.println(c1.equals(c2)); // false
		//
		// c2.id = 17;
		// System.out.println(c1.hashCode()); // 13
		// System.out.println(c2.hashCode()); // 17
		// System.out.println(c1.equals(c2)); // false
		//
		// Set<Comment> set = DAO.findNewestComments(5);
		//
		// System.out.println(set.contains(c1));
		// System.out.println(set.contains(c2));
	}

	private static final String STATE_FIELD = "state";

	private static final String ID_FIELD = "id";
	private static final String TEXT_FIELD = "text";
	private static final String HTML_FIELD = "html";
	private static final String USERNAME_FIELD = "username";

	private static final String PARENT_ID_FIELD = "parentID";
	private static final String PARENT_USERNAME_FIELD = "parentUsername";

	private static final String TOPIC_ID_FIELD = "topicID";
	private static final String TOPIC_TITLE_FIELD = "topicTitle";

	private static final String ADOPTION_ATTEMPTS_FIELD = "adoptionAttempts";
	private static final String DELIVERY_ATTEMPTS_FIELD = "deliveryAttempts";

	private static final String DATE_CREATED_FIELD = "dateCreated";
	private static final String DATE_LAST_UPDATED_FIELD = "dateLastUpdated";

	public enum State {
		NEW, ADOPTED, ADOPTION_FAILED, SKIP, DELIVERED, DELIVERY_FAILED
	}

	public State state;

	public Integer id;
	public String text;
	public String html;
	public String username;

	public Integer parentID;
	public String parentUsername;

	public Integer topicID;
	public String topicTitle;

	public Integer adoptionAttempts;
	public Integer deliveryAttempts;

	public Long dateCreated;
	public Long dateLastUpdated;

	public Comment() {
		state = State.NEW;

		adoptionAttempts = 0;
		deliveryAttempts = 0;

		dateCreated = System.currentTimeMillis();
		dateLastUpdated = dateCreated;
	}

	public Comment(DBObject obj) {
		Object val = obj.get(STATE_FIELD);

		if (val != null)
			state = State.valueOf((String) val);

		id = (Integer) obj.get(ID_FIELD);
		text = (String) obj.get(TEXT_FIELD);
		html = (String) obj.get(HTML_FIELD);
		username = (String) obj.get(USERNAME_FIELD);

		parentID = (Integer) obj.get(PARENT_ID_FIELD);
		parentUsername = (String) obj.get(PARENT_USERNAME_FIELD);

		topicID = (Integer) obj.get(TOPIC_ID_FIELD);
		topicTitle = (String) obj.get(TOPIC_TITLE_FIELD);

		adoptionAttempts = (Integer) obj.get(ADOPTION_ATTEMPTS_FIELD);
		deliveryAttempts = (Integer) obj.get(DELIVERY_ATTEMPTS_FIELD);

		dateCreated = (Long) obj.get(DATE_CREATED_FIELD);
		dateLastUpdated = (Long) obj.get(DATE_LAST_UPDATED_FIELD);
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		else
			return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = false;

		// If not-null and either equals this OR has the same hashcode.
		if (obj != null
				&& (this == obj || ((obj instanceof Comment) && (hashCode() == obj
						.hashCode()))))
			equal = true;

		return equal;
	}

	@Override
	public String toString() {
		return Comment.class.getName() + "@" + hashCode() + "\t[state=" + state
				+ ", id=" + id + ", username=" + username + ", parentID="
				+ parentID + ", parentUsername=" + parentUsername
				+ ", topicID=" + topicID + ", topicTitle=" + topicTitle
				+ ", adoptionAttempts=" + adoptionAttempts
				+ ", deliveryAttempts=" + deliveryAttempts + ", dateCreated="
				+ dateCreated + ", dateLastUpdated=" + dateLastUpdated + "]";
	}

	public DBObject toDBObject() {
		DBObject obj = new BasicDBObject();

		if (state != null)
			obj.put(STATE_FIELD, state.name());

		if (id != null)
			obj.put(ID_FIELD, id);

		if (text != null)
			obj.put(TEXT_FIELD, text);

		if (html != null)
			obj.put(HTML_FIELD, html);

		if (username != null)
			obj.put(USERNAME_FIELD, username);

		if (parentID != null)
			obj.put(PARENT_ID_FIELD, parentID);

		if (parentUsername != null)
			obj.put(PARENT_USERNAME_FIELD, parentUsername);

		if (topicID != null)
			obj.put(TOPIC_ID_FIELD, topicID);

		if (topicTitle != null)
			obj.put(TOPIC_TITLE_FIELD, topicTitle);

		if (adoptionAttempts != null)
			obj.put(ADOPTION_ATTEMPTS_FIELD, adoptionAttempts);

		if (deliveryAttempts != null)
			obj.put(DELIVERY_ATTEMPTS_FIELD, deliveryAttempts);

		if (dateCreated != null)
			obj.put(DATE_CREATED_FIELD, dateCreated);

		if (dateLastUpdated != null)
			obj.put(DATE_LAST_UPDATED_FIELD, dateLastUpdated);

		return obj;
	}
}