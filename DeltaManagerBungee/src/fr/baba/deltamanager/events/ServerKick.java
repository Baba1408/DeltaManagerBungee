package fr.baba.deltamanager.events;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.baba.deltamanager.Config;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerKick implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void kick(ServerKickEvent e){
		if(Config.getConfig().getBoolean("save-connexion.enabled")){
			if(!e.getKickReason().toLowerCase().contains(Config.getConfig().getString("save-connexion.reason").toLowerCase())) return;
			
			Boolean onlobby = false;
			
			for(ServerInfo srv : ProxyServer.getInstance().getServers().values()){
				for(String name : Config.getConfig().getStringList("save-connexion.list")){
					if(srv.getName().equalsIgnoreCase(name)){
						onlobby = true;
						break;
					}
				}
			}
			
			if(!onlobby){
				for(String name : Config.getConfig().getStringList("save-connexion.list")){
					ServerInfo srv = ProxyServer.getInstance().getServers().get(name);
					
					Socket s = new Socket();
		        	
		        	try {
						s.connect(new InetSocketAddress(srv.getAddress().getAddress(), srv.getAddress().getPort()), 20);
						s.close();
			        	
						e.setCancelServer(srv);
			        	e.setCancelled(true);
			        	
			        	String msg = null;
			        	for(String line : Config.getConfig().getStringList("save-connexion.message")){
							if(msg == null){
								msg = line;
							} else msg = msg + line;
						}

			        	e.getPlayer().sendMessage(msg);
			        	return;
					} catch(ConnectException er){
						ProxyServer.getInstance().getConsole().sendMessage(e.getPlayer().getName() + " want to go Lobby but is offline !");
						//e.setKickReason("§cConnexion au Lobby impossible\n&cVeuillez réessayer plus tard");
					} catch (IOException er){
						ProxyServer.getInstance().getConsole().sendMessage(e.getPlayer().getName() + " want to go Lobby but is offline !");
						//e.setKickReason("§cConnexion au Lobby impossible\n&cVeuillez réessayer plus tard");
					}
				}
				
				String msg = null;
				for(String line : Config.getConfig().getStringList("save-connexion.kick-message")){
					if(msg == null){
						msg = line;
					} else msg = msg + line;
				}
				
				e.setKickReason(msg);
			}
		}
	}
}
