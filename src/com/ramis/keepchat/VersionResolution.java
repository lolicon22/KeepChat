package com.ramis.keepchat;

import static de.robv.android.xposed.XposedHelpers.findClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import android.content.Context;
import android.util.SparseArray;

public class VersionResolution {

	private String version;

	private static String basename = KeepChat.SNAPCHAT_PACKAGE_NAME + ".";
	private LoadPackageParam lpparam;

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

	private SparseArray<String> names = new SparseArray<String>();
	private SparseArray<Object[]> params = new SparseArray< Object[]>();

	private Map<String, String> namesReso = new HashMap<String, String>();
	private Map<String, String> paramsReso = new HashMap<String, String>();

	private String namesVersion, paramsVersion;

	public VersionResolution(String version, LoadPackageParam lpparam) {
		this.version = version;
		this.lpparam = lpparam;

		namesReso.put("5.0.23.0", "5.0.23.0");
		paramsReso.put("5.0.23.0", "5.0.23.0");

		namesReso.put("5.0.27.1", "5.0.27.1");
		paramsReso.put("5.0.27.1", "5.0.23.0");

		namesReso.put("5.0.32.1", "5.0.32.1");
		paramsReso.put("5.0.32.1", "5.0.32.1");
		
		namesReso.put("5.0.34.4", "5.0.34.4");
		paramsReso.put("5.0.34.4", "5.0.32.1");

		setNames();
	}

	public SparseArray<String> getNames() {
		return names;
	}

	public SparseArray<Object[]> getParams() {
		return params;
	}

	private void setNames() {
		setVersion();
		createNames();
		createParams();
	}

	private void setVersion() {

		String keyValue, finalVersionCode = "0";

		for (String key : namesReso.keySet()) {

			keyValue = key;
			if (versionCompare(keyValue, this.version) <= 0) {
				if (versionCompare(keyValue, finalVersionCode) == 1) {
					finalVersionCode = keyValue;
				}
			}
		}
		namesVersion = namesReso.get(finalVersionCode);
		paramsVersion = paramsReso.get(finalVersionCode);
	}

	private void createNames() {
		//@formatter:off
		names.put(CLASS_RECEIVEDSNAP, basename + "model.ReceivedSnap");
		names.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, "a");
		names.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"K");
		names.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED,"p");
		names.put(FUNCTION_RECEIVEDSNAP_GETSENDER,"j");
		
		names.put(CLASS_STORY, basename + "model.StorySnap");
		names.put(FUNCTION_STORY_GETIMAGEBITMAP, "a");
		names.put(FUNCTION_STORY_GETSENDER, "at");
		
		names.put(CLASS_SNAPVIEW, basename + "ui.snapview.SnapView");
		names.put(FUNCTION_SNAPVIEW_SHOWIMAGE, "b");
		names.put(FUNCTION_SNAPVIEW_SHOWVIDEO, "a");
		
		names.put(FUNCTION_SNAP_GETTIMESTAMP, "W");
		
		names.put(CLASS_SNAP_PREVIEW_FRAGMENT,basename + "SnapPreviewFragment");
		names.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "v");
		names.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO, "u");
		
		names.put(CLASS_SNAPUPDATE, basename + "model.server.SnapUpdate");
		
		names.put(CLASS_STORYVIEWRECORD, basename + "model.StoryViewRecord");
		
		names.put(FUNCTION_SNAPBRYO_GETSNAPBITMAP, "A");
		names.put(FUNCTION_SNAPBRYO_VIDEOURI, "C");
		names.put(FUNCTION_SNAPBRYO_ISIMAGE,"x");
		
		names.put(CLASS_SNAPSTATEMESSAGE_BUILDER, basename + "model.server.chat.SnapStateMessage.Builder");
		names.put(FUNCTION_SETSCREENSHOTCOUNT, "setScreenshotCount");
		
		names.put(CLASS_SCREENSHOTDETECTOR, basename + "screenshotdetection.ScreenshotDetector");
		names.put(FUNCTION_DECTECTIONSESSION, "b");
		
		if (namesVersion.equals("5.0.27.1")){
			names.put(FUNCTION_STORY_GETSENDER, "ax");
		} else if (namesVersion.equals("5.0.32.1")){
			names.put(FUNCTION_STORY_GETSENDER, "ay");
			names.put(FUNCTION_SNAP_GETTIMESTAMP, "X");
			names.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "A");
			names.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO, "v");
			names.put(FUNCTION_SNAPBRYO_GETSNAPBITMAP, "B");
			names.put(FUNCTION_SNAPBRYO_VIDEOURI, "D");
			names.put(FUNCTION_SNAPBRYO_ISIMAGE,"y");
		} else if (namesVersion.equals("5.0.34.4")){
			names.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI,"J");
			names.put(FUNCTION_STORY_GETSENDER, "az");
			names.put(FUNCTION_SNAP_GETTIMESTAMP, "Y");
			names.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, "A");
			names.put(VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO, "v");
			names.put(FUNCTION_SNAPBRYO_GETSNAPBITMAP, "B");
			names.put(FUNCTION_SNAPBRYO_VIDEOURI, "D");
			names.put(FUNCTION_SNAPBRYO_ISIMAGE,"y");
			names.put(CLASS_SNAPVIEW, basename + "ui.SnapView");
		} 
		//@formatter:on
	}

	private void createParams() {

		//@formatter:off
		params.put(FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP, new Object[] { Context.class });
		params.put(FUNCTION_RECEIVEDSNAP_MARKVIEWED, new Object[] {});
		params.put(FUNCTION_RECEIVEDSNAP_GETVIDEOURI, new Object[] {});
		params.put(FUNCTION_STORY_GETIMAGEBITMAP, new Object[] { Context.class });
		params.put(FUNCTION_SNAPVIEW_SHOWIMAGE, new Object[] {boolean.class, boolean.class, boolean.class, boolean.class });
		params.put(FUNCTION_SNAPVIEW_SHOWVIDEO, new Object[] {boolean.class, boolean.class, boolean.class, boolean.class });
		params.put(FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING, new Object[] {});
		params.put(CLASS_SNAPUPDATE, new Class<?>[] { Long.class, Integer.class, Integer.class, Double.class });
		params.put(CLASS_STORYVIEWRECORD, new Class<?>[] { String.class, Long.class, Integer.class });
		params.put(FUNCTION_SETSCREENSHOTCOUNT, new Object[] { long.class });
		params.put(FUNCTION_DECTECTIONSESSION, new Object[] { ArrayList.class });
		
		if (paramsVersion.equals("5.0.32.1")){
			params.put(CLASS_SNAPUPDATE, new Class<?>[] { findClass(basename + "model.ReceivedSnap",lpparam.classLoader) });
		}
		//@formatter:on
	}
	
	public boolean newSnapUpdate(){
		if (versionCompare(this.version, "5.0.32.1") == -1){
			return false;
		}
		return true;
	}

	/**
	 * Used to compare if a version is older or not by using the version name
	 * 
	 * @param str1
	 *            - version name 1
	 * @param str2
	 *            - version name 2
	 * @return Returns 1 if version1 > version2 <br>
	 *         Returns -1 if version1 < version2 <br>
	 *         Returns 0 if version1 = version2
	 */
	private Integer versionCompare(String str1, String str2) {

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
