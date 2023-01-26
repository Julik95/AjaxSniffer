package sniffer.main.schedule;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class CleanImagesDirScheduler {

	private final String NAME_OF_JOB = "cleanImageDirJob";  
    private final String NAME_OF_GROUP = "group1";  
    private final String NAME_OF_TRIGGER = "triggerStart";  
    private int TIME_INTERVAL = 24; 
    
	private static Scheduler scheduler;  
	
	public CleanImagesDirScheduler() {
        try {
        	scheduler = new StdSchedulerFactory().getScheduler();
        	Trigger triggerNew =  createTrigger(); 
			scheduleJob(triggerNew);
		} catch (Exception e) {
			e.printStackTrace();
		}  
	}
	
	private void scheduleJob(Trigger triggerNew) throws Exception {  
		JobDetail jobInstance = JobBuilder.newJob(CleanImagesDirJob.class).withIdentity(NAME_OF_JOB, NAME_OF_GROUP).build();    
	    scheduler.scheduleJob(jobInstance, triggerNew);  
	} 
	
	private Trigger createTrigger() {    
        Trigger triggerNew = TriggerBuilder.newTrigger().withIdentity(NAME_OF_TRIGGER, NAME_OF_GROUP)  
                .withSchedule(  
                		//SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(TIME_INTERVAL).repeatForever())  
                        SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(30).repeatForever())  
                .build();  
        return triggerNew;  
    }  
	
	public void startSchedule() throws SchedulerException {
		scheduler.start();
		
	}
	
	public void stopSchedule() throws SchedulerException {
		scheduler.shutdown(false);
	}
}
