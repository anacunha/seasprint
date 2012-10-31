package com.engineering.printer;

import java.io.IOException;
import android.util.Log;

import com.trilead.ssh2.Connection;

public class ConnectionFactory {
	
    public ConnectionFactory() {
        
    }
    
    public Connection MakeConnection(String username, String password) throws IOException {
        Connection conn = new Connection("eniac.seas.upenn.edu");
        conn.connect();
        conn.authenticateWithPassword(username, password);
        return conn; 
    }
    
    public Connection MakeConnection(String username, String password, String host, int port) throws IOException {
    	
        Connection conn = new Connection(host,port);
        conn.connect();
        if (!conn.authenticateWithPassword(username, password)) {
            throw new IOException();
        }
        return conn;
    }
    
    public Connection MakeConnectionKey(String username, String key, String host, int port) throws IOException {
    	Log.d("Boot", "With Key!");
        Connection conn = new Connection(host,port);
        conn.connect();
        Log.d("Boot", ""+conn.authenticateWithPublicKey(username, key.toCharArray(),null));//conn.authenticateWithPassword(username, password);
        Log.d("Boot", "Yay!");
        return conn;
    }
    
}
