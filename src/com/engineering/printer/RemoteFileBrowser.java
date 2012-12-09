package com.engineering.printer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;

/**
 * A file browser that traverse files on the ENIAC server.
 *
 * @author Jun Ying
 */
public class RemoteFileBrowser extends FileBrowser {
	private final int REQUEST_LOGIN = 701;
	private CommandConnection mConn = null;
	
	@Override
	protected void initStorage() throws StorageNotReadyException
	{
		if (LoginScreen.getConnection() == null) {
			Intent myIntent = new Intent(this, LoginScreen.class);
			startActivityForResult(myIntent, REQUEST_LOGIN);
		} 
		else
		{
			mConn = new CommandConnection(LoginScreen.getConnection());
			initListItems();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode ==REQUEST_LOGIN){
			if(resultCode!=RESULT_OK){
				this.finish();
			}
			else
			{
				mConn = new CommandConnection(LoginScreen.getConnection());
				initListItems();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		
	}
	
	@Override
	protected FileEntry[] listFiles(String path) throws FileNotFoundException, StorageNotReadyException{
		try {
			//Enumerate subdirectories
			String list_subdirs = mConn.execWithReturn("pushd \"" + path +"\">/dev/null; find . -maxdepth 1  \\( ! -regex '.*/\\..*' \\)  -xtype d | xargs -l basename; popd>/dev/null");
			//Enumerate files
			String list_files = mConn.execWithReturn("pushd \"" + path +"\">/dev/null; find . -maxdepth 1  \\( ! -regex '.*/\\..*' \\)  -xtype f | xargs -l basename; popd >/dev/null");
			
			ArrayList<FileEntry> ret = new ArrayList<FileEntry>();
			
			String path_sep = path;
			if(path_sep.lastIndexOf("/") <= path_sep.length()-1)
				path_sep += "/";
			
			//Parse results and construct FileEntry array
			for(String s:list_subdirs.split("\n"))
			{
				if(!"".equals(s) && !".".equals(s) && !"..".equals(s))
					ret.add(new FileEntry(path_sep + s, s + "/", true));
			}
			
			for(String s:list_files.split("\n"))
			{
				if(!"".equals(s) && !".".equals(s) && !"..".equals(s))
					ret.add(new FileEntry(path_sep + s, s, false));
			}
			
			return ret.toArray(new FileEntry[0]);
		} catch (IOException e) {
			LoginScreen.resetConnection();
			throw new StorageNotReadyException();
		}
	}

	@Override
	protected String getRoot() throws StorageNotReadyException {
		if (mConn == null) {
			throw new StorageNotReadyException();
		}
		else
		{
			try {
				//The home directory
				return mConn.execWithReturn("echo ~");
			} catch (IOException e) {
				LoginScreen.resetConnection();
				throw new StorageNotReadyException();
			}
		}
		
	}

}
