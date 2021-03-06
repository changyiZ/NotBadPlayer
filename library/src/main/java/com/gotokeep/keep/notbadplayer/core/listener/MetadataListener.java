/*
 * Copyright (C) 2016 Brian Wernick,
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gotokeep.keep.notbadplayer.core.listener;

import com.google.android.exoplayer2.metadata.Metadata;

/**
 * A listener for receiving ID3 metadata parsed from the media stream.
 */
public interface MetadataListener {
    /**
     * Called each time there is a metadata associated with current playback time.
     *
     * @param metadata The metadata.
     */
    void onMetadata(Metadata metadata);
}
