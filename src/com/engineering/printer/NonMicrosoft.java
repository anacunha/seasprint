//package com.engineering.printer;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.net.Uri;
//import android.util.Log;
//import android.widget.Toast;
//
//
//public class NonMicrosoft extends Activity {
//    public void onCreate() {
//        super.onStart();
//        InputStream is = null;
//        try {
//            is = getContentResolver().openInputStream(getIntent().getData());
//        }
//        catch  (FileNotFoundException fnf){
//            Log.e("Connection","File Not Found");
//        }
//        Document.load(is);
//        Document.setDescriptor(getIntent().getData());
//        
//       
//  
//       Intent myIntent = new Intent(getApplicationContext(), EngineeringPrinter.class).putExtra("Microsoft", false);
//       startActivityForResult(myIntent, 0);
//    }
//    
//    public void onRestart() {
//        super.onRestart();
//        finish();
//    }
//}
