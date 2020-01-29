package de.mannheim.binias.navgrabber.deka;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;


public class DekaGrabber {
	private static final String urlPrefix = "https://www.deka.de/privatkunden/fondsprofil?id=";
	
	private void grab(String isin) throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.deka.de")).build();
//		HttpRequest request = HttpRequest.newBuilder(URI.create(urlPrefix + isin.toUpperCase())).build();
		
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
	    System.out.println(response.statusCode());
	    if (response.statusCode() != 200) {
	    	System.out.println(response.body());
	    	return;
	    }
	    
		ArrayList<String> details = new ArrayList<String>(6);
		String stand = "";

		String body = response.body();
		int index = body.indexOf("Aktuelle Fondsdaten vom ") + 24;
		if (index <= 23) {
			index = 0;
			System.out.println("No Stand-Datum");
//			throw new RuntimeException();
		}
		stand = body.substring(index, index + 10);

		for (int i = 0; i < 2; i++) {
			index = body.indexOf("<div class=\"item-detail\">", index + 10) + 24;
			if (index <=23) {
				throw new RuntimeException();
			}
			int toIndex = body.indexOf('<', index + 1);
			String raw = body.substring(index, toIndex);
			System.out.println(raw);
		}
		
		System.out.println(stand);
		System.out.println(details.toString());
	}
	
	private void run() throws Exception {
		grab("DE0008474511");
	}
	
	public static void main(String args[]) {
		try {
			new DekaGrabber().run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
