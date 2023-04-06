package fr.baba.deltamanager.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class ServersUtils {

	@SuppressWarnings("deprecation")
	public static boolean pingName(String name){
		ServerInfo srv = ProxyServer.getInstance().getServers().get(name);
		Socket s = new Socket();
		
		try {
			s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 20);
			s.close();
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean pingSRV(ServerInfo srv){
		Socket s = new Socket();
		
		try {
			s.connect(new InetSocketAddress(srv.getAddress().getAddress().getHostAddress(), srv.getAddress().getPort()), 20);
			s.close();
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		return false;
	}
}
