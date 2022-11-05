package fr.baba.deltamanager.events;

import java.awt.Color;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import fr.baba.deltamanager.Webhook;
import fr.baba.deltamanager.Webhook.EmbedObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitch implements Listener {
	private Main main = Main.getInstance();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void serverswitch(ServerSwitchEvent e){
		if(e.getFrom() == null) return;
		ProxiedPlayer p = e.getPlayer();
		
		if(Config.getConfig().getBoolean("logs.switch.enabled")){
			ProxyServer.getInstance().getConsole().sendMessage(Config.getConfig().getString("logs.switch.message")
					.replace("%player%", p.getName())
					.replace("%from%", e.getFrom().getName())
					.replace("%to%", p.getServer().getInfo().getName())
					.replace("&", "§"));
		}
		
		if(Config.getConfig().getBoolean("webhook.switch.enabled")){
			ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime zone = now.withZoneSameInstant(ZoneId.of(Config.getConfig().getString("ZoneId")));
				
				Webhook webhook = new Webhook(Config.getConfig().getString("webhook.switch.url"));
				
				EmbedObject embed = new Webhook.EmbedObject();
				embed.setTitle("Server Switch")
					.setColor(Color.CYAN)
					.setAuthor(p.getName(), "", "https://mc-heads.net/avatar/" + p.getName())
					.addField("UUID :", "" + p.getUniqueId(), false)
					.addField("From :", e.getFrom().getName(), true)
					.addField("To :", e.getPlayer().getServer().getInfo().getName(), true)
					.setFooter(dtf.format(zone), "");
				
				webhook.addEmbed(embed);
				
				try {
					webhook.execute();
				} catch (IOException e1) {
					e1.printStackTrace();
					ProxyServer.getInstance().getConsole().sendMessage("[DeltaManagerBungee] Error when sending the Webhook");
				}
			});
		}
	}
}
