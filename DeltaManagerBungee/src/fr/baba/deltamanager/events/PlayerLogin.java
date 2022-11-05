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
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerLogin implements Listener {
	private Main main = Main.getInstance();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void login(LoginEvent e){
		PendingConnection p = e.getConnection();
		
		if(Config.getConfig().getBoolean("logs.login.enabled")){
			ProxyServer.getInstance().getConsole().sendMessage(Config.getConfig().getString("logs.login.message")
					.replace("%player%", p.getName())
					.replace("&", "§"));
		}
		
		if(Config.getConfig().getBoolean("webhook.login.enabled")){
			ProxyServer.getInstance().getScheduler().runAsync(main, () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
				ZonedDateTime now = ZonedDateTime.now();
				ZonedDateTime zone = now.withZoneSameInstant(ZoneId.of(Config.getConfig().getString("ZoneId")));
				
				Webhook webhook = new Webhook(Config.getConfig().getString("webhook.login.url"));
				
				EmbedObject embed = new Webhook.EmbedObject();
				embed.setTitle("Login successful")
					.setColor(Color.GREEN)
					.setAuthor(p.getName(), "", "https://mc-heads.net/avatar/" + p.getUniqueId() + "/128")
					.addField("UUID :", "" + p.getUniqueId(), false)
					.setFooter(dtf.format(zone), "");
				
				if(Config.getConfig().getBoolean("webhook.login.show-ip")) embed.addField("IP :", e.getConnection().getAddress().getAddress().getHostAddress(), false);
				
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
