package fr.baba.deltamanager.events;

import java.util.concurrent.TimeUnit;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.managers.MonitorManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PostLogin implements Listener {

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPostLogin(PostLoginEvent e){
		ProxiedPlayer p = e.getPlayer();
		
		if(!MonitorManager.getOffline().isEmpty()){
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				public void run() {
					String msg = Config.getConfig().getString("monitor.notify.on-join.message");
					
					String servers = null;
					if(msg.contains("%servers%")){
						for(String name : MonitorManager.getOffline()){
							if(servers == null){
								servers = Config.getConfig().getString("monitor.notify.on-join.servers")
										.replace("%server%", name)
										.replace("\n", "\n");
							} else servers = servers + Config.getConfig().getString("monitor.notify.on-join.servers")
									.replace("%server%", name)
									.replace("\n", "\n");
						}
					}
					
					p.sendMessage(msg
							.replace("%servers%", servers)
							.replace("&", "§"));
				}
			}, 1, TimeUnit.SECONDS);
		}
	}
}
