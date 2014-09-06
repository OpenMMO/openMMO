package opent4c.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;

public class ThreadsUtil {

	private static ExecutorService exService = Executors.newFixedThreadPool(5);
	private static ScheduledExecutorService timerExService = Executors.newScheduledThreadPool(7);
	private static ExecutorService queueExecutor = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Utility class to manage Threaded tasks
	 */
	private ThreadsUtil() {
		//Utility class
	}

	/**
	 * Posts a {@link Runnable} in the main graphical {@link Thread}
	 * @param r : {@link Runnable} to be posted (get it from {@link RunnableCreatorUtil})
	 */
	public static void executeInGraphicalThread(Runnable r) {
		Gdx.app.postRunnable(r);
	}
	
	/**
	 * Posts a {@link Runnable} in a new {@link Thread}
	 * @param r : {@link Runnable} to be posted (get it from {@link RunnableCreatorUtil})
	 * @return A {@link Future} to be able to manage failures & cancels.
	 */
	public static Future<?> executeInThread(Runnable r) {
		return exService.submit(r);
	}
	
	/**
	 * Queues a {@link Runnable} in a single {@link Thread}
	 * @param r : {@link Runnable} to be queued (get it from {@link RunnableCreatorUtil})
	 * @return A {@link Future} to be able to manage failures & cancels.
	 */
	public static Future<?> queueInSingleThread(Runnable r){
		return queueExecutor.submit(r);
	}
	
	/**
	 * Posts a Runnable to be executed periodically in its own {@link Thread}.
	 * @param r : {@link Runnable} to be posted (get it from {@link RunnableCreatorUtil})
	 * @param delay : time in 'unit' waited before first execution
	 * @param period : period in 'unit' between 2 executions
	 * @param unit : {@link TimeUnit} for delay & period
	 * @return A {@link ScheduledFuture} to be able to manage failures & cancels.
	 */
	public static ScheduledFuture<?> executePeriodicallyInThread(Runnable r, int delay, int period, TimeUnit unit){
		return timerExService.scheduleAtFixedRate(r, delay, period, unit);
	}
}
