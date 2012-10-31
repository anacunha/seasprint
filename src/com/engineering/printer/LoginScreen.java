package com.engineering.printer;

import java.io.IOException;

import com.trilead.ssh2.Connection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginScreen extends Activity implements OnClickListener{
	
	private static final String PREFS_NAME = "PrintToEngineeringPrefs";
	private static final String PASSWORD_FAIL = "";
	private static final String USER_FAIL = "";
	private static final String PASSWORD_KEY = "pw";
	private static final String USER_KEY = "user";
	private static final String HOST_FAIL = "eniac.seas.upenn.edu";
	private static final String HOST_KEY = "host";
	private static final String PORT_FAIL = "22";
	private static final String PORT_KEY = "port";
	private static final String SAVED = "saved";
	
	private static String user;
	private static String password;
	private static String host;
	private static int port;
	
	
	private static Connection connect = null;
	
	public static void resetConnection()
	{
		if(connect != null)
			connect.close();
		connect = null;
	}
	
	public static Connection getConnection()
	{
		if (LoginScreen.connect != null
				&& LoginScreen.connect.isAuthenticationComplete())
			return connect;
		else
			return null;
	}
	
	private class LoginTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pd;
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        //Set up progress dialog
	        pd = new ProgressDialog(LoginScreen.this);
			pd.setMessage("Connecting...");
			pd.setIndeterminate(true);
			pd.setCancelable(true);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener(){
				public void onCancel(DialogInterface dialog) {
					LoginTask.this.cancel(true);
					LoginScreen.this.finish();
				}
			});
			
			pd.show();
			
			//Read user input
            final EditText usertext = (EditText) findViewById(R.id.usertext);
            final EditText passtext = (EditText) findViewById(R.id.passtext);
            final EditText hostname = (EditText) findViewById(R.id.hostname);
            final EditText portname = (EditText) findViewById(R.id.portname);
            
        	user=usertext.getText().toString();
        	password =passtext.getText().toString();
			host = hostname.getText().toString();
        	if(portname.getText().toString().length()!=0)
        	{
        		String temp=portname.getText().toString();
        		port = Integer.parseInt(temp);
        		if(port>=65536 || port <=0)
        			port=22;
        	}
        	else
        		port = 22;
        	
	    }
	    @Override
	    protected void onPostExecute(Boolean result){
	    	super.onPostExecute(result);
	    	//Close progress dialog
	    	pd.dismiss();
	    	
	    	//Successful?
	    	if(result.equals(false))
	    	{
	    		new AlertDialog.Builder(LoginScreen.this).setMessage("Could not connect to server! Verify login information and network status.").create().show();
	    	}
	    	else
	    	{
	    		//Save preference settings
	    		final CheckBox checkBox = (CheckBox) findViewById(R.id.save);
	            final SharedPreferences pref1 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	        	final Editor edit = pref1.edit();
	        	if(checkBox.isChecked())
	        	{
	            	edit.putString(USER_KEY, user);
	            	edit.putString(PASSWORD_KEY, password);
	            	edit.putString(HOST_KEY, host);
	            	edit.putString(PORT_KEY, port+"");
	        	}
	        	edit.putBoolean(SAVED,checkBox.isChecked());
	        	edit.commit();
	        	
	        	//Return intent
	        	Intent intent = LoginScreen.this.getIntent();
	        	LoginScreen.this.setResult(RESULT_OK, intent);
	        	LoginScreen.this.finish();
	    	}
			
	    }
		@Override
		protected Boolean doInBackground(Void... params) {
    		try{
            	Log.e("user",user);
            	Log.e("host",host);
            	Log.e("port",((Integer)(port)).toString());
            	connect =(new ConnectionFactory()).MakeConnection(user, password,host, port);
            	
    		} catch(IOException e) {
    			return false;
    		}
			return true;
		}

	}
	
	public void onClick(View v) {
		new LoginTask().execute((Void)null);
	}
    	
	@Override
    public void onStop() {
		if(getConnection() == null) {
			Intent intent = this.getIntent();
			this.setResult(RESULT_CANCELED, intent);
		}
		super.onStop();
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        this.setTitle("Login to Proceed");
    }
    
    @Override
    public void onStart() {
        super.onStart();
        setContentView(R.layout.login);
    	final Button printbutton = (Button) findViewById(R.id.button);
    	printbutton.setOnClickListener(this);

        //Clean up connection
        resetConnection();

        //Read preference settings
        final SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        user = pref.getString(USER_KEY, USER_FAIL);
        password = pref.getString(PASSWORD_KEY, PASSWORD_FAIL);
        host = pref.getString(HOST_KEY, HOST_FAIL);
        String portStr = pref.getString(PORT_KEY, PORT_FAIL);
        
        final EditText usertext = (EditText) findViewById(R.id.usertext);
        final EditText passtext = (EditText) findViewById(R.id.passtext);
        final EditText hostname = (EditText) findViewById(R.id.hostname);
        final EditText portname = (EditText) findViewById(R.id.portname);
        
        usertext.setText(user);
        passtext.setText(password);
        hostname.setText(host);
        portname.setText(portStr);
        
        final CheckBox checkBox = (CheckBox) findViewById(R.id.save);
        checkBox.setChecked(pref.getBoolean(SAVED, false));
        

    }
}
