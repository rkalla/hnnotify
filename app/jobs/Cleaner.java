package jobs;

import models.Comment.State;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import dao.DAO;

/**
 * This is a partner job to the {@link Reconciler} and {@link Mailer} classes as
 * it comes in behind them and cleans up all comments in a dead state, namely:
 * {@link State#SKIP}, {@link State#ADOPTION_FAILED} or
 * {@link State#DELIVERY_FAILED}.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
@Every("1h")
public class Cleaner extends Job<Void> {
	private static final String LOG_PREFIX = "[Cleaner]";

	@Override
	public void doJob() throws Exception {
		long time = System.currentTimeMillis();
		Logger.info("%s Job Starting at %tc...", LOG_PREFIX, time);

		Logger.info("\t%s Deleting SKIPPED comments...", LOG_PREFIX);
		DAO.deleteCommentByState(State.SKIP);

		Logger.info("\t%s Deleting ADOPTION_FAILED comments...", LOG_PREFIX);
		DAO.deleteCommentByState(State.ADOPTION_FAILED);

		Logger.info("\t%s Deleting DELIVER_FAILED comments...", LOG_PREFIX);
		DAO.deleteCommentByState(State.DELIVERY_FAILED);

		Logger.info("%s Job Complete! Elapsed Time: %d ms", LOG_PREFIX,
				(System.currentTimeMillis() - time));
	}
}