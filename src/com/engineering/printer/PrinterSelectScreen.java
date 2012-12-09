package com.engineering.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PrinterSelectScreen extends Activity {
	private static final String PRINTER_PREF = "SEASPrintingFavorite";
	private static final String PRINTER_HISTORY_KEY = "PrinterHistory";

	private Document mDocument;
	private Checkable mDuplexCheck;
	private Checkable mFitToPageCheck;
	private ToggleButton mPageOrientation;
	private Spinner mSpinner;
	private Button mPrintbutton;
	private NumberPicker mNumberPicker;
	private RadioGroup mRadioPageRange;
	private HistoryManager printerHistory;
	private final int REQUEST_LOGIN = 1;
	private final Context context = this;

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

		//Set up printer history
		printerHistory =  new HistoryManager(PrinterSelectScreen.this, PRINTER_PREF, PRINTER_HISTORY_KEY, 4);
		
		//Set up controls
		mSpinner = (Spinner) findViewById(R.id.printer_spinner);
		mDuplexCheck = (Checkable) findViewById(R.id.duplex_check);
		mFitToPageCheck = (Checkable) findViewById(R.id.fitpage_check);
		mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);
		mPageOrientation = (ToggleButton) findViewById(R.id.page_orientation);
		
		mRadioPageRange = (RadioGroup) findViewById(R.id.radioPageRange);
		final EditText initialPageEdit = (EditText) findViewById(R.id.initialPage);
		initialPageEdit.setEnabled(false);
		final EditText finalPageEdit = (EditText) findViewById(R.id.finalPage);
		finalPageEdit.setEnabled(false);
		
		initialPageEdit.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
			public void afterTextChanged(Editable s) {
				if(finalPageEdit.getText().toString().equals(""))
					finalPageEdit.setText(s);
			}
	    }); 
		
		mRadioPageRange.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
	            if (checkedId == R.id.radioAllPages) {
	            	initialPageEdit.setText("");
	            	initialPageEdit.setEnabled(false);
	            	finalPageEdit.setText("");
	                finalPageEdit.setEnabled(false);
	            } else {
	            	initialPageEdit.setEnabled(true);
	                finalPageEdit.setEnabled(true);
	            }
	        }
		});
		
		mPrintbutton = (Button) findViewById(R.id.print_button);
		mPrintbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String initialPage = initialPageEdit.getText().toString();
				String finalPage = finalPageEdit.getText().toString();
				PageRange pageRange = null;
				if(initialPageEdit.isEnabled() && (initialPage.equals("") || finalPage.equals(""))) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(context);
					dialog.setMessage("Incorrect page range!");
					dialog.setTitle("Error");
					dialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();							
						}
					});
					dialog.show();
					return;
				}
				else
					if(initialPageEdit.isEnabled())
						pageRange = new PageRange(Integer.parseInt(initialPage), Integer.parseInt(finalPage));
				
				// PRINT
				String chosenPrinter = mSpinner.getSelectedItem().toString();
				printerHistory.putHistory(chosenPrinter);
				
				PrinterOptions options = new PrinterOptions(mDuplexCheck.isChecked(), mFitToPageCheck.isChecked(), mNumberPicker.value, mPageOrientation.getText().toString(), pageRange);
				PrintJobInfo job = new PrintJobInfo(mDocument, chosenPrinter, options);

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
					printerHistory.putHistory(uri.toString());
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
				

				ArrayAdapter<CharSequence> adpHistories = new ArrayAdapter<CharSequence>(
						getApplicationContext(), R.layout.spinner_item,
						new ArrayList<CharSequence>());
				adpHistories.setDropDownViewResource(R.layout.spinner_dropdown_item);
				List<String> history = printerHistory.getHistory();
				for(String p: history)
				{
					if(ps.contains(p))
						adpHistories.add(p);
				}
				
				ArrayAdapter<CharSequence> adpPrinters = new ArrayAdapter<CharSequence>(
						getApplicationContext(), R.layout.spinner_item,
						new ArrayList<CharSequence>());
				adpPrinters.setDropDownViewResource(R.layout.spinner_dropdown_item);
				
				for (String s: ps) {
					adpPrinters.add(s);
				}
				
				SeparatedListAdapter adp = new SeparatedListAdapter(PrinterSelectScreen.this);
				adp.setUseConvertView(false);
				
				if(!adpHistories.isEmpty())
					adp.addSection("RECENT", adpHistories);
				adp.addSection("ALL PRINTERS", adpPrinters);
				
				mSpinner.setAdapter(adp);

				if(!adpHistories.isEmpty()){
					mSpinner.setSelection(1); //Select most recent printer
				}
				else
				{
					int defaultPrinter = adpPrinters.getPosition("169");
					defaultPrinter += 1;//The "ALL PRINTERS" header
					mSpinner.setSelection(defaultPrinter);
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