package t4cPlugin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadsUtil {

	private static ExecutorService exService = Executors.newFixedThreadPool(15);
	private static ScheduledExecutorService timerExService = Executors.newScheduledThreadPool(3);
	
	private ThreadsUtil() {
		//Utility class
	}
	
	public static void executeInThread(Runnable r) {
		exService.execute(r);
	}
	
	public static void executePeriodicallyInThread(Runnable r, int delay, int period, TimeUnit tu){
		timerExService.scheduleAtFixedRate(r, delay, period, tu);
	}
	
	
}
