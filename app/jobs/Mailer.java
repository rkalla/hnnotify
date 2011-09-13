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

			// Get the user on-file that wanted the notification.
			User u = DAO.findUserByUsername(c.parentUsername);

			// Handle two unexpected error scenarios without exploding.
			if (u == null) {
				Logger.error(
						"%s UNEXPECTED ERROR, when trying to load the User for username [%s], DB returned nothing to us. Reconciler may have adopted the comment to the wrong parent, comment: %s",
						LOG_PREFIX, c.parentUsername, c);
				continue;
			} else if (u.email == null) {
				Logger.error(
						"%s UNEXPECTED ERROR, user [%s] had no email registered with them on file. This is likely a bug.",
						LOG_PREFIX, u.username);
				continue;
			}

			if (ReplyMailer.notify(u, c)) {
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