package com.proxy.interceptor;

import java.net.Socket;

public interface Connection {
	public void runServer();
	public void configureSocket(Socket socket);
}
