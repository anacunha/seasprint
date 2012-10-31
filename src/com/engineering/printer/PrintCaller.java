package com.engineering.printer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

public class PrintCaller {
    
    private CommandConnection mConn;
    
    public PrintCaller(CommandConnection conn) {
        mConn = conn;
    }
    
	private String DEFAULT_PRINTER = null;
	
	public String runCommand(String cmd) throws IOException{ 
		String out = mConn.execWithReturn(cmd);
		return out;
	}
	
	public String toString() {
		String out = "";
		try {
			out = runCommand("lpstat -a");
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringTokenizer st = new StringTokenizer(out, "\n");
		String printers = "";
		while(st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			printers += st2.nextToken() + "\n";
		}
		return printers;
	}
	
	public List<String> getPrinters() throws IOException {
		String out = runCommand("lpstat -a");
		StringTokenizer st = new StringTokenizer(out, "\n");
		LinkedList<String> printers = new LinkedList<String>();
		while(st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken());
			printers.add(st2.nextToken());
		}
		return printers;
	}
	
	public void printFile(String fileName, boolean isMicrosoft, String printerName, int numCopies, boolean duplex) throws IOException {
		//Print document
	    if (isMicrosoft) {
    	    //runCommand("ooffice -norestore -nofirststartwizard -nologo -headless -pt " + printerName + " " + fileName);
	    	runCommand("ooffice --norestore --nofirststartwizard --nologo --headless --print-to-file --outdir ~ \"" + fileName + "\"");
	    	Log.d("Print", "lpr -P" + printerName + " -# " + numCopies + (duplex ? " -o sides=two-sided-long-edge" : "") + " \"" + fileName + ".ps\"");
    	    runCommand("lpr -P" + printerName + " -# " + numCopies + (duplex ? " -o sides=two-sided-long-edge" : "") + " \"" + fileName + ".ps\"");
    	    runCommand("rm \"" + fileName + ".ps\"");
    	}
    	else {
    		Log.d("Print", "lpr -P" + printerName + " -# " + numCopies + (duplex ? " -o sides=two-sided-long-edge" : "") + " \"" + fileName + "\"");
    	    runCommand("lpr -P" + printerName + " -# " + numCopies + (duplex ? " -o sides=two-sided-long-edge" : "") + " \"" + fileName + "\"");
    	}
	}
	
	public String getDefaultPrinter() throws IOException {
		return (DEFAULT_PRINTER == null) ? getPrinters().get(0): DEFAULT_PRINTER;
	}
	
	public void setDefaultPrinter(String printer) {
		DEFAULT_PRINTER = printer;
	}
	
}
