package com.engineering.printer;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public class UploadFileTask extends AsyncTask<PrintJobInfo, Integer, Boolean> {
	private FileUpload.Future upload;
	private Activity mAct;
	ProgressDialog mProgressDialog;
	ErrorCallback eb;
	PrintJobInfo mJobInfo;

	UploadFileTask(Activity act) {
		mAct = act;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		eb = new ErrorCallback(mAct);

		// set up progress dialog
		mProgressDialog = new ProgressDialog(mAct);
		mProgressDialog.setTitle("Uploading...");
		
		mProgressDialog.setMessage("Waiting for response...");
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
			mProgressDialog.setProgressNumberFormat("");
		mProgressDialog.setCancelable(false);
		//TODO For now, upload can't be cancelled. Maybe fix this later.
		/*
		mProgressDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						UploadFileTask.this.cancel(true);
					}
				});
				*/
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);

		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		mProgressDialog.show();
	}

	@Override
	protected void onCancelled() {
		// TODO clean up temporary file
	}

	private String errMsg = "Oops! Upload failed.";
	
	@Override
	protected Boolean doInBackground(PrintJobInfo... arg0) {
		mJobInfo = arg0[0];
		
		byte [] data;
		try {
			data = mJobInfo.doc.getData();
		}
		catch(Exception e)
		{
			Log.e("IO", "Cannot read file.");
			e.printStackTrace();
			errMsg = "Oops! Can not read file.";
			return false;
		}
		try {

			if (LoginScreen.getConnection() == null)
				return false;

			FileUpload fu = new FileUpload(LoginScreen.getConnection());
			upload = fu.startUpload(data, eb);
		} catch (Exception e) {
			LoginScreen.resetConnection();
			Log.e("Connection", "Failed to connect or send");
			errMsg = "Oops! Cannot connect to ENIAC.";
			e.printStackTrace();
			return false;
		}

		// Publish upload progress
		try {
			while (upload.PercentComplete() < 100) {
				Thread.sleep(100);
				publishProgress(upload.PercentComplete());
			}
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		mProgressDialog.setProgress(progress[0]);
		mProgressDialog.setMessage(PrepareStatus(upload.BytesWritten(),
				upload.TotalBytes()));
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mProgressDialog.dismiss();
		if (result.equals(true)) {
			mJobInfo.remoteFilename = upload.GetResult();
			new CallPrinterTask(mAct).execute(mJobInfo);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
			builder.setMessage(errMsg)
			.setTitle("Error")
			.create()
			.show();
		}
	}

	private String PrepareStatus(int bytesWritten, int totalBytes) {
		int orders_of_magnitude = 0;
		if (totalBytes >= 0 && totalBytes < 1024) {
			orders_of_magnitude = 1;
		} else if (totalBytes >= 1024 && totalBytes < 1024 * 1024) {
			orders_of_magnitude = 1024;
		} else {
			orders_of_magnitude = 1024 * 1024;
		}
		float writ = (float) bytesWritten / (float) orders_of_magnitude;
		float total = (float) totalBytes / (float) orders_of_magnitude;

		String suffix = null;
		if (orders_of_magnitude == 1) {
			suffix = "B";
		} else if (orders_of_magnitude == 1024) {
			suffix = "KiB";
		} else {
			suffix = "MB";
		}
		return String.format("%.2f %s out of %.2f %s uploaded.", writ, suffix,
				total, suffix);
	}
}
