package fr.baba.deltamanager.events;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.managers.MonitorManager;
import fr.baba.deltamanager.managers.UpdatesManager;
import fr.baba.deltamanager.utils.TimeUtils;
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
		if(!p.hasPermission("deltamanager.monitor.alerts")) return;
		
		if(!MonitorManager.getOffline().isEmpty()){
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				public void run() {
					String msg = Config.getConfig().getString("monitor.notify.on-join.message");
					
					String servers = null;
					if(msg.contains("%servers%")){
						for(String name : MonitorManager.getOffline()){
							String date = TimeUtils.format(Duration.between(MonitorManager.getInstant(name), Instant.now()));
							
							if(servers == null){
								servers = Config.getConfig().getString("monitor.notify.on-join.servers")
										.replace("%server%", name)
										.replace("%duration%", date);
							} else servers = servers + Config.getConfig().getString("monitor.notify.on-join.servers")
									.replace("%server%", name)
									.replace("%duration%", date);
						}
					}
					
					p.sendMessage(msg
							.replace("%servers%", servers)
							.replace("&", "§"));
				}
			}, 1, TimeUnit.SECONDS);
		}
		
		if(UpdatesManager.isUpdate() && Config.getConfig().getInt("updater.staff-message.cooldown") >= 0){
			ProxyServer.getInstance().getScheduler().schedule(Main.getInstance(), new Runnable() {
				public void run() {
					UpdatesManager.sendAlert(p);
				}
			}, Config.getConfig().getInt("updater.staff-message.cooldown"), TimeUnit.SECONDS);
		}
	}
}
