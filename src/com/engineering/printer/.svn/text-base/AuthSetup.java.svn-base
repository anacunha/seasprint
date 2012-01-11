package com.engineering.printer;

import java.io.IOException;

import android.util.Log;

import com.trilead.ssh2.Connection;

public class AuthSetup {
	
	String mUsername;
	String mPassword;
	String mHost;
	int mPort;
	
	public AuthSetup(String username, String pw, String host, int port) {
		mUsername = username;
		mPassword = pw;
		mHost = host;
		mPort = port;
	}

	public String keyGen() {
		try {
			Connection conn =(new ConnectionFactory()).MakeConnection(mUsername, mPassword,mHost, mPort);
			CommandConnection cc = new CommandConnection(conn);
			Log.d("Boot", "test ssh-keygen");
			Log.d("Boot", cc.execWithReturn("rm ~/.ssh/seasprint_rsa"));
			Log.d("Boot", cc.execWithReturn("rm ~/.ssh/seasprint_rsa.pub"));
			Log.d("Boot", cc.execWithReturn("ssh-keygen -t rsa -N '' -f ~/.ssh/seasprint_rsa"));
			cc.execWithReturn("cat ~/.ssh/seasprint_rsa.pub >> ~/.ssh/authorized_keys");
			String str = cc.execWithReturn("cat ~/.ssh/seasprint_rsa");
			return str;
		}
		catch (IOException e) {
			throw new RuntimeException(e.fillInStackTrace());
		}
	}
	
}
