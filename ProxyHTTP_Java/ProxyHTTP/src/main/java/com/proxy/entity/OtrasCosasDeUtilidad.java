package com.proxy.entity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;


public class OtrasCosasDeUtilidad {
	
	
	public static void main(String[] args) {
		HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .priority(1)
                .version(HttpClient.Version.HTTP_2)
				.build();
		
		var request = HttpRequest.newBuilder()  // GET request!
		        .uri(URI.create("http://ingenieriainformatica.uniovi.es/secretaria/impresos"))
		        .header("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36")
		        .GET()
		        .build();
		
		var response = client.sendAsync(request, BodyHandlers.ofString())
				.join();	
		
		HttpHeaders httpHeaders = response.headers();
		
		Map<String, List<String>> headers = httpHeaders.map();
//		headers.forEach((clave, valor) -> System.out.println( clave + "{ " + valor + "}" ));
            
		String protocol = response.version().toString().replace("_", ".").replaceFirst("\\.", "/");
//		System.out.println(protocol);
		
		int code = response.statusCode();
		System.err.println("Status code: "+code);
		
		String date = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC));
		// System.out.println(date);
		
		var crlf = "\r\n";
		var responseString = protocol + " " + code + " Sent. " + crlf;
		responseString += "Date: " + date + crlf;
		
		for (String key : headers.keySet()) {
			responseString += key + ":";
			for (String valor : headers.get(key)) {
				responseString += " " + valor;
			}
			responseString += crlf;
		}
		
		responseString += crlf; // espacio cabeceras y cuerpo

		responseString += response.body();
		
		System.out.println( responseString );
	}
	
	

	void manageConnection(Connection conn) {
		
		/*
		OutputStream outstream;
		try {
			outstream = clientConn.getClientSocket().getOutputStream();
			
			PrintWriter out = new PrintWriter(outstream);

			out.print( responseString );
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/
		
	}

	
}
