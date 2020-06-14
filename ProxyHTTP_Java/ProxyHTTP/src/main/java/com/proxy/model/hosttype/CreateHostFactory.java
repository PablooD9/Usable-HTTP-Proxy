package com.proxy.model.hosttype;

/** Interfaz que permite crear un Host a partir de un HostType.
 * @author Pablo
 *
 */
public interface CreateHostFactory {
	/** A partir de un HostType pasado por parámetro, se devuelve un Host.
	 * @param hostType HostType.
	 * @return Host de un tipo concreto.
	 */
	Host createHost(HostType hostType);
}
