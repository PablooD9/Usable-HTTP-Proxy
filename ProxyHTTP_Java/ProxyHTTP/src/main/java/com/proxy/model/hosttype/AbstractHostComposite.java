package com.proxy.model.hosttype;

/** Clase abstracta que define las operaciones necesarias para añadir distintos
 * HostType siguiendo el patrón de diseño Composite.
 * @author Pablo
 *
 */
public abstract class AbstractHostComposite extends Host {

	@Override
	public String getURLOfHostList() {
		throw new IllegalStateException("");
	}
	
	/** Añade un nuevo tipo de Host a la lista de HostType.
	 * @param hostType HostType a añadir.
	 */
	public abstract void addHostType(HostType hostType);
	
	/**
	 * Actualiza las listas de hosts que se encuentran en las colecciones de la base de datos.
	 */
	public abstract void updateMongoHostsList();
}
