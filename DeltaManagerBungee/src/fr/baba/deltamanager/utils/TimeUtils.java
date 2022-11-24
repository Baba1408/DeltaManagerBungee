package fr.baba.deltamanager.utils;

import java.time.Duration;
import java.util.List;

import fr.baba.deltamanager.Config;

public class TimeUtils {
	private static List<String> spacer = Config.getConfig().getStringList("spacers");
	private static List<String> plural = Config.getConfig().getStringList("date.format.plural");
	private static List<String> singular = Config.getConfig().getStringList("date.format.singular");

	public static String format(Duration d) {
		if(!Config.getConfig().getBoolean("monitor.notify.show-seconds") && d.getSeconds() < 60) return Config.getConfig().getString("monitor.notify.no-seconds");
		
		long days = d.toDays();
	    d = d.minusDays(days);
	    long hours = d.toHours();
	    d = d.minusHours(hours);
	    long minutes = d.toMinutes();
	    d = d.minusMinutes(minutes);
	    long seconds = d.getSeconds();
	    
	    String m = (days != 1?"":days + spacer.get(0) + singular.get(2) + spacer.get(1))
	    		+ (days <= 1?"":days + spacer.get(0) + plural.get(2) + spacer.get(1))
	    		+ (hours != 1?"":hours + spacer.get(0) + singular.get(3) + spacer.get(1))
	    		+ (hours <= 1?"":hours + spacer.get(0) + plural.get(3) + spacer.get(1))
	    		+ (minutes != 1?"":minutes + spacer.get(0) + singular.get(4) + spacer.get(1))
	    		+ (minutes <= 1?"":minutes + spacer.get(0) + plural.get(4) + spacer.get(1))
	    		+ (seconds != 1 || !Config.getConfig().getBoolean("monitor.notify.show-seconds")?"":seconds + spacer.get(0) + singular.get(5))
	    		+ (seconds <= 1 || !Config.getConfig().getBoolean("monitor.notify.show-seconds")?"":seconds + spacer.get(0) + plural.get(5));
	    
	    return m.substring(0, m.length() - spacer.get(1).length());
	}
}
