package fr.baba.deltamanager.events;

import java.awt.Color;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.managers.ReconnectManager;
import fr.baba.deltamanager.utils.Webhook;
import fr.baba.deltamanager.utils.Webhook.EmbedObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerDisconnect implements Listener {
	private Main main = Main.getInstance();

	@EventHandler
	public void disconnect(PlayerDisconnectEvent e){
		ProxiedPlayer p = e.getPlayer();
		
		String lsrv;
		if(p.getServer() == null){
			lsrv = "Unknown";
		} else lsrv = p.getServer().getInfo().getName();
		
		if(Config.getConfig().getBoolean("logs.disconnect.enabled")){
			ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("logs.disconnect.message")
					.replace("%player%", p.getName())
					.replace("%server%", lsrv)
					.replace("&", "�")));
		}
		
		if(Config.getConfig().getBoolean("webhook.disconnect.enabled")){
			ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime zone = now.withZoneSameInstant(ZoneId.of(Config.getConfig().getString("ZoneId")));
				
				Webhook webhook = new Webhook(Config.getConfig().getString("webhook.disconnect.url"));
				
				EmbedObject embed = new Webhook.EmbedObject();
				embed.setTitle("Network disconnect")
					.setColor(Color.RED)
					.setAuthor(p.getName(), "", "https://mc-heads.net/avatar/" + p.getUniqueId() + "/128")
					.addField("UUID :", "" + p.getUniqueId(), false)
					.addField("Last server :", lsrv, true)
					.setFooter(dtf.format(zone), "");
				
				webhook.addEmbed(embed);
				
				try {
					webhook.execute();
				} catch (IOException e1) {
					e1.printStackTrace();
					ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("[DeltaManagerBungee] Error when sending the Webhook"));
				}
			});
		}
		
		ReconnectManager.removePlayer(p, false);
		
		if(ProxyServer.getInstance().getPlayers().size() > 1) return;
		
		ProxyServer.getInstance().getScheduler().schedule(main, () -> {
			if(ProxyServer.getInstance().getPlayers().contains(p)){
				if(ProxyServer.getInstance().getPlayers().size() > 1) return;
			} else if(ProxyServer.getInstance().getPlayers().size() > 0) return;
			
			if(Config.getConfig().getBoolean("logs.nomoreplayers.enabled")){
				ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("logs.nomoreplayers.message")
						.replace("%player%", p.getName())
						.replace("%server%", lsrv)
						.replace("&", "�")));
			}
			
			if(Config.getConfig().getBoolean("webhook.nomoreplayers.enabled")){
				Webhook webhook = new Webhook(Config.getConfig().getString("webhook.nomoreplayers.url"));
				webhook.setContent(Config.getConfig().getString("webhook.nomoreplayers.content"));
				
				try {
					webhook.execute();
				} catch (IOException e1) {
					e1.printStackTrace();
					ProxyServer.getInstance().getConsole().sendMessage(TextComponent.fromLegacyText("[DeltaManagerBungee] Error while sending the Webhook " + e.toString()));
				}
			}
		}, 2, TimeUnit.SECONDS);
	}
}
