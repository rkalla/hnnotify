package dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.Author;
import models.Comment;
import models.IModel;
import models.Comment.State;
import models.Service;
import models.User;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/*
 * Because of the author/username query, need an index on BOTH fields of the
 * authors collection.
 * 
 * Maybe just create an iniIndexes call that an OnApplicationStart job calls
 * instead of trying to honor those down in the save/update methods.
 * 
 * Also need index on user's email address for unsubscription
 */
public class DAO {
	private static final String DB_NAME = "hnnotify";

	private static final String USER_COL_NAME = "users";
	private static final String AUTHOR_COL_NAME = "authors";
	private static final String COMMENT_COL_NAME = "comments";
	private static final String SERVICE_COL_NAME = "service";

	private static final int VERIFY_USER_INDEX = 0;
	private static final int VERIFY_AUTHOR_INDEX = 1;
	private static final int VERIFY_COMMENT_INDEX = 2;
	private static final boolean[] INDEX_VERIFIED_FLAGS = new boolean[3];

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

	public static Service getService() {
		DBCollection col = db.getCollection(SERVICE_COL_NAME);
		DBObject obj = col.findOne();

		return (obj == null ? null : new Service(obj));
	}

	public static void updateService(Service s) {
		if (s == null)
			return;

		DBCollection col = db.getCollection(SERVICE_COL_NAME);
		col.update(null, s.toDBObject(), true, false);
	}

	public static User findUser(String username) {
		DBObject obj = find(USER_COL_NAME, "username", username);
		return (obj == null ? null : new User(obj));
	}

	public static User findUserByEmail(String email) {
		DBObject obj = find(USER_COL_NAME, "email", email);
		return (obj == null ? null : new User(obj));
	}

	public static void saveUser(User u) {
		if (u == null)
			return;

		save(USER_COL_NAME, VERIFY_USER_INDEX, "id", u);
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

	public static Author findAuthor(Integer parentID) {
		DBObject obj = find(AUTHOR_COL_NAME, "parentID", parentID);
		return (obj == null ? null : new Author(obj));
	}

	public static Author findAuthor(String username) {
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

		save(AUTHOR_COL_NAME, VERIFY_AUTHOR_INDEX, "parentID", author);
	}

	public static Comment findComment(Integer id) {
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

		save(COMMENT_COL_NAME, VERIFY_COMMENT_INDEX, "id", c);
	}

	public static void updateComment(Comment c) {
		if (c == null)
			return;

		update(COMMENT_COL_NAME, "id", c.id, c);
	}

	private static <T> DBObject find(String colName, String idFieldName, T id) {
		DBCollection col = db.getCollection(colName);
		return col.findOne(new BasicDBObject().append(idFieldName, id));
	}

	private static void save(String colName, int ensureIndex,
			String idFieldName, IModel model) {
		DBCollection col = db.getCollection(colName);

		/*
		 * Run at least once during app execution, ensures the unique index on
		 * 'id' exists. Harmless to ensure this multiple times, but we can avoid
		 * ensuring it EVER time unnecessarily with this simple boolean check.
		 */
		if (!INDEX_VERIFIED_FLAGS[ensureIndex]) {
			col.ensureIndex(
					new BasicDBObject().append(idFieldName, Integer.valueOf(1)),
					null, true);
			INDEX_VERIFIED_FLAGS[ensureIndex] = true;
		}

		col.save(model.toDBObject(), WriteConcern.SAFE);
	}

	private static <T> void update(String colName, String idFieldName, T id,
			IModel model) {
		DBCollection col = db.getCollection(colName);
		col.update(new BasicDBObject().append(idFieldName, id),
				model.toDBObject(), false, false, WriteConcern.SAFE);
	}
}