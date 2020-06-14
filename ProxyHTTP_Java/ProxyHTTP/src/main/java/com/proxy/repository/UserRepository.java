package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.proxy.model.User;

/** Interfaz que representa el repositorio correspondiente a la entidad User.
 * @author Pablo
 *
 */
public interface UserRepository extends MongoRepository<User, String>{
	
	/**Busca y devuelve un usuario a partir de su Email.
	 * @param email Email del usuario.
	 * @return Usuario encontrado.
	 */
	User findByEmail(String email);
	
}
