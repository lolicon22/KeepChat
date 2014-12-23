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

import android.content.Context;

import java.io.File;

public class AskToSaveModel {
    private final Context context;
    private final KeepChat.MediaType mediaType;
    private final File imageFile;
    private final File videoFile;
    private final boolean hasOverlay;

    private AskToSaveModel(Builder builder) {
        this.context = builder.context;
        this.mediaType = builder.mediaType;
        this.imageFile = builder.imageFile;
        this.videoFile = builder.videoFile;
        this.hasOverlay = builder.hasOverlay;
    }

    public Context getContext() {
        return context;
    }

    public KeepChat.MediaType getMediaType() {
        return mediaType;
    }

    public File getImageFile() {
        return imageFile;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public boolean hasOverlay() {
        return hasOverlay;
    }

    public static class Builder {
        private Context context;
        private KeepChat.MediaType mediaType;
        private File imageFile;
        private File videoFile;
        private boolean hasOverlay;

        public Builder(Context context, KeepChat.MediaType mediaType) {
            this.context = context;
            this.mediaType = mediaType;
        }

        public void setImageFile(File imageFile) {
            this.imageFile = imageFile;
        }

        public void setVideoFile(File videoFile) {
            this.videoFile = videoFile;
        }

        public void setHasOverlay(boolean hasOverlay) {
            this.hasOverlay = hasOverlay;
        }

        public AskToSaveModel build() {
            return new AskToSaveModel(this);
        }
    }
}
