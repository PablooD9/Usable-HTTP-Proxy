package com.proxy.interceptor;

import java.net.InetSocketAddress;
import java.net.Socket;

public interface ConnectionHandler extends Runnable {
	void handleConnection(Socket socket, InetSocketAddress hostTarget, boolean sslConnection);
	void setConnectionHandler(ConnectionHandler connHandler);
}
