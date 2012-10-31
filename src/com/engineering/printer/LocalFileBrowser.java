package com.engineering.printer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.os.Environment;

/**
 * A local file browser that traverse the external storage.
 * @author Jun Ying
 *
 */
public class LocalFileBrowser extends FileBrowser {


	@Override
	protected FileEntry[] listFiles(String path) throws FileNotFoundException, StorageNotReadyException{
		File dir = new File(path);
		
		ArrayList<FileEntry> ret = new ArrayList<FileEntry>();
		File[] files = dir.listFiles();
		if(files==null)
			return new FileEntry[0];
		
		for(File f: files)
		{
			if(f.isDirectory())
				ret.add(new FileEntry(f.getAbsolutePath(), f.getName() + "/", f.isDirectory()));
			else
				ret.add(new FileEntry(f.getAbsolutePath(), f.getName(), f.isDirectory()));
		}
		
		return ret.toArray(new FileEntry[0]);
	}

	@Override
	protected String getRoot()  throws StorageNotReadyException {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    	return Environment.getExternalStorageDirectory().getPath();
	    }
	    else
	    	throw new StorageNotReadyException();
		
	}

}
