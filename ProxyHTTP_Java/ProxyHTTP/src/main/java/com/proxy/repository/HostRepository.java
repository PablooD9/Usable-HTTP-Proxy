
package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface HostRepository<T, ID> extends MongoRepository<T, ID> {
	
}
