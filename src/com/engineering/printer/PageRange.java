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
		if(initialPage > 0)
			this.initialPage = initialPage;
		else
			this.initialPage = 1;
		
		if(finalPage >= this.initialPage)
			this.finalPage = finalPage;
		else
			this.finalPage = this.initialPage;
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
