package com.proxy.model.hosttype;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proxy.model.UserConfiguration;

/** Clase que contiene una lista de HostType y a partir de la cual
 * se irán cargando en la aplicación los distintos tipos de hosts a partir
 * de las correspondientes URL.
 * @author Pablo
 *
 */
public class HostComposite extends AbstractHostComposite {

	private List<HostType> hostTypes = new ArrayList<>();
	
	private final static String PROXYDB = "ProxyDB";
	private final static String CONNECTION_STRING =
			"mongodb+srv://pablotfg:1234@proxytfgcluster-eszbe.mongodb.net";
	
	public void addHostType(HostType hostType) {
		hostTypes.add( hostType );
	}
	
	public void updateMongoHostsList() {
		MongoClient client = createConnectionToMongoClient();
		MongoDatabase dbConnection = connectToDatabase(client);
		UserConfiguration.getInstance().setMaliciousHostsToScan(new ArrayList<>());
		
		Thread[] threads = new Thread[ hostTypes.size() ];
		
		AtomicInteger counter = new AtomicInteger();
		hostTypes.forEach(hType -> { threads[counter.get()] = new Thread() {
			public void run() {
				MongoCollection<Document> collection = getHostsCollection(dbConnection, hType.name());
				deleteAllHostsFromCollection(collection);
				List<Host> hostList = loadHostListFromHostType( hType );
				UserConfiguration.getInstance().getMaliciousHostsToScan().addAll(hostList);
				List<Document> hostDocuments = hostsListToDocument( hostList );
				if (hostDocuments != null) {
					collection.insertMany( hostDocuments );
				}
			}};
			threads[counter.getAndIncrement()].start();
		});
		
		for (int i = 0; i < threads.length; i++) {
		    try {
		       threads[i].join(); // We wait for each of the threads to finish their task
		    } catch (InterruptedException ignore) {}
		}
		client.close();
	}
	
	/** A partir de un tipo de Host, se devuelve una lista de Hosts.
	 * @param hostType HostType.
	 * @return Lista de Host.
	 */
	public List<Host> loadHostListFromHostType(HostType hostType) {
		CreateHostFactory chf = new CreateHost();
		Host host = chf.createHost(hostType);
		
		List<Host> hostsList = host.loadHostsList();
		return hostsList;
	}

	/** Obtiene una colección de la base de datos.
	 * @param db Base de datos MongoDB.
	 * @param collectionName Nombre de la colección.
	 * @return Colección de la Base de datos.
	 */
	private MongoCollection<Document> getHostsCollection(MongoDatabase db, String collectionName){
		return db.getCollection( collectionName );
	}
	
	private void deleteAllHostsFromCollection(MongoCollection<Document> collection) {
		collection.deleteMany(new Document());
	}
	
	private MongoClient createConnectionToMongoClient() {
        MongoClient client = MongoClients.create( CONNECTION_STRING );
        return client;
	}
	
	private MongoDatabase connectToDatabase(MongoClient client) {
		MongoDatabase db = client.getDatabase( PROXYDB );
		return db;
	}
	
	/** Método que convierte en Documentos de MongoDB una lista de hosts.
	 * @param hostsList Lista de Host que será convertida en Lista de Documentos.
	 * @return Lista de Documentos.
	 */
	private List<Document> hostsListToDocument( List<Host> hostsList ) {
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < hostsList.size(); i++) {
		    documents.add(new Document(String.valueOf(i+1), hostsList.get( i ).getHostName()));
		}
		return documents;
	}

}
