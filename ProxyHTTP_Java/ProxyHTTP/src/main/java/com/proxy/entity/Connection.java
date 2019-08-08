package com.proxy.entity;

import java.net.Socket;

public interface Connection extends Runnable {
	void setSocket(Socket sSocket);
}
