package com.engineering.printer;

/**
 * A helper data structure that records relevant information for a printing job
 * @author Jun Ying, Ana Cunha
 *
 */
public class PrintJobInfo
{
	/**
	 * The document to print
	 */
	private Document document;
	
	/**
	 * The path for this document on the remote ENIAC server
	 */
	private String remoteFilename;
	
	/**
	 * The name of the printer to be used
	 */
	private String printer;
	
	/**
	 * Printer options of the printing job.
	 */
	private PrinterOptions options;
	
	/**
	 * Creates a new Printing job.
	 */
	public PrintJobInfo(Document document, String printer, PrinterOptions options) {
		this.document = document;
		this.printer = printer;
		this.options = options;
	}
	
	/**
	 * @return the doc
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @return the remoteFilename
	 */
	public String getRemoteFilename() {
		return remoteFilename;
	}

	/**
	 * @param remoteFilename the remoteFilename to set
	 */
	public void setRemoteFilename(String remoteFilename) {
		this.remoteFilename = remoteFilename;
	}

	/**
	 * @return the printer
	 */
	public String getPrinter() {
		return printer;
	}

	/**
	 * @return the options
	 */
	public PrinterOptions getOptions() {
		return options;
	}
}