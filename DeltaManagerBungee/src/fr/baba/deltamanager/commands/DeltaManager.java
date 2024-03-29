package fr.baba.deltamanager.commands;

import java.util.ArrayList;
import java.util.Arrays;

import fr.baba.deltamanager.Config;
import fr.baba.deltamanager.Main;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class DeltaManager extends Command {

	public DeltaManager(String name) {
		super(name);
	}
	
	static ArrayList<String> commands = new ArrayList<>(Arrays.asList("help", "reload"));
	static ArrayList<String> help = new ArrayList<>(Arrays.asList("Help#Display the usage menu for the plugin",
			"Reload#Reload the plugin configuration"));

	@Override
	public void execute(CommandSender sender, String[] args){
		
		if(!sender.hasPermission("deltamanager.admin")){
			sender.sendMessage(TextComponent.fromLegacyText(Config.getConfig().getString("nopermission")
					.replace("&", "�")));
			return;
		}
		
		if(args.length <= 0 || args[0].equalsIgnoreCase("help") || !commands.contains(args[0].toLowerCase())){
			String line = "�8�m-----------------------------------";
			String msg = line + "\n";
			
			for(String w : help){
				String[] x = w.split("#");
				msg = msg + "�6" + x[0] + "�r �8�l��r �e" + x[1] + "�r\n";
			}
			
			msg = msg + line;
			
			sender.sendMessage(TextComponent.fromLegacyText(msg));
			
			return;
		} else if(args[0].equalsIgnoreCase("reload")){
			Main.reload();
			
			if(sender instanceof ProxiedPlayer){
				sender.sendMessage(TextComponent.fromLegacyText("The configuration has been successfully reloaded!"));
			}
		}
	}
}
