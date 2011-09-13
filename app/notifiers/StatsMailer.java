package notifiers;

import static common.Constants.*;

import models.Comment;
import play.mvc.Mailer;

public class StatsMailer extends Mailer {
	public static void newCommentsJobStats(long time) {
		setSubject("%s Metrics - NewCommentsJob Stats", EMAIL_PREFIX);
		addRecipient("rkalla@gmail.com");
		setFrom("HN Notify <hnnotify@thebuzzmedia.com>");

		send(time);
	}
}