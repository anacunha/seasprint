package com.engineering.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PrinterSelectScreen extends Activity {
	private static final String PRINTER_PREF = "SEASPrintingFavorite";
	private static final String PRINTER_KEY = "printerpreference";
	private String mFavoredPrinter;

	private Document mDocument;

	private Checkable mDuplexCheck;
	private Spinner mSpinner;
	private Button mPrintbutton;
	private NumberPicker mNumberPicker;
	private ArrayAdapter<CharSequence> mAdapter;

	private final int REQUEST_LOGIN = 1;

	// public static Integer pps;

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater infl = new MenuInflater(this);
		infl.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.connection_config) {
			Intent myIntent = new Intent(this, LoginScreen.class);
			startActivityForResult(myIntent, REQUEST_LOGIN);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setTitle("Ready to Print");

		setContentView(R.layout.printers);

		SharedPreferences settings = getSharedPreferences(PRINTER_PREF, 0);
		mFavoredPrinter = settings.getString(PRINTER_KEY, null);
		if (mFavoredPrinter == null) {
			mFavoredPrinter = "169";
		}

		mDuplexCheck = (Checkable) findViewById(R.id.duplex_check);

		mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);
		
		mPrintbutton = (Button) findViewById(R.id.print_button);
		mPrintbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PRINTER_PREF,
						0);
				SharedPreferences.Editor ed = settings.edit();
				ed.putString(PRINTER_KEY, mFavoredPrinter);
				ed.commit();

				// PRINT
				PrintJobInfo job = new PrintJobInfo();
				job.doc = mDocument;
				job.duplex = mDuplexCheck.isChecked();
				job.numCopies = mNumberPicker.value;
				job.printer = mFavoredPrinter;
				if(!mDocument.isRemote())
					new UploadFileTask(PrinterSelectScreen.this).execute(job);
				else
					new CallPrinterTask(PrinterSelectScreen.this).execute(job);
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();

		try {
			// Load document
			if (null != getIntent().getData()) {
				if (null != getIntent().getType())
					mDocument = new Document(this, getIntent().getData(),
							getIntent().getType());
				else
					mDocument = new Document(this, getIntent().getData());

				if (!mDocument.canRead())
					throw new Exception("Cannot read file.");
			} else if (getIntent().getExtras() != null
					&& getIntent().getExtras().containsKey(
							"com.engineering.printer.remotePath")) {
				String remotePath = getIntent().getExtras().getString(
						"com.engineering.printer.remotePath");
				mDocument = new Document(this, remotePath);
			} else {
				throw new Exception("File not specified.");
			}

			// Check file format
			if (mDocument != null && !mDocument.getMimeType().equals("")) {
				if (!mDocument.IsSupported()) {
					throw new Exception("File format is not supported.");
				}
			} else {
				Toast.makeText(PrinterSelectScreen.this,
						"File format not recognized, proceed anyway.",
						Toast.LENGTH_LONG).show();
			}

			//Put local document into history
			if(!mDocument.isRemote())
			{
				Uri uri = mDocument.getUri();
				if(uri != null)
					PrintHistory.putHistory(this, uri.toString());
			}
			
			// Set file display name
			final TextView tvFileName = (TextView) findViewById(R.id.tvFilename);
			tvFileName.setText(mDocument.getDisplayName());

			// Show login screen if connection not established
			if (LoginScreen.getConnection() == null) {
				Intent myIntent = new Intent(this, LoginScreen.class);
				startActivityForResult(myIntent, REQUEST_LOGIN);
			} else {
				// List printers available
				new EnumeratePrintersTask().execute((Void) null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(PrinterSelectScreen.this, e.getMessage(),
					Toast.LENGTH_SHORT).show();
			this.finish();
			return;
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				new EnumeratePrintersTask().execute((Void) null);
			} else
				this.finish();
		} else
			this.finish();

		super.onActivityResult(requestCode, resultCode, data);
	}

	public class MyOnItemSelectedListener implements
			OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// PRINTER WAS SELECTED
			mFavoredPrinter = parent.getItemAtPosition(pos).toString();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	private class EnumeratePrintersTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog pd;
		private List<String> ps;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(PrinterSelectScreen.this);
			pd.setMessage("Listing available printers...");
			pd.setIndeterminate(true);
			pd.setCancelable(true);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					EnumeratePrintersTask.this.cancel(true);
					PrinterSelectScreen.this.finish();
				}
			});
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (result.equals(false)) {
				Toast.makeText(
						PrinterSelectScreen.this,
						"Could not connect to server! Verify login information and network status.",
						Toast.LENGTH_LONG).show();
				PrinterSelectScreen.this.finish();
				return;
			} else {
				boolean has_favored = false;
				mAdapter = new ArrayAdapter<CharSequence>(
						getApplicationContext(), R.layout.spinner_item,
						new ArrayList<CharSequence>());
				mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
				for (Iterator<String> iter = ps.iterator(); iter.hasNext();) {
					mAdapter.add(iter.next());
				}
				mSpinner = (Spinner) findViewById(R.id.printer_spinner);
				mSpinner.setAdapter(mAdapter);
				mSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener());

				has_favored = ps.contains(mFavoredPrinter);
				if (has_favored) {
					int pos = mAdapter.getPosition(mFavoredPrinter);
					mSpinner.setSelection(pos);
				}

				pd.dismiss();
			}

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Log.d("Connection", "Start Connecting");
				PrintCaller pc = new PrintCaller(new CommandConnection(
						LoginScreen.getConnection()));
				ps = pc.getPrinters();
			} catch (IOException ioe) {
				LoginScreen.resetConnection();
				Log.d("Connection", "Failed to connect or send");
				return false;
			}
			return true;

		}
	}

}
