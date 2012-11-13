package com.engineering.printer;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
/**
 * Document that is pending to be printed
 */
public class Document {
	private boolean isRemote = false;
    private String remotePath = "";
    private Uri uri;
    
    private InputStream mInputStream;
    
    private String mDisplayName = "N/A";
    private String mType = "";
    private String mExt = "";
    
    private Context context;

    /*
     * Mime-type of Microsoft office documents
     */
	private final static String [] msDocsMimeType = {
			"application/vnd.ms-powerpoint",
			"application/vnd.ms-excel",
			"application/msword",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

	/**
	 * mime type prefix of other supported documents
	 */
	private final static String [] otherDocsMimeType = {
		"text/",
		"image/",
		"application/pdf"	
		};
	
	/**
	 * extension of microsoft office documents
	 */
	private final static String[] msDocsExt = { "ppt", "pps", "xls", "doc",
		"docx", "xlsx", "pptx", "ppsx" };

	/**
	 * Other extensions of supported documents
	 */
	private final static String[] otherDocsExt = { "bmp", "gif", "jpg", "png",
			"txt", "rtf", "pdf" };

	/**
	 * Give a filename appropriate for display.
	 * @param context
	 * @param uriStr a Uri string or a file path.
	 * @return
	 */
	public static String guessDisplayName(Context context, String uriStr)
	{
		Uri uri = Uri.parse(uriStr);
		String fileName = uriStr;
		if(uri!=null)
		{
			String scheme = uri.getScheme();
			if ("file".equals(scheme))
			{
				fileName = uri.getLastPathSegment();
			}
			else if("content".equals(scheme)){
			    String[] proj = { MediaStore.Images.Media.TITLE };
			    try
			    {
				    Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
				    if (cursor != null && cursor.getCount() != 0) {
				        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
				        cursor.moveToFirst();
				        fileName = cursor.getString(columnIndex);
				    }
			    }
			    catch(Exception e)
			    { }
			}
			else
				fileName = uriStr.substring(uriStr.lastIndexOf("/")+1,
						uriStr.length());
		}
		return fileName;
	}

	public static String guessExt(String path) {
		return path.substring(path.lastIndexOf(".")+1,path.length());
	}
	
	/**
	 * Load a resource. Because mime type is not given, this method will also try to 
	 * guess the type based on file name and its content. If all attempts fail,
	 * mime type will be left as an empty string.
	 * Set the display name to the base name if it's a file. Otherwise, set the display
	 * name as the uri.
	 * @param context
	 * @param uri
	 */
    public Document(Context context, Uri uri)
    {
		this.context = context;
		this.uri = uri;
    	
		setDisplayName( guessDisplayName(context, uri.toString()) );
		mExt = guessExt(uri.toString());
		
    	String typeFromName = URLConnection.guessContentTypeFromName(uri.toString()); 
    	if(typeFromName != null)
    		setMimeType(typeFromName);
    	else
    	{
    		try
    		{
	    		InputStream is = getStream();
	        	String typeFromStream = URLConnection.guessContentTypeFromStream(is);
	        	if(typeFromStream != null)
	        		setMimeType(typeFromStream);
    		}
    		catch(Exception e)
    		{ 
    			e.printStackTrace();
    		}
    	}
    }

    /**
     * Load a document and set the mime type as specified.
     * Set the display name to the base name if it's a file. Otherwise, set the display
	 * name as the uri.
     * @param context
     * @param uri
     * @param mimeType
     */
    public Document(Context context, Uri uri, String mimeType)
    {
		this.context = context;
		this.uri = uri;
    	
		setDisplayName( guessDisplayName(context, uri.toString()) );
		mExt = guessExt(uri.toString());
        setMimeType(mimeType);
	}   
    
    /**
     * Specify a remote document. 
     * @param context
     * @param remotePath The file path on the remote server
     */
    public Document(Context context, String remotePath)
    {
		this.context = context;
    	this.isRemote = true;
    	this.remotePath = remotePath;
    	
    	String typeFromName = URLConnection.guessContentTypeFromName(remotePath); 
    	if(typeFromName != null)
    		setMimeType(typeFromName);
    	
    	setDisplayName( guessDisplayName(context, remotePath) );
    	mExt = guessExt(remotePath);
    }


    public boolean isRemote()
    {
    	return isRemote;
    }
    
    public String getRemotePath()
    {
    	return remotePath;
    }
    
    public void setMimeType(String mimeType)
    {
    	if(mimeType != null)
    		mType = mimeType;
    }
    
    public String getMimeType()
    {
    	return mType;
    }
    
    /**
     * Is this document a microsoft office document?
     * Determined based on mime type and file name extension.
     * @return
     */
    public boolean IsMicrosoft() {
    	for(String s : msDocsMimeType)
    	{
    		if (mType.equals(s))
    			return true;
    	}
		for(String s : msDocsExt)
    	{
    		if (mExt.equalsIgnoreCase(s))
    			return true;
    	}
        return false;
    }
    
    /**
     * Is this document supported?
     * Determined based on mime type and file name extension.
     * @return
     */
    public boolean IsSupported()
    {
		for(String s : otherDocsMimeType)
    	{
    		if (mType.startsWith(s))
    			return true;
    	}
		for(String s : otherDocsExt)
    	{
    		if (mExt.equalsIgnoreCase(s))
    			return true;
    	}
		return IsMicrosoft();
    }

    /**
     * Set the display name of the document.
     * @param displayName
     */
    public void setDisplayName(String displayName) {
    	if(displayName != null)
    		mDisplayName = displayName;
    }
    
    /**
     * get the display name.
     * @return
     */
    public String getDisplayName(){
    	return mDisplayName;
    }
    
    /**
     * Get the input stream to read this document.
     * @return The InputStream. Null if it's a remote document.
     * @throws FileNotFoundException 
     */
    public InputStream getStream() throws FileNotFoundException{
    	return context.getContentResolver().openInputStream(uri);
    }
    
    /**
     * Get the data of this document.
     * Poor performance on large document.
     * @return
     * @throws IOException 
     * @see getStream()
     */
    public byte[] getData() throws IOException{
    	InputStream is = getStream();
    	int count = is.available();
    	byte [] data = new byte[count];
		is.read(data);
    	return data;
    }
}
