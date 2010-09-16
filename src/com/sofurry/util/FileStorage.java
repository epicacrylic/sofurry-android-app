package com.sofurry.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class FileStorage {

	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;
	
	private static String pathroot = "/Android/data/com.sofurry/files/";
	
	/**
	 * Liefert den Root des Pfades zur�ck
	 * @return
	 */
	public static String getPathRoot() {
		return Environment.getExternalStorageDirectory()+pathroot;
	}
	
	/**
	 * Returns the complete filename to the app storage
	 * @param filename
	 * The filename to complete with path
	 * @return
	 */
	public static String getPath(String filename) {
		return getPathRoot()+filename;
	}
	
	/**
	 * Returns a readymade fileoutput stream to store the file into
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static FileOutputStream getFileOutputStream(String filename) throws Exception {
		checkExternalMedia();
		if (!mExternalStorageWriteable) {
			Log.i("FileStorage", "External storage not writeable");
			return null;
		}

		// Check if our datapath exists
		File d = new File(getPath(filename));
		ensureDirectory(d.getParent());
				
		File f = new File(getPath(filename));
		//if (f.canWrite()) {
		//	Log.i("FileStorage", "writing file "+filename);
			FileOutputStream fo = new FileOutputStream(f);
			return fo;
		//}
		//throw new Exception("CanWrite is false, outputstream creation failed.");
	}
	
	/**
	 * Ensures that a Directory exists, and creates it, should it not
	 * @param path
	 * @throws Exception
	 */
	public static void ensureDirectory(String path) throws Exception {
		File d = new File(path);
		if (!d.exists()) d.mkdirs();
	}
	
	/**
	 * Returns true, if the file in question exists
	 * @param filename
	 * The file's filename
	 * @return
	 * @throws IOException
	 */
	public static boolean fileExists(String filename) throws IOException {
		File f = new File(getPath(filename));
		
		return f.exists();
	}

	public static FileInputStream getFileInputStream(String filename) throws FileNotFoundException {
		checkExternalMedia();
		if (!mExternalStorageAvailable) {
			Log.i("FileStorage", "External storage not readable");
			return null;
		}
		
		File f = new File(getPath(filename));
		if (f.canRead()) {
			return new FileInputStream(f);
		} else {
			Log.i("FileStorage", "Can't read file "+filename);
		}
		return null;
	}
		
	
	private static void checkExternalMedia() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
}
