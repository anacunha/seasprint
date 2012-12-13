/**
 * 
 */
package com.engineering.printer;

/**
 * Printer options for a printing job.
 * 
 * @author Ana Cunha
 */
public class PrinterOptions {
	
	/**
	 * Enables double-sided printing. 
	 */
	private boolean duplex;
	
	/**
	 * Enables fit-to-page printing.
	 */
	private boolean fitToPage;
	
	/**
	 * Number of copies.
	 */
	private int numCopies;
	
	/**
	 * Page orientation. 
	 */
	private String orientation;
	
	/**
	 * Page range. 
	 */
	private PageRange range;
	
	/**
	 * is to be timed print
	 */
	private boolean timed;
	
	public PrinterOptions(boolean duplex, boolean fitToPage, int numCopies, String orientation, PageRange range, boolean timed)
	{
		this.duplex = duplex;
		this.fitToPage = fitToPage;
		this.numCopies = numCopies;
		this.orientation = orientation;
		this.range = range;
		this.timed = timed;
	}

	/**
	 * @return the duplex
	 */
	public boolean isDuplex() {
		return duplex;
	}

	/**
	 * @return the fitToPage
	 */
	public boolean isFitToPage() {
		return fitToPage;
	}

	/**
	 * @return the numCopies
	 */
	public int getNumCopies() {
		return numCopies;
	}

	/**
	 * @return the page orientation
	 */
	public String getOrientation() {
		return orientation;
	}

	/**
	 * @return the range
	 */
	public PageRange getRange() {
		return range;
	}
	
	public boolean isTimed(){
		return timed;
	}
}
