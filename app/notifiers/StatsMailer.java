package notifiers;

import static common.Constants.EMAIL_PREFIX;
import play.mvc.Mailer;

public class StatsMailer extends Mailer {
	public static void newCommentsJobStats(long time) {
		setSubject("%s Metrics - NewCommentsJob Stats", EMAIL_PREFIX);
		addRecipient("rkalla@gmail.com");
		setFrom("HN Notify <hnnotify@thebuzzmedia.com>");

		send(time);
	}
}