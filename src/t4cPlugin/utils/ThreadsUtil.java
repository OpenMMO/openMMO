package t4cPlugin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadsUtil {

	private static ExecutorService exService = Executors.newFixedThreadPool(15);
	
	private ThreadsUtil() {
		//Utility class
	}
	
	public static void executeInThread(Runnable r) {
		exService.execute(r);
	}
	
	
}
