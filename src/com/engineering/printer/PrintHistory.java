package com.engineering.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

;
/**
 * Stores and retrieves printing history. Printing history contains a fixed
 * number of recently printed items.
 * 
 * @author Jun YING
 * 
 */
public class PrintHistory {
	private PrintHistory() {
	}

	private final static String PREF_NAME = "SEASPrintHistory";
	private final static String HISTORY_KEY = "PrintHistory";
	/**
	 * Maximum number of history entries
	 */
	private final static int MAX_ITEMS = 7;

	/**
	 * Get a list of history items
	 * Each item is a uri string. The most recent item is the first item.
	 * @param c Context
	 * @return a list of history items.
	 */
	public static List<String> getHistory(Context c) {
		SharedPreferences s = c.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		String h = s.getString(HISTORY_KEY, "");
		if("".equals(h))
			return new ArrayList<String>();
		else
			return Arrays.asList(h.split("\n"));
	}

	private static String join(Collection<?> s, String delimiter) {
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}

	/**
	 * Put a new item to the history.
	 * Each item is a uri string. The most recent item is the first item.
	 * This method will merge adjacent identical items.
	 * @param c Context
	 * @param uriStr 
	 */
	public static void putHistory(Context c, String uriStr) {
		List<String> history = getHistory(c);
		
		ArrayList<String> history_no_dup = new ArrayList<String>();
		history_no_dup.add(uriStr);
		for(String s: history)
		{
			if(!history_no_dup.contains(s))
				history_no_dup.add(s);
			
		}
		
		while (history_no_dup.size() > MAX_ITEMS) {
			history_no_dup.remove(history_no_dup.size()-1);
		}
		
		SharedPreferences s = c.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = s.edit();
		editor.putString(HISTORY_KEY, join(history_no_dup, "\n"));
		editor.commit();
	}
}
