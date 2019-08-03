package com.proxy.entity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Connection extends Thread
{
	private ServerSocket sSocket;
	private final int socketTimeOut = 120000;
	private ConnectionHandler handler;
	
	public Connection(ConnectionHandler handler) {
		this.handler = handler;
	}
	
	public void createConnection(ServerSocket sSocket) {
		this.sSocket = sSocket;
	}
	
	public synchronized void startProxy() {
		setDaemon(true); // OJO!!
		start();
	}
	
	@Override
	public void run() {
		try {
			Socket socket;
			do {
				socket = sSocket.accept();
				System.out.println( "=========================== Starting new request =============================");
				configureSocket( socket );
				handler.handleConnection( socket, null, false );
				closeSocket( socket );
			} while (!sSocket.isClosed());
		} catch (IOException ioe) {
			if (!sSocket.isClosed()) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}
	}
	
	
	private void configureSocket(Socket socket) {
		try {
			socket.setSoTimeout( socketTimeOut );
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void closeSocket(Socket socket) {
		try {
			if (!socket.isClosed())
				socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
