package com.engineering.printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
		Log.d("ENIAC Command", cmd);
		String out = mConn.execWithReturn(cmd);
		return out;
	}
	
	public int hourToUTC(int hour){
		
		return (hour+5)%24;
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

	public void printFile(PrintJobInfo printJob) throws IOException {
<<<<<<< HEAD
		String printCommand = getPrintCommand(printJob);
		
		//Print document
	    if (printJob.getDocument().IsMicrosoft()) {
    	    runCommand("unoconv --stdout \"" + printJob.getRemoteFilename() + "\" | " + printCommand);
    	}
//    	else {
//    		if (printJob.getOptions().isTimed()) {
//    			String[] filename_arr = printJob.getRemoteFilename().split("/");
//    			String filename = filename_arr[filename_arr.length-1];
//    			runCommand("curl -L https://raw.github.com/pbwingo/cets_autoprint/master/setup.sh | sh");
//    			runCommand("cp " + printJob.getRemoteFilename() + " ~/to_print"); 
//    			runCommand("echo \""+  printCommand +"\" > ~/to_print/." + filename + "opts");
//    			}
    		else
    			runCommand(printCommand + " \"" + printJob.getRemoteFilename() + "\"");
    	}
//}
	
=======
		if (printJob.getOptions().isTimed()){
			ArrayList<String> jobs = getTimedPrintCommand(printJob);
			for (int i=0;i<jobs.size();i++){
					runCommand(jobs.get(i) + " \"" + printJob.getRemoteFilename() + "\"");
			}

		}
		else {
			String printCommand = getPrintCommand(printJob);

			//Print document
			if (printJob.getDocument().IsMicrosoft()) {
				runCommand("unoconv --stdout \"" + printJob.getRemoteFilename() + "\" | " + printCommand);
			}
			else {
				runCommand(printCommand + " \"" + printJob.getRemoteFilename() + "\"");
			}
		}
	}

>>>>>>> 7d977a60620bb4293a527903c6b1ed9d39aeabde
	private String getPrintCommand(PrintJobInfo printJob)
	{
		StringBuilder printCommand = new StringBuilder();
		//set printer
		printCommand.append("lpr -P");
		printCommand.append(printJob.getPrinter());
		//set number of pages
		printCommand.append(" -# ");
		printCommand.append(printJob.getOptions().getNumCopies());

		//set double sided printing
		if(printJob.getOptions().isDuplex()) {
			if(printJob.getOptions().getOrientation().equals("Portrait")) {
				printCommand.append(" -o portrait");
				printCommand.append(" -o sides=two-sided-long-edge");
			}
			else {
				printCommand.append(" -o landscape");
				printCommand.append(" -o sides=two-sided-short-edge");
			}
		}
		else
			printCommand.append(" -o sides=one-sided");

		//set fit to page
		if(printJob.getOptions().isFitToPage())
			printCommand.append(" -o fit-to-page");

		//set page range
		if(printJob.getOptions().getRange() != null) {
			printCommand.append(" -o page-ranges=" + printJob.getOptions().getRange().getInitialPage());
			printCommand.append("-" + printJob.getOptions().getRange().getFinalPage());
		}

		return printCommand.toString();
	}

	private ArrayList<String> getTimedPrintCommand(PrintJobInfo printJob)
	{

		ArrayList<String> strings = new ArrayList<String>();
		final long HALF_HOUR= 1800*1000;
		long now = new Date().getTime();

		for (int i=0;i<20;i++){

			Date print_at = new Date(now+i*HALF_HOUR);
			String date = print_at.toString();
			date = date.split(" ")[3];
			if (date != null) {
				int hour = Integer.parseInt(date.split(":")[0]);
				hour = hourToUTC(hour);
				int minute = Integer.parseInt(date.split(":")[1]);
				minute = (minute+2)%60;
				String strhour;
				String strminute;
				if (hour < 10)
					strhour = "0"+hour;
				else
					strhour = String.valueOf(hour);
				if (minute < 10)
					strminute = "0" + minute;
				else
					strminute = String.valueOf(minute);
				date = (strhour+":" +strminute);
			}

			StringBuilder printCommand = new StringBuilder();
			//set printer
			printCommand.append("lpr -P");
			printCommand.append(printJob.getPrinter());
			//set number of pages
			printCommand.append(" -# ");
			printCommand.append(printJob.getOptions().getNumCopies());

			//set double sided printing
			if(printJob.getOptions().isDuplex()) {
				if(printJob.getOptions().getOrientation().equals("Portrait")) {
					printCommand.append(" -o portrait");
					printCommand.append(" -o sides=two-sided-long-edge");
				}
				else {
					printCommand.append(" -o landscape");
					printCommand.append(" -o sides=two-sided-short-edge");
				}
			}
			else
				printCommand.append(" -o sides=one-sided");

			//set fit to page
			if(printJob.getOptions().isFitToPage())
				printCommand.append(" -o fit-to-page");
			//set page range

			if(printJob.getOptions().getRange() != null) {
				int initial_page = printJob.getOptions().getRange().getInitialPage();
				int last_page = printJob.getOptions().getRange().getFinalPage();
				int first_page = initial_page+5*i;
				int max = Math.min(initial_page+5*i+5, last_page);

				printCommand.append(" -o page-ranges=" + first_page);
				printCommand.append("-" + max);
			}
			else {
				int first = 1+5*i;
				int last = 5+5*i;
				printCommand.append(" -o page-ranges=" + first);
				printCommand.append("-" + last);				
			}
			printCommand.append(" -o job-hold-until=" + date);
			strings.add(i,printCommand.toString());
		}
		return strings;
	}

	public String getDefaultPrinter() throws IOException {
		return (DEFAULT_PRINTER == null) ? getPrinters().get(0): DEFAULT_PRINTER;
	}

	public void setDefaultPrinter(String printer) {
		DEFAULT_PRINTER = printer;
	}

}
