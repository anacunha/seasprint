package com.engineering.printer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.engineering.printer.TextWithIconAdapter.ItemWithIcon;
import com.engineering.printer.TextWithIconAdapter.SimpleItemWithIcon;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import io.filepicker.FilePicker;
import io.filepicker.FilePickerAPI;

/**
 * 
 * Welcome screen of the app. Allows the user to choose the location
 * where the documents to print are stored.
 * @author Jun Ying
 *
 */
public class SourceSelectScreen extends Activity implements OnItemClickListener {
	private static final String FILEPICKER_KEY = "AC0gvvjhEQzK-cP5HE5cLz";

	//Request codes for different activities
	private final int REQUEST_PRINT = 1;
	private final int REQUEST_SDCARD = 2;
	private final int REQUEST_ENIAC = 3;
	private final int REQUEST_THIRDPARTY = 4;

	//The combined adapter
	private SeparatedListAdapter section_adapter;
	
	private List<String> m3rdPartyPackageName = new ArrayList<String>();
	
	private List<ItemWithIcon> history_entries = new ArrayList<ItemWithIcon>();;
	private List<String> history_items = new ArrayList<String>();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sourceselect);
		
		//Set FilePicker key
		FilePickerAPI.setKey(FILEPICKER_KEY);

		//Prepare the file source list items
		final ListView lvFileSource = (ListView) findViewById(R.id.lvFileSource);
		lvFileSource.setOnItemClickListener(this);

		//Feature lists
		List<ItemWithIcon> primary_entries = new ArrayList<ItemWithIcon>();
		primary_entries.add(new SimpleItemWithIcon("SD Card", R.drawable.sdcard_icon,
				null));
		primary_entries.add(new SimpleItemWithIcon("Remote ENIAC Server",
				R.drawable.eniac_icon, null));
		primary_entries.add(new SimpleItemWithIcon("Cloud Services",
				R.drawable.filepicker_icon, null));
		
		//List third party file browser
		List<ItemWithIcon> thirdparty_entries = new ArrayList<ItemWithIcon>();
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(
				intent, PackageManager.GET_RESOLVED_FILTER);

		for (ResolveInfo info : resInfo) {
			ActivityInfo aInfo = info.activityInfo;
			if (aInfo != null) {
				ApplicationInfo appInfo = aInfo.applicationInfo;
				m3rdPartyPackageName.add(appInfo.packageName);
				thirdparty_entries.add(new SimpleItemWithIcon(appInfo.loadLabel(getPackageManager()), null, 
						appInfo.loadIcon(getPackageManager())));

			}
		}
		
		//Put them on the listview
		section_adapter = new SeparatedListAdapter(this);
		
		TextWithIconAdapter primary_adapter = new TextWithIconAdapter(this,
				R.layout.filesource_item, primary_entries);
		section_adapter.addSection("PRINT FROM", primary_adapter);
		
		if(!thirdparty_entries.isEmpty())
		{
			TextWithIconAdapter thirdparty_adapter = new TextWithIconAdapter(this,
					R.layout.filesource_item, thirdparty_entries);
			section_adapter.addSection("ALSO FROM APPS", thirdparty_adapter);
		}
				
		TextWithIconAdapter history_adapter = new TextWithIconAdapter(this,
				R.layout.file_item, history_entries);
		section_adapter.addSection("HISTORY", history_adapter);
	

		lvFileSource.setAdapter(section_adapter);

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		HistoryManager history = new HistoryManager(this, "SEASPrintHistory", "FileHistory", 7);
		this.history_items = history.getHistory();
		history_entries.clear();
		if(history_items.isEmpty())
			history_entries.add(new SimpleItemWithIcon("No history yet.", null, null));
		else
		{
			for(String s:history_items)
			{
				history_entries.add(new SimpleItemWithIcon(Document.guessDisplayName(this, s), R.drawable.file_icon, null));
			}
		}
		section_adapter.notifyDataSetChanged();
	
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Result was cancelled by the user or there was an error
		if (resultCode != RESULT_OK)
			return;
		
		if (requestCode == REQUEST_PRINT)
			;// Do nothing
		else if (requestCode == FilePickerAPI.REQUEST_CODE_GETFILE
				|| requestCode == REQUEST_SDCARD 
				|| requestCode == REQUEST_THIRDPARTY) {

			//Read uri
			Uri uri = null;
			if (requestCode == REQUEST_SDCARD) {
				String fileChosen = data.getExtras().getString(
						"com.engineering.printer.fileChosen");
				uri = Uri.fromFile(new File(fileChosen));
			} else if (requestCode == REQUEST_THIRDPARTY
					|| requestCode == FilePickerAPI.REQUEST_CODE_GETFILE)
				uri = data.getData();
			
			if(uri!=null)
			{
				//Send Intent to PrinterSelectScreen
				Intent printIntent = new Intent(this, PrinterSelectScreen.class);
				printIntent.setDataAndType(uri, data.getType());
				startActivityForResult(printIntent, REQUEST_PRINT);
			}
			else
			{
				Toast.makeText(this, "Cannot read file.",
						Toast.LENGTH_LONG).show();
			}
		} else if (requestCode == REQUEST_ENIAC) {
			if(data.getExtras() == null)
				return;
			String fileChosen = data.getExtras().getString("com.engineering.printer.fileChosen");
			if(fileChosen == null)
				return;
			//Send Intent to PrinterSelectScreen
			Intent printIntent = new Intent(this, PrinterSelectScreen.class);
			printIntent.putExtra("com.engineering.printer.remotePath", fileChosen);
			startActivityForResult(printIntent, REQUEST_PRINT);
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position < section_adapter.getLowestIndex(1))
		{
			int inner_position = position - section_adapter.getLowestIndex(0); 
			if (inner_position == 0) { //SD CARD
				Intent intent = new Intent(this, LocalFileBrowser.class);
				startActivityForResult(intent, REQUEST_SDCARD);
			} else if (inner_position == 1) { //Remote ENIAC Server
				LoginScreen.resetConnection();
				Intent intent = new Intent(this, RemoteFileBrowser.class);
				startActivityForResult(intent, REQUEST_ENIAC);
			} else if (inner_position == 2) { //FilePicker API
				Intent intent = new Intent(this, FilePicker.class);
				startActivityForResult(intent, FilePickerAPI.REQUEST_CODE_GETFILE);
			}
		}
		else if(!m3rdPartyPackageName.isEmpty() && position < section_adapter.getLowestIndex(2)) // start 3rd party file browser
		{
			try
			{
				int inner_position = position - section_adapter.getLowestIndex(1);
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				intent.setPackage(m3rdPartyPackageName.get(inner_position));
				startActivityForResult(intent, REQUEST_THIRDPARTY);
			}
			catch(android.content.ActivityNotFoundException e)
			{
				Log.e("SourceSelectScreen", "App activity not found.");
			}
		}
		else
		{
			int inner_position;
			if(m3rdPartyPackageName.isEmpty())
				inner_position = position - section_adapter.getLowestIndex(1);
			else
				inner_position = position - section_adapter.getLowestIndex(2);
			if(!history_items.isEmpty()) //In case clicking on "No history yet."
			{
				Uri uri = Uri.parse(history_items.get(inner_position));
				//Send Intent to PrinterSelectScreen
				Intent printIntent = new Intent(this, PrinterSelectScreen.class);
				printIntent.setData(uri);
				startActivityForResult(printIntent, REQUEST_PRINT);
			}
		}
			
	}

}
