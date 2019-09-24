package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.proxy.model.SecurityException;

public interface SecurityExceptionRepository extends MongoRepository<SecurityException, String>{

	SecurityException findByEmail(String email);
	
}
