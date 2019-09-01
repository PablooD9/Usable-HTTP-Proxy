package com.proxy.services;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.proxy.model.User;
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
	
	public User findUserByCreds(String email, String pass) {
		return userRepository.findByCreds(email, pass);
	}
	
	public void saveUser(User user) {
	    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    userRepository.save(user);
	}
	
	public String getEmailOfLoggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username;
		if (auth.getPrincipal() instanceof UserDetails) {
			username = ((UserDetails) auth.getPrincipal()).getUsername();
		} else {
			username = auth.getPrincipal().toString();
		}
		
		return username;
	}

	public User getUserLoggedIn() {
		String email = getEmailOfLoggedInUser();
		return findUserByEmail(email);
	}
	
	public boolean userIsLoggedIn() {
		return (getUserLoggedIn() != null) ? true : false;
	}
}
