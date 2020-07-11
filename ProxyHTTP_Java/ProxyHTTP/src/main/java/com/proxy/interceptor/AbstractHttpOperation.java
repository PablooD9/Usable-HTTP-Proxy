package com.proxy.interceptor;

import java.util.ArrayList;
import java.util.List;

import com.proxy.interceptor.httpOperation.request.Header;

/** Clase abstracta que implementa las operaciones comunes de las peticiones
 * y las respuestas HTTP.
 * @author Pablo
 *
 */
public abstract class AbstractHttpOperation implements IHttpOperation {

	private String host;
	private byte[] body;
	private List<Header> headers;
	private List<String> errorMessages;
	
	public AbstractHttpOperation() {
		headers = new ArrayList<>();
		errorMessages = new ArrayList<>();
	}
	
	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	public List<Header> getHeaders() {
		return headers;
	}
	
	@Override
	public Header getHeader(String name) {
		Header headerFound = headers.parallelStream().filter(header -> header.getKey().equalsIgnoreCase(name))
													 .findFirst()
													 .orElse(null);
		
		return headerFound;
	}

	@Override
	public void setHeader(String headerName, String newValue) {
		Header header;
		if (( header=getHeader(headerName)) != null) {
			header.setValues(newValue);
		}
	}

	@Override
	public byte[] getBody() {
		return body;
	}

	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}

	@Override
	public void addErrorMessage(String message) {
		if (message != null) {
			errorMessages.add(message);
		}
	}

	public String getAllMessages() {
		String allMessages = "";
		String newLine = "\r\n";
		for (String errorMessage : getErrorMessages()) {
			allMessages = allMessages.concat(errorMessage).concat(newLine);
		}
		return allMessages;
	}
}
