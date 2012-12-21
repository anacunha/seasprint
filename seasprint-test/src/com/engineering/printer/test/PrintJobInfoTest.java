package com.engineering.printer.test;

import com.engineering.printer.Document;
import com.engineering.printer.PageRange;
import com.engineering.printer.PrintJobInfo;
import com.engineering.printer.PrinterOptions;

import android.test.AndroidTestCase;

public class PrintJobInfoTest extends AndroidTestCase {
	private PrintJobInfo info;
	private Document doc;
	private PrinterOptions opt;
	
	protected void setUp()
	{
		doc = new Document(this.getContext(),"/");
		opt = new PrinterOptions(true, true, 1, "", new PageRange(1,1), true);
		info = new PrintJobInfo(doc, "169", opt);
	}
	
	public void testPrintJobInfo()
	{
		assertEquals(doc, info.getDocument());
		assertEquals(opt, info.getOptions());
		assertEquals("169", info.getPrinter());
	}
}
