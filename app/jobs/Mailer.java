package jobs;

import static common.Constants.MAX_DELIVERY_ATTEMPTS;

import java.util.List;

import models.Comment;
import models.Comment.State;
import models.User;
import notifiers.ReplyMailer;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import dao.DAO;

/**
 * Class used to retrieve all comments with a state of {@link State#ADOPTED} and
 * send notification emails to the accounts on file of the new response then
 * update the state of the comment.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
@Every("60s")
public class Mailer extends Job<Void> {
	private static final String LOG_PREFIX = "[Mailer]";

	@Override
	public void doJob() throws Exception {
		long time = System.currentTimeMillis();
		Logger.info("%s Job Starting at %d...", LOG_PREFIX, time);
		List<Comment> commentList = DAO.findCommentsByState(State.ADOPTED);

		if (commentList.isEmpty())
			Logger.info(
					"%s NO ADOPTED comments in DB to mail notifications for, will try again later.",
					LOG_PREFIX);
		else
			Logger.info(
					"%s Found %d ADOPTED comments in DB to mail notifications for.",
					LOG_PREFIX, commentList.size());

		for (int i = 0, size = commentList.size(); i < size; i++) {
			Comment c = commentList.get(i);
			Logger.info("%s Attempting to deliver comment [%d of %d]: %s",
					LOG_PREFIX, (i + 1), size, c);

			/*
			 * Get all the users on-file that want to be notified of replies to
			 * this username.
			 * 
			 * More specifically, we let anyone watch anyone else's replies, so
			 * username 'patio11' might have 10 people watching for replies to
			 * his posts.
			 */
			List<User> userList = DAO.findUsersByUsername(c.parentUsername);

			Logger.info(
					"%s Found %d registered users wanting notifications for username [%s]",
					LOG_PREFIX, userList.size(), c.parentUsername);

			boolean mostlyDelivered = false;

			// Attempt to deliver to all the interested people.
			for (int j = 0, jSize = userList.size(); j < jSize; j++) {
				User u = userList.get(j);

				// Keep track that we at least delivered it once.
				if (ReplyMailer.notify(u, c))
					mostlyDelivered = true;
			}

			/*
			 * We don't currently track comments in such a way that we can keep
			 * track of m:n successful or failed deliveries, so instead we keep
			 * track of a SINGLE successful delivery and consider the comment
			 * delivered at that point.
			 * 
			 * If we couldn't even get ONE successful delivery though, then
			 * there is something wrong like a network issue and we consider the
			 * comment undelivered.
			 */
			if (mostlyDelivered) {
				c.state = State.DELIVERED;
				Logger.info("\t%s Delivery SUCCESSFUL!", LOG_PREFIX);
			} else {
				c.deliveryAttempts++;

				Logger.warn("%s Delivery FAILED (%d attempt(s)).", LOG_PREFIX,
						c.deliveryAttempts);

				// Comment goes into limbo if delivery has failed a lot.
				if (c.deliveryAttempts > MAX_DELIVERY_ATTEMPTS) {
					c.state = State.DELIVERY_FAILED;
					Logger.error(
							"\t%s Comment permanently IGNORED, too many failed delivery attempts.",
							LOG_PREFIX);
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