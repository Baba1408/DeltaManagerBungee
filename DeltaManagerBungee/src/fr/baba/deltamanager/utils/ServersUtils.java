package fr.baba.deltamanager.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class ServersUtils {

	public static boolean pingName(String name){
		ServerInfo srv = ProxyServer.getInstance().getServers().get(name);
		return srv != null ? pingSRV(srv) : false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean pingSRV(ServerInfo srv){
		try (Socket s = new Socket()) { //Socket s = new Socket();
			s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 500);
			return true; //s.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		return false;
	}
}
