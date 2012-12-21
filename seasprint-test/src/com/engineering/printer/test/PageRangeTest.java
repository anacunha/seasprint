package com.engineering.printer.test;

import com.engineering.printer.*;
import android.test.AndroidTestCase;


public class PageRangeTest extends AndroidTestCase{

	private PageRange pageRange;
	
	public void testPageRange() {
		pageRange = new PageRange(1, 10);
		assertEquals(1, pageRange.getInitialPage());
		assertEquals(10, pageRange.getFinalPage());
	}
	
	public void testPageRangeWithZero() {
		pageRange = new PageRange(0, 10);
		assertEquals(1, pageRange.getInitialPage());
		assertEquals(10, pageRange.getFinalPage());
	}
	
	public void testPageRangeWithNegativeValue() {
		pageRange = new PageRange(0, -10);
		assertEquals(1, pageRange.getInitialPage());
		assertEquals(1, pageRange.getFinalPage());
	}
	
	public void testPageRangeInitialGreaterThanFinal() {
		pageRange = new PageRange(20, 10);
		assertEquals(20, pageRange.getInitialPage());
		assertEquals(20, pageRange.getFinalPage());
	}
}
