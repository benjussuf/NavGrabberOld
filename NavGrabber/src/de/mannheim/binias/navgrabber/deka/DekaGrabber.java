package de.mannheim.binias.navgrabber.deka;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DekaGrabber {
	private static final String uriPrefix = "https://www.deka.de/privatkunden/fondsprofil?id=";
	
	private void grab(String isin) throws Exception {
		String body = readWebPage(new URI(uriPrefix + isin).toURL());

		ArrayList<String> details = new ArrayList<String>(6);
		String stand = "";

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

	private String readWebPage(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("cc-proxy.dekager.dekabank.intern", 82)));
		conn.connect();

		try {
			System.out.printf("%d %s\n", conn.getResponseCode(), conn.getResponseMessage());
			if (conn.getResponseCode() != 200) {
				throw new IOException("Seite nicht gefunden");
			}
			
			String contentType = conn.getHeaderField("Content-Type");
			Charset charset = StandardCharsets.UTF_8;
			
			if (contentType != null) {
				int i = contentType.indexOf("charset");
				if (i >= 0) {
					while ((i < contentType.length()) && (contentType.charAt(i) != '=')) i++;
					while ((Character.isWhitespace(contentType.charAt(i))) && (i < contentType.length())) i++;
					int startIndex = i;
					while ((i < contentType.length()) && (contentType.charAt(i) != ';')) i++;
					charset = (i > startIndex) ? Charset.forName(contentType.substring(startIndex + 1, i).trim()) : StandardCharsets.UTF_8;
				} else {
					charset = StandardCharsets.UTF_8;
				}
			}
					
			StringBuilder sb = new StringBuilder(100 << 10);
			Reader r = new InputStreamReader(conn.getInputStream(), charset);
			char buf[] = new char[200 << 10];
			
			int n;
			while (true) {
				if ((n = r.read(buf)) <= 0) {
					break;
				}
				sb.append(buf, 0, n);
			}
			
			return sb.toString();
		} finally {
			conn.disconnect();
		}
	}
	
	private void downloadPacFile(URI target) throws Exception {
		URL url = new URL("http://deka-pacfile-webserver.dekager.dekabank.intern/cc-proxy.pac");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		
		CharBuffer cb = CharBuffer.allocate(4096);
		Reader r = new InputStreamReader(conn.getInputStream());
		Writer w = new OutputStreamWriter(Files.newOutputStream(Paths.get(target)));
		
		while (true) {
			int n = r.read(cb);
			if (n <= 0) {
				break;
			}
			w.write(cb.array());
			cb.flip();
		}
		
		w.flush();
		w.close();
		
		conn.disconnect();
		System.out.println("Done!");
	}
	

//	private void grab11(String isin) throws Exception {
//		HttpClient client = HttpClient.newHttpClient();
//		HttpRequest request = HttpRequest.newBuilder(URI.create("https://www.deka.de")).build();
////		HttpRequest request = HttpRequest.newBuilder(URI.create(urlPrefix + isin.toUpperCase())).build();
//		
//		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//	    System.out.println(response.statusCode());
//	    if (response.statusCode() != 200) {
//	    	System.out.println(response.body());
//	    	return;
//	    }
//	    
//		ArrayList<String> details = new ArrayList<String>(6);
//		String stand = "";
//
//		String body = response.body();
//		int index = body.indexOf("Aktuelle Fondsdaten vom ") + 24;
//		if (index <= 23) {
//			index = 0;
//			System.out.println("No Stand-Datum");
////			throw new RuntimeException();
//		}
//		stand = body.substring(index, index + 10);
//
//		for (int i = 0; i < 2; i++) {
//			index = body.indexOf("<div class=\"item-detail\">", index + 10) + 24;
//			if (index <=23) {
//				throw new RuntimeException();
//			}
//			int toIndex = body.indexOf('<', index + 1);
//			String raw = body.substring(index, toIndex);
//			System.out.println(raw);
//		}
//		
//		System.out.println(stand);
//		System.out.println(details.toString());
//	}
	
	private void run() throws Exception {
		grab("DE0008474511");
	}
	
	public static void main(String args[]) {
		try {
			DekaGrabber g = new DekaGrabber();
//			g.downloadPacFile(new URI("file:///c:/temp/pac.txt"));
			g.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
