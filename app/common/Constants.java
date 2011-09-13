package common;

public class Constants {
	public static final int CONNECTION_TIMEOUT = 30000;
	public static final int RETRY_DELAY = 1500;
	public static final int MAX_RETRIES = 5;
	
	public static final int MAX_ADOPTION_ATTEMPTS = 7;
	public static final int MAX_DELIVERY_ATTEMPTS = 7;

	public static final String BASE_URL = "http://news.ycombinator.com/";
	
	public static final String ITEM_URL = BASE_URL + "item?id=";
	public static final String USER_URL = BASE_URL + "user?id=";
	public static final String NEW_COMMENTS_URL = BASE_URL + "newcomments";
	
	public static final String EMAIL_PREFIX = "[HN Notify]";
	
	public static final String FORM_DEFAULT_EMAIL = "your@email.com";
	public static final String FORM_DEFAULT_USERNAME = "HN Username";

	// public static final int MAX_RETRY_ATTEMPTS = 5;
	// public static final int MAX_PAGE_DEPTH = 5;
	// public static final int MAX_MAILER_THREADS = 16;
	//
	// public static final String BASE_HREF = "http://news.ycombinator.com/";
	//
	// public static final String ITEM_STEM = "item?id=";
	// public static final String NEW_COMMENTS_STEM = "newcomments";
	// public static final String USER_COMMENTS_STEM = "threads?id=";
	//
	// public static final String EMAIL_PREFIX = "[HN Notify]";
	//
	// public static final String LAST_COMMENT_CACHE_KEY =
	// "lastCommentCacheKey";
	//
	// public static final String LAST_COMMENT_REDIS_KEY =
	// "lastCommentRedisKey";
	//
	// public static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
	// public static final AtomicInteger COMMENT_COUNTER = new AtomicInteger(1);

	// Async executes too fast, getting flood-blocked by gmail
	// public static ExecutorService MAILER_SERVICE = Executors
	// .newFixedThreadPool(MAX_MAILER_THREADS, new ThreadFactory() {
	// @Override
	// public Thread newThread(Runnable r) {
	// Thread daemon = new Thread(r, "HNNotify-MAILER-THREAD #"
	// + THREAD_COUNTER.getAndIncrement());
	// daemon.setDaemon(true);
	//
	// return daemon;
	// }
	// });
}