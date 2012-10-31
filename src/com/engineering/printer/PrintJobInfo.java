package com.engineering.printer;

/**
 * A helper data structure that records relevant information for a printing job
 * @author Jun Ying
 *
 */
public class PrintJobInfo
{
	/**
	 * The document to print
	 */
	public Document doc;
	/**
	 * The path for this document on the remote ENIAC server
	 */
	public String remoteFilename;
	/**
	 * The name of the printer to be used
	 */
	public String printer;
	/**
	 * Should we enable duplex printing?
	 */
	public boolean duplex;
	/**
	 * number of copies to be printed
	 */
	public int numCopies;
}