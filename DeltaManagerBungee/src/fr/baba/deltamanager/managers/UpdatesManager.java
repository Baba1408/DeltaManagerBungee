package fr.baba.deltamanager.managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class UpdatesManager {
	private static Main main = Main.getInstance();
	static String prefix = "§2§l[§a§lDeltaManager§2§l]§a ";
	static String newversion = null;
	
	public static void check() {
		ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
			String version;
			
			try {
				HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=106116").openConnection();
				con.setRequestMethod("GET");
				version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			} catch (Exception ex) {
				main.getLogger().info("Failed to check for updates on spigot.");
				return;
			}
			
			if(version != null && !version.isEmpty()){
				int[] P = toReadable(main.getDescription().getVersion());
			    int[] S = toReadable(version);
			    
			    Boolean x = false;
			    
			    if (P[0] < S[0]){
			    	x = true;
			    } else if(P[1] < S[1]){
			    	x = true;
			    } else x = P[2] < S[2];
			    
			    if(x){
			    	newversion = version;
			    } else if(newversion != null) newversion = null;
			}
		});
	}
	
	private static int[] toReadable(String v) {
		return Arrays.stream(v.split("\\.")).mapToInt(Integer::parseInt).toArray();
	}
	
	public static boolean isUpdate() {
		return newversion != null;
	}
	
	@SuppressWarnings("deprecation")
	public static void sendAlert(ProxiedPlayer p) {
		if(newversion != null){
			p.sendMessage(prefix + "A new version of the plugin is available and can be downloaded");
			if(Config.getConfig().getBoolean("updater.staff-message.reminder-versions")) p.sendMessage("§bCurrent version : §6" + main.getDescription().getVersion() + "§3 / §bAvailable version : §6" + newversion);
			if(Config.getConfig().getBoolean("updater.staff-message.show-link")) p.sendMessage("§6§lLink : §ehttps://www.spigotmc.org/resources/106116");
		}
	}
}
