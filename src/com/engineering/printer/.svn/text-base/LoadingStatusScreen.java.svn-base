package com.engineering.printer;

import java.io.IOException;
import java.io.InputStream;

import com.engineering.printer.PrinterSelectScreen.MyOnItemSelectedListener;
import com.trilead.ssh2.Connection;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class LoadingStatusScreen extends Activity {
	public String user=EngineeringPrinter.user;
	public String password = EngineeringPrinter.password;
	public String printer = MyOnItemSelectedListener.printer;
	public boolean duplex = PrinterSelectScreen.duplex;
	public Integer number = PrinterSelectScreen.number;
	//public Integer pps = PrinterSelectScreen.pps;
	
	public FileUpload.Future upload = EngineeringPrinter.upload;

    private ProgressBar mProgress;
    private TextView mUpdate;
    private TextView mConstantLoading;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadingstatus);
        
    }
    
	 public void onResume() {
	       super.onResume();
	        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
	        mProgress.setProgress(0);
	        
	        mUpdate = (TextView) findViewById(R.id.byte_progress);
	        mUpdate.setText("Initializing upload.");
	        
	           mConstantLoading = (TextView) findViewById(R.id.loading_constant);
	            mConstantLoading.setText("Initializing upload.");
	        
            try {
                FileUpload fu = new FileUpload(EngineeringPrinter.connect);
                upload  = fu.startUpload(Document.data,  EngineeringPrinter.eb);
            }
            catch (IOException ioe){
                //ALERT DIALOG
                Log.e("Connection", "Failed to connect or send");
            }
	        
            final Handler handle = new Handler();
	        //Toast.makeText(LoadingStatusScreen.this, user + " "+ password + " "+ printer + " " + duplex + " "+ number + " "+ pps, Toast.LENGTH_LONG).show();
	         // Start lengthy operation in a background thread
            new Thread( new Runnable() {
                 public void run() {
        	        while (mProgressStatus < 100) {
                    	 //DO STUFF
                         try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
        
                        }
                         // Update the progress bar
                        handle.post(new Runnable()  {
                            public void run() {
                                 int val = upload.PercentComplete();
                                 mProgressStatus = val;
                                 mProgress.setProgress(val);
                                 Log.i("Connection", "Percentage " + Integer.toString(val));
                                 mUpdate.setText(PrepareStatus(upload.BytesWritten(), upload.TotalBytes()));
                            }
                        });
                     }
        	        final String filename = upload.GetResult();
        	        handle.post(new Runnable() {
        	            public void run() {
        	                UploadComplete(filename);
        	            }
        	        });
                 }
            }).start();
	         //Log.i("Connection", "Temporary filename is " + filename);  
	 }
	 
	 private void UploadComplete(final String filename) {
	     mProgress.setVisibility(View.GONE);
	     mUpdate.setVisibility(View.GONE);
	     mConstantLoading.setVisibility(View.GONE);
	     
	     new Thread( new Runnable() {
	     public void run() {
        	     try {
        	         String local_filename = filename;
        	         String local_printer = printer;
        	         boolean local_duplex = duplex;
        	         int local_number = number;
                    CommandConnection cc = new CommandConnection(EngineeringPrinter.connect);
        	         new PrintCaller(cc).printFile(local_filename, local_printer, local_number, local_duplex);
        	         cc.execWithReturn("rm " + local_filename);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        	     }
    	     }
	     ).start();
	     Toast.makeText(LoadingStatusScreen.this, "Success! File uploaded and sent to printer!", Toast.LENGTH_LONG).show();
	     this.finish();
	 }
	 
	 
	 private String ContentProcessing(String filename) {
	     return filename;
	 }
	 
	 

     private String PrepareStatus(
             int bytesWritten, int totalBytes) {
         int orders_of_magnitude = 0;
         if (totalBytes >= 0 && totalBytes < 1024) {
             orders_of_magnitude = 1;
         }
         else if (totalBytes >= 1024 && totalBytes <1024*1024) {
             orders_of_magnitude = 1024;
         }
         else {
             orders_of_magnitude = 1024*1024;
         }
         float writ = (float)bytesWritten/(float)orders_of_magnitude;
         float total = (float)totalBytes/(float)orders_of_magnitude;
         
         String suffix = null;
         if (orders_of_magnitude ==1) {
             suffix = "B";
         }
         else if (orders_of_magnitude==1024) {
             suffix = "KiB";
         }
         else {
             suffix = "MB";
         }
         return String.format("%.2f %s out of %.2f %s uploaded.", writ, suffix, total, suffix);
     }
	                     
}
