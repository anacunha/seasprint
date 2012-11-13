package com.engineering.printer;

import java.util.List;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A list view adapter that can display a text long with an icon.
 * User of TextWithIconAdapter should implement TextWithIconAdapter.ItemWithIcon 
 * @author Jun Ying
 *
 */
public class TextWithIconAdapter extends
		ArrayAdapter<TextWithIconAdapter.ItemWithIcon> {
	/**
	 * An interface for list item that TextWithIconAdapter recognize.
	 * @author Jun Ying
	 *
	 */
	public interface ItemWithIcon {
		public CharSequence getText();

		/**
		 * Get icon resource id. It's okay to implement only one of
		 * getIconResourceId() and getIconDrawable(), and let another always
		 * return null.
		 * 
		 * @return icon resource id.
		 */

		public Integer getIconResourceId();

		/**
		 * Get an drawable object for the icon. It's okay to implement only one
		 * of getIconResourceId() and getIconDrawable(), and let another always
		 * return null.
		 * 
		 * @return drawable object.
		 */
		public Drawable getIconDrawable();
	}

	/**
	 * A simple implementation of TextWithIconAdapter
	 * @author Jun Ying
	 *
	 */
	public static class SimpleItemWithIcon implements
			TextWithIconAdapter.ItemWithIcon {
		CharSequence t;
		Integer resid;
		Drawable drawable;

		/**
		 * Only need to specify one of resid and drawable. If both are null this list item will
		 * have no icon. 
		 * @param t The text label of this icon
		 * @param resid Resource id for the item
		 * @param drawable A Drawable object for this icon
		 */
		public SimpleItemWithIcon(CharSequence t, Integer resid, Drawable drawable) {
			this.t = t;
			this.resid = resid;
			this.drawable = drawable;
		}

		public CharSequence getText() {
			return t;
		}

		public Integer getIconResourceId() {
			return resid;
		}

		public Drawable getIconDrawable() {
			return drawable;
		}
	}

	private ImageView iv;
	private TextView tv;
	int textViewResourceId;

	/**
	 * Create an adapter.
	 * @param context
	 * @param textViewResourceId the layout of list item.
	 * @param items all list items
	 */
	public TextWithIconAdapter(Context context, int textViewResourceId,
			List<ItemWithIcon> items) {
		super(context, textViewResourceId, items);
		this.textViewResourceId = textViewResourceId;
	}

	/**
	 * Create an adapter.
	 * @param context
	 * @param textViewResourceId the layout of list item.
	 * @param items all list items
	 */
	public TextWithIconAdapter(Context context, int textViewResourceId,
			ItemWithIcon[] items) {
		super(context, textViewResourceId, items);
		this.textViewResourceId = textViewResourceId;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			// ROW INFLATION
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(this.textViewResourceId, parent, false);
		}

		// Get item
		ItemWithIcon item = this.getItem(position);

		// Get reference to ImageView
		iv = (ImageView) row.findViewById(R.id.item_icon);

		// Get reference to TextView
		tv = (TextView) row.findViewById(R.id.item_text);

		// Set country name
		tv.setText(item.getText());

		if(item.getIconResourceId()!=null)
			iv.setImageResource(item.getIconResourceId());
		else if(item.getIconDrawable()!=null)
			iv.setImageDrawable(item.getIconDrawable());
		else
			iv.setImageResource(0);

		return row;
	}
}
