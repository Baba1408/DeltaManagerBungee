package fr.baba.deltamanager.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

	public static Boolean verify(String url){
		try {
			@SuppressWarnings("unused")
			URL urlc = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		
		return true;
	}
}
