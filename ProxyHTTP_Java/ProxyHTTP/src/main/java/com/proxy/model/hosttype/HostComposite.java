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

public class HostComposite extends AbstractHostComposite {

	private List<HostType> hostTypes = new ArrayList<>();
	
	private final static String PROXYDB = "ProxyDB";
	private final static String CONNECTION_STRING =
			"mongodb+srv://pablotfg:1234@proxytfgcluster-eszbe.mongodb.net";
	
	public void addHost(HostType hostType) {
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
				List<Host> hostList = loadHostList( hType );
				UserConfiguration.getInstance().getMaliciousHostsToScan().addAll(hostList);
				List<Document> hostDocuments = hostsListToDocument( hostList );
				if (hostDocuments.size() == 0)
					System.err.println("Empty: " + hType);
				else if (hostDocuments != null)
					collection.insertMany( hostDocuments );
				
				System.out.println("Collection " + hType.name() + " has " + collection.countDocuments() + " documents.");
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
	
	public List<Host> loadHostList(HostType hostType) {
		CreateHostFactory chf = new CreateHost();
		Host host = chf.createHost(hostType);
		
		List<Host> hostsList = host.loadHostsList();
		return hostsList;
	}

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
	
	private List<Document> hostsListToDocument( List<Host> hostsList ) {
		List<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < hostsList.size(); i++) {
		    documents.add(new Document(String.valueOf(i+1), hostsList.get( i ).getHostName()));
		}
		System.out.println(documents);
		return documents;
	}

	@Override
	public List<Host> obtainHostsList(HostType hType) {
		List<Host> hosts = loadHostList(hType);
		
		return hosts;
	}

}
