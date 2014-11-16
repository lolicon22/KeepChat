package com.ramis.keepchat;

public class Obfuscator {
    /**
     * Check if Keepchat is compatible with this Snapchat version.
     * @param versionCode The version code of the current Snapchat version
     * @return Whether it's supported
     */
    public static boolean isSupported(int versionCode) {
        return versionCode >= SUPPORTED_VERSION_CODE;
    }

    // Keepchat supports v5.0.38.1 and up
    private static int SUPPORTED_VERSION_CODE = 427;

    // ReceivedSnap class
    public static final String RECEIVEDSNAP_CLASS = "com.snapchat.android.model.ReceivedSnap";
    // ReceivedSnap.getImageBitmap()
    public static final String RECEIVEDSNAP_GETIMAGEBITMAP = "b";
    // ReceivedSnap.getVideoUri()
    public static final String RECEIVEDSNAP_GETVIDEOURI = "N";
    // ReceivedSnap.markViewed()
    public static final String RECEIVEDSNAP_MARKVIEWED = "p";
    // ReceivedSnap.getSender()
    public static final String RECEIVEDSNAP_GETSENDER = "j";

    // StorySnap class
    public static final String STORYSNAP_CLASS = "com.snapchat.android.model.StorySnap";
    // StorySnap.getImageBitmap()
    public static final String STORYSNAP_GETIMAGEBITMAP = "a";
    // StorySnap.getSender()
    public static final String STORYSNAP_GETSENDER = "aD";

    // SnapView class
    public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.SnapView";
    // SnapView.showImage()
    public static final String SNAPVIEW_SHOWIMAGE = "b";
    // SnapView.showVideo()
    public static final String SNAPVIEW_SHOWVIDEO = "a";

    // Snap.getTimestamp()
    public static final String SNAP_GETTIMESTAMP = "ac";

    // SnapPreviewFragment class
    public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.SnapPreviewFragment";
    // SnapPreviewFragment.prepareSnapForSending()
    public static final String SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING = "E";
    // SnapBryo instance variable name in SnapPreviewFragment
    public static final String SNAPPREVIEWFRAGMENT_VAR_SNAPBYRO = "s";

    // SnapBryo.getImageBitmap()
    public static final String SNAPBRYO_GETSNAPBITMAP = "B";
    // SnapBryo.getVideoUri()
    public static final String SNAPBRYO_VIDEOURI = "D";
    // SnapBryo.isImage()
    public static final String SNAPBRYO_ISIMAGE = "y";

    // SnapUpdate class
    public static final String SNAPUPDATE_CLASS = "com.snapchat.android.model.server.SnapUpdate";

    // StoryViewRecord class
    public static final String STORYVIEWRECORD_CLASS = "com.snapchat.android.model.StoryViewRecord";

    // SnapStateMessage class
    public static final String SNAPSTATEMESSAGE_BUILDER_CLASS = "com.snapchat.android.model.server.chat.SnapStateMessage.Builder";
    // SnapStateMessage.Builder.setScreenshotCount()
    public static final String SETSCREENSHOTCOUNT = "setScreenshotCount";

    // ScreenshotDetector class
    public static final String SCREENSHOTDETECTOR_CLASS = "com.snapchat.android.screenshotdetection.ScreenshotDetector";
    // ScreenshotDetector.runDectectionSession()
    public static final String SCREENSHOTDETECTOR_RUNDECTECTIONSESSION = "a";
}
