package com.engineering.printer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.ManagerFactoryParameters;

import com.trilead.ssh2.Connection;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class EngineeringPrinter extends Activity {
	
	public static final String PREFS_NAME = "PrintToEngineeringPrefs";
	public static final String PASSWORD_FAIL = "";
	public static final String USER_FAIL = "";
	public static final String PASSWORD_KEY = "pw";
	public static final String USER_KEY = "user";
	public static final String HOST_FAIL = "eniac.seas.upenn.edu";
	public static final String HOST_KEY = "host";
	public static final String PORT_FAIL = "22";
	public static final String PORT_KEY = "port";
	public static final String KEY_KEY = "privatekey";
	public static final String SAVED = "saved";
	public static String user;
	public static String password;
	public static String host;
	public static String privatekey;
	public static int port;
	public static Connection connect = null;
	public static FileUpload.Future upload;
	public static ErrorCallback eb;
	public static boolean Microsoft;
	public static String type;
	
	private static final char KEY = 1337;
	
	public String encryptPassword(String pw) {
		char letters[] = pw.toCharArray();
		for(int i = 0; i < letters.length; i++) letters[i] = (char)(KEY ^ pw.charAt(letters.length - i - 1));
		return new String(letters);
	}
	
	public String decryptPassword(String pw) {
		char letters[] = pw.toCharArray();
		for(int i = 0; i < letters.length; i++) letters[i] = (char)(KEY ^ pw.charAt(letters.length - i - 1));
		return new String(letters);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//      InputStream is = null;
//      try {
//          is = getContentResolver().openInputStream(getIntent().getData());
//      }
//      catch  (FileNotFoundException fnf){
//          Log.e("Connection","File Not Found");
//      }
//      Document.load(is);
//      Document.setDescriptor(getIntent().getData());
//        Microsoft = MicrosoftSink.Filter(getIntent().getType());
//        type = getIntent().getType();
//        
        eb = new ErrorCallback(this);
        
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onStart() {
        super.onStart();
        setContentView(R.layout.main);
        
    	TextView printing = (TextView) findViewById(R.id.printing);
    	TextView microsoft = (TextView) findViewById(R.id.microsoft);

    	printing.setText(Document.descriptor);
    	
    	microsoft.setText(type + " " +(Microsoft  ? "Microsoft" : "Not Microsoft") );
    	//connect.close();
        connect=null;
        /*try {
            InputStream is = getContentResolver().openInputStream(getIntent().getData());
            ConnectionFactory cf = new ConnectionFactory();
            Connection c = cf.MakeConnection();
            FileUpload fu = new FileUpload(c);
            String tempfile = fu.startUpload(is);
            PrintCaller pc = new PrintCaller(new CommandConnection(c));
            pc.printFile(tempfile, "grafix", 1, false);
        
        }
        catch (IOException ioe){
        	//ALERT DIALOG
            Log.e("Connection", "Failed to connect or send");
        }*/
        
        final SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (pref.contains(PASSWORD_KEY)) {
        	pref.edit().remove(PASSWORD_KEY).commit();
        }
        user = pref.getString(USER_KEY, USER_FAIL);
        //password = decryptPassword(pref.getString(PASSWORD_KEY, PASSWORD_FAIL));
        host = pref.getString(HOST_KEY, HOST_FAIL);
        String portStr = pref.getString(PORT_KEY, PORT_FAIL);
        
        //USERNAME TEXT BOX
        final EditText usertext = (EditText) findViewById(R.id.usertext);
        
        //PASSWORD TEXT BOX
        final EditText passtext = (EditText) findViewById(R.id.passtext);
        
        //HOST NAME TEXT BOX
        final EditText hostname = (EditText) findViewById(R.id.hostname);
        
        //Port TEXT BOX
        final EditText portname = (EditText) findViewById(R.id.portname);
        
        usertext.setText(user);
        passtext.setText("");
        hostname.setText(host);
        portname.setText(portStr);
        
        final CheckBox checkBox = (CheckBox) findViewById(R.id.save);
        checkBox.setChecked(pref.getBoolean(SAVED, false));
        

        
    	final Button printbutton = (Button) findViewById(R.id.button);
    	printbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(connect == null) {
            		// Perform action on key press
            		try {
	                	user=usertext.getText().toString();
	                	password =passtext.getText().toString();
            			host = hostname.getText().toString();
	                	if(portname.getText().toString().length()!=0)
	                	{
	                		String temp=portname.getText().toString();
	                		port = Integer.parseInt(temp);
	                	}
	                	else
	                		port = 22;
	                			             
	                	ConnectionFactory cf = new ConnectionFactory();
	                	Log.e("user",user);
	                	Log.e("host",host);
	                	Log.e("port",((Integer)(port)).toString());
	                	String key = (new AuthSetup(user, password, host, port)).keyGen();
	                	connect = cf.MakeConnectionKey(user, key, host, port);
                        final SharedPreferences pref1 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	                	final Editor edit = pref1.edit();
	                	if(checkBox.isChecked())
	                	{
		                	edit.putString(USER_KEY, user);
		                	edit.putString(KEY_KEY, key);
		                	edit.putString(HOST_KEY, host);
		                	edit.putString(PORT_KEY, port+"");
	                	}
	                	edit.putBoolean(SAVED,checkBox.isChecked());
	                	edit.commit();
	                	
            		} catch(IOException e) {
            			new AlertDialog.Builder(v.getContext()).setMessage("Could not connect to server! Verify login information and network status.").create().show();
            			return;
            		}
            		Intent myIntent = new Intent(v.getContext(), PrinterSelectScreen.class);
                    startActivityForResult(myIntent, 0);
            	};

        
       /* portname.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                	if(connect == null) {
                		// Perform action on key press
                		try {
		                	user=usertext.getText().toString();
		                	password =passtext.getText().toString();
                			host = hostname.getText().toString();
		                	if(portname.getText().toString().length()!=0)
		                	{
		                		String temp=portname.getText().toString();
		                		port = Integer.parseInt(temp);
		                	}
		                	else
		                		port = 22;
		                			             
		                	ConnectionFactory cf = new ConnectionFactory();
		                	Log.e("user",user);
		                	Log.e("password",password);
		                	Log.e("host",host);
		                	Log.e("port",((Integer)(port)).toString());
		                	connect = cf.MakeConnection(user, password, host, port);
		                	if(checkBox.isChecked())
		                	{
			                	edit.putString(USER_KEY, user);
			                	edit.putString(PASSWORD_KEY, encryptPassword(password));
			                	edit.putString(HOST_KEY, host);
			                	edit.putString(PORT_KEY, port+"");
			                	edit.commit();
		                	}
		                	
                		} catch(IOException e) {
                			new AlertDialog.Builder(v.getContext()).setMessage("Could not connect to server! Verify login information and network status.").create().show();
                			return false;
                		}
                	}
                	//PreferenceManager.//getDefaultSharedPreferences(this);
                	//prefs.edit().putString("user", user);
                	//prefs.edit().putString("password", password);
                	Intent myIntent = new Intent(v.getContext(), PrinterSelectScreen.class);
                    startActivityForResult(myIntent, 0);
                  return true;
                }
                return false;
            }
        });*/
        // Check if there is a key setup already
        /*SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String key = prefs.getString("key", KEY_FAIL);
        user = prefs.getString("user", USER_FAIL);
        if(key.equals(KEY_FAIL)) {
        	if(user.equals(USER_FAIL)) { // need to login completely
        		
        	}
        } else if(user.equals(USER_FAIL)){ // odd but okay I guess 	
        	try {
        		ConnectionFactory cf = new ConnectionFactory();
				connect = cf.MakeConnectionKey(user, key);
				passtext.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
			} catch (IOException e) { // Connection Failed 
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }*/

            }
    	});
    }
}
