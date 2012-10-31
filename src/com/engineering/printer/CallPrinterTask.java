package com.engineering.printer;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class CallPrinterTask extends AsyncTask<PrintJobInfo, Integer, Boolean> {
	ProgressDialog mProgressDialog;
	private Activity mAct;

	CallPrinterTask(Activity act) {
		mAct = act;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		// set up progress dialog
		mProgressDialog = new ProgressDialog(mAct);
		mProgressDialog.setTitle("Printing...");
		mProgressDialog.setMessage("Sending job to printer...");
		mProgressDialog.setCancelable(true);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
			public void onCancel(DialogInterface dialog) {
				CallPrinterTask.this.cancel(true);
			}
		});
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		mProgressDialog.show();
	}
	
	@Override
	protected void onCancelled()
	{
		//TODO clean up temporary file
	}
	
	@Override
	protected Boolean doInBackground(PrintJobInfo... params) {
		try {
			String remote_filename = params[0].remoteFilename;
			String printer = params[0].printer;
			boolean duplex = params[0].duplex;
			int numCopies = params[0].numCopies;
			Document doc = params[0].doc;
			if (null == LoginScreen.getConnection())
				return false;
			CommandConnection cc = new CommandConnection(
					LoginScreen.getConnection());
			new PrintCaller(cc).printFile(remote_filename, doc.IsMicrosoft(),
					printer, numCopies, duplex);
			cc.execWithReturn("rm " + remote_filename);

			return true;
		} catch (IOException e) {
			LoginScreen.resetConnection();
			return false;
		}
	}

	public class MyAlertDialog extends AlertDialog {

		protected MyAlertDialog(Context context) {
			super(context);
		}

		public void onStop() {
			super.onStop();
			mAct.finish();
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mProgressDialog.dismiss();
		AlertDialog dialog = new MyAlertDialog(mAct);
		if (result == true) {
			dialog.setMessage("The document is successfully sent to printer!");
			dialog.setTitle("Done!");
		} else {
			dialog.setMessage("Sorry, faild to call printer. :-(");
			dialog.setTitle("Error");

		}
		dialog.show();

	}

}