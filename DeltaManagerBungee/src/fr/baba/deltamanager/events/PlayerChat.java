package fr.baba.deltamanager.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;

public class PlayerChat implements Listener {
	static ArrayList<String> Cstartswith = new ArrayList<>();
	static ArrayList<String> Cregex = new ArrayList<>();
	static ArrayList<String> Mregex = new ArrayList<>();
	static Map<UUID, Map<String, Integer>> vl = new HashMap<>();
	
	public static void init() {
		CommandSender cs = Main.getInstance().getProxy().getConsole();
		
		Cstartswith.clear();
		Cregex.clear();
		if(Config.getConfig().getBoolean("chat.command-blacklist.enabled")){
			cs.sendMessage(TextComponent.fromLegacyText("§2§l[Chat Module]§a Loading blacklisted commands..."));
			if(Config.getConfig().getStringList("chat.command-blacklist.startsWith") != null && !Config.getConfig().getStringList("chat.command-blacklist.startsWith").isEmpty()){
				for(String b : Config.getConfig().getStringList("chat.command-blacklist.startsWith")){
					Cstartswith.add(b);
				}
			} else cs.sendMessage(TextComponent.fromLegacyText("§4§l[Chat Module]§c command-blacklist > startsWith : has not been loaded because it does not exist in the configuration file or is empty"));
			
			if(Config.getConfig().getStringList("chat.command-blacklist.regex") != null && !Config.getConfig().getStringList("chat.command-blacklist.regex").isEmpty()){
				for(String b : Config.getConfig().getStringList("chat.command-blacklist.regex")){
					Cregex.add("\\b" + b + "\\b");
				}
			} else cs.sendMessage(TextComponent.fromLegacyText("§4§l[Chat Module]§c command-blacklist > regex : has not been loaded because it does not exist in the configuration file or is empty"));
		}
		
		Mregex.clear();
		if(Config.getConfig().getBoolean("chat.message-blacklist.enabled")){
			cs.sendMessage(TextComponent.fromLegacyText("§2§l[Chat Module]§a Loading blacklisted message..."));
			if(Config.getConfig().getStringList("chat.message-blacklist.blacklisted-messages") != null && !Config.getConfig().getStringList("chat.message-blacklist.blacklisted-messages").isEmpty()){
				for(String b : Config.getConfig().getStringList("chat.message-blacklist.blacklisted-messages")){
					Mregex.add("\\b" + b + "\\b");
				}
			} else cs.sendMessage(TextComponent.fromLegacyText("§4§l[Chat Module]§c message-blacklist > blacklisted-messages : has not been loaded because it does not exist in the configuration file or is empty"));
		}
	}
	
	public static void clear() {
		Cstartswith.clear();
		Cregex.clear();
		Mregex.clear();
		
		Main.getInstance().getProxy().getConsole().sendMessage(TextComponent.fromLegacyText("§4§l[Chat Module]§c Module disabled..."));
	}

	@EventHandler
	public void command(ChatEvent e){
		if(!(e.getSender() instanceof ProxiedPlayer)) return;
		ProxiedPlayer p = (ProxiedPlayer) e.getSender();
		
		//Command
		if(e.isCommand() || e.isProxyCommand()){
			if(p.hasPermission("deltamanager.chat.command-blacklist.bypass")) return;
			String msg = e.getMessage().toLowerCase().substring(1);
			//System.out.println("(" + msg + ") length: " + msg.length());
			
			for(String b : Cregex){
				Pattern pat = Pattern.compile(b, Pattern.CASE_INSENSITIVE);
				Matcher m = pat.matcher(msg);
				if(m.find()){
					punish(p, "command-blacklist", "Regex: " + m.group() + " (" + b + ")", e);
					return;
				}
			}
			
			String[] args = msg.split(" ");
			for(String b : Cstartswith){
				if(args[0].equalsIgnoreCase(b) || (b.contains(" ") && msg.startsWith(b))){
					punish(p, "command-blacklist", "startsWith: " + b, e);
					return;
				}
			}
		
		//Message
		} else {
			if(p.hasPermission("deltamanager.chat.message-blacklist.bypass")) return;
			String msg = e.getMessage();
			
			for(String b : Mregex){
				Pattern pat = Pattern.compile(b, Pattern.CASE_INSENSITIVE);
				Matcher m = pat.matcher(msg);
				if(m.find()){
					punish(p, "message-blacklist", "Regex: " + m.group() + " (" + b + ")", e);
					System.out.println("Le message est interdit.");
					return;
				}
			}
		}
	}
	
	//chat.*type*.+punishments
	@SuppressWarnings("deprecation")
	public static void punish(ProxiedPlayer p, String type, String flag, ChatEvent e) {
		UUID id = p.getUniqueId();
		
		String type2;
		if(type.contains("command")){
			type2 = "command";
		} else type2 = "message";
		
		Map<String, Integer> x = new HashMap<>();
		if(vl.get(id) == null){
			x.put(type2, 1);
			vl.put(id, x);
		} else if(vl.get(id).get(type2) != null) {
			x = vl.get(id);
			x.put(type2, vl.get(id).get(type2) + 1);
			vl.put(id, x);
		} else {
			x = vl.get(id);
			x.put(type2, 1);
			vl.put(id, x);
		}
		
		int vl2 = vl.get(p.getUniqueId()).get(type2);
		if(Config.getConfig().getStringList("chat." + type + ".punishments." + vl.get(p.getUniqueId())) == null || Config.getConfig().getStringList("chat." + type + ".punishments." + vl.get(p.getUniqueId())).isEmpty()){
			vl2 = 0;
			if(Config.getConfig().getStringList("chat." + type + ".punishments.0") == null || Config.getConfig().getStringList("chat." + type + ".punishments.0").isEmpty()){
				e.setCancelled(true);
			}
		}
		
		
		
		for(String sanction : Config.getConfig().getStringList("chat." + type + ".punishments." + vl2)){
			sanction = sanction
					.replace("&", "§")
					.replace("%player%", p.getName())
					.replace("%msg%", e.getMessage())
					.replace("%type%", type2)
					.replace("%flag%", flag)
					.replace("%vl%", vl.get(id).get(type2).toString());
			
			switch (sanction) {
			case "cancel":
				e.setCancelled(true);
				break;
			case "alert":
				TextComponent alert = new TextComponent(Config.getConfig().getString("chat." + type + ".alert-message.content")
						.replace("&", "§")
						.replace("%player%", p.getName())
						.replace("%msg%", e.getMessage())
						.replace("%type%", type2)
						.replace("%flag%", flag)
						.replace("%vl%", vl.get(id).get(type2).toString()));
				StringBuilder builder = new StringBuilder();
				final int size = Config.getConfig().getStringList("chat." + type + ".alert-message.hoverMessage").size();
				int i = 1;
				for(final String hover : Config.getConfig().getStringList("chat." + type + ".alert-message.hoverMessage")) {
					if(i == size){
						builder.append(hover
								.replace("&", "§")
								.replace("%player%", p.getName())
								.replace("%msg%", e.getMessage())
								.replace("%type%", type2)
								.replace("%flag%", flag)
								.replace("%vl%", vl.get(id).get(type2).toString()));
					} else {
						builder.append(hover
								.replace("&", "§")
								.replace("%player%", p.getName())
								.replace("%msg%", e.getMessage())
								.replace("%type%", type2)
								.replace("%flag%", flag)
								.replace("%vl%", vl.get(id).get(type2).toString()) + "\n");
					}
					++i;
				}
				
				alert.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(builder.toString())));
				
				for(ProxiedPlayer pl : ProxyServer.getInstance().getPlayers()){
					if(pl.hasPermission("deltamanager.chat." + type + ".alerts")){
						pl.sendMessage(alert);
					}
				}
				
				break;
			case "replace":
				int y = (int)(Math.random() * Config.getConfig().getStringList("chat.message-blacklist.replaced-messages").size());
				e.setMessage(Config.getConfig().getStringList("chat.message-blacklist.replaced-messages").get(y));
				break;
			default:
				if(sanction.contains("=")){
					String[] args = sanction.split("=");
					if(args.length < 2) continue;
					
					switch (args[0]) {
					case "cmd":
						PluginManager pm = BungeeCord.getInstance().getPluginManager();
			    		pm.dispatchCommand(BungeeCord.getInstance().getConsole(), args[1]);
						break;
					
					case "msg":
						p.sendMessage(TextComponent.fromLegacyText(args[1]));
						break;
					
					default:
						break;
					}
				}
				
				break;
			}
		}
	}
}




