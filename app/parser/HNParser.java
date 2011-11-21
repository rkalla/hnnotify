package parser;

import static common.Constants.CONNECTION_TIMEOUT;
import static common.Constants.ITEM_URL;
import static common.Constants.MAX_RETRIES;
import static common.Constants.RETRY_DELAY;
import static common.Constants.USER_URL;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import models.Comment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import play.Logger;

public class HNParser {
	public static Document parseURL(String url)
			throws IllegalArgumentException, IOException {
		if (url == null || url.isEmpty())
			throw new IllegalArgumentException("url cannot be null or empty.");

		int tryCount = 0;
		Document doc = null;
		Throwable lastException = null;

		do {
			try {
				Logger.info("Creating Document for URL: %s", url);
				doc = Jsoup.parse(new URL(url), CONNECTION_TIMEOUT);
			} catch (Exception e) {
				e.printStackTrace();
				lastException = e;

				doc = null;
				tryCount++;

				try {
					Thread.sleep(RETRY_DELAY);
				} catch (Exception ex) {
					// no-op
				}
			}
		} while (doc == null && tryCount < MAX_RETRIES);

		if (tryCount < MAX_RETRIES)
			Logger.info("\tDocument successfully created after %d attempt(s)!",
					(tryCount + 1));
		else {
			String message = "Failed to create HNDocument instance for URL ["
					+ url + "] after " + tryCount + " attempts.";
			Logger.info("\t%s", message);
			throw new IOException(message, lastException);
		}

		return doc;
	}

	public static String parseMoreURL(Document doc)
			throws IllegalArgumentException {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		String moreURL = null;

		// Should only be one element representing the more-link cell.
		Elements elements = doc.select("td.title");

		if (!elements.isEmpty()) {
			// Get the containing 'td' element
			Element td = elements.get(0);

			// Get the 'More' a-href which is the only child in this td.
			Element link = td.child(0);

			// Get the 'href' attribute val, which is the more URL.
			moreURL = link.attr("href");

			/*
			 * Some simple post-processing if we had a good link, otherwise
			 * reset the null since jsoup avoids nulls using "".
			 */
			if (moreURL.length() > 0) {
				// Chop the leading slash if there was one.
				if (moreURL.charAt(0) == '/')
					moreURL = moreURL.substring(1);
			} else
				moreURL = null;
		}

		return moreURL;
	}

	public static List<Comment> parseComments(Document doc)
			throws IllegalArgumentException {
		if (doc == null)
			throw new IllegalArgumentException("doc cannot be null");

		/*
		 * Get the 'comhead' span blocks, they contain the 3 or 4 links that
		 * apply to every comment.
		 */
		Elements comheads = doc.select("span.comhead");

		/*
		 * Get the 'comment' td blocks, they contain the actual comment text and
		 * the number of elements returned should match the number of comheads
		 * we have above.
		 */
		Elements comments = doc.select("span.comment");

		List<Comment> commentList = new ArrayList<Comment>(comheads.size());

		// Parse links for each comhead and match with every comment body.
		for (int i = 0, size = comheads.size(); i < size; i++) {
			// Parse all the a-href links out of the comhead.
			Elements links = comheads.get(i).select("a[href]");

			/*
			 * Delegate the actual Comment object creation to a helper method,
			 * being sure to match the comment header info and the comment text
			 * itself.
			 */
			Comment c = createComment(links,
					(i < comments.size() ? comments.get(i) : null));

			// Ensure we have a valid comment to add to our list.
			if (c != null)
				commentList.add(c);
		}

		return commentList;
	}

	public static String parseCommentUsername(int itemID)
			throws IllegalArgumentException {
		if (itemID < 1)
			throw new IllegalArgumentException("itemID [" + itemID
					+ "] must be >= 1");

		Document doc = null;
		String username = null;

		try {
			doc = parseURL(ITEM_URL + itemID);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(
					"Unable to load item URL [%s] in order to parse author username from it.",
					ITEM_URL + itemID);

			return username;
		}

		/*
		 * 2 SCENARIOS: 1, if this is the original post and root comment, all
		 * the data we want is stored in the 'subtext' block. 2, if this is a
		 * normal sub-comment to some post, everything we want is in 'comhead'
		 * block.
		 */
		Elements block = doc.select("td.subtext");

		// SCENARIO #1
		if (block != null && block.size() > 0) {
			Logger.info("td.subtext found, parsing root post header...");

			// Grab first 'subtext' and get its links.
			Elements links = block.get(0).select("a[href]");

			// First link is the username.
			if (links.size() > 0)
				username = links.get(0).text();
		}
		// SCENARIO #2
		else {
			// Username is the first a-href in the comhead block.
			block = doc.select("span.comhead");

			if (block != null && block.size() > 0) {
				Logger.info("span.comhead found, parsing standard comment header...");

				// Grab first 'comhead' and get its links.
				Elements links = block.get(0).select("a[href]");

				// First link is the username.
				if (links.size() > 0)
					username = links.get(0).text();
			}
		}

		return username;
	}

	public static boolean isValidUsername(String username) {
		boolean valid = false;

		try {
			Document doc = parseURL(USER_URL + username);
			Elements head = doc.select("head");

			/*
			 * When an invalid user is specified, the <head> section comes back
			 * totally empty along with a body of "No such user", unfortunately
			 * pulling the .text() for the body to check is really expensive
			 * because it would be the element text for the entire page.
			 * 
			 * It is a lot easier to just check if the head is totally empty.
			 */
			if (!head.isEmpty() && !head.text().isEmpty())
				valid = true;
		} catch (Exception e) {
			// no-op, we don't care, it failed.
		}

		return valid;
	}

	private static Comment createComment(Elements links, Element comment) {
		Comment c = null;

		// Ensure we have the valid comhead structure we need.
		if (links.size() < 3)
			return c;

		c = new Comment();

		// First link is the username, the text contains the username itself.
		c.username = links.get(0).text();

		// Next is the self-link.
		String idText = links.get(1).attr("href");

		try {
			// Chop the numeric ID out of the URI stem.
			idText = idText.substring(idText.lastIndexOf('=') + 1);
			c.id = Integer.parseInt(idText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Next is the parent-link.
		idText = links.get(2).attr("href");

		try {
			// Chop the numeric ID out of the URI stem.
			idText = idText.substring(idText.lastIndexOf('=') + 1);
			c.parentID = Integer.parseInt(idText);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Fill in the text/html of the comment if we have a matching block.
		if (comment != null) {
			c.text = comment.text();
			c.html = comment.html();
		}

		// Check for the 'on' topic title that some comments show.
		if (links.size() > 3) {
			Element onLink = links.get(3);
			c.topicTitle = onLink.text();

			// Get the root topic item ID
			idText = onLink.attr("href");

			try {
				// Chop the numeric ID out of the URI stem.
				idText = idText.substring(idText.lastIndexOf('=') + 1);
				c.topicID = Integer.parseInt(idText);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return c;
	}
}