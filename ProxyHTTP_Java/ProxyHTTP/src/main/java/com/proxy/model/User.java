package com.proxy.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Clase que modela un Usuario en la aplicación. Los usuarios
 * tienen un Email (representa su username), una contraseña y un
 * nombre.
 * Dos usuarios distintos pueden tener el mismo nombre, pero nunca
 * el mismo Email. 
 * @author Pablo
 *
 */
@Document(collection = "User")
public class User {
	
	@Id
	private ObjectId id;
	
	private String email;
	private String password;
	private String name;
	
	public User() {}
	
	// For tests.
	public User(ObjectId id, String email, String password, String name) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
	}

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
