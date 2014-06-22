package t4cPlugin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadsUtil {

	private static ExecutorService exService = Executors.newFixedThreadPool(5);
	private static ScheduledExecutorService timerExService = Executors.newScheduledThreadPool(5);
	
	private ThreadsUtil() {
		//Utility class
	}
	
	public static void executeInThread(Runnable r) {
		exService.execute(r);
	}
	
	public static ScheduledFuture<?> executePeriodicallyInThread(Runnable r, int delay, int period, TimeUnit tu){
		return timerExService.scheduleAtFixedRate(r, delay, period, tu);
	}
	
	
}
