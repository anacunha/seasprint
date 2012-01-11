package com.engineering.printer;

import java.io.IOException;
import java.util.StringTokenizer;

import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.trilead.ssh2.Connection;

public class ConnectionFactory {
	
    public ConnectionFactory() {
        
    }
    
    public Connection MakeConnection(String username, String password) throws IOException {
        Connection conn = new Connection("eniac.seas.upenn.edu");
        conn.connect();
        conn.authenticateWithPassword(username, password);
        CommandConnection cc = new CommandConnection(conn);
        /*Log.d("Boot", "test ssh-keygen");
        Log.d("Boot", cc.execWithReturn("ssh-keygen -p -P \'\' -N \'\' -f ~/.ssh/id_rsa"));
        cc.execWithReturn("cat /home1/r/rgormley/.ssh/id_rsa.pub >> /home1/r/rgormley/.ssh/authorized_keys");
        String str = cc.execWithReturn("cat /home1/r/rgormley/.ssh/id_rsa");
        Log.d("Boot", str);
        String key = str;//parsePrivateKey(str);
        Editor edit = EngineeringPrinter.prefs.edit();
        edit.putString("user", username);
        edit.putString("key", key);
        Log.d("Boot", edit.commit()+"");
        Log.d("Boot", EngineeringPrinter.prefs.getAll()+"");*/
        return conn; 
    }
    
    public Connection MakeConnection(String username, String password, String host, int port) throws IOException {
        Connection conn = new Connection(host,port);
        conn.connect();
        if (!conn.authenticateWithPassword(username, password)) {
            throw new IOException();
        }
        return conn;
        
//        CommandConnection cc = new CommandConnection(conn);
//        Log.d("Boot", "test ssh-keygen");
//        Log.d("Boot", cc.execWithReturn("rm ~/.ssh/seasprint_rsa"));
//        Log.d("Boot", cc.execWithReturn("rm ~/.ssh/seasprint_rsa.pub"));
//        Log.d("Boot", cc.execWithReturn("ssh-keygen -t rsa -N '' -f ~/.ssh/seasprint_rsa"));
//        cc.execWithReturn("cat ~/.ssh/seasprint_rsa.pub >> ~/.ssh/authorized_keys");
//        String str = cc.execWithReturn("cat ~/.ssh/seasprint_rsa");
//        Log.d("Boot", str);
//        //String key = str;//parsePrivateKey(str);
//        //Editor edit = EngineeringPrinter.prefs.edit();
//        //edit.putString("user", username);
//        //edit.putString("key", key);
//        //Log.d("Boot", edit.commit()+"");
//        //Log.d("Boot", EngineeringPrinter.prefs.getAll()+"");
//        conn.close();
//        Connection conn2 = new Connection(host,port);
//        conn2.connect();
//        boolean success = conn2.authenticateWithPublicKey(username, str.toCharArray(), null);
//        Log.d("Boot","Authentication was " + Boolean.valueOf(success));
//        return conn2;
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
