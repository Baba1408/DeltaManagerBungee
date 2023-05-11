package fr.baba.deltamanager.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Command;

public class BungeeTeleport extends Command {

	public BungeeTeleport(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender s, String[] args) {
		if(!(s instanceof ProxiedPlayer)){
			s.sendMessage(TextComponent.fromLegacyText("§cYou must be a player to execute this command!"));
			return;
		}
		
		if(!s.hasPermission("deltamanager.bungeeteleport")){
			s.sendMessage(TextComponent.fromLegacyText("§cYou do not have permission to use this command!"));
			return;
		}
		
		if(args.length < 1){
			s.sendMessage(TextComponent.fromLegacyText("§cPlease specify the player you wish to teleport to on their server."));
			return;
		}
		
		ProxiedPlayer t = ProxyServer.getInstance().getPlayer(args[0]);
		
		if(t == null){
			s.sendMessage(TextComponent.fromLegacyText("§cThe specified player is not online"));
			return;
		}
		
		ProxiedPlayer p = (ProxiedPlayer) s;
		
		if(p.getServer() == t.getServer()){
			s.sendMessage(TextComponent.fromLegacyText("§cYou are already connected to the same server."));
			return;
		}
		
		p.connect(t.getServer().getInfo(), Reason.COMMAND);
	}
}
