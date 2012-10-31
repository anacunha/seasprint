package com.engineering.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PrinterSelectScreen extends Activity {
	static final String PRINTER_PREF = "SEASPrintingFavorite";
	static final String PRINTER_KEY = "printerpreference";
	static String mFavored;

	String printer;
	boolean duplex;
	boolean dTemp;
	Integer number;
	Document mDocument;

	private ToggleButton mTogglebutton;
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
		mFavored = settings.getString(PRINTER_KEY, null);
		if (mFavored == null) {
			mFavored = "169";
		}

		mTogglebutton = (ToggleButton) findViewById(R.id.duplex_togglebutton);
		mTogglebutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				if (mTogglebutton.isChecked()) {
					dTemp = true;
				} else {
					dTemp = false;
				}
			}
		});

		mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);
		// TextView t1 = (TextView) findViewById(R.id.duplex_label);
		// TextView t2 = (TextView) findViewById(R.id.number_label);

		// if (Document.isMicrosoft) {
		// t1.setVisibility(View.GONE);
		// t2.setVisibility(View.GONE);
		// mNumberPicker.setVisibility(View.GONE);
		// mTogglebutton.setVisibility(View.GONE);
		// }

		mPrintbutton = (Button) findViewById(R.id.print_button);
		mPrintbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				number = mNumberPicker.value;
				duplex = dTemp;

				SharedPreferences settings = getSharedPreferences(PRINTER_PREF,
						0);
				SharedPreferences.Editor ed = settings.edit();
				ed.putString(PRINTER_KEY, mFavored);
				ed.commit();

				// PRINT
				PrintJobInfo job = new PrintJobInfo();
				job.doc = mDocument;
				job.duplex = dTemp;
				job.numCopies = mNumberPicker.value;
				job.printer = mFavored;
				new UploadFileTask(PrinterSelectScreen.this).execute(job);
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		try {
			if (null != getIntent().getData()) {
				if (null != getIntent().getType())
					mDocument = new Document(this, getIntent().getData(),
							getIntent().getType());
				else
					mDocument = new Document(this, getIntent().getData());
				if (mDocument.getMimeType() != null) {
					if (!mDocument.IsSupported()) {
						Toast.makeText(PrinterSelectScreen.this,
								"File format is not supported.",
								Toast.LENGTH_LONG).show();
						this.finish();
						return;
					}
				} else {
					Toast.makeText(PrinterSelectScreen.this,
							"File format not recognized, proceed anyway.",
							Toast.LENGTH_LONG).show();
				}
			}
			else
			{
				Toast.makeText(this, "Cannot read file.",
						Toast.LENGTH_LONG).show();
				this.finish();
				return;
			}
		} catch (IOException e) {
			Log.e("Connection", "File Not Found");
			Toast.makeText(PrinterSelectScreen.this, "Cannot open file.",
					Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}

		final TextView tvFileName = (TextView) findViewById(R.id.tvFilename);
		tvFileName.setText(mDocument.getDisplayName());

		if (LoginScreen.getConnection() == null) {
			Intent myIntent = new Intent(this, LoginScreen.class);
			startActivityForResult(myIntent, REQUEST_LOGIN);
		} else {
			new EnumeratePrinters().execute((Void) null);
		}
		
		

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				new EnumeratePrinters().execute((Void) null);
			} else
				this.finish();
		} else
			this.finish();

		super.onActivityResult(requestCode, resultCode, data);
	}

	public static class MyOnItemSelectedListener implements
			OnItemSelectedListener {
		public static String printer;

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// PRINTER WAS SELECTED
			printer = parent.getItemAtPosition(pos).toString();
			mFavored = printer;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	private class EnumeratePrinters extends AsyncTask<Void, Void, Boolean> {
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
					EnumeratePrinters.this.cancel(true);
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

				has_favored = ps.contains(mFavored);
				if (has_favored) {
					int pos = mAdapter.getPosition(mFavored);
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
