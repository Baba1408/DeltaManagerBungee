package fr.baba.deltamanager.timers;

import java.util.concurrent.TimeUnit;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.managers.MonitorManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class Monitor {
	private static ScheduledTask task;
	public static int delay = 120;
	
	public static void trystart() {
		if(Config.getConfig().getInt("monitor.interval") != delay || task == null){
			if(!Main.isStarting) System.out.println("Changing interval from " + delay + " seconds to " + Config.getConfig().getInt("monitor.interval") + " seconds");
			delay = Config.getConfig().getInt("monitor.interval");
			cancel();
			start();
		}
	}
	
	public static void start() {
		task = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
			if(Main.debug) System.out.println("Refresh...");
			MonitorManager.refresh();
		}, delay, delay, TimeUnit.SECONDS);
	}
	
	public static void cancel() {
		if(task != null){
			task.cancel();
			task = null;
		}
	}
}
