package com.engineering.printer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.content.Context;
import android.net.Uri;
/**
 * Document that is pending to be printed
 */
public class Document {
	private boolean isRemote = false;
    private String remotePath = "";
    
    private byte [] mData;
    
    private String mDisplayName = "N/A";
    private String mType = "";
    private String mExt = "";
    /*
     * Mime type of microsoft office documents
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
	 * Load a uri resource to mData. 
	 * Set the display name to the base name if it's a file. Otherwise, set the display
	 * name as the uri. 
	 * @param context
	 * @param uri
	 * @throws IOException
	 */
	private void load(Context context, Uri uri) throws IOException {
		InputStream datain = context.getContentResolver().openInputStream(uri);
		int count = datain.available();
		mData = new byte[count];
	
		datain.read(mData, 0, count);

		guessDisplayName(uri.toString());
	}
	
	private void guessDisplayName(String uriStr)
	{
		if (uriStr.substring(0, 4).equals("file"))
			setDisplayName(uriStr.substring(uriStr.lastIndexOf("/")+1,
					uriStr.length()));
		else {
			setDisplayName(uriStr);
		}
		
		mExt = uriStr.substring(uriStr.lastIndexOf(".")+1,uriStr.length());
	}
	
	/**
	 * Load a resource. Because mime type is not given, this method will also try to 
	 * guess the type based on file name and its content. If all attempts fail,
	 * mime type will be left as an empty string.
	 * Set the display name to the base name if it's a file. Otherwise, set the display
	 * name as the uri.
	 * @param context
	 * @param uri
	 * @throws IOException
	 */
    public Document(Context context, Uri uri) throws IOException
    {
    	load(context, uri);
    	
    	String typeFromName = URLConnection.guessContentTypeFromName(uri.toString()); 
    	if(typeFromName != null)
    		setMimeType(typeFromName);
    	else
    	{
    		ByteArrayInputStream is = new ByteArrayInputStream(mData);
        	String typeFromStream = URLConnection.guessContentTypeFromStream(is);
        	if(typeFromStream != null)
        		setMimeType(typeFromStream);
    	}
    }

    /**
     * Load a document and set the mime type as specified.
     * Set the display name to the base name if it's a file. Otherwise, set the display
	 * name as the uri.
     * @param context
     * @param uri
     * @param mimeType
     * @throws IOException
     */
    public Document(Context context, Uri uri, String mimeType) throws IOException
    {
    	load(context, uri);
        setMimeType(mimeType);
	}   
    
    /**
     * Specify a remote document. 
     * @param remotePath
     */
    public Document(String remotePath)
    {
    	this.isRemote = true;
    	this.remotePath = remotePath;
    	
    	String typeFromName = URLConnection.guessContentTypeFromName(remotePath); 
    	if(typeFromName != null)
    		setMimeType(typeFromName);
    	
    	setDisplayName(remotePath.substring(remotePath.lastIndexOf("/")+1,
    			remotePath.length()));
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
     * Read the data of this document.
     * @return
     */
    public byte[] getData(){
    	return mData;
    }
    
}
