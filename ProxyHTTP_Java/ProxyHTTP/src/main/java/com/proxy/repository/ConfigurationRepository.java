package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.proxy.model.Configuration;

public interface ConfigurationRepository extends MongoRepository<Configuration, String>{

	
	
}
