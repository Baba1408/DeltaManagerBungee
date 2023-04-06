package fr.baba.deltamanager.events;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.managers.ReconnectManager;
import fr.baba.deltamanager.utils.ConfigUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerKick implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void kick(ServerKickEvent e){
		//if(!Config.getConfig().getBoolean("save-connexion.enabled")) return;
		if(!TextComponent.toLegacyText(e.getKickReasonComponent()).toLowerCase().contains(Config.getConfig().getString("save-connexion.reason").toLowerCase())) return;
		
		ProxiedPlayer p = e.getPlayer();
		Boolean onlobby = false;
		
		for(String name : Config.getConfig().getStringList("save-connexion.list")){
			if(p.getServer().getInfo().getName().equalsIgnoreCase(name)){
				onlobby = true;
				break;
			}
		}
		
		if(!onlobby){
			for(String name : Config.getConfig().getStringList("save-connexion.list")){
				ServerInfo srv = ProxyServer.getInstance().getServers().get(name);
				
				Socket s = new Socket();
	        	
	        	try {
					s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 20);
					s.close();
		        	
					e.setCancelServer(srv);
		        	e.setCancelled(true);
		        	
		        	e.getPlayer().sendMessage(TextComponent.fromLegacyText(ConfigUtils.getListtoString("save-connexion.message").replace("&", "§")));
		        	
		        	ReconnectManager.addPlayer(e.getKickedFrom(), p);
		        	return;
				} catch(ConnectException er){
					ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(e.getPlayer().getName() + " want to go Lobby but is offline !"));
					//e.setKickReason("§cConnexion au Lobby impossible\n&cVeuillez réessayer plus tard");
				} catch (IOException er){
					ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(e.getPlayer().getName() + " want to go Lobby but is offline !"));
					//e.setKickReason("§cConnexion au Lobby impossible\n&cVeuillez réessayer plus tard");
				}
			}
		}
		
		e.setKickReasonComponent(TextComponent.fromLegacyText(ConfigUtils.getListtoString("save-connexion.kick-message").replace("&", "§")));
	}
}
