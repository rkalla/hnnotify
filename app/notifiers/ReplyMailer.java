package notifiers;

import static common.Constants.EMAIL_PREFIX;

import java.util.concurrent.Future;

import models.Comment;
import models.User;
import play.Logger;
import play.mvc.Mailer;

public class ReplyMailer extends Mailer {
	public static boolean notify(User user, Comment comment) {
		if (comment.topicTitle == null)
			setSubject("%s %s replied to you at Hacker News", EMAIL_PREFIX,
					comment.username);
		else
			setSubject("%s %s replied to you on '%s' at Hacker News",
					EMAIL_PREFIX, comment.username, comment.topicTitle);

		addRecipient(user.email);
		setFrom("HN Notify <hnnotify@thebuzzmedia.com>");

		boolean sent = false;

		try {
			Future<Boolean> result = send(user, comment);

			if (result != null)
				sent = result.get();
		} catch (Exception e) {
			Logger.error(e, "Unable to sent notification email.");
		}
		
		return sent;
	}
}