package fr.baba.deltamanager.managers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.timers.Monitor;
import fr.baba.deltamanager.utils.PlayerUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class MonitorManager {
	static Map<String, Integer> status = new HashMap<>();
	static Main main = Main.getInstance();
	
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
			
			if(Config.getConfig().getBoolean("debug")){
				System.out.println("Address : " + srv.getAddress().getAddress().getHostAddress() + "\nPort : " + srv.getAddress().getPort());
			}
			
			Boolean error = false;
			Socket s = new Socket();
        	
        	try {
				s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 20);
				s.close();
	        	
				if(status.get(name) > 0 && Config.getConfig().getBoolean("monitor.notify.back-online.enabled")){
					status.put(name, 0);
					PlayerUtils.broadcast(Config.getConfig().getString("monitor.notify.back-online.message")
							.replace("%server%", name)
							.replace("&", "§"), "deltamanager.monitor.alerts");
				}
			} catch(ConnectException e){
				error = true;
			} catch(IOException e){
				error = true;
			} catch(Exception e){
				error = true;
			}
        	
        	if(error){
        		status.put(name, status.get(name) + 1);
				
				if(Config.getConfig().getBoolean("monitor.notify.offline.enabled") && status.get(name) == Config.getConfig().getInt("monitor.detected-offline")){
					PlayerUtils.broadcast(Config.getConfig().getString("monitor.notify.offline.message")
							.replace("%server%", name)
							.replace("&", "§"), "deltamanager.monitor.alerts");
					if(Config.getConfig().getBoolean("debug")) System.out.println("Server : " + name + " - " + status.get(name));
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
}
