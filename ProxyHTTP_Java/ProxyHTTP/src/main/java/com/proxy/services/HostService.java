package com.proxy.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proxy.parser.HostParser;

@Service
public class HostService {

	private final static String URL_hosts =
			"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn-social/hosts";
	
	private final static String CONNECTION_STRING =
			"mongodb+srv://pablo:$passUsUariO_@proxycluster-9ipe3.mongodb.net/test?retryWrites=true";
	
	private List<String> maliciousHosts;
	
	
	/** Método encargado de actualizar la lista de hosts maliciosos, usada para bloquearlos al usuario.
	 * Se encarga de lo siguiente:
	 * 1º - Actualiza la lista que ofrece la URL 
	 * 		"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn-social/hosts"
	 * 2º - Se conecta a la base de datos MongoDB
	 * 3º - Borra todos los documentos (que almacenan los hosts) de una colección
	 * 4º - Vuelve a cargar la lista descargada desde la URL anterior
	 * 5º - Cierra la conexión con la base de datos
	 */
	public void updateHostsList() {
		getHostsList();
//		parsedHostsList.forEach((host) -> System.out.println( host ));
		
		MongoClient client = createConnectionToMongoClient();
		MongoDatabase dbConnection = connectToDatabase(client);
		MongoCollection<Document> collection = getHostsCollection(dbConnection);
		
		deleteAllHosts(collection);
		
		List<Document> hostDocuments = hostsListToDocument();
		collection.insertMany( hostDocuments );
		
		System.out.println(collection.countDocuments());
		
		client.close();
	}
	
	/** Obtiene la lista de hosts maliciosos desde la URL:
	 * 		"https://raw.githubusercontent.com/StevenBlack/hosts/master/alternates/fakenews-gambling-porn-social/hosts"
	 * Los hosts obtenidos de la lista anterior son parseados e insertados en una lista.
	 */
	public void getHostsList(){
		maliciousHosts = new ArrayList<>();
		
		HostParser parser = new HostParser();
		URL url;
		URLConnection conn;
		BufferedReader reader = null;
		try {
			url = new URL( URL_hosts );
			conn = url.openConnection();
			
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (parser.parse( line ) != null)
					maliciousHosts.add( line );
			}
		} catch (MalformedURLException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Almacenar error en log
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Almacenar error en log
					e.printStackTrace();
				}
		}
	}
	
	public MongoClient createConnectionToMongoClient() {
        MongoClient client = MongoClients.create( CONNECTION_STRING );
        return client;
	}
	
	public MongoDatabase connectToDatabase(MongoClient client) {
		MongoDatabase db = client.getDatabase("Malicious_host");
		return db;
	}
	
	public MongoCollection<Document> getHostsCollection(MongoDatabase db){
		return db.getCollection("Hosts");
	}
	
	public List<Document> hostsListToDocument() {
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < maliciousHosts.size(); i++) {
		    documents.add(new Document(String.valueOf(i+1), maliciousHosts.get( i )));
		}
		
		return documents;
	}
	
	public void deleteAllHosts(MongoCollection<Document> collection) {
		collection.deleteMany(new Document());
	}
	
	
	
	// ==== Usando repositorios (muy lento...) ==== \\
	
	/*
	@Autowired
	private HostRepository hostRepository; 
	
	
	public void updateHostsList_2() {
		hostRepository.deleteAll();
		
		getHostsList();
		hostRepository.saveAll( hostsListToDocument_2() );
		
		System.out.println(hostRepository.count());
	}
	
	
	public List<Host> hostsListToDocument_2() {
		List<Host> documents = new ArrayList<>();
		for (int i = 0; i < maliciousHosts.size(); i++) {
		    documents.add(new Host( i,  maliciousHosts.get( i )));
		}
		
		return documents;
	}
	*/
	
}
