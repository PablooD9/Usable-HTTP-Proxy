package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.proxy.model.user.User;

public interface UserRepository extends MongoRepository<User, String>{
	
	User findByEmail(String email);
	
}
