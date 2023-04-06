package fr.baba.deltamanager.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.events.PlayerChat;
import fr.baba.deltamanager.events.PlayerDisconnect;
import fr.baba.deltamanager.events.PlayerLogin;
import fr.baba.deltamanager.events.PostLogin;
import fr.baba.deltamanager.events.ServerConnect;
import fr.baba.deltamanager.events.ServerKick;
import fr.baba.deltamanager.events.ServerSwitch;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;

public class ModulesManager {
	public static Map<Listener, ArrayList<String>> modules = new HashMap<>();
	public static ArrayList<String> sevents = new ArrayList<>();

	public static void init() {
		modules.put(new PlayerLogin(), new ArrayList<>(Arrays.asList("logs.login.enabled", "webhook.login.enabled")));
		modules.put(new ServerSwitch(), new ArrayList<>(Arrays.asList("logs.switch.enabled", "webhook.switch.enabled", "save-connexion.reconnect.enabled")));
		modules.put(new PlayerDisconnect(), new ArrayList<>(Arrays.asList("logs.disconnect.enabled", "webhook.disconnect.enabled", "save-connexion.reconnect.enabled")));
		
		modules.put(new PlayerChat(), new ArrayList<>(Arrays.asList("chat.enabled")));
		modules.put(new PostLogin(), new ArrayList<>(Arrays.asList("monitor.notify.on-join.enabled", "domain-redirection.enabled")));
		modules.put(new ServerKick(), new ArrayList<>(Arrays.asList("save-connexion.enabled")));
		modules.put(new ServerConnect(), new ArrayList<>(Arrays.asList("domain-redirection.enabled")));
	}
	
	public static void reload() {
		PluginManager pm = ProxyServer.getInstance().getPluginManager();
		specificsLoads();
		
		for(Entry<Listener, ArrayList<String>> entry : modules.entrySet()){
			String listener = entry.getKey().toString().substring(entry.getKey().toString().lastIndexOf('.') + 1);
			listener = listener.split("@")[0];
			
			for(String module : entry.getValue()){
				if(Config.getConfig().getBoolean(module)){
					if(!sevents.contains(listener)){
						System.out.println("Loading event : " + listener);
						pm.registerListener(Main.getInstance(), entry.getKey());
						sevents.add(listener);
					}
					break;
				} else if(entry.getValue().indexOf(module) >= entry.getValue().size()){
					if(sevents.contains(listener)){
						System.out.println("Unloading event : " + listener);
						pm.unregisterListener(entry.getKey());
						sevents.remove(listener);
					}
				}
			}
		}
	}
	
	public static void specificsLoads() {
		if(Config.getConfig().getBoolean("monitor.enabled")){
			MonitorManager.init();
			if(Config.getConfig().getBoolean("save-connexion.reconnect.enabled")) ReconnectManager.init();
		} else MonitorManager.clear();
		
		if(Config.getConfig().getBoolean("chat.enabled")){
			PlayerChat.init();
		} else PlayerChat.clear();
	}
}
