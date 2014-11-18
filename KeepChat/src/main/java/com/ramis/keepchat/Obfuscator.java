/*
 * Copyright (C) 2014  Sturmen, stammler, Ramis and P1nGu1n
 *
 * This file is part of Keepchat.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    // ReceivedSnap.getImageBitmap(Context)
    public static final String RECEIVEDSNAP_GETIMAGEBITMAP = "b";
    // ReceivedSnap.getVideoUri()
    public static final String RECEIVEDSNAP_GETVIDEOURI = "N";
    // ReceivedSnap.markViewed()
    public static final String RECEIVEDSNAP_MARKVIEWED = "p";
    // ReceivedSnap.getSender()
    public static final String RECEIVEDSNAP_GETSENDER = "j";

    // StorySnap class
    public static final String STORYSNAP_CLASS = "com.snapchat.android.model.StorySnap";
    // StorySnap.getImageBitmap(Context)
    public static final String STORYSNAP_GETIMAGEBITMAP = "a";
    // StorySnap.getSender()
    public static final String STORYSNAP_GETSENDER = "aD";

    // SnapView class
    public static final String SNAPVIEW_CLASS = "com.snapchat.android.ui.SnapView";
    // SnapView.showImage(Boolean, Boolean, Boolean, Boolean)
    public static final String SNAPVIEW_SHOWIMAGE = "b";
    // SnapView.showVideo(Boolean, Boolean, Boolean, Boolean)
    public static final String SNAPVIEW_SHOWVIDEO = "a";

    // com.snapchat.model.Snap
    // Snap.getTimestamp()
    public static final String SNAP_GETTIMESTAMP = "ac";

    // SnapPreviewFragment class
    public static final String SNAPPREVIEWFRAGMENT_CLASS = "com.snapchat.android.SnapPreviewFragment";
    // SnapPreviewFragment.prepareSnapForSending()
    public static final String SNAPPREVIEWFRAGMENT_PREPARESNAPFORSENDING = "E";
    // Snapbryo instance variable name in SnapPreviewFragment
    public static final String SNAPPREVIEWFRAGMENT_VAR_SNAPBYRO = "s";

    // com.snapchat.android.model.Snapbryo
    // Snapbryo.getImageBitmap()
    public static final String SNAPBRYO_GETSNAPBITMAP = "B";
    // Snapbryo.getVideoUri()
    public static final String SNAPBRYO_VIDEOURI = "D";
    // Snapbryo.isImage()
    public static final String SNAPBRYO_ISIMAGE = "y";

    // SnapUpdate class
    public static final String SNAPUPDATE_CLASS = "com.snapchat.android.model.server.SnapUpdate";

    // StoryViewRecord class
    public static final String STORYVIEWRECORD_CLASS = "com.snapchat.android.model.StoryViewRecord";

    // SnapStateMessage class
    public static final String SNAPSTATEMESSAGE_BUILDER_CLASS = "com.snapchat.android.model.server.chat.SnapStateMessage.Builder";
    // SnapStateMessage.Builder.setScreenshotCount(Long)
    public static final String SETSCREENSHOTCOUNT = "setScreenshotCount";

    // ScreenshotDetector class
    public static final String SCREENSHOTDETECTOR_CLASS = "com.snapchat.android.screenshotdetection.ScreenshotDetector";
    // ScreenshotDetector.runDectectionSession()
    public static final String SCREENSHOTDETECTOR_RUNDECTECTIONSESSION = "a";
}
