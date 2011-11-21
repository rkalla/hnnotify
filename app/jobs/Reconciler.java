package jobs;

import static common.Constants.MAX_ADOPTION_ATTEMPTS;

import java.util.Date;
import java.util.List;

import models.Author;
import models.Comment;
import models.Comment.State;
import models.User;
import parser.HNParser;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import dao.DAO;

/**
 * Class used to pull all comments out of the database with a state of
 * {@link State#NEW} and reconcile their parentage by looking up the username
 * that posted the parent comment from the DB or from HN directly and then
 * updating the state to either {@link State#SKIP} if the parent user is not an
 * HNNotify user and there is nothing to do with the comment, to
 * {@link State#ADOPTED} if parent was found and they are an HNNotify subscriber
 * and {@link State#ADOPTION_FAILED} if too many failed attempts at adoption
 * were made and the comment is given up on.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
@Every("30s")
public class Reconciler extends Job<Void> {
	private static final String LOG_PREFIX = "[Reconciler]";

	@Override
	public void doJob() throws Exception {
		long time = System.currentTimeMillis();
		Logger.info("%s Job Starting at %tc...", LOG_PREFIX, time);
		List<Comment> commentList = DAO.findCommentsByState(State.NEW);

		if (commentList.isEmpty())
			Logger.info(
					"%s NO NEW comments in DB to reconcile, will try again later.",
					LOG_PREFIX);
		else
			Logger.info("%s Found %d NEW comments in DB to reconcile.",
					LOG_PREFIX, commentList.size());

		for (int i = 0, size = commentList.size(); i < size; i++) {
			Comment c = commentList.get(i);

			Author author = DAO.findAuthorByParentID(c.parentID);

			/*
			 * We first try and discover who wrote the parent comment by
			 * checking the DB to see if we had cached that information yet. If
			 * not, we make an attempt at parsing that information directly from
			 * HN.
			 */
			if (author != null) {
				c.parentUsername = author.username;
				Logger.info("\t%s Cache HIT, parent username found in DB.",
						LOG_PREFIX);
			} else {
				Logger.info(
						"\t%s Cache MISS, parsing parent username from HN directly.",
						LOG_PREFIX);

				c.parentUsername = HNParser.parseCommentUsername(c.parentID);

				if (c.parentUsername == null)
					Logger.error(
							"\t\t%s HN Parent Username parse FAILED! HN could be blocking or screen-scraping failed to identify parent.",
							LOG_PREFIX);
				else
					Logger.info("\t\t%s HN Parent Username parsed SUCCESS: %s",
							LOG_PREFIX, c.parentUsername);
			}

			/*
			 * If we were unable to determine the username of the parent comment
			 * for this comment, that could be caused by HN rate-limiting our
			 * queries or some other error. Either way, we will chalk this up as
			 * an adoption failure and update the comment accordingly.
			 * 
			 * Otherwise the comment was successfully adopted in which case we
			 * will handle that as well.
			 */
			if (c.parentUsername == null) {
				c.adoptionAttempts++;

				Logger.warn(
						"%s Adoption FAILED (%d attempt(s)). Unable to determine parent username for comment: %s",
						LOG_PREFIX, c.adoptionAttempts, c);

				// Comment goes into limbo if adoption has failed a lot.
				if (c.adoptionAttempts > MAX_ADOPTION_ATTEMPTS) {
					c.state = State.ADOPTION_FAILED;
					Logger.error("\t%s Comment permanently ORPHANED, too many failed adoption attempts.");
				}
			} else {
				Logger.info(
						"%s Adoption SUCCESSFUL. Retrieved parent username [%s] for comment: %s",
						LOG_PREFIX, c.username, c);

				/*
				 * Cache the parent comment author information if we didn't have
				 * it cached in the DB already.
				 */
				if (author == null) {
					author = new Author(c.parentID, c.parentUsername);
					DAO.saveAuthor(author);

					Logger.info(
							"\t%s Cached username [%s] for parentID [%d] in the DB.",
							LOG_PREFIX, author.username, author.parentID);
				}

				/*
				 * Now that we know who wrote the parent comment, determine if
				 * they are a member and want notifications or if this comment
				 * is useless to us (no one wants notifications of it) and we
				 * can just skip it in the delivery step.
				 */
				User user = DAO.findUserByUsername(c.parentUsername);

				/*
				 * If there is no registered HNNotify user in the DB with this
				 * username, it means we notify no one of this reply (SKIP it).
				 * 
				 * If we did find a user, then that means that person registered
				 * for notifications with us and we'll mark the comment so it
				 * will be delivered by the Mailer service.
				 */
				if (user == null) {
					c.state = State.SKIP;
					Logger.info(
							"%s Comment will be SKIPPED, parent [%s] is not a member of HNNotify. No one to notify.",
							LOG_PREFIX, c.parentUsername);
				} else {
					c.state = State.ADOPTED;

					String date = (c.dateCreated == null ? "date-null"
							: new Date(c.dateCreated).toString());
					Logger.info(
							"%s Comment will be DELIVERED, parent [%s] has been a member of HNNotify since %s.",
							LOG_PREFIX, c.parentUsername, date);
				}
			}

			// Update the comment in the DB.
			c.dateLastUpdated = System.currentTimeMillis();
			DAO.updateComment(c);
		}

		Logger.info("%s Job Complete! Elapsed Time: %d ms", LOG_PREFIX,
				(System.currentTimeMillis() - time));
	}
}