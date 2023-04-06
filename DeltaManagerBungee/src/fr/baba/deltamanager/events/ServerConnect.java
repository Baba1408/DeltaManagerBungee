package fr.baba.deltamanager.events;

import java.util.ArrayList;
import java.util.List;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.managers.ReconnectManager;
import fr.baba.deltamanager.utils.ServersUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnect implements Listener {
	List<String> players = new ArrayList<>();
	
	@EventHandler
    public void preLogin(PreLoginEvent e) {
		PendingConnection p = e.getConnection();
		String domain = p.getVirtualHost().getHostName().replace(".", "_");

		if(Config.getConfig().getBoolean("domain-redirection.domains." + domain + ".enabled")){
			players.add(p.getName());
			System.out.println(p.getName() + " put in queue for domain: " + domain);
		}
    }
	
	@EventHandler
    public void playerDisconnect(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		if(players.contains(p.getName())){
			players.remove(p.getName());
			//System.out.println(p.getName() + " removed from queue due to disconnection");
		}
	}
	
	@EventHandler
	public void serverConnect(ServerConnectEvent e){
		ProxiedPlayer p = e.getPlayer();
		//System.out.println("ServerConnectEvent" + p.getUniqueId());
		if(!players.contains(p.getName())) return;
		players.remove(p.getName());
		
		String domain = p.getPendingConnection().getVirtualHost().getHostName().replace(".", "_");
		String path = "domain-redirection.domains." + domain + ".";
		String type = Config.getConfig().getString(path + "type");
		List<String> servers = Config.getConfig().getStringList(path + "servers");
		
		//System.out.println(p.getName() + " connected with a specific domain: " + domain);
		
		switch (type) {
		case "random": {
			for(int i = 0; i < 15; i++){
				int r = (int) (Math.random() * (servers.size() - 0)) + 0;
				ServerInfo srv = ProxyServer.getInstance().getServers().get(servers.get(r));
				if(ServersUtils.pingSRV(srv)){
					e.setTarget(srv);
					return;
				}
			}
			
			p.sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("domain-redirection.error").replace("&", "§")));
			break;
		}
		
		case "availability-order": {
			for(String server : servers){
				ServerInfo srv = ProxyServer.getInstance().getServers().get(server);
				if(ServersUtils.pingSRV(srv)){
					e.setTarget(srv);
					return;
				} else if(Config.getConfig().getInt(path + "queue") >= 0){
					ReconnectManager.addPlayer(ProxyServer.getInstance().getServers().get(servers.get(Config.getConfig().getInt(path + "queue"))), p);
					return;
				}
			}
			
			p.sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("domain-redirection.error").replace("&", "§")));
			break;
		}

		default:
			break;
		}
	}
}
