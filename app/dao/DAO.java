package dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Author;
import models.Comment;
import models.Comment.State;
import models.IModel;
import models.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class DAO {
	private static final String DB_NAME = "hnnotify";

	private static final String USER_COL_NAME = "users";
	private static final String AUTHOR_COL_NAME = "authors";
	private static final String COMMENT_COL_NAME = "comments";

	private static Mongo m;
	private static DB db;

	static {
		try {
			m = new Mongo();
			db = m.getDB(DB_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ensureIndices() {
		DBCollection col = db.getCollection(USER_COL_NAME);

		col.ensureIndex(
				new BasicDBObject().append("email", Integer.valueOf(1)), null,
				false);
		col.ensureIndex(
				new BasicDBObject().append("username", Integer.valueOf(1)),
				null, false);

		col = db.getCollection(AUTHOR_COL_NAME);
		col.ensureIndex(
				new BasicDBObject().append("parentID", Integer.valueOf(1)),
				null, false);
		col.ensureIndex(
				new BasicDBObject().append("username", Integer.valueOf(1)),
				null, false);

		col = db.getCollection(COMMENT_COL_NAME);
		col.ensureIndex(new BasicDBObject().append("id", Integer.valueOf(1)),
				null, false);
		col.ensureIndex(
				new BasicDBObject().append("state", Integer.valueOf(1)), null,
				false);
	}

	public static long getUserCount() {
		DBCollection col = db.getCollection(USER_COL_NAME);
		return col.count();
	}

	public static List<User> findAllUsers() {
		List<User> userList = new ArrayList<User>();
		DBCollection col = db.getCollection(USER_COL_NAME);
		DBCursor cur = col.find();

		while (cur.hasNext())
			userList.add(new User(cur.next()));

		return userList;
	}

	public static User findUserByUsername(String username) {
		DBObject obj = find(USER_COL_NAME, "username", username);
		return (obj == null ? null : new User(obj));
	}

	public static List<User> findUsersByUsername(String username) {
		DBCollection col = db.getCollection(USER_COL_NAME);
		DBCursor cur = col.find(new BasicDBObject()
				.append("username", username));
		List<User> userList = new ArrayList<User>(cur.count());

		while (cur.hasNext())
			userList.add(new User(cur.next()));

		return userList;
	}

	public static User findUserByEmail(String email) {
		DBObject obj = find(USER_COL_NAME, "email", email);
		return (obj == null ? null : new User(obj));
	}

	public static List<User> findUsersByEmail(String email) {
		DBCollection col = db.getCollection(USER_COL_NAME);
		DBCursor cur = col.find(new BasicDBObject().append("email", email));
		List<User> userList = new ArrayList<User>(cur.count());

		while (cur.hasNext())
			userList.add(new User(cur.next()));

		return userList;
	}

	public static void saveUser(User u) {
		if (u == null)
			return;

		save(USER_COL_NAME, u);
	}

	public static void updateUser(User u) {
		if (u == null)
			return;

		update(USER_COL_NAME, "username", u.username, u);
	}

	public static void deleteUser(User u) {
		if (u == null)
			return;

		DBCollection col = db.getCollection(USER_COL_NAME);
		col.remove(new BasicDBObject().append("username", u.username),
				WriteConcern.SAFE);
	}

	public static Author findAuthorByParentID(Integer parentID) {
		DBObject obj = find(AUTHOR_COL_NAME, "parentID", parentID);
		return (obj == null ? null : new Author(obj));
	}

	public static Author findAuthorByUsername(String username) {
		DBObject obj = find(AUTHOR_COL_NAME, "username", username);
		return (obj == null ? null : new Author(obj));
	}

	public static List<Author> findAllAuthors() {
		List<Author> authorList = new ArrayList<Author>();
		DBCollection col = db.getCollection(AUTHOR_COL_NAME);
		DBCursor cur = col.find();

		while (cur.hasNext())
			authorList.add(new Author(cur.next()));

		return authorList;
	}

	public static void saveAuthor(Author author) {
		if (author == null)
			return;

		save(AUTHOR_COL_NAME, author);
	}

	public static long getCommentCount() {
		DBCollection col = db.getCollection(COMMENT_COL_NAME);
		return col.count();
	}

	public static long getDeliveredCommentCount() {
		DBCollection col = db.getCollection(COMMENT_COL_NAME);
		return col.count(new BasicDBObject().append("state",
				State.DELIVERED.name()));
	}

	public static Comment findCommentByID(Integer id) {
		DBObject obj = find(COMMENT_COL_NAME, "id", id);
		return (obj == null ? null : new Comment(obj));
	}

	public static Set<Comment> findNewestComments(int count) {
		DBCollection col = db.getCollection(COMMENT_COL_NAME);
		DBCursor cur = col.find().sort(
				new BasicDBObject().append("id", Integer.valueOf(-1)));

		Set<Comment> comments = new HashSet<Comment>();

		for (int i = 0; cur.hasNext() && i < 5; i++)
			comments.add(new Comment(cur.next()));

		return comments;
	}

	public static List<Comment> findCommentsByState(State state) {
		List<Comment> commentList = new ArrayList<Comment>();

		if (state != null) {
			DBCollection col = db.getCollection(COMMENT_COL_NAME);
			DBCursor cur = col.find(new BasicDBObject().append("state",
					state.name()));

			while (cur.hasNext())
				commentList.add(new Comment(cur.next()));
		}

		return commentList;
	}

	public static List<Comment> findAllComments() {
		List<Comment> commentList = new ArrayList<Comment>();
		DBCollection col = db.getCollection(COMMENT_COL_NAME);
		DBCursor cur = col.find();

		while (cur.hasNext())
			commentList.add(new Comment(cur.next()));

		return commentList;
	}

	public static void saveComment(Comment c) {
		if (c == null)
			return;

		save(COMMENT_COL_NAME, c);
	}

	public static void updateComment(Comment c) {
		if (c == null)
			return;

		update(COMMENT_COL_NAME, "id", c.id, c);
	}

	public static void deleteCommentByState(State state) {
		if (state == null)
			return;

		DBCollection col = db.getCollection(COMMENT_COL_NAME);
		col.remove(new BasicDBObject().append("state", state.name()));
	}

	private static <T> DBObject find(String colName, String idFieldName, T id) {
		DBCollection col = db.getCollection(colName);
		return col.findOne(new BasicDBObject().append(idFieldName, id));
	}

	private static void save(String colName, IModel model) {
		DBCollection col = db.getCollection(colName);
		col.save(model.toDBObject(), WriteConcern.SAFE);
	}

	private static <T> void update(String colName, String idFieldName, T id,
			IModel model) {
		DBCollection col = db.getCollection(colName);
		col.update(new BasicDBObject().append(idFieldName, id),
				model.toDBObject(), false, false, WriteConcern.SAFE);
	}
}