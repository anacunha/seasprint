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
			Document doc = params[0].getDocument();
			
			//Override if file is already on the ENIAC server
			if(doc.isRemote())
				params[0].setRemoteFilename(doc.getRemotePath());
			
			if (null == LoginScreen.getConnection())
				return false;
			CommandConnection cc = new CommandConnection(
					LoginScreen.getConnection());
			new PrintCaller(cc).printFile(params[0]);
			
			//DONT remove original file
			if(!doc.isRemote())
				cc.execWithReturn("rm " + params[0].getRemoteFilename());

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
			dialog.setMessage("The document was successfully sent to printer!");
			dialog.setTitle("Done!");
		} else {
			dialog.setMessage("Sorry, faild to call printer. :-(");
			dialog.setTitle("Error");

		}
		dialog.show();

	}

}