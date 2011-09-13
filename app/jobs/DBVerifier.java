package jobs;

import dao.DAO;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class DBVerifier extends Job<Void> {
	@Override
	public void doJob() throws Exception {
		DAO.ensureIndices();
	}
}