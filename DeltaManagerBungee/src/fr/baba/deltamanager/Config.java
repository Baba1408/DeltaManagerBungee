package fr.baba.deltamanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config {
	static Main main = Main.getInstance();
	
	public static File configfile;
	public static Configuration configcfg;
	
	public static void setup() {
		if(!main.getDataFolder().exists()) main.getDataFolder().mkdir();
		
		setupConfig();
	}
	
	//Config
	public static void setupConfig() {
		String config = "config";
		configfile = new File(main.getDataFolder(), config + ".yml");
		
		if(!configfile.exists()) {
			//main.getDataFolder().mkdir();
				
			try {
				Files.copy(main.getResourceAsStream("config.yml"), // This will copy your default config.yml from the jar
					configfile.toPath());
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
		try {
			configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Configuration getConfig() {
		return configcfg;
	}
	
	public static void saveConfig() {
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configcfg, configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadConfig() {
		try {
			configcfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
