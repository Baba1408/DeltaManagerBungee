package fr.baba.deltamanager;

import java.util.ArrayList;
import java.util.Map.Entry;

import fr.baba.deltamanager.commands.BungeeTeleport;
import fr.baba.deltamanager.commands.DeltaManager;
import fr.baba.deltamanager.commands.ServerQueue;
import fr.baba.deltamanager.managers.ModulesManager;
import fr.baba.deltamanager.managers.MonitorManager;
import fr.baba.deltamanager.managers.UpdatesManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Main extends Plugin {
	private static Main instance;
	public static String prefix = "[DeltaManagerBungee]";
	public static Boolean isStarting = true;
	public static Boolean debug = false;
	public static Boolean cache = false;
	
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
		
		if(Config.getCache().getBoolean("save-in-cache")) cache = true;
		
		//Commands
		pm.registerCommand(this, new DeltaManager("deltamanager"));
		pm.registerCommand(this, new DeltaManager("dm"));
		pm.registerCommand(this, new ServerQueue("serverqueue"));
		pm.registerCommand(this, new BungeeTeleport("bungeeteleport"));
		pm.registerCommand(this, new BungeeTeleport("btp"));
		
		ModulesManager.init();
		
		load();
		UpdatesManager.check();
		if(Config.cachefile.delete()){
			getLogger().info("The cache file has been deleted");
		} else getLogger().warning("The cache file could not be deleted");
		isStarting = false;
		cache = false;
	}
	
	public static Main getInstance(){
		return instance;
	}
	
	public static void reload(){
		CommandSender cs = getInstance().getProxy().getConsole();
		cs.sendMessage(TextComponent.fromLegacyText(prefix + " Reload config..."));
		Config.setup();
		load();
		if(Config.getConfig().getBoolean("debug")) debug = true;
		cs.sendMessage(TextComponent.fromLegacyText(prefix + " The configuration has been successfully reloaded!"));
	}
	
	public static void load(){
		CommandSender cs = instance.getProxy().getConsole();
		
		if(isStarting){
			cs.sendMessage(TextComponent.fromLegacyText("§2" + prefix + "§a Loading..."));
		} else cs.sendMessage(TextComponent.fromLegacyText("§2" + prefix + " §a Reloading..."));
		
		ModulesManager.reload();
	}
	
	@Override
	public void onDisable(){
		getProxy().unregisterChannel("delta:manager");
		
		if(Config.getConfig().getBoolean("save-in-cache")){
			Config.setupCache();
			
			for(Entry<String, Integer> e : MonitorManager.getStatus().entrySet()){
				if(e.getValue() > 0){
					Config.getCache().set("monitor." + e.getKey(), e.getValue());
				}
			}
			
			Config.saveCache();
			getLogger().info("All data have been saved in the cache file.");
		}
	}
}
