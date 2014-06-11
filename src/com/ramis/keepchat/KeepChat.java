package com.ramis.keepchat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

public class KeepChat implements IXposedHookLoadPackage {

	private static final String PACKAGE_NAME = KeepChat.class.getPackage()
			.getName();
	public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";

	XSharedPreferences prefs;

	final int SAVE_AUTO = 0;
	final int SAVE_ASK = 1;
	final int DO_NOT_SAVE = 2;

	private String savePath;
	private int imagesSnapSavingMode;
	private int videosSnapSavingMode;
	private int imagesStorySavingMode;
	private int videosStorySavingMode;
	private boolean toastMode;
	private int toastLength;
	private boolean saveSentSnaps;
	private boolean debugMode;
	private boolean sortFilesMode;
	private boolean sortFileUsername;

	private boolean isStory = false;
	private boolean isSnap = false;
	private boolean isSnapImage = false;
	private boolean isSnapVideo = false;
	private boolean displayDialog = false;

	private boolean isSaved = false;

	private String toastMessage = "";

	private String mediaPath = "";

	private String prevFileName = "";

	private Context context;

	private String versionName;

	private boolean isImmune = false;
	private String senderName = "";

	private ArrayList<String> gods = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 305323815158425760L;

		{
			add("2d8fb4c2fe931aefc4abaddaedc45708"); // r
			add("955f633fdb4a6dce8e99254b93fe0807"); // w
		}
	};

	// loading package
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SNAPCHAT_PACKAGE_NAME))
			return;

		initialInfoLogAndGetVersion(lpparam);

		VersionResolution resolution = new VersionResolution(versionName);

		final SparseArray<String> names = resolution.getNames();
		final SparseArray<Object[]> params = resolution.getParams();

		try {
			refreshPreferences();
			printSettings();

			/*
			 * getImageBitmap() hook The ReceivedSnap class has a method to load
			 * a Bitmap in preparation for viewing. This method returns said
			 * bitmap back so the application can display it. We hook this
			 * method to intercept the result and write it to the SD card. The
			 * file path is stored in the mediaPath member for later use in the
			 * showImage() hook.
			 */
			findAndHookMethod(
					names.get(VersionResolution.CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP),
					appendValue(
							params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {

									String sender = (String) callMethod(
											param.thisObject,
											names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETSENDER));

									SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
											"yyyy-MM-dd_HH-mm-ss", Locale
													.getDefault());

									Date timestamp = new Date(
											(Long) callMethod(
													param.thisObject,
													names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));

									String filename = sender
											+ "_"
											+ (fnameDateFormat
													.format(timestamp));

									if (prevFileName.equals(filename)) {
										prevFileName = "";
										return;
									}

									prevFileName = filename;

									refreshPreferences();
									printSettings();
									logging("\n----------------------- KEEPCHAT ------------------------");
									logging("Image Snap opened");
									isSnap = true;
									isStory = false;

									if (checkGod(sender)) {
										return;
									}

									if (imagesSnapSavingMode == DO_NOT_SAVE) {
										logging("Not Saving Image");
										logging("---------------------------------------------------------");
										return;
									}

									File file = createFile(filename + ".jpg",
											"/RecievedSnaps", sender);

									logging(mediaPath);

									if (file.exists()) {
										logging("Image Snap already Exists");
										toastMessage = "The image already exists.";
										isSaved = true;
									} else {
										isSaved = false;
										Bitmap image = (Bitmap) param
												.getResult();
										if (saveImage(image, file)) {
											logging("Image Snap has been Saved");
											toastMessage = "The image has been saved.";
										} else {
											logging("Error Saving Image Snap. Error 1.");
											toastMessage = "The image could not be saved. Error 1.";
										}
									}
								}
							}));

			/*
			 * getImageBitmap() hook The Story class has a method to load a
			 * Bitmap in preparation for viewing a Image in Story. This method
			 * returns said bitmap back so the application can display it. We
			 * hook this method to intercept the result and write it to the SD
			 * card. The file path is stored in the mediaPath member for later
			 * use in the showImage() hook.
			 */
			findAndHookMethod(
					names.get(VersionResolution.CLASS_STORY),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_STORY_GETIMAGEBITMAP),
					appendValue(
							params.get(VersionResolution.FUNCTION_STORY_GETIMAGEBITMAP),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {

									String sender = (String) callMethod(
											param.thisObject,
											names.get(VersionResolution.FUNCTION_STORY_GETSENDER));

									SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
											"yyyy-MM-dd_HH-mm-ss", Locale
													.getDefault());

									Date timestamp = new Date(
											(Long) callMethod(
													param.thisObject,
													names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));

									String filename = sender
											+ "_"
											+ (fnameDateFormat
													.format(timestamp));

									if (prevFileName.equals(filename)) {
										prevFileName = "";
										return;
									}

									prevFileName = filename;

									refreshPreferences();
									printSettings();

									logging("\n----------------------- KEEPCHAT ------------------------");
									logging("Image Story opened");
									isSnap = false;
									isStory = true;

									if (checkGod(sender)) {
										return;
									}

									if (imagesStorySavingMode == DO_NOT_SAVE) {
										logging("Not Saving Image");
										logging("---------------------------------------------------------");
										return;
									}

									File file = createFile(filename + ".jpg",
											"/Stories", sender);

									logging(mediaPath);

									if (file.exists()) {
										logging("Image Story already Exists");
										isSaved = true;
										toastMessage = "The image already exists.";
									} else {
										isSaved = false;
										Bitmap image = (Bitmap) param
												.getResult();
										if (saveImage(image, file)) {
											logging("Image Story has been Saved");
											toastMessage = "The image has been saved.";
										} else {
											logging("Error Saving Image Snap. Error 1.");
											toastMessage = "The image could not be saved. Error 1.";
										}
									}
								}
							}));

			/*
			 * getVideoUri() hook The ReceivedSnap class treats videos a little
			 * differently. Videos are not their own object, so they can't be
			 * passed around. The Android system basically provides a VideoView
			 * for viewing videos, which you just provide it the location of the
			 * video and it does the rest.
			 * 
			 * Unsurprisingly, Snapchat makes use of this View. This method in
			 * the ReceivedSnap class gets the URI of the video in preparation
			 * for one of these VideoViews. We hook in, intercept the result (a
			 * String), then copy the bytes from that location to our SD
			 * directory. This results in a bit of a slowdown for the user, but
			 * luckily this takes place before they actually view it.
			 * 
			 * The file path is stored in the mediaPath member for later use in
			 * the showVideo() hook.
			 */
			findAndHookMethod(
					names.get(VersionResolution.CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETVIDEOURI),
					appendValue(
							params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETVIDEOURI),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {

									refreshPreferences();
									printSettings();
									if (param.thisObject.toString().contains(
											"StorySnap")) {
										logging("\n----------------------- KEEPCHAT ------------------------");
										logging("Video Story opened");
										isSnap = false;
										isStory = true;
										if (videosStorySavingMode == DO_NOT_SAVE) {
											logging("Not Saving Video");
											logging("---------------------------------------------------------");
											return;
										}

										String sender = (String) callMethod(
												param.thisObject,
												names.get(VersionResolution.FUNCTION_STORY_GETSENDER));

										if (checkGod(sender)) {
											return;
										}

										SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
												"yyyy-MM-dd_HH-mm-ss", Locale
														.getDefault());

										Date timestamp = new Date(
												(Long) callMethod(
														param.thisObject,
														names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));

										String filename = sender
												+ "_"
												+ (fnameDateFormat
														.format(timestamp));

										File file = createFile(filename
												+ ".mp4", "/Stories", sender);

										logging(mediaPath);
										if (file.exists()) {
											logging("Video Story already Exists");
											toastMessage = "The video already exists";
											isSaved = true;
										} else {
											isSaved = false;
											String videoUri = (String) param
													.getResult();
											if (saveVideo(videoUri, file)) {
												logging("Video Story has been Saved");
												toastMessage = "The video has been saved.";
											} else {
												logging("Error Saving Video Story. Error 2.");
												toastMessage = "The video could not be saved. Error 2.";
											}
										}
									} else {

										logging("\n----------------------- KEEPCHAT ------------------------");
										logging("Video Snap opened");
										isSnap = true;
										isStory = false;

										if (videosSnapSavingMode == DO_NOT_SAVE) {
											logging("Not Saving Video");
											logging("---------------------------------------------------------");
											return;
										}

										String sender = (String) callMethod(
												param.thisObject,
												names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETSENDER));

										if (checkGod(sender)) {
											return;
										}

										SimpleDateFormat fnameDateFormat = new SimpleDateFormat(
												"yyyy-MM-dd_HH-mm-ss", Locale
														.getDefault());
										Date timestamp = new Date(
												(Long) callMethod(
														param.thisObject,
														names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));
										String filename = sender
												+ "_"
												+ (fnameDateFormat
														.format(timestamp));

										File file = createFile(filename
												+ ".mp4", "/RecievedSnaps",
												sender);
										logging(mediaPath);
										if (file.exists()) {
											isSaved = true;
											logging("Video Snap already Exists");
											toastMessage = "The video already exists";
										} else {
											isSaved = false;
											String videoUri = (String) param
													.getResult();
											if (saveVideo(videoUri, file)) {
												logging("Video Snap has been Saved");
												toastMessage = "The video has been saved.";
											} else {
												logging("Error Saving Video Snap. Error 2.");
												toastMessage = "The video could not be saved. Error 2.";
											}
										}
									}

								}

							}));

			findAndHookMethod(
					names.get(VersionResolution.CLASS_SNAP_PREVIEW_FRAGMENT),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING),
					appendValue(
							params.get(VersionResolution.FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING),
							new XC_MethodHook() {

								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {

									refreshPreferences();

									if (saveSentSnaps == true) {
										printSettings();
										logging("\n----------------------- KEEPCHAT ------------------------");
										Date cDate = new Date();
										String filename = new SimpleDateFormat(
												"yyyy-MM-dd_HH-mm-ss", Locale
														.getDefault())
												.format(cDate);
										Object obj = getObjectField(
												param.thisObject,
												names.get(VersionResolution.VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO));

										Class<?> snapbyro = obj.getClass();
										Method getType = snapbyro.getMethod(
												names.get(VersionResolution.FUNCTION_SNAPBRYO_ISIMAGE),
												(Class<?>[]) null);
										Method getImage = snapbyro.getMethod(
												names.get(VersionResolution.FUNCTION_SNAPBRYO_GETSNAPBITMAP),
												(Class<?>[]) null);
										Method getVideoUri = snapbyro.getMethod(
												names.get(VersionResolution.FUNCTION_SNAPBRYO_VIDEOURI),
												(Class<?>[]) null);

										int type = (Integer) getType.invoke(
												obj, (Object[]) null);

										if (type == 0) {
											logging("Image Sent Snap");
											File file = createFile(filename
													+ ".jpg", "/SentSnaps", "");
											logging(mediaPath);
											if (file.exists()) {
												logging("Image Sent Snap already exists");
												toastMessage = "The image already exists";
											} else {

												Bitmap image = (Bitmap) getImage
														.invoke(obj,
																(Object[]) null);

												if (image == null) {
													logging("IMAGE IS NULL");
												}
												if (saveImage(image, file)) {
													logging("Image Sent Snap has been Saved");
													toastMessage = "The image has been saved.";
												} else {
													logging("Error Saving Image Sent Snap. Error 3.");
													toastMessage = "The image could not be saved. Error 3.";
												}
											}
										} else {
											logging("Video Sent Snap");
											File file = createFile(filename
													+ ".mp4", "/SentSnaps", "");
											logging(mediaPath);

											if (file.exists()) {
												logging("Video Sent Snap already Exists");
												toastMessage = "This video already exists.";
											} else {

												Uri uri = (Uri) getVideoUri
														.invoke(obj,
																(Object[]) null);

												if (saveVideo(uri.getPath(),
														file)) {
													logging("Video Sent Snap has been Saved");
													toastMessage = "The video has been saved.";
												} else {
													logging("Error Saving Video Sent Snap. Error 3.");
													toastMessage = "The video could not be saved. Error 3.";
												}
											}
										}

										runMediaScanAndToast((Context) callMethod(
												param.thisObject, "getActivity"));
									}
								}

							}));

			/*
			 * showVideo() and showImage() hooks Because getVideoUri() and
			 * getImageBitmap() do not handily provide a context, nor do their
			 * parent classes (ReceivedSnap), we are unable to get the context
			 * necessary in order to display a notification and call the media
			 * scanner.
			 * 
			 * But these getters are called from the corresponding showVideo()
			 * and showImage() methods of com.snapchat.android.ui.SnapView,
			 * which deliver the needed context. So the work that needs a
			 * context is done here, while the file saving work is done in the
			 * getters. The getters also save the file paths in the mediaPath
			 * member, which we use here.
			 */

			findAndHookMethod(
					names.get(VersionResolution.CLASS_SNAPVIEW),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWIMAGE),
					appendValue(
							params.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWIMAGE),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {
									refreshPreferences();
									isSnapVideo = false;
									isSnapImage = true;

									if (((isSnap == true) && (imagesSnapSavingMode != DO_NOT_SAVE))
											|| ((isStory == true) && (imagesStorySavingMode != DO_NOT_SAVE))) {
										// At this point the context is put in
										// the
										// private member so that the dialog can
										// be
										// initiated from the markViewed() hook
										context = (Context) callMethod(
												param.thisObject, "getContext");

										if (isImmune == true) {
											immuneToast();
											return;
										}

										if (((isSnap == true) && (imagesSnapSavingMode == SAVE_AUTO))
												|| ((isStory == true) && (imagesStorySavingMode == SAVE_AUTO))) {
											runMediaScanAndToast(context);
										} else {
											displayDialog = true;
										}
									}
								}
							}));

			findAndHookMethod(
					names.get(VersionResolution.CLASS_SNAPVIEW),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWVIDEO),
					appendValue(
							params.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWVIDEO),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {

									isSnapVideo = true;
									isSnapImage = false;
									refreshPreferences();
									if (((isSnap == true) && (videosSnapSavingMode != DO_NOT_SAVE))
											|| ((isStory == true) && (videosStorySavingMode != DO_NOT_SAVE))) {
										// At this point the context is put in
										// the
										// private member so that the dialog can
										// be
										// initiated from the markViewed() hook
										context = (Context) callMethod(
												param.thisObject, "getContext");

										if (isImmune == true) {
											immuneToast();
											return;
										}

										if (((isSnap == true) && (videosSnapSavingMode == SAVE_AUTO))
												|| ((isStory == true) && (videosStorySavingMode == SAVE_AUTO))) {
											runMediaScanAndToast(context);
										} else {
											displayDialog = true;
										}
									}
								}
							}));

			findAndHookMethod(
					names.get(VersionResolution.CLASS_RECEIVEDSNAP),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_MARKVIEWED),
					appendValue(
							params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_MARKVIEWED),
							new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(
										MethodHookParam param) throws Throwable {
									refreshPreferences();
									// check if its save ask AND that it doesn't
									// exist
									if (displayDialog == true) {
										if (((isSnapImage == true)
												&& (isSaved == false) && (imagesSnapSavingMode == SAVE_ASK))
												|| ((isSnapVideo == true)
														&& (isSaved == false) && (videosSnapSavingMode == SAVE_ASK))) {
											showDialog(context);
											displayDialog = false;
										}
									}
								}
							}));

			Constructor<?> constructor;
			constructor = findConstructorBestMatch(
					findClass(names.get(VersionResolution.CLASS_SNAPUPDATE),
							lpparam.classLoader),
					(Class<?>[]) params.get(VersionResolution.CLASS_SNAPUPDATE));

			XposedBridge.hookMethod(constructor, new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {

					param.args[1] = 0;

				}
			});

			Constructor<?> constructor2 = findConstructorBestMatch(
					findClass(
							names.get(VersionResolution.CLASS_STORYVIEWRECORD),
							lpparam.classLoader),
					(Class<?>[]) params
							.get(VersionResolution.CLASS_STORYVIEWRECORD));

			XposedBridge.hookMethod(constructor2, new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					param.args[2] = 0;
				}
			});

			findAndHookMethod(
					names.get(VersionResolution.CLASS_SNAPSTATEMESSAGE_BUILDER),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SETSCREENSHOTCOUNT),
					appendValue(
							params.get(VersionResolution.FUNCTION_SETSCREENSHOTCOUNT),
							new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(
										MethodHookParam param) throws Throwable {
									param.args[0] = 0L;

								}
							}));

			findAndHookMethod(
					names.get(VersionResolution.CLASS_SCREENSHOTDETECTOR),
					lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_DECTECTIONSESSION),
					appendValue(params
							.get(VersionResolution.FUNCTION_DECTECTIONSESSION),
							XC_MethodReplacement.returnConstant(null)));

			XposedBridge
					.log("---------------------------------------------------------");

		} catch (Exception e) {
			logging("Exception");
			logging(Log.getStackTraceString(e));
			Log.v(PACKAGE_NAME, Log.getStackTraceString(e));
			Log.v(PACKAGE_NAME, "Exception");
			XposedBridge.log("Keepchat doesn't currently support version '"
					+ versionName + "', wait for an update");
			XposedBridge
					.log("If you can, pull the apk off your device and submit it to the xda thread");
			findAndHookMethod("com.snapchat.android.LandingPageActivity",
					lpparam.classLoader, "onCreate", Bundle.class,
					new XC_MethodHook() {
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							Toast.makeText(
									(Context) callMethod(param.thisObject,
											"getApplicationContext"),
									"This version of snapchat is currently not supported.",
									Toast.LENGTH_LONG).show();
						}
					});
			XposedBridge
					.log("---------------------------------------------------------");
		}

	}

	private String initialInfoLogAndGetVersion(LoadPackageParam lpparam) {
		XposedBridge
				.log("\n------------------- KEEPCHAT STARTED --------------------");
		XposedBridge.log("Snapchat Loaded");

		try {
			Object activityThread = callStaticMethod(
					findClass("android.app.ActivityThread", null),
					"currentActivityThread");
			Context context = (Context) callMethod(activityThread,
					"getSystemContext");
			PackageInfo piSnapChat = context.getPackageManager()
					.getPackageInfo(lpparam.packageName, 0);
			versionName = piSnapChat.versionName;
			XposedBridge
					.log("SnapChat Version Name: " + piSnapChat.versionName);
			XposedBridge
					.log("SnapChat Version Code: " + piSnapChat.versionCode);
			PackageInfo piKeepchat = context.getPackageManager()
					.getPackageInfo(PACKAGE_NAME, 0);
			XposedBridge
					.log("KeepChat Version Name: " + piKeepchat.versionName);
			XposedBridge
					.log("KeepChat Version Code: " + piKeepchat.versionCode);
			XposedBridge.log("Android Release: " + Build.VERSION.RELEASE);

		} catch (Exception e) {
			XposedBridge.log("Exception while trying to get version info. ("
					+ e.getMessage() + ")");
			return null;
		}

		return versionName;
	}

	private void refreshPreferences() {

		prefs = new XSharedPreferences(new File(
				Environment.getExternalStorageDirectory(), "Android/data/"
						+ PACKAGE_NAME + "/files/" + PACKAGE_NAME
						+ "_preferences" + ".xml"));
		prefs.reload();
		sortFileUsername = prefs.getBoolean("pref_key_sort_files_username",
				true);
		savePath = prefs.getString("pref_key_save_location", "");
		imagesSnapSavingMode = Integer.parseInt(prefs.getString(
				"pref_key_snaps_images", Integer.toString(SAVE_AUTO)));
		videosSnapSavingMode = Integer.parseInt(prefs.getString(
				"pref_key_snaps_videos", Integer.toString(SAVE_AUTO)));
		imagesStorySavingMode = Integer.parseInt(prefs.getString(
				"pref_key_stories_images", Integer.toString(SAVE_AUTO)));
		videosStorySavingMode = Integer.parseInt(prefs.getString(
				"pref_key_stories_videos", Integer.toString(SAVE_AUTO)));
		toastMode = prefs.getBoolean("pref_key_toasts_checkbox", true);
		saveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", false);
		toastLength = Integer
				.parseInt(prefs.getString("pref_key_toasts_duration",
						Integer.toString(Toast.LENGTH_LONG)));
		debugMode = prefs.getBoolean("pref_key_debug_mode", true);
		sortFilesMode = prefs.getBoolean("pref_key_sort_files_mode", true);
		// in case the user doesn't open settings when first installed. need a
		// default save location
		if (savePath == "") {
			String root = Environment.getExternalStorageDirectory().toString();
			savePath = root + "/keepchat";
		}

	}

	private void printSettings() {
		logging("\n------------------- KEEPCHAT SETTINGS -------------------");
		logging("savepath: " + savePath);
		if (imagesSnapSavingMode == SAVE_AUTO) {
			logging("imagesSnapSavingMode: " + "SAVE_AUTO");
		} else if (imagesSnapSavingMode == SAVE_ASK) {
			logging("imagesSnapSavingMode: " + "SAVE_ASK");
		} else if (imagesSnapSavingMode == DO_NOT_SAVE) {
			logging("imagesSnapSavingMode: " + "DO_NOT_SAVE");
		}

		if (videosSnapSavingMode == SAVE_AUTO) {
			logging("videosSnapSavingMode: " + "SAVE_AUTO");
		} else if (videosSnapSavingMode == SAVE_ASK) {
			logging("videosSnapSavingMode: " + "SAVE_ASK");
		} else if (videosSnapSavingMode == DO_NOT_SAVE) {
			logging("videosSnapSavingMode: " + "DO_NOT_SAVE");
		}

		if (imagesStorySavingMode == SAVE_AUTO) {
			logging("imagesStorySavingMode: " + "SAVE_AUTO");
		} else if (imagesStorySavingMode == SAVE_ASK) {
			logging("imagesStorySavingMode: " + "SAVE_ASK");
		} else if (imagesStorySavingMode == DO_NOT_SAVE) {
			logging("imagesStorySavingMode: " + "DO_NOT_SAVE");
		}

		if (videosStorySavingMode == SAVE_AUTO) {
			logging("videosStorySavingMode: " + "SAVE_AUTO");
		} else if (videosStorySavingMode == SAVE_ASK) {
			logging("videosStorySavingMode: " + "SAVE_ASK");
		} else if (videosStorySavingMode == DO_NOT_SAVE) {
			logging("videosStorySavingMode: " + "DO_NOT_SAVE");
		}
		logging("toastMode: " + toastMode);
		logging("saveSentSnaps: " + saveSentSnaps);
		logging("toastLength: " + toastLength);
		logging("sortFilesMode: " + sortFilesMode);
		logging("sortFileUsername: " + sortFileUsername);
		logging("---------------------------------------------------------");
	}

	private void logging(String message) {
		// Log.v(PACKAGE_NAME, message);
		if (debugMode == true) {
			XposedBridge.log(message);
		}
	}

	private File createFile(String fileName, String savePathSuffix,
			String sender) {

		File myDir;
		if (sortFilesMode == true) {
			if (sortFileUsername == true) {
				myDir = new File(savePath + savePathSuffix + "/" + sender);
			} else {
				myDir = new File(savePath + savePathSuffix);
			}

		} else {
			myDir = new File(savePath);
		}

		myDir.mkdirs();

		File toReturn = new File(myDir, fileName);

		try {
			mediaPath = toReturn.getCanonicalPath();
		} catch (IOException e) {
			XposedBridge.log(Log.getStackTraceString(e));
		}

		return toReturn;
	}

	// function to saveimage
	private boolean saveImage(Bitmap myImage, File fileToSave) {

		try {
			FileOutputStream out = new FileOutputStream(fileToSave);
			myImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			XposedBridge.log(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}

	// function to save video
	private boolean saveVideo(String videoUri, File fileToSave) {

		try {
			FileInputStream in = new FileInputStream(new File(videoUri));
			FileOutputStream out = new FileOutputStream(fileToSave);

			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			XposedBridge.log(Log.getStackTraceString(e));
			return false;
		}
		return true;
	}

	/*
	 * Tells the media scanner to scan the newly added image or video so that it
	 * shows up in the gallery without a reboot. And shows a Toast message where
	 * the media was saved.
	 * 
	 * @param context Current context
	 * 
	 * @param filePath File to be scanned by the media scanner
	 */
	private void runMediaScanAndToast(Context context) {

		try {
			logging("MediaScanner running ");
			// Run MediaScanner on file, so it shows up in Gallery instantly
			MediaScannerConnection.scanFile(context,
					new String[] { mediaPath }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							if (uri != null) {
								logging("MediaScanner ran successfully: "
										+ uri.toString());
							} else {
								logging("Unknown error occurred while trying to run MediaScanner");
							}
							logging("---------------------------------------------------------");
						}
					});
		} catch (Exception e) {
			logging("Error occurred while trying to run MediaScanner");
			e.printStackTrace();
			logging("---------------------------------------------------------");
		}
		// construct the toast notification
		if (toastMode == true) {
			logging("Toast Displayed");
			if (toastLength == 0) {
				Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
			}
		}

	}

	private void showDialog(final Context dContext) {
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(dContext);
		// 2. Chain together various setter methods to set the dialog
		// characteristics
		final String mediaTypeStr = isSnapImage ? "image" : "video";
		builder.setMessage("Would you like to save the " + mediaTypeStr + "?\n")
				.setTitle("Save " + mediaTypeStr + "?");

		builder.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						runMediaScanAndToast(dContext);
					}
				});

		builder.setNegativeButton("Discard",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if ((new File(mediaPath)).delete())
					logging("File deleted successfully");
				else
					logging("Could not delete file.");

				logging("---------------------------------------------------------");
			}
		});
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		logging("dialog show");
		dialog.show();
	}

	private Object[] appendValue(Object[] obj, Object newObj) {

		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
		temp.add(newObj);
		return temp.toArray();

	}

	private boolean checkGod(String sender) {

		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md.update(sender.getBytes());
			byte byteData[] = md.digest();

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
						.substring(1));
			}

			String mSender = sb.toString();

			for (String s : gods) {
				if (mSender.equals(s)) {
					isImmune = true;
					senderName = mSender;
					return true;
				} else {
					isImmune = false;
				}
			}

		} catch (NoSuchAlgorithmException e) {
		}
		return false;
	}

	private void immuneToast() {
		String message;
		if (senderName.equals(gods.get(0))) {
			message = "You cannot save the snaps of a GOD!";
		} else {
			message = "You cannot save the snaps of this minor person!";
		}

		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
