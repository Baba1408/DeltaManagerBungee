package fr.baba.deltamanager.timers;

import java.util.concurrent.TimeUnit;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.managers.MonitorManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Monitor {
	private static ScheduledTask task;
	private static int delay = 0;
	
	public static void trystart() {
		if(Config.getConfig().getInt("monitor.interval") != delay){
			System.out.println("Changing interval from " + delay + " seconds to " + Config.getConfig().getInt("monitor.interval") + " seconds");
			delay = Config.getConfig().getInt("monitor.interval");
			cancelTask();
			start();
		}
	}
	
	public static void start() {
		Main main = Main.getInstance();
		
		task = ProxyServer.getInstance().getScheduler().schedule(main, () -> {
			if(Config.getConfig().getBoolean("debug")) System.out.println("Refresh...");
			MonitorManager.refresh();
		}, delay, delay, TimeUnit.SECONDS);
	}
	
	public static void cancelTask() {
		if(task != null){
			task.cancel();
			task = null;
		}
	}
}
