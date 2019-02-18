package main;

public class Startup {
	
	public static void main(String[] args) 
	{
		Proxy_Config configuracionProxy = new Proxy_Config( 8080 );
		Proxy_Main proxy = new Proxy_Main( configuracionProxy );
		
		proxy.run();
    }

}
