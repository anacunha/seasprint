package com.engineering.printer.test;

import com.engineering.printer.*;
import android.test.AndroidTestCase;

public class DocumentTest extends AndroidTestCase {

	public void testIsMicrosoft_Ext()
	{
		String[] msDocsExt = { "ppt", "pps", "xls", "doc",
				"docx", "xlsx", "pptx", "ppsx" };
		for(String ext :msDocsExt)
		{
			Document doc = new Document(this.getContext(), "/a." + ext);
			assertTrue(doc.IsMicrosoft());
			assertTrue(doc.IsSupported());
		}
	}
	
	public void testIsMicrosoft_Ext_Case()
	{
		String[] msDocsExt = { "pPt", "pPs", "XLs", "DOC" };
		for(String ext :msDocsExt)
		{
			Document doc = new Document(this.getContext(), "/a." + ext);
			assertTrue(doc.IsMicrosoft());
			assertTrue(doc.IsSupported());
		}
	}
	
	public void testIsMicrosoft_Ext_NonMS()
	{
		String[] otherDocsExt = { "bmp", "gif", "jpg", "png",
			"txt", "rtf", "pdf" };
		for(String ext :otherDocsExt)
		{
			Document doc = new Document(this.getContext(), "/a." + ext);
			assertFalse(doc.IsMicrosoft());
			assertTrue(doc.IsSupported());
		}
	}
}
