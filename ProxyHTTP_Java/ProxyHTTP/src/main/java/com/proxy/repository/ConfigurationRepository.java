package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.proxy.model.Configuration;

/** Interfaz que representa el repositorio correspondiente a la entidad Configuration.
 * @author Pablo
 *
 */
public interface ConfigurationRepository extends MongoRepository<Configuration, String>{
	
}
