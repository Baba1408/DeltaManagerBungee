package fr.baba.deltamanager.utils;

import fr.baba.deltamanager.Config;

public class ConfigUtils {

	public static String getListtoString(String path) {
		String msg = null;
		for(String line : Config.getConfig().getStringList(path)){
			if(msg == null){
				msg = line;
			} else msg = msg + "\n" + line;
		}
		return msg;
	}
}
