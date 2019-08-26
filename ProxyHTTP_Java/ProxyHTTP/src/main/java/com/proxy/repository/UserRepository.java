package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.proxy.model.user.User;

public interface UserRepository extends MongoRepository<User, String>{
	
	User findByEmail(String email);
	
	@Query("{ 'email' : ?0, 'password' : ?1 }")
	User findByCreds(String email, String pass);
}
