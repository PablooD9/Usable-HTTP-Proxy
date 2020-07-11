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

/** Clase encargada de controlar todo el acceso a datos relacionados con los usuarios.
 * @author Pablo
 *
 */
@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final static String ROLE_USER = "ROLE_USER";
	
	public UserService() {
		super();
	}
	
	/** Constructor usado para las pruebas con Mockito.
	 * @param userRepositoryMock Mock de UserRepository.
	 */
	public UserService(UserRepository userRepositoryMock) {
		this.userRepository = userRepositoryMock;
	}

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
	
	/** Devuelve el usuario cuyo Email coincide con el pasado por parámetro.
	 * @param email Email del usuario.
	 * @return Usuario.
	 */
	public User findUserByEmail(String email) {
	    return userRepository.findByEmail(email);
	}
	
	/** Almacena un usuario en la base de datos. Antes de eso, se cifra su contraseña.
	 * @param user Usuario a añadir.
	 */
	public User saveUser(User user) {
		if (bCryptPasswordEncoder == null) {
			bCryptPasswordEncoder = new BCryptPasswordEncoder();
		}
	    user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
	    if (userRepository.findByEmail(user.getEmail()) == null) {
	    	return userRepository.save(user);
	    }
	    else {
	    	return null;
	    }
	}
	
	public String getEmailOfLoggedInUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username=null;
		if (auth != null && auth.getPrincipal() instanceof UserDetails) {
			username = ((UserDetails) auth.getPrincipal()).getUsername();
		} else if (auth != null && auth.getName() != "anonymousUser") {
			username = auth.getPrincipal().toString();
		}
		
		return username;
	}

	public User getUserLoggedIn() {
		String email = getEmailOfLoggedInUser();
		return findUserByEmail(email);
	}
	
	public boolean userIsLoggedIn() {
		return (getEmailOfLoggedInUser() != null) ? true : false;
	}
}
