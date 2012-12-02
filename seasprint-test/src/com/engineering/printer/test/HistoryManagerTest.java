package com.engineering.printer.test;

import java.util.List;

import com.engineering.printer.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

public class HistoryManagerTest extends AndroidTestCase {

	private HistoryManager manager;
	protected void setUp()
	{
		manager = new HistoryManager(this.getContext(), "HMTest", "HMTest", 3);
		SharedPreferences s = getContext().getSharedPreferences("HMTest",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = s.edit();
		editor.putString("HMTest", "");
		editor.commit();
	}
	
	public void testPutHistory_Simple()
	{
		manager.putHistory("aaa");
		manager.putHistory("bbb");
		SharedPreferences s = getContext().getSharedPreferences("HMTest",
				Context.MODE_PRIVATE);
		assertEquals("bbb\naaa", s.getString("HMTest",""));
	}
	
	public void testPutHistory_Duplicate()
	{
		manager.putHistory("aaa");
		manager.putHistory("bbb");
		manager.putHistory("aaa");
		SharedPreferences s = getContext().getSharedPreferences("HMTest",
				Context.MODE_PRIVATE);
		assertEquals("aaa\nbbb", s.getString("HMTest",""));
	}
	
	public void testPutHistory_MaxItems()
	{
		manager.putHistory("aaa");
		manager.putHistory("bbb");
		manager.putHistory("ccc");
		manager.putHistory("ddd");
		SharedPreferences s = getContext().getSharedPreferences("HMTest",
				Context.MODE_PRIVATE);
		assertEquals("ddd\nccc\nbbb", s.getString("HMTest",""));
	}
	
	public void testGetHistory()
	{
		SharedPreferences s = getContext().getSharedPreferences("HMTest",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = s.edit();
		editor.putString("HMTest", "aaa\nbbb\nccc");
		editor.commit();
		List<String> hist = manager.getHistory();
		assertEquals("aaa", hist.get(0));
		assertEquals("bbb", hist.get(1));
		assertEquals("ccc", hist.get(2));
	}
}
