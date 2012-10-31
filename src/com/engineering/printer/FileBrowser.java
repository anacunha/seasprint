package com.engineering.printer;

import java.io.FileNotFoundException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An abstract file browser activity.
 * This activity implements a common UI layout for a file browser, but leaves
 * the details of traversing a file sytem unimplemented.
 * @author Jun Ying
 *
 */
public abstract class FileBrowser extends Activity implements
		OnItemClickListener {

	public class StorageNotReadyException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private FileEntry[] mFiles;
	private String mPath;

	private int REQUEST_SUBDIRECTORY = 1;

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ListView lvFiles = (ListView) findViewById(R.id.lvFiles);
		FileEntry entry = (FileEntry) lvFiles.getItemAtPosition(position);
		if (entry.isDirectory) {
			Intent intent = new Intent(this, this.getClass());
			intent.putExtra("com.engineering.printer.path", entry.path);
			startActivityForResult(intent, REQUEST_SUBDIRECTORY);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);

		} else {
			Intent intent = getIntent();
			intent.putExtra("com.engineering.printer.fileChosen", entry.path);
			this.setResult(RESULT_OK, intent);
			this.finish();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SUBDIRECTORY) {
			overridePendingTransition(R.anim.slide_in_left,
					R.anim.slide_out_right);
			if (resultCode == RESULT_OK) {
				String fileChosen = data.getExtras().getString(
						"com.engineering.printer.fileChosen");
				Intent intent = getIntent();
				intent.putExtra("com.engineering.printer.fileChosen",
						fileChosen);
				this.setResult(RESULT_OK, intent);
				this.finish();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser);

		this.setTitle("Please select a document");

		ListView lvFiles = (ListView) findViewById(R.id.lvFiles);
		lvFiles.setOnItemClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		//Read the path to browse
		Intent intent = this.getIntent();
		Bundle bd = intent.getExtras();
		if (bd != null)
			mPath = bd.getString("com.engineering.printer.path");

		//Initialize back-end storage
		try {
			initStorage();
		} catch (StorageNotReadyException e) {
			Log.e("FileBrowser", "Storage not ready.");
			Toast.makeText(this, "Cannot access storage device.",
					Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * An asynchronous task to enumerate files and put them to ListView.
	 * While working, this class shows a progress dialog. And dismiss it when finished.
	 * @author nleven
	 *
	 */
	private class InitListItemsTask extends AsyncTask<Void, Void, Boolean> {
		ProgressDialog pg;
		String errMsg;

		@Override
		protected void onPreExecute() {
			pg = new ProgressDialog(FileBrowser.this);
			pg.setMessage("Listing files...");
			pg.setCancelable(true);
			pg.setIndeterminate(false);
			pg.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					InitListItemsTask.this.cancel(true);
					FileBrowser.this.finish();
				}
			});
			pg.show();

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if (mPath == null)
					mPath = getRoot();

				mFiles = listFiles(mPath);
				return true;
			} catch (StorageNotReadyException e) {
				Log.e("FileBrowser", "Storage not ready.");
				errMsg = "Cannot access storage device.";
				return false;
			} catch (FileNotFoundException e) {
				errMsg = "Cannot open directory.";
				Log.e("FileBrowser", "File not found");
				return false;
			}

		}

		@Override
		protected void onPostExecute(Boolean result) {
			pg.dismiss();
			if (result.equals(false)) {
				Toast.makeText(FileBrowser.this, errMsg, Toast.LENGTH_SHORT)
						.show();
				FileBrowser.this.finish();
			} else {
				if (mFiles.length == 0) {
					Toast.makeText(FileBrowser.this, "Directory is empty.",
							Toast.LENGTH_SHORT).show();
					FileBrowser.this.finish();
				}

				// Display path on title
				TextView tvPath = (TextView) findViewById(R.id.tvPath);
				tvPath.setText(mPath);

				// Display files to list view
				ListView lvFiles = (ListView) findViewById(R.id.lvFiles);
				TextWithIconAdapter arrAdapter = new TextWithIconAdapter(
						FileBrowser.this, R.layout.file_item, mFiles);
				lvFiles.setAdapter(arrAdapter);

			}

		}

	}

	/**
	 * Initialize list items. This should be called after backend file system
	 *  is ready.
	 */
	protected void initListItems() {
		new InitListItemsTask().execute((Void) null);
	}

	/**
	 * A helper class to store file names and other file information.
	 * Also designed to work with TextWithIconAdapter.
	 * @author Jun Ying
	 *
	 */
	public class FileEntry implements TextWithIconAdapter.ItemWithIcon {
		public String path;
		public String displayName;
		public boolean isDirectory;

		
		/**
		 * 
		 * @param path The absolute path of this entry
		 * @param displayName The display name of this file. (usually the base name)
		 * @param isDir Is this file a directory?
		 */
		public FileEntry(String path, String displayName, boolean isDir) {
			this.path = path;
			this.displayName = displayName;
			this.isDirectory = isDir;
		}

		public String getText() {
			return displayName;
		}

		/**
		 * If this is a directory, return a directory icon.
		 * Otherwise, return a icon of a file.
		 */
		public Integer getIconResourceId() {
			if (isDirectory)
				return R.drawable.folder_icon;
			else
				return R.drawable.file_icon;
		}

		public Drawable getIconDrawable() {
			return null;
		}
	}

	protected void initStorage() throws StorageNotReadyException {
		initListItems();
	}

	protected void finalizeStorage() throws StorageNotReadyException {

	}

	/**
	 * Get the root directory of the file system
	 * @return
	 * @throws StorageNotReadyException
	 */
	abstract protected String getRoot() throws StorageNotReadyException;

	/**
	 * List the files of the specified path.
	 * @param path
	 * @return A FileEntry array representing files and directories under this path.
	 * @throws FileNotFoundException
	 * @throws StorageNotReadyException
	 */
	abstract protected FileEntry[] listFiles(String path)
			throws FileNotFoundException, StorageNotReadyException;

}
