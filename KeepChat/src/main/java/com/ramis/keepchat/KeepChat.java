package com.ramis.keepchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

public class KeepChat implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    public static final String LOG_TAG = "KeepChat";
    private Context context;
    private String versionName;

    // Modes for saving Snapchats
	final static int SAVE_AUTO = 0;
	final static int SAVE_ASK = 1;
	final static int DO_NOT_SAVE = 2;
    // Directories for sorting by category
    public static final String DIR_SNAPS = "/ReceivedSnaps";
    public static final String DIR_STORIES = "/Stories";
    public static final String DIR_SENT = "/SentSnaps";
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;

    // Preferences and their default values
    public static int MODE_SNAP_IMAGE = SAVE_AUTO;
    public static int MODE_SNAP_VIDEO = SAVE_AUTO;
    public static int MODE_STORY_IMAGE = SAVE_AUTO;
    public static int MODE_STORY_VIDEO = SAVE_AUTO;
    public static boolean TOAST_ENABLED = true;
    public static int TOAST_LENGTH = Toast.LENGTH_LONG;
    public static String SAVE_PATH = Environment.getExternalStorageDirectory().toString() + "/keepchat";
    public static boolean SAVE_SENT_SNAPS = false;
    public static boolean SORT_BY_CATEGORY = true;
    public static boolean SORT_BY_USERNAME = true;
    public static boolean DEBUGGING = true;

	private boolean isStory = false;
	private boolean isSnap = false;
	private boolean isSnapImage = false;
	private boolean isSnapVideo = false;
	private boolean displayDialog = false;
	private boolean isSaved = false;
	private String toastMessage = "";
	private String mediaPath = "";
	private String prevFileName = "";


	private static XModuleResources mResources;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());

	// loading package
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(SNAPCHAT_PACKAGE_NAME))
			return;

		versionName = initialInfoLogAndGetVersion(lpparam);
		VersionResolution resolution = new VersionResolution(versionName, lpparam);

		final SparseArray<String> names = resolution.getNames();
		final SparseArray<Object[]> params = resolution.getParams();

		try {
			refreshPreferences();

			/*
			 * getImageBitmap() hook The ReceivedSnap class has a method to load
			 * a Bitmap in preparation for viewing. This method returns said
			 * bitmap back so the application can display it. We hook this
			 * method to intercept the result and write it to the SD card. The
			 * file path is stored in the mediaPath member for later use in the
			 * showImage() hook.
			 */
            findAndHookMethod(names.get(VersionResolution.CLASS_RECEIVEDSNAP), lpparam.classLoader,
                    names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP),
                    appendValue(params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETIMAGEBITMAP), new XC_MethodHook() {

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            String sender = (String) callMethod(param.thisObject, names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETSENDER));
                            Date timestamp = new Date((Long) callMethod(param.thisObject, names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));
                            String filename = sender + "_" + dateFormat.format(timestamp);

                            if (prevFileName.equals(filename)) {
                                prevFileName = "";
                                return;
                            }

                            prevFileName = filename;

                            refreshPreferences();
                            Utils.log("\n----------------------- KEEPCHAT ------------------------");
                            Utils.log("Image Snap opened");
                            isSnap = true;
                            isStory = false;

                            if (MODE_SNAP_IMAGE == DO_NOT_SAVE) {
                                Utils.log("Not Saving Image");
                                Utils.log("---------------------------------------------------------");
                                return;
                            }

                            File file = createFile(filename + ".jpg", DIR_SNAPS, sender);
                            Utils.log(mediaPath);

                            if (file.exists()) {
                                Utils.log("Image Snap already Exists");
                                toastMessage = mResources.getString(R.string.image_exists);
                                isSaved = true;
                            } else {
                                isSaved = false;
                                Bitmap image = (Bitmap) param.getResult();
                                if (saveImage(image, file)) {
                                    Utils.log("Image Snap has been Saved");
                                    toastMessage = mResources.getString(R.string.image_saved);
                                } else {
                                    toastMessage = mResources.getString(R.string.image_not_saved);
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
            findAndHookMethod(names.get(VersionResolution.CLASS_STORY), lpparam.classLoader,
                    names.get(VersionResolution.FUNCTION_STORY_GETIMAGEBITMAP),
                    appendValue(params.get(VersionResolution.FUNCTION_STORY_GETIMAGEBITMAP),

                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                    String sender = (String) callMethod(param.thisObject,
                                            names.get(VersionResolution.FUNCTION_STORY_GETSENDER));
                                    Date timestamp = new Date((Long) callMethod(param.thisObject,
                                            names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));
                                    String filename = sender + "_" + dateFormat.format(timestamp);

                                    if (prevFileName.equals(filename)) {
                                        prevFileName = "";
                                        return;
                                    }

                                    prevFileName = filename;

                                    refreshPreferences();

                                    Utils.log("\n----------------------- KEEPCHAT ------------------------");
                                    Utils.log("Image Story opened");
                                    isSnap = false;
                                    isStory = true;

                                    if (MODE_STORY_IMAGE == DO_NOT_SAVE) {
                                        Utils.log("Not Saving Image");
                                        Utils.log("---------------------------------------------------------");
                                        return;
                                    }

                                    File file = createFile(filename + ".jpg", DIR_STORIES, sender);
                                    Utils.log(mediaPath);

                                    if (file.exists()) {
                                        Utils.log("Image Story already Exists");
                                        isSaved = true;
                                        toastMessage = mResources.getString(R.string.image_exists);
                                    } else {
                                        isSaved = false;
                                        Bitmap image = (Bitmap) param.getResult();
                                        if (saveImage(image, file)) {
                                            Utils.log("Image Story has been Saved");
                                            toastMessage = mResources.getString(R.string.image_saved);
                                        } else {
                                            toastMessage = mResources.getString(R.string.image_not_saved);
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
            findAndHookMethod(names.get(VersionResolution.CLASS_RECEIVEDSNAP), lpparam.classLoader,
                    names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETVIDEOURI),
                    appendValue(params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETVIDEOURI),
                            new XC_MethodHook() {

                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                                    refreshPreferences();
                                    if (param.thisObject.toString().contains("StorySnap")) {
                                        Utils.log("\n----------------------- KEEPCHAT ------------------------");
                                        Utils.log("Video Story opened");
                                        isSnap = false;
                                        isStory = true;

                                        if (MODE_STORY_VIDEO == DO_NOT_SAVE) {
                                            Utils.log("Not Saving Video");
                                            Utils.log("---------------------------------------------------------");
                                            return;
                                        }

                                        String sender = (String) callMethod(param.thisObject,
                                                names.get(VersionResolution.FUNCTION_STORY_GETSENDER));
                                        Date timestamp = new Date((Long) callMethod(param.thisObject,
                                                names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));
                                        String filename = sender + "_" + dateFormat.format(timestamp);

                                        File file = createFile(filename + ".mp4", DIR_STORIES, sender);
                                        Utils.log(mediaPath);

                                        if (file.exists()) {
                                            Utils.log("Video Story already Exists");
                                            toastMessage = mResources.getString(R.string.video_exists);
                                            isSaved = true;
                                        } else {
                                            isSaved = false;
                                            String videoUri = (String) param.getResult();
                                            if (saveVideo(videoUri, file)) {
                                                Utils.log("Video Story has been Saved");
                                                toastMessage = mResources.getString(R.string.video_saved);
                                            } else {
                                                toastMessage = mResources.getString(R.string.video_not_saved);
                                            }
                                        }
                                    } else {
                                        Utils.log("\n----------------------- KEEPCHAT ------------------------");
                                        Utils.log("Video Snap opened");
                                        isSnap = true;
                                        isStory = false;

                                        if (MODE_SNAP_VIDEO == DO_NOT_SAVE) {
                                            Utils.log("Not Saving Video");
                                            Utils.log("---------------------------------------------------------");
                                            return;
                                        }

                                        String sender = (String) callMethod(param.thisObject,
                                                names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_GETSENDER));
                                        Date timestamp = new Date((Long) callMethod(param.thisObject,
                                                names.get(VersionResolution.FUNCTION_SNAP_GETTIMESTAMP)));
                                        String filename = sender + "_" + dateFormat.format(timestamp);

                                        File file = createFile(filename + ".mp4", DIR_SNAPS, sender);
                                        Utils.log(mediaPath);

                                        if (file.exists()) {
                                            isSaved = true;
                                            Utils.log("Video Snap already Exists");
                                            toastMessage = mResources.getString(R.string.video_exists);
                                        } else {
                                            isSaved = false;
                                            String videoUri = (String) param.getResult();
                                            if (saveVideo(videoUri, file)) {
                                                Utils.log("Video Snap has been Saved");
                                                toastMessage = mResources.getString(R.string.video_saved);
                                            } else {
                                                toastMessage = mResources.getString(R.string.video_not_saved);
                                            }
                                        }
                                    }
                                }
                            }));

			findAndHookMethod(names.get(VersionResolution.CLASS_SNAP_PREVIEW_FRAGMENT), lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING),
					appendValue(params.get(VersionResolution.FUNCTION_SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING),
							new XC_MethodHook() {

								@Override
								protected void afterHookedMethod(MethodHookParam param) throws Throwable {
									refreshPreferences();

									if (SAVE_SENT_SNAPS) {
										Utils.log("\n----------------------- KEEPCHAT ------------------------");
										Date cDate = new Date();
										String filename = dateFormat.format(cDate);
										Object obj = getObjectField(param.thisObject,
												names.get(VersionResolution.VARIABLE_SNAPPREVIEWFRAGMENT_SNAPBYRO));

										Class<?> snapbyro = obj.getClass();
										Method getType = snapbyro.getMethod(names.get(VersionResolution.FUNCTION_SNAPBRYO_ISIMAGE));
										Method getImage = snapbyro.getMethod(names.get(VersionResolution.FUNCTION_SNAPBRYO_GETSNAPBITMAP));
										Method getVideoUri = snapbyro.getMethod(names.get(VersionResolution.FUNCTION_SNAPBRYO_VIDEOURI));

										int type = (Integer) getType.invoke(obj);

										if (type == 0) {
											Utils.log("Image Sent Snap");
											File file = createFile(filename + ".jpg", DIR_SENT, "");
											Utils.log(mediaPath);
											if (file.exists()) {
												Utils.log("Image Sent Snap already exists");
												toastMessage = mResources.getString(R.string.image_exists);
											} else {
												Bitmap image = (Bitmap) getImage.invoke(obj);

												if (image == null) {
													Utils.log("IMAGE IS NULL");
												}
												if (saveImage(image, file)) {
													Utils.log("Image Sent Snap has been Saved");
													toastMessage = mResources.getString(R.string.image_saved);
												} else {
													toastMessage = mResources.getString(R.string.image_not_saved);
												}
											}
										} else {
											Utils.log("Video Sent Snap");
											File file = createFile(filename + ".mp4", DIR_SENT, "");
											Utils.log(mediaPath);

											if (file.exists()) {
												Utils.log("Video Sent Snap already Exists");
												toastMessage = mResources.getString(R.string.video_exists);
											} else {
												Uri uri = (Uri) getVideoUri.invoke(obj);

												if (saveVideo(uri.getPath(), file)) {
													Utils.log("Video Sent Snap has been Saved");
													toastMessage = mResources.getString(R.string.video_saved);
												} else {
													toastMessage = mResources.getString(R.string.video_not_saved);
												}
											}
										}

										runMediaScanAndToast((Context) callMethod(param.thisObject, "getActivity"));
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

			findAndHookMethod(names.get(VersionResolution.CLASS_SNAPVIEW), lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWIMAGE),
					appendValue(params.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWIMAGE),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(MethodHookParam param) throws Throwable {
									refreshPreferences();
									isSnapVideo = false;
									isSnapImage = true;

									if ((isSnap && (MODE_SNAP_IMAGE != DO_NOT_SAVE))
											|| (isStory && (MODE_STORY_IMAGE != DO_NOT_SAVE))) {
										// At this point the context is put in the private member so
										// that the dialog can be initiated from the markViewed() hook
										context = (Context) callMethod(param.thisObject, "getContext");

										if ((isSnap && (MODE_SNAP_IMAGE == SAVE_AUTO))
												|| (isStory && (MODE_STORY_IMAGE == SAVE_AUTO))) {
											runMediaScanAndToast(context);
										} else {
											displayDialog = true;
										}
									}
								}
							}));

			findAndHookMethod(names.get(VersionResolution.CLASS_SNAPVIEW), lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWVIDEO),
					appendValue(params.get(VersionResolution.FUNCTION_SNAPVIEW_SHOWVIDEO),
							new XC_MethodHook() {
								@Override
								protected void afterHookedMethod(MethodHookParam param) throws Throwable {

									isSnapVideo = true;
									isSnapImage = false;
									refreshPreferences();
									if ((isSnap && (MODE_SNAP_VIDEO != DO_NOT_SAVE))
											|| (isStory && (MODE_STORY_VIDEO != DO_NOT_SAVE))) {
										// At this point the context is put in
										// the
										// private member so that the dialog can
										// be
										// initiated from the markViewed() hook
										context = (Context) callMethod(param.thisObject, "getContext");

										if ((isSnap && (MODE_SNAP_VIDEO == SAVE_AUTO))
												|| (isStory && (MODE_STORY_VIDEO == SAVE_AUTO))) {
											runMediaScanAndToast(context);
										} else {
											displayDialog = true;
										}
									}
								}
							}));

			findAndHookMethod(names.get(VersionResolution.CLASS_RECEIVEDSNAP), lpparam.classLoader,
					names.get(VersionResolution.FUNCTION_RECEIVEDSNAP_MARKVIEWED),
					appendValue(params.get(VersionResolution.FUNCTION_RECEIVEDSNAP_MARKVIEWED),
							new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
									refreshPreferences();
									// check if its save ask AND that it doesn't
									// exist
									if (displayDialog) {
										if ((isSnapImage && !isSaved && (MODE_SNAP_IMAGE == SAVE_ASK))
												|| (isSnapVideo && !isSaved && (MODE_SNAP_VIDEO == SAVE_ASK))) {
											showDialog(context);
											displayDialog = false;
										}
									}
								}
							}));

			Constructor<?> constructor;
			constructor = findConstructorBestMatch(findClass(names.get(VersionResolution.CLASS_SNAPUPDATE), lpparam.classLoader),
					(Class<?>[]) params.get(VersionResolution.CLASS_SNAPUPDATE));

			if (resolution.newSnapUpdate()) {
				XposedBridge.hookMethod(constructor, new XC_MethodHook() {
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						setObjectField(param.thisObject, "c", 0);
					}
				});
			} else {
				XposedBridge.hookMethod(constructor, new XC_MethodHook() {
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						param.args[1] = 0;
					}
				});
			}

			Constructor<?> constructor2 = findConstructorBestMatch(
					findClass(names.get(VersionResolution.CLASS_STORYVIEWRECORD), lpparam.classLoader),
					(Class<?>[]) params.get(VersionResolution.CLASS_STORYVIEWRECORD));

			XposedBridge.hookMethod(constructor2, new XC_MethodHook() {
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[2] = 0;
				}
			});

			findAndHookMethod(names.get(VersionResolution.CLASS_SNAPSTATEMESSAGE_BUILDER),
                    lpparam.classLoader, names.get(VersionResolution.FUNCTION_SETSCREENSHOTCOUNT),
					appendValue(params.get(VersionResolution.FUNCTION_SETSCREENSHOTCOUNT),
							new XC_MethodHook() {
								@Override
								protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
									param.args[0] = 0L;
								}
							}));

			findAndHookMethod(names.get(VersionResolution.CLASS_SCREENSHOTDETECTOR),
					lpparam.classLoader, names.get(VersionResolution.FUNCTION_DECTECTIONSESSION),
					appendValue(params.get(VersionResolution.FUNCTION_DECTECTIONSESSION),
							XC_MethodReplacement.returnConstant(null)));

		} catch (Exception e) {
            Utils.log("Error occured: Keepchat doesn't currently support version " + versionName + ", wait for an update", e);

			findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Toast.makeText((Context) param.thisObject, "This version of snapchat is currently not supported by Keepchat.", Toast.LENGTH_LONG).show();
                }
            });
		}
	}

	private String initialInfoLogAndGetVersion(LoadPackageParam lpparam) {
		Utils.log("\n------------------- KEEPCHAT STARTED --------------------", false, true);

		try {
			Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
			Context context = (Context) callMethod(activityThread, "getSystemContext");
			PackageInfo piSnapChat = context.getPackageManager().getPackageInfo(lpparam.packageName, 0);

			Utils.log("SnapChat Version Name: " + piSnapChat.versionName, false, true);
            Utils.log("SnapChat Version Code: " + piSnapChat.versionCode, false, true);

            Utils.log("KeepChat Version Name: " + BuildConfig.VERSION_NAME, false, true);
            Utils.log("KeepChat Version Code: " + BuildConfig.VERSION_CODE, false, true);
            Utils.log("Android Release: " + Build.VERSION.RELEASE, false, true);

            return versionName;
		} catch (Exception e) {
            Utils.log("Exception while trying to get version info", e);
			return null;
		}
	}

	private void refreshPreferences() {
        XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        prefs.reload();

		MODE_SNAP_IMAGE = prefs.getInt("pref_key_snaps_images", MODE_SNAP_IMAGE);
		MODE_SNAP_VIDEO = prefs.getInt("pref_key_snaps_videos", MODE_SNAP_VIDEO);
		MODE_STORY_IMAGE = prefs.getInt("pref_key_stories_images", MODE_STORY_IMAGE);
		MODE_STORY_VIDEO = prefs.getInt("pref_key_stories_videos", MODE_STORY_VIDEO);
		TOAST_ENABLED = prefs.getBoolean("pref_key_toasts_checkbox", TOAST_ENABLED);
        TOAST_LENGTH = prefs.getInt("pref_key_toasts_duration", TOAST_LENGTH);
        SAVE_PATH = prefs.getString("pref_key_save_location", SAVE_PATH);
		SAVE_SENT_SNAPS = prefs.getBoolean("pref_key_save_sent_snaps", SAVE_SENT_SNAPS);
        SORT_BY_CATEGORY = prefs.getBoolean("pref_key_sort_files_mode", SORT_BY_CATEGORY);
        SORT_BY_USERNAME = prefs.getBoolean("pref_key_sort_files_username", SORT_BY_USERNAME);
		DEBUGGING = prefs.getBoolean("pref_key_debug_mode", DEBUGGING);

        if (DEBUGGING) {
            Utils.log("\n------------------- KEEPCHAT SETTINGS -------------------");
            String[] saveModes = {"SAVE_AUTO", "SAVE_ASK", "DO_NOT_SAVE"};
            Utils.log("MODE_SNAP_IMAGE: " + saveModes[MODE_SNAP_IMAGE]);
            Utils.log("MODE_SNAP_VIDEO: " + saveModes[MODE_SNAP_VIDEO]);
            Utils.log("MODE_STORY_IMAGE: " + saveModes[MODE_STORY_IMAGE]);
            Utils.log("MODE_STORY_VIDEO: " + saveModes[MODE_STORY_VIDEO]);
            Utils.log("TOAST_ENABLED: " + TOAST_ENABLED);
            Utils.log("TOAST_LENGTH: " + TOAST_LENGTH);
            Utils.log("SAVE_PATH: " + SAVE_PATH);
            Utils.log("SAVE_SENT_SNAPS: " + SAVE_SENT_SNAPS);
            Utils.log("SORT_BY_CATEGORY: " + SORT_BY_CATEGORY);
            Utils.log("SORT_BY_USERNAME: " + SORT_BY_USERNAME);
            Utils.log("---------------------------------------------------------");
        }
	}

	private File createFile(String fileName, String savePathSuffix, String sender) {
		File directory;
		if (SORT_BY_CATEGORY) {
			if (SORT_BY_USERNAME) {
				directory = new File(SAVE_PATH + savePathSuffix + "/" + sender);
			} else {
				directory = new File(SAVE_PATH + savePathSuffix);
			}
		} else {
			directory = new File(SAVE_PATH);
		}

        if (!directory.exists()) {
            directory.mkdirs();
        }

		File result = new File(directory, fileName);
        mediaPath = result.getAbsolutePath();
		return result;
	}

	// function to saveimage
	private boolean saveImage(Bitmap myImage, File fileToSave) {
		try {
			FileOutputStream out = new FileOutputStream(fileToSave);
			myImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
            Utils.log("Exception while saving an image", e);
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
            Utils.log("Exception while saving a video", e);
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
			Utils.log("MediaScanner running ");
			// Run MediaScanner on file, so it shows up in Gallery instantly
			MediaScannerConnection.scanFile(context, new String[] { mediaPath }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							if (uri != null) {
								Utils.log("MediaScanner ran successfully: " + uri.toString());
							} else {
								Utils.log("Unknown error occurred while trying to run MediaScanner");
							}
							Utils.log("---------------------------------------------------------");
						}
					});
		} catch (Exception e) {
            Utils.log("Error occurred while trying to run MediaScanner", e);
		}
		// construct the toast notification
		if (TOAST_ENABLED) {
			Utils.log("Toast Displayed");
			if (TOAST_LENGTH == 0) {
				Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
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
		// final String mediaTypeStr = isSnapImage ? "image" : "video";

		String message, title, pButton, nButton;

		if (isSnapImage) {
			message = mResources.getString(R.string.save_message_image);
			title = mResources.getString(R.string.save_dialog_title_image);
		} else {
			message = mResources.getString(R.string.save_message_video);
			title = mResources.getString(R.string.save_dialog_title_video);
		}

		pButton = mResources.getString(R.string.save_button);
		nButton = mResources.getString(R.string.discard_button);

		builder.setMessage(message + "\n").setTitle(title);

		builder.setPositiveButton(pButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                runMediaScanAndToast(dContext);
            }
        });

		builder.setNegativeButton(nButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if ((new File(mediaPath)).delete())
					Utils.log("File deleted successfully");
				else
					Utils.log("Could not delete file.");

				Utils.log("---------------------------------------------------------");
			}
		});
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		Utils.log("dialog show");
		dialog.show();
	}

	private Object[] appendValue(Object[] obj, Object newObj) {
		ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(obj));
		temp.add(newObj);
		return temp.toArray();
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mResources = XModuleResources.createInstance(startupParam.modulePath, null);
	}
}
