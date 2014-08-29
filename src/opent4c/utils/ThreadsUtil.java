package opent4c.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;

public class ThreadsUtil {

	private static ExecutorService exService = Executors.newFixedThreadPool(15);
	private static ScheduledExecutorService timerExService = Executors.newScheduledThreadPool(15);
	private static ExecutorService queueExecutor = Executors.newSingleThreadScheduledExecutor();

	
	private ThreadsUtil() {
		//Utility class
	}

	public static void executeInGraphicalThread(Runnable r) {
		Gdx.app.postRunnable(r);
	}
	
	public static Future<?> executeInThread(Runnable r) {
		return exService.submit(r);
	}
	
	public static Future<?> queueInSingleThread(Runnable r){
		return queueExecutor.submit(r);
	}
	
	public static ScheduledFuture<?> executePeriodicallyInThread(Runnable r, int delay, int period, TimeUnit tu){
		return timerExService.scheduleAtFixedRate(r, delay, period, tu);
	}
	
	
}
