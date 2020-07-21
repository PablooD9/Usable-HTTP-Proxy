package com.proxy.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.proxy.model.SecurityException;

/** Interfaz que representa el repositorio correspondiente a la entidad SecurityException.
 * @author Pablo
 *
 */
public interface SecurityExceptionRepository extends MongoRepository<SecurityException, String>{
	/** Busca una SecurityException a partir del Email del usuario que la ha aplicado.
	 * @param email Email del usuario.
	 * @return La SecurityException encontrada.
	 */
	SecurityException findByEmail(String email);
}
