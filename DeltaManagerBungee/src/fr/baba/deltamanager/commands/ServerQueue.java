package fr.baba.deltamanager.commands;

import fr.baba.deltamanager.managers.ReconnectManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ServerQueue extends Command {

	public ServerQueue(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args){
		if(!(sender instanceof ProxiedPlayer)) return;
		
		ProxiedPlayer p = (ProxiedPlayer) sender;
		ReconnectManager.removePlayer(p, true);
	}
}
