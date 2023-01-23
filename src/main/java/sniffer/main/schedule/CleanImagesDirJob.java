package sniffer.main.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import sniffer.main.utils.Utils;

public class CleanImagesDirJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Utils.getInstance().emptyImegesDir();
	}

}
