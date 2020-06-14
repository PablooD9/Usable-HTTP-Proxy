package com.proxy.model.functionality;

import com.proxy.interceptor.IHttpOperation;

/** Interfaz que define las operaciones necesarias para modificar una operación HTTP.
 * @author Pablo
 *
 */
public interface IProxyFunctionality {
	/** Método que modifica una operación HTTP.
	 * @param operation Operación HTTP a modificar.
	 * @return La operación HTTP modificada.
	 */
	IHttpOperation modify(IHttpOperation operation);
}
