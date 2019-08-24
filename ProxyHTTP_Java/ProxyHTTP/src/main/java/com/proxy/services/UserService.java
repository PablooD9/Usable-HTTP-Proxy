package com.proxy.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.proxy.model.user.User;
import com.proxy.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final static String ROLE_USER = "ROLE_USER";
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		User user = userRepository.findByEmail(email);
	    if(user != null) {
	    	
	    	List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(ROLE_USER));
	    	
	        return buildUserForAuthentication(user, authorities);
	    } else {
	        throw new UsernameNotFoundException("Username not found in collection 'User'");
	    }
		
	}
	
	private UserDetails buildUserForAuthentication(User user, List<SimpleGrantedAuthority> authorities) {
	    return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}
	
	
	public User findUserByEmail(String email) {
	    return userRepository.findByEmail(email);
	}
	
	public void saveUser(User user) {
	    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    userRepository.save(user);
	}

}
