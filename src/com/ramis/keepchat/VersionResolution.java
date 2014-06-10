package com.ramis.keepchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.SparseArray;

public class VersionResolution {

	private String version;

	private static String basename = KeepChat.SNAPCHAT_PACKAGE_NAME + ".";

	//@formatter:off
	final static int CLASS_RECEIVEDSNAP = 0;                     				// ReceivedSnap class name
	final static int FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP = 1;    				// ReceivedSnap.getImageBitmap() function name
	final static int FUNCTION_RECEIVEDSNAP_GETVIDEOURI = 2;      				// ReceivedSnap.getVideoUri() function name
	final static int FUNCTION_RECEIVEDSNAP_MARKVIEWED = 3;       				// ReceivedSnap.markViewed() function name
	final static int FUNCTION_RECEIVEDSNAP_GETSENDER = 4;       				// ReceivedSnap.getSender() function name
	
	final static int CLASS_STORY = 10;                             				// Story class name
	final static int FUNCTION_STORY_GETIMAGEBITMAP = 11;          				// Story.getImageBitmap() function name
    final static int FUNCTION_STORY_GETSENDER = 12;              				// Story.getSender()
    
    final static int CLASS_SNAPVIEW = 20;                        				// SnapView class name
    final static int FUNCTION_SNAPVIEW_SHOWIMAGE = 21;           				// SnapView.showImage() function name
    final static int FUNCTION_SNAPVIEW_SHOWVIDEO = 22;           				// SnapView.showVideo() function name
    
    final static int FUNCTION_SNAP_GETTIMESTAMP = 30;            				// Snap.getTimestamp()
    
    final static int CLASS_SNAP_PREVIEW_FRAGMENT = 40;			  				// SnapPreviewFragment class name
    final static int FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING = 41;	// SnapPreviewFragment.prepareSnapForSending() function name
    final static int VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO = 42;				// SnapBryo class variable name
    
    final static int CLASS_SNAPUPDATE = 50;										// SnapUpdate class name
    
    final static int CLASS_STORYVIEWRECORD = 60;								// StoryViewRecord class name
    
    final static int FUNCTION_SNAPBRYO_GETSNAPBITMAP = 70;						// SnapBryo.getImageBitmap() function name
    final static int FUNCTION_SNAPBRYO_VIDEOURI = 71;							// SnapBryo.getVideoUri() function name
    final static int FUNCTION_SNAPBRYO_ISIMAGE = 72;						    // SnapBryo.isImage() function name. image = 0, video = 1
    
    final static int CLASS_SNAPSTATEMESSAGE_BUILDER = 80;						// SnapStateMessage.Builder class name
    final static int FUNCTION_SETSCREENSHOTCOUNT = 81;							// SnapStateMessage.Builder.setScreenshotCount() function name
    
    final static int CLASS_SCREENSHOTDETECTOR = 90;								// ScreenshotDetector class name
    final static int FUNCTION_DECTECTIONSESSION = 91;							// ScreenshotDetector.runDectectionSession() function name
    //@formatter:on

	private Map<String,SparseArray<String>> versionResolutionNames =  new HashMap<String, SparseArray<String>>();
	private Map<String,SparseArray<Object[]>> versionResolutionParams = new HashMap<String, SparseArray<Object[]>>(); 

	private SparseArray<String> currentVersionNames = new SparseArray<String>();
	private SparseArray<Object[]> currentParams = new SparseArray<Object[]>();

	private SparseArray<String> names_50230 = new SparseArray<String>();

	private SparseArray<Object[]> params_50230 = new SparseArray<Object[]>();

	public VersionResolution(String version) {
		this.version = version;
		
		//@formatter:off
		versionResolutionNames.put("5.0.23.0", names_50230); // 5.0.23.0
		versionResolutionParams.put("5.0.23.0", params_50230); // 5.0.23.0
		//@formatter:on
		setNames();
	}

	public SparseArray<String> getNames() {
		return currentVersionNames;
	}

	public SparseArray<Object[]> getParams() {
		return currentParams;
	}

	private void setNames() {
		createNames();
		createParams();
		setCurrentVersionNames();
	}

	private void setCurrentVersionNames() {

		String keyValue, finalVersionCode = "0";
		
		for (String key : versionResolutionNames.keySet()){
			
			keyValue = key;
			if (versionCompare(keyValue, this.version)  <= 0) {
				if (versionCompare(keyValue, finalVersionCode) == 1) {
					finalVersionCode = keyValue;
				}
			}
		}
		currentVersionNames = versionResolutionNames.get(finalVersionCode);
		currentParams = versionResolutionParams.get(finalVersionCode);
	}

	private void createNames() {
		//@formatter:off
		names_50230.put(CLASS_RECEIVEDSNAP, basename + "model.ReceivedSnap");
		names_50230.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, "a");
		names_50230.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"K");
		names_50230.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"p");
		names_50230.put(FUNCTION_RECEIVEDSNAP_GETSENDER,"j");
		
		names_50230.put(CLASS_STORY, basename + "model.StorySnap");
		names_50230.put(FUNCTION_STORY_GETIMAGEBITMAP, "a");
		names_50230.put(FUNCTION_STORY_GETSENDER, "at");
		
		names_50230.put(CLASS_SNAPVIEW, basename + "ui.snapview.SnapView");
		names_50230.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "b");
		names_50230.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "a");
		
		names_50230.put(FUNCTION_SNAP_GETTIMESTAMP, "W");
		
		names_50230.put(CLASS_SNAP_PREVIEW_FRAGMENT,basename + "SnapPreviewFragment");
		names_50230.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "v");
		names_50230.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO, "u");
		
		names_50230.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		
		names_50230.put(CLASS_STORYVIEWRECORD, basename + "model.StoryViewRecord");
		
		names_50230.put(FUNCTION_SNAPBRYO_GETSNAPBITMAP, "A");
		names_50230.put(FUNCTION_SNAPBRYO_VIDEOURI, "C");
		names_50230.put(FUNCTION_SNAPBRYO_ISIMAGE,"x");
		
		names_50230.put(CLASS_SNAPSTATEMESSAGE_BUILDER, basename + "model.server.chat.SnapStateMessage.Builder");
		names_50230.put(FUNCTION_SETSCREENSHOTCOUNT, "setScreenshotCount");
		
		names_50230.put(CLASS_SCREENSHOTDETECTOR, basename + "screenshotdetection.ScreenshotDetector");
		names_50230.put(FUNCTION_DECTECTIONSESSION, "b");
		//@formatter:on
	}

	private void createParams() {

		//@formatter:off
		params_50230.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, new Object[] { Context.class });
		params_50230.put(FUNCTION_STORY_GETIMAGEBITMAP, new Object[] { Context.class });
		params_50230.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI, new Object[] {});
		params_50230.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, new Object[] {});
		params_50230.put(FUNCTION_SNAPVIEW_SHOWIMAGE, new Object[] {boolean.class, boolean.class, boolean.class, boolean.class });
		params_50230.put(FUNCTION_SNAPVIEW_SHOWVIDEO, new Object[] {boolean.class, boolean.class, boolean.class, boolean.class });
		params_50230.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED, new Object[] {});
		params_50230.put(CLASS_SNAPUPDATE, new Class<?>[] { Long.class, Integer.class, Integer.class, Double.class });
		params_50230.put(CLASS_STORYVIEWRECORD, new Class<?>[] { String.class, Long.class, Integer.class });
		params_50230.put(FUNCTION_SETSCREENSHOTCOUNT, new Object[] { long.class });
		params_50230.put(FUNCTION_DECTECTIONSESSION, new Object[] { ArrayList.class });
		//@formatter:on
	}
	
	/**
	 * Used to compare if a version is older or not by using the version name
	 * @param str1 - version name 1
	 * @param str2 - version name 2
	 * @return
	 * Returns 1 if version1 > version2 <br> Returns -1 if version1 < version2 <br> Returns 0 if version1 = version2 
	 */
	private static Integer versionCompare(String str1, String str2) {

		int version1 = Integer.parseInt(str1.replaceAll("\\.", "").replaceAll(
				"[^0-9.]", ""));
		int version2 = Integer.parseInt(str2.replaceAll("\\.", "").replaceAll(
				"[^0-9.]", ""));

		if (version1 > version2) {
			return 1;
		} else if (version1 < version2) {
			return -1;
		} else {
			if (str1.length() == str2.length()) {
				return 0;
			} else if (str1.length() > str2.length()) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
}
