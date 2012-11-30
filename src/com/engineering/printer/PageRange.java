/**
 * 
 */
package com.engineering.printer;

/**
 * Represents the page range for a printing job.
 * 
 * @author Ana Cunha
 */
public class PageRange {
	
	/**
	 * Initial page of range.
	 */
	private int initialPage;
	
	/**
	 * Final page of range.
	 */
	private int finalPage;
	
	/**
	 * Creates a new PageRange with a initial and final page.
	 */
	public PageRange(int initialPage, int finalPage) {
		this.initialPage = initialPage;
		this.finalPage = finalPage;
	}

	/**
	 * @return the initialPage
	 */
	public int getInitialPage() {
		return initialPage;
	}

	/**
	 * @return the finalPage
	 */
	public int getFinalPage() {
		return finalPage;
	}
}
