package jobs;

import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;

import common.Constants;

import dao.DAO;

import models.Comment;

import parser.HNParser;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import static common.Constants.*;

/**
 * Class used to index all the incoming new comments on HN.
 * <p/>
 * It is the job of this class to determine what comments are new and which are
 * old, then parse all new comments and store them in the DB for processing by
 * subsequent portions of the system.
 * 
 * @author Riyad Kalla (software@thebuzzmedia.com)
 */
//@Every("60s")
public class Indexer extends Job<Void> {
	private static final String LOG_PREFIX = "[Indexer]";

	@Override
	public void doJob() throws Exception {
		long time = System.currentTimeMillis();
		Logger.info("%s Job Starting at %d...", LOG_PREFIX, time);

		/*
		 * Load (up to) the last 5 most recent indexed comments so we have
		 * checksums we can use to identify when we have hit already-indexed
		 * comments. We grab the last 5 and not the last 1 because comments can
		 * be deleted, and in the off chance our latest indexed comment was
		 * erased on the site, we will never encounter it during indexing and
		 * therefore never find a valid stop-condition for indexing.
		 */
		Set<Comment> stopConditions = DAO.findNewestComments(5);

		Logger.info(
				"%s Loaded %d previously indexed comments to use as stop-conditions.",
				LOG_PREFIX, stopConditions.size());

		int stopIndex = -1;
		String url = NEW_COMMENTS_URL;

		/*
		 * If the DB is empty and we cannot load any stop conditions from it,
		 * then we only want to parse the front page of comments and not go
		 * crazy recursing forever. So if no stopConditions were found, set
		 * 'more' to false so we only parse the first page.
		 */
		boolean more = !stopConditions.isEmpty();

		/*
		 * Keep following the 'more' URL links and parsing more and more
		 * comments until our logic below flips the boolean and tells us to
		 * stop.
		 * 
		 * Our stop condition is dictated by running into any comments that we
		 * have already parsed before as identified by the contents of
		 * stopConditions.
		 */
		do {
			Document doc = null;

			try {
				doc = HNParser.parseURL(url);
			} catch (Exception e) {
				Logger.error(
						"%s Unable to parse JSoup Document for URL [%s], job cancelling and will try again later...",
						LOG_PREFIX, NEW_COMMENTS_URL);

				// Cancel the job, try again later.
				return;
			}

			// Parse the comments on this page (doc).
			List<Comment> newComments = HNParser.parseComments(doc);

			if (newComments.isEmpty()) {
				Logger.error(
						"%s Parsed 0 new comments which may be valid on a slow posting day, but is otherwise a problem. Possible HTML reformat broke the scraping? Indexer will quit...",
						LOG_PREFIX);
				return;
			}

			Logger.info("%s Parsed %d comments for processing...", LOG_PREFIX,
					newComments.size());

			/*
			 * Now we need to determine if we have reached a stop-condition.
			 * 
			 * A stop-condition is detected when one of the 'new' comments we
			 * just parsed is contained in our stopCondition collection of
			 * newest comments previously parsed. That is how we know we indexed
			 * all the new comments and have now hit overlap and need to stop.
			 * 
			 * NOTE: If we had no stopConditions to start with, more is already
			 * false and we will save all the comments we parsed.
			 */
			for (int i = 0, size = newComments.size(); more && i < size; i++) {
				Comment c = newComments.get(i);

				// Check if we hit a stop condition.
				if (stopConditions.contains(c)) {
					// Flip the more flag, we found a stop condition.
					more = false;

					/*
					 * Remember which comment we stopped on, everything up until
					 * that index is new.
					 */
					stopIndex = i;

					Logger.info(
							"\t%s Found STOP-CONDITION at index %d, comment: %s",
							LOG_PREFIX, stopIndex, c);
					Logger.info("\t%s Will add %d new comments to DB.",
							LOG_PREFIX, stopIndex);
				}
			}

			// Determine how many of the 'new' comments we parsed to keep.
			int endIndex = (stopIndex == -1 ? newComments.size() : stopIndex);

			// Insert all the new comments in the DB.
			for (int i = 0; i < endIndex; i++)
				DAO.saveComment(newComments.get(i));

			Logger.info("%s Saved %d new comments to DB.", LOG_PREFIX, endIndex);

			// Check if we need to recurse on the 'More' URL for more comments.
			if (more) {
				String moreURL = HNParser.parseMoreURL(doc);

				if (moreURL == null) {
					Logger.error(
							"%s Unable to parse the 'More' URL from the current Document, unable to recurse and continue indexing job, quiting...",
							LOG_PREFIX);
					return;
				}

				url = BASE_URL + HNParser.parseMoreURL(doc);

				Logger.info(
						"%s Successfull parsed 'More' URL, will recurse for more comments at: %s",
						LOG_PREFIX, url);
			}
		} while (more);

		Logger.info("%s Job Complete! Elapsed Time: %d ms", LOG_PREFIX,
				(System.currentTimeMillis() - time));
	}
}