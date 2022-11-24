package fr.baba.deltamanager.managers;

import java.awt.Color;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.Webhook;
import fr.baba.deltamanager.Webhook.EmbedObject;
import fr.baba.deltamanager.timers.Monitor;
import fr.baba.deltamanager.utils.PlayerUtils;
import fr.baba.deltamanager.utils.TimeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorManager {
	static Map<String, Integer> status = new HashMap<>();
	static Map<String, Instant> dates = new HashMap<>();
	private static Main main = Main.getInstance();
	
	public static void init(){
		if(Config.getConfig().getInt("monitor.interval") <= 1){
			System.out.println("monitor.interval must be greater than 1 second");
		}
		
		if(!status.isEmpty()) status.clear();
		
		if(Config.getConfig().getBoolean("monitor.check-all-servers")){
			for(ServerInfo srv : ProxyServer.getInstance().getServers().values()){
				String name = srv.getName();
				status.put(name, 0);
			}
		} else {
			for(String name : Config.getConfig().getStringList("monitor.server-list")){
				if(ProxyServer.getInstance().getServers().keySet().contains(name)){
					String nname = ProxyServer.getInstance().getServers().get(name).getName();
					status.put(nname, 0);
				} else System.out.println("The server " + name + " was not found");
			}
		}
		
		if(Config.getConfig().getBoolean("debug")) System.out.println(status.keySet());
		
		Monitor.trystart();
	}
	
	@SuppressWarnings("deprecation")
	public static void refresh(){
		for(String name : status.keySet()){
			ServerInfo srv = ProxyServer.getInstance().getServers().get(name);
			
			Boolean error = false;
			Socket s = new Socket();
        	
        	try {
				s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 20);
				s.close();
				
				if(status.get(name) < Config.getConfig().getInt("monitor.detected-offline")){
					status.put(name, 0);
					continue;
				}
				
				status.put(name, 0);
				Duration d = Duration.between(dates.get(name), Instant.now());
				
				if(Config.getConfig().getBoolean("monitor.notify.back-online.log.enabled")){
					System.out.println(Config.getConfig().getString("monitor.notify.back-online.log.log")
							.replace("%server%", name)
							.replace("%duration%", TimeUtils.format(d))
							.replace("&", "§"));
				}
	        	
				PlayerUtils.broadcast(Config.getConfig().getString("monitor.notify.back-online.staff-message")
						.replace("%server%", name)
						.replace("%duration%", TimeUtils.format(d))
						.replace("&", "§"), "deltamanager.monitor.alerts");
				
				if(Config.getConfig().getBoolean("monitor.notify.back-online.webhook.enabled")){
					ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
						String path = "monitor.notify.back-online.webhook.";
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
						ZonedDateTime now = ZonedDateTime.now();
						ZonedDateTime zone = now.withZoneSameInstant(ZoneId.of(Config.getConfig().getString("ZoneId")));
						
						Webhook webhook = new Webhook(Config.getConfig().getString(path + "url"));
						
						EmbedObject embed = new Webhook.EmbedObject();
						embed.setAuthor(Config.getConfig().getString(path + "author")
								.replace("%server%", name)
								.replace("%duration%", TimeUtils.format(d)), "", Config.getConfig().getString(path + "authoricon")
								.replace("%server%", name)
								.replace("%duration%", TimeUtils.format(d)))
							.setColor(Color.GREEN)
							.setTitle(Config.getConfig().getString(path + "title")
									.replace("%server%", name)
									.replace("%duration%", TimeUtils.format(d)))
							.setFooter(dtf.format(zone), "");
						
						webhook.addEmbed(embed);
						
						try {
							webhook.execute();
						} catch (IOException e) {
							e.printStackTrace();
							ProxyServer.getInstance().getConsole().sendMessage("[DeltaManagerBungee] Error when sending the Webhook");
						}
					});
				}
				
				dates.remove(name);
			} catch(ConnectException e){
				error = true;
			} catch(IOException e){
				error = true;
			} catch(Exception e){
				error = true;
			}
        	
        	if(error){
        		status.put(name, status.get(name) + 1);
        		
        		if(status.get(name) != Config.getConfig().getInt("monitor.detected-offline")) continue;
        		dates.put(name, Instant.now());
        		
        		if(Config.getConfig().getBoolean("monitor.notify.offline.log.enabled")){
        			System.out.println(Config.getConfig().getString("monitor.notify.offline.log.log")
							.replace("%server%", name)
							.replace("&", "§"));
        		}
        		
        		PlayerUtils.broadcast(Config.getConfig().getString("monitor.notify.offline.staff-message")
						.replace("%server%", name)
						.replace("&", "§"), "deltamanager.monitor.alerts");
				
				if(Config.getConfig().getBoolean("monitor.notify.offline.webhook.enabled")){
					ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
						String path = "monitor.notify.offline.webhook.";
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
						ZonedDateTime now = ZonedDateTime.now();
						ZonedDateTime zone = now.withZoneSameInstant(ZoneId.of(Config.getConfig().getString("ZoneId")));
						
						Webhook webhook = new Webhook(Config.getConfig().getString(path + "url"));
						
						EmbedObject embed = new Webhook.EmbedObject();
						embed.setAuthor(Config.getConfig().getString(path + "author")
								.replace("%server%", name), "", Config.getConfig().getString(path + "authoricon")
								.replace("%server%", name))
							.setTitle(Config.getConfig().getString(path + "title")
									.replace("%server%", name))
							.setColor(Color.red)
							.setFooter(dtf.format(zone), "");
						
						webhook.addEmbed(embed);
						
						try {
							webhook.execute();
						} catch (IOException e) {
							e.printStackTrace();
							ProxyServer.getInstance().getConsole().sendMessage("[DeltaManagerBungee] Error when sending the Webhook");
						}
					});
				}
        	}
		}
	}
	
	public static Map<String, Integer> getStatus(){
		return status;
	}
	
	public static ArrayList<String> getOffline(){
		ArrayList<String> o = new ArrayList<>();
		for(String name : status.keySet()) if(status.get(name) >= Config.getConfig().getInt("monitor.detected-offline")) o.add(name);
		return o;
	}
	
	public static Instant getInstant(String name){
		return dates.get(name);
	}
}
