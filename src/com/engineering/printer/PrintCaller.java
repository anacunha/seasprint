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
		//Prepare files for lpr command, convert if necessary
		String doc_lpr = printJob.getRemoteFilename();
		boolean converted_to_pdf = false;
		if(printJob.getDocument().IsMicrosoft())
		{
			runCommand("libreoffice --headless --invisible --convert-to pdf \"" + printJob.getRemoteFilename() +"\" --outdir=~");
			doc_lpr = printJob.getRemoteFilename() + ".pdf";
			converted_to_pdf = true;
		}
		if (printJob.getOptions().isTimed()){
			int totalPages = 50; //Default to 50
			String totalPagesStr = runCommand("pdfinfo \""+ doc_lpr + "\" 2>/dev/null |awk '/^Pages/ { print $2}'");
			try
			{
				totalPages = Integer.parseInt(totalPagesStr);
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
			
			ArrayList<String> jobs = getTimedPrintCommand(printJob, totalPages);
			for (int i=0;i<jobs.size();i++){
				runCommand(jobs.get(i) + " \"" + doc_lpr + "\"");
			}

		}
		else {
			String printCommand = getPrintCommand(printJob);
			runCommand(printCommand + " \"" + doc_lpr + "\"");
		}
		
		//Cleanup temporary files.
		if(converted_to_pdf)
		{
			runCommand("rm \"" + doc_lpr + "\"");
		}
	}

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

	private ArrayList<String> getTimedPrintCommand(PrintJobInfo printJob, int totalPagesInDoc)
	{

		ArrayList<String> strings = new ArrayList<String>();
		final long HALF_HOUR= 1800*1000;
		long now = new Date().getTime();

		//Get the basic printing command without page range options
		PrinterOptions opt = printJob.getOptions();
		PrinterOptions opt_no_range = new PrinterOptions(opt.isDuplex(), opt.isFitToPage(), opt.getNumCopies(), opt.getOrientation(), null, opt.isTimed());
		String basicPrintCommand = getPrintCommand(new PrintJobInfo(printJob.getDocument(), printJob.getPrinter(), opt_no_range));
		
		//set page range
		int initial_page = 1;
		int last_page = totalPagesInDoc;
		if(printJob.getOptions().getRange() != null) {
			initial_page = Math.max(initial_page, printJob.getOptions().getRange().getInitialPage());
			last_page = Math.min(last_page, printJob.getOptions().getRange().getFinalPage());
		}
		
		int max_page_per_job = 5;
		if(printJob.getOptions().isDuplex())
			max_page_per_job *= 2;
		
		for (int i=0; ; i++){
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

			StringBuilder printCommand = new StringBuilder(basicPrintCommand);
			
			
			int job_first_page = initial_page+max_page_per_job*i;
			if(job_first_page > totalPagesInDoc)
				break;
			int job_final_page= Math.min(job_first_page+max_page_per_job-1, last_page);

			printCommand.append(" -o page-ranges=" + job_first_page);
			printCommand.append("-" + job_final_page);
			
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