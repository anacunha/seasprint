package com.engineering.printer;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PrinterOptionsTest {

	private PrinterOptions printerOptions;
	private PrinterOptions printerOptions2;
	
	@Before
	public void setUp() {
		printerOptions = new PrinterOptions(true, true, 1, "Portrait", new PageRange(1,10));
		printerOptions2 = new PrinterOptions(false, false, 5, "Landscape", null);
	}
	
	@Test
	public void testIsDuplex() {
		assertTrue(printerOptions.isDuplex());
	}
	
	@Test
	public void testIsNotDuplex() {
		assertFalse(printerOptions2.isDuplex());
	}
	
	@Test
	public void testIsFitToPage() {
		assertTrue(printerOptions.isFitToPage());
	}
	
	@Test
	public void testIsNotFitToPage() {
		assertFalse(printerOptions2.isFitToPage());
	}
	
	@Test
	public void testNumCopies() {
		assertEquals(1, printerOptions.getNumCopies());
		assertEquals(5, printerOptions2.getNumCopies());
	}
	
	@Test
	public void testOrientationPortrait() {
		assertEquals("Portrait", printerOptions.getOrientation());
	}
	
	@Test
	public void testOrientationLandscape() {
		assertEquals("Landscape", printerOptions2.getOrientation());
	}
	
	@Test
	public void testPageRange() {
		assertEquals(1, printerOptions.getRange().getInitialPage());
		assertEquals(10, printerOptions.getRange().getFinalPage());
	}
	
	@Test
	public void testPageRangeNull() {
		assertNull(printerOptions2.getRange());
	}
}
