package com.proxy.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoConfiguration {

	@Autowired
	private Environment env;

	private final static String CONNECTION_STRING =
			"mongodb+srv://pablotfg:1234@proxytfgcluster-eszbe.mongodb.net";
	private final static String PROXY_DB_TEST = "ProxyDBTest";
	
	public void deleteAllDBTest() {
		if (env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equalsIgnoreCase("test")) {
			MongoClient client = MongoClients.create( CONNECTION_STRING );
			MongoDatabase dbTest = client.getDatabase(PROXY_DB_TEST);
			dbTest.drop();
		}
	}

}
