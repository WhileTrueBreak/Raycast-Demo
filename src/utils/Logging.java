package utils;

import java.util.ArrayList;

public class Logging {
	
	private static ArrayList<String> logs = new ArrayList<String>();
	
	public static void addLog(String log) {
		logs.add(log);
	}
	
	public static void dumpLog() {
		System.out.println("[Dump]\t\t---Start Dump---");
		for(String log:logs) {
			System.out.println("[Dump]\t\t\t"+log);
		}
		System.out.println("[Dump]\t\t----End Dump----");
		clearLog();
	}
	
	public static void clearLog() {
		logs = new ArrayList<String>();
	}
	
	
}
