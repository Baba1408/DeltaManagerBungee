package fr.baba.deltamanager;

import java.util.ArrayList;

import fr.baba.deltamanager.commands.DeltaManager;
import fr.baba.deltamanager.events.PlayerDisconnect;
import fr.baba.deltamanager.events.PlayerLogin;
import fr.baba.deltamanager.events.PostLogin;
import fr.baba.deltamanager.events.ServerKick;
import fr.baba.deltamanager.events.ServerSwitch;
import fr.baba.deltamanager.managers.MonitorManager;
import fr.baba.deltamanager.managers.UpdatesManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Main extends Plugin {
	private static Main instance;
	static String prefix = "[BungeeManager]";
	
	static ArrayList<String> events = new ArrayList<>();
	
	@Override
	public void onLoad(){
		instance = this;
		Config.setup();
	}

	@Override
	public void onEnable(){
		PluginManager pm = getProxy().getPluginManager();
		
		//Channels
		getProxy().registerChannel("delta:manager");
		//getProxy().registerChannel("delta:managerupdate");
		
		//Commands
		pm.registerCommand(this, new DeltaManager("deltamanager"));
		pm.registerCommand(this, new DeltaManager("dm"));
		
		load();
	}
	
	public static Main getInstance(){
		return instance;
	}
	
	@SuppressWarnings("deprecation")
	public static void reload(){
		CommandSender cs = getInstance().getProxy().getConsole();
		cs.sendMessage(prefix + " Reload config...");
		Config.setup();
		load();
		cs.sendMessage(prefix + " The configuration has been successfully reloaded!");
	}
	
	@SuppressWarnings("deprecation")
	public static void load(){
		PluginManager pm = instance.getProxy().getPluginManager();
		CommandSender cs = instance.getProxy().getConsole();
		
		//Events
		if(Config.getConfig().getBoolean("save-connexion.enabled") && !events.contains("ServerKick")){
			pm.registerListener(instance, new ServerKick());
			events.add("ServerKick");
			cs.sendMessage("ServerKick event started");
		}
		
		if((Config.getConfig().getBoolean("logs.login.enabled") || Config.getConfig().getBoolean("webhook.login.enabled")) && !events.contains("PlayerLogin")){
			pm.registerListener(instance, new PlayerLogin());
			events.add("PlayerLogin");
			cs.sendMessage("PlayerLogin event started");
		}
		
		if((Config.getConfig().getBoolean("logs.switch.enabled") || Config.getConfig().getBoolean("webhook.switch.enabled")) && !events.contains("ServerSwitch")){
			pm.registerListener(instance, new ServerSwitch());
			events.add("ServerSwitch");
			cs.sendMessage("ServerSwitch event started");
		}
		
		if((Config.getConfig().getBoolean("logs.disconnect.enabled") || Config.getConfig().getBoolean("webhook.disconnect.enabled")
				|| Config.getConfig().getBoolean("logs.nomoreplayers.enabled") || Config.getConfig().getBoolean("logs.nomoreplayers.enabled")) && !events.contains("PlayerDisconnect")){
			pm.registerListener(instance, new PlayerDisconnect());
			events.add("PlayerDisconnect");
			cs.sendMessage("PlayerDisconnect event started");
		}
		
		if(Config.getConfig().getBoolean("monitor.enabled")){
			MonitorManager.init();
			
			if(Config.getConfig().getBoolean("monitor.notify.on-join.enabled") && !events.contains("PostLogin")){
				pm.registerListener(instance, new PostLogin());
				events.add("PostLogin");
				cs.sendMessage("PlayerDisconnect event started");
			}
		}
		
		UpdatesManager.check();
	}
	
	@Override
	public void onDisable(){
		getProxy().unregisterChannel("delta:manager");
	}
}
