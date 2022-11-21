package fr.baba.deltamanager.utils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerUtils {

	@SuppressWarnings("deprecation")
	public static void broadcast(String msg, String perm){
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()){
			
			if(p.hasPermission(perm)) p.sendMessage(msg);
		}
	}
}
