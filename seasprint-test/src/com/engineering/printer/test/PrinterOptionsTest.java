package com.engineering.printer.test;

import com.engineering.printer.*;
import android.test.AndroidTestCase;


public class PrinterOptionsTest extends AndroidTestCase{

	private PrinterOptions printerOptions;
	private PrinterOptions printerOptions2;
	
	public void setUp() {
		printerOptions = new PrinterOptions(true, true, 1, "Portrait", new PageRange(1,10));
		printerOptions2 = new PrinterOptions(false, false, 5, "Landscape", null);
	}
	
	public void testIsDuplex() {
		assertTrue(printerOptions.isDuplex());
	}
	
	public void testIsNotDuplex() {
		assertFalse(printerOptions2.isDuplex());
	}
	
	public void testIsFitToPage() {
		assertTrue(printerOptions.isFitToPage());
	}
	
	public void testIsNotFitToPage() {
		assertFalse(printerOptions2.isFitToPage());
	}
	
	public void testNumCopies() {
		assertEquals(1, printerOptions.getNumCopies());
		assertEquals(5, printerOptions2.getNumCopies());
	}
	

	public void testOrientationPortrait() {
		assertEquals("Portrait", printerOptions.getOrientation());
	}
	

	public void testOrientationLandscape() {
		assertEquals("Landscape", printerOptions2.getOrientation());
	}
	

	public void testPageRange() {
		assertEquals(1, printerOptions.getRange().getInitialPage());
		assertEquals(10, printerOptions.getRange().getFinalPage());
	}
	

	public void testPageRangeNull() {
		assertNull(printerOptions2.getRange());
	}
}
