package fr.baba.deltamanager.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.utils.ServersUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class ReconnectManager {
	static Map<ServerInfo, ArrayList<UUID>> queue = new HashMap<ServerInfo, ArrayList<UUID>>();
	static Map<ServerInfo, ScheduledTask> tasks = new HashMap<ServerInfo, ScheduledTask>();
	static Map<ServerInfo, Instant> instants = new HashMap<ServerInfo, Instant>();
	static Map<UUID, ServerInfo> players = new HashMap<UUID, ServerInfo>();
	static Boolean requirepermission = false;
	static Boolean chatmessages = false;
	static Boolean actionbarmessages = false;
	static Boolean logs = false;
	static Integer timeout = 180;
	static Integer period = 10;
	
	public static void init(){
		requirepermission = Config.getConfig().getBoolean("save-connexion.reconnect.require-permission");
		chatmessages = Config.getConfig().getBoolean("save-connexion.reconnect.messages.chat.enabled");
		actionbarmessages = Config.getConfig().getBoolean("save-connexion.reconnect.messages.action-bar.enabled");
		logs = Config.getConfig().getBoolean("save-connexion.reconnect.logs");
		
		if(Config.getConfig().getInt("save-connexion.reconnect.timeout") > 20) timeout = Config.getConfig().getInt("save-connexion.reconnect.timeout");
		if(Config.getConfig().getInt("save-connexion.reconnect.period") >= 2) period = Config.getConfig().getInt("save-connexion.reconnect.period");
	}

	public static void addPlayer(ServerInfo srv, ProxiedPlayer p){
		if(requirepermission && !p.hasPermission("deltamanager.reconnect")) return;
		if(containsPlayer(p)) return;
		
		if(Config.getConfig().getString("save-connexion.reconnect.servers-list.type").equalsIgnoreCase("blacklist")){
			for(String name : Config.getConfig().getStringList("save-connexion.reconnect.servers-list.list")) if(srv.getName().equalsIgnoreCase(name)) return;
		} else if(Config.getConfig().getString("save-connexion.reconnect.servers-list.type").equalsIgnoreCase("whitelist")){
			boolean pass = false;
			for(String name : Config.getConfig().getStringList("save-connexion.reconnect.servers-list.list")){
				if(srv.getName().equalsIgnoreCase(name)){
					pass = true;
					break;
				}
			}
			if(!pass) return;
		}
		
		if(logs) Main.getInstance().getLogger().log(Level.INFO, "[Reconnect] queued " + p.getName() + " to " + srv.getName());
		
		if(queue.get(srv) != null){
			ArrayList<UUID> list = queue.get(srv);
			list.add(p.getUniqueId());
			queue.put(srv, list);
		} else {
			queue.put(srv, new ArrayList<>(Arrays.asList(p.getUniqueId())));
			startTimer(srv);
		}
		
		players.put(p.getUniqueId(), srv);

		TextComponent msg = new TextComponent(Config.getConfig().getString("save-connexion.reconnect.messages.chat.added")
				.replace("%server%", srv.getName())
				.replace("&", "§"));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "serverqueue quit"));
		p.sendMessage(msg);
	}
	
	public static void removePlayer(ProxiedPlayer p, Boolean msg){
		if(!players.containsKey(p.getUniqueId())) return;
		if(logs) Main.getInstance().getLogger().log(Level.INFO, "[Reconnect] unqueued " + p.getName() + " from " + players.get(p.getUniqueId()).getName());
		ServerInfo srv = players.get(p.getUniqueId());
		ArrayList<UUID> list = queue.get(srv);
		list.remove(p.getUniqueId());
		players.remove(p.getUniqueId());
		if(list.isEmpty()){
			stopTimer(srv);
			return;
		}
		
		queue.put(srv, list);
		if(msg && p.isConnected()) p.sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("save-connexion.reconnect.messages.removed")));
	}
	
	public static boolean containsPlayer(ProxiedPlayer p){
		return players.containsKey(p.getUniqueId());
	}
	
	public static void checkPlayer(ProxiedPlayer p){
		if(p.getServer() == players.get(p.getUniqueId())) removePlayer(p, true);
	}
	
	public static void startTimer(ServerInfo srv){
		if(logs) Main.getInstance().getLogger().log(Level.INFO, "[Reconnect] starting timer for: " + srv.getName());
		instants.put(srv, Instant.now());
		tasks.put(srv, ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				if(ServersUtils.pingSRV(srv)){
					toggleActionBar(srv, "available", 15);
					ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), () -> {
						toggleActionBar(srv, null, 1);
						for(UUID id : queue.get(srv)){
							ProxyServer.getInstance().getPlayer(id).connect(srv);
							players.remove(id);
						}
						sendMessage(srv, "success");
						stopTimer(srv);
					}, 15, TimeUnit.SECONDS);
					tasks.get(srv).cancel();
				} else if(Duration.between(instants.get(srv), Instant.now()).getSeconds() > timeout){
					sendMessage(srv, "timeout");
					stopTimer(srv);
				} else sendMessage(srv, "unavailable");
			}

		}, 20, period, TimeUnit.SECONDS));
	}
	
	public static void stopTimer(ServerInfo srv){
		if(logs) Main.getInstance().getLogger().log(Level.INFO, "[Reconnect] Stoppping timer for: " + srv.getName());
		if(tasks.get(srv) != null) tasks.get(srv).cancel();
		if(bartimer != null) bartimer.cancel();
		tasks.remove(srv);
		bartimer = null;
		queue.remove(srv);
		instants.remove(srv);
	}
	
	public static int bartimeout = 0;
	public static ScheduledTask bartimer;
	public static void toggleActionBar(ServerInfo srv, String type, Integer timeout){
		bartimeout = timeout;
		if(bartimer != null){
			bartimer.cancel();
			bartimer = null;
			if(type == null) return;
		}
		
		bartimer = ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				if(queue.get(srv) == null){
					bartimer.cancel();
					bartimer = null;
				} else {
					BaseComponent[] msg = TextComponent.fromLegacyText(Config.getConfig().getString("save-connexion.reconnect.messages.action-bar." + type)
							.replace("%server%", srv.getName())
							.replace("%timeout%", Integer.toString(bartimeout))
							.replace("&", "§"));
					
					for(UUID id : queue.get(srv)) ProxyServer.getInstance().getPlayer(id).sendMessage(ChatMessageType.ACTION_BAR, msg);
					if(--bartimeout <= 0){
						bartimer.cancel();
						bartimer = null;
					}
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	private static void sendMessage(ServerInfo srv, String type) {
		if(chatmessages && Config.getConfig().getString("save-connexion.reconnect.messages.chat." + type) != null){
			BaseComponent[] msg = TextComponent.fromLegacyText(Config.getConfig().getString("save-connexion.reconnect.messages.chat." + type)
					.replace("%server%", srv.getName())
					.replace("&", "§"));
			for(UUID id : queue.get(srv)) ProxyServer.getInstance().getPlayer(id).sendMessage(ChatMessageType.ACTION_BAR, msg);
		}
		
		if(actionbarmessages && Config.getConfig().getString("save-connexion.reconnect.messages.action-bar." + type) != null){
			BaseComponent[] msg = TextComponent.fromLegacyText(Config.getConfig().getString("save-connexion.reconnect.messages.action-bar." + type)
					.replace("%server%", srv.getName())
					.replace("&", "§"));
			for(UUID id : queue.get(srv)) ProxyServer.getInstance().getPlayer(id).sendMessage(ChatMessageType.ACTION_BAR, msg);
		}
	}
}
