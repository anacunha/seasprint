package com.engineering.printer;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;

public class FileUpload implements Runnable {
    
    private SCPClient mSCP;
    private Future mF;
    private CommandConnection mConn;
    private byte [] mData;
    
    public FileUpload(Connection conn) throws IOException {
        mSCP = new SCPClient(conn);
        mConn = new CommandConnection(conn);
    }
    
    public class Future {
        
        public Future(int totalbytes) {
            mFinish = new Semaphore(0);
            mTotalBytes = totalbytes;
            mBytesWritten = new AtomicInteger();
            mPassed = false;
        }
        
        public boolean NearComplete() {
            return mTotalBytes == mBytesWritten.get();
        }
        
        private int mTotalBytes;
        private AtomicInteger mBytesWritten;
        private Semaphore mFinish;
        private String mTarget;
        private boolean mPassed;
        
        public void IncrementProgress(int additional_bytes_written) {
            int val = mBytesWritten.addAndGet(additional_bytes_written);
            Log.i("Connection", val + " bytes written!");
        }
        
        public int BytesWritten() {
            return mBytesWritten.get();
        }
        
        public int TotalBytes() {
            return mTotalBytes;
        }
        
        public int PercentComplete() {
            return( (100)*mBytesWritten.get()) / mTotalBytes;
        }
        
        public void SignalCompletion(String filename) {
            mTarget = filename;
            mFinish.release();
        }
        
        public String GetResult()  {
            if (!mPassed) {
                try {
                    mFinish.acquire();
                }
                catch (InterruptedException ie) {
                   GetResult();
                }
                mPassed = true;
            }
            return mTarget;
            
        }
        
    }
    
    public void run() {
        try {
            String home = mConn.execWithReturn("echo ~");
            String tmpfile = mConn.execWithReturn("echo `mktemp " + home  + "/SEASPRINT_XXXXXXXX`");
            //String tmpfile = "/home1/j/jmccaf/tmpprint.pdf"; 
            String [] toks = tmpfile.split("/");
            /*
            String rebuild = "";
            
            for (int i = 0 ; i < toks.length - 1;i++) {
                //rebuild = rebuild + "/" + toks[i];
            	rebuild = rebuild + toks[i] + "/";
            }
            String remoteTargetDirectory = rebuild;*/
            String remoteTargetDirectory = home;
            String remoteFileName = toks[toks.length - 1];
    
            Log.i("Connection", tmpfile);
            mSCP.put(mF,mData, remoteFileName, remoteTargetDirectory);
            mF.SignalCompletion(tmpfile);
        }
        catch (IOException ioe) {
            mCb.error();
        }
        
    }
    
    private ErrorCallback mCb;
    
    public Future startUpload(byte [] data, ErrorCallback cb) throws IOException {
        
        mCb = cb;
        mData = data;
        mF = new Future(data.length);
        Thread thr = new Thread(this);
        thr.start();
        return mF;
        

    }

}
