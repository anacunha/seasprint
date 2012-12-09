package com.engineering.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;

;
/**
 * Stores and retrieves printing history. Printing history contains a fixed
 * number of recently printed items.
 * 
 * @author Jun YING
 * 
 */
public class HistoryManager {
	private Context c;
	private String mPrefName;
	private String mHistKey;
	/**
	 * Maximum number of history entries
	 */
	private int mMaxItems;
	
	public HistoryManager(Context c, String prefName, String histKey, int maxItems) {
		this.c = c;
		this.mPrefName = prefName;
		this.mHistKey = histKey;
		this.mMaxItems = maxItems;
	}

	/**
	 * Get a list of history items
	 * Each item is a uri string. The most recent item is the first item.
	 * @param c Context
	 * @return a list of history items.
	 */
	public List<String> getHistory() {
		SharedPreferences s = c.getSharedPreferences(mPrefName,
				Context.MODE_PRIVATE);
		String h = s.getString(mHistKey, "");
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
	 * This method will merge identical items, leaving only the most recent one.
	 * If history items exceed MAX_NUM, the least recent items will be removed.
	 * @param c Context
	 * @param uriStr 
	 */
	public void putHistory(String uriStr) {
		List<String> history = getHistory();
		
		ArrayList<String> history_no_dup = new ArrayList<String>();
		history_no_dup.add(uriStr);
		for(String s: history)
		{
			if(!history_no_dup.contains(s))
				history_no_dup.add(s);
			
		}
		
		while (history_no_dup.size() > mMaxItems) {
			history_no_dup.remove(history_no_dup.size()-1);
		}
		
		SharedPreferences s = c.getSharedPreferences(mPrefName,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = s.edit();
		editor.putString(mHistKey, join(history_no_dup, "\n"));
		editor.commit();
	}
}
