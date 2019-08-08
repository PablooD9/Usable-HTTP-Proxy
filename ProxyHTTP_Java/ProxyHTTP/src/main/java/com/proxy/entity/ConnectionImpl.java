package com.proxy.entity;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ConnectionImpl implements Connection
{
	private Socket socket;
	private ConnectionHandler handler;
	
	
	public ConnectionImpl(ConnectionHandler handler) {
		this.handler = handler;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
//		System.out.println( "=========================== Starting new request. THREAD: " + Thread.currentThread().getName() + " =============================");
		configureSocket( socket );
		handler.handleConnection( socket, null, false );
	}
	
	private void configureSocket(Socket socket) {
		try {
			socket.setSoTimeout( ProxyConfig.getInstance().getSocketTimeOut() );
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private Executor createExecutorThreadPool() {
		return Executors.newCachedThreadPool(new ThreadFactory() {
			private String prefix = "Thread-";
			private int numberOfThread = 0;
			
			private synchronized String getName() {
				return prefix + (++numberOfThread);
			}

			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, getName());
				t.setDaemon(true);
				return t;
			}
		});
	}

}
