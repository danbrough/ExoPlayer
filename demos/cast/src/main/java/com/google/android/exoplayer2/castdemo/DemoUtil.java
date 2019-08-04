/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.castdemo;

import static com.google.android.exoplayer2.util.MimeTypes.AUDIO_AAC;
import static com.google.android.exoplayer2.util.MimeTypes.AUDIO_MPEG;
import static com.google.android.exoplayer2.util.MimeTypes.AUDIO_MPEG_L2;

import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ext.cast.MediaItem;
import com.google.android.exoplayer2.ext.cast.MediaItem.DrmConfiguration;
import com.google.android.exoplayer2.util.MimeTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility methods and constants for the Cast demo application. */
/* package */ final class DemoUtil {

  public static final String MIME_TYPE_DASH = MimeTypes.APPLICATION_MPD;
  public static final String MIME_TYPE_HLS = MimeTypes.APPLICATION_M3U8;
  public static final String MIME_TYPE_SS = MimeTypes.APPLICATION_SS;
  public static final String MIME_TYPE_VIDEO_MP4 = MimeTypes.VIDEO_MP4;

  /** The list of samples available in the cast demo app. */
  public static final List<MediaItem> SAMPLES;


  static {
    ArrayList<MediaItem> samples = new ArrayList<>();

    // Clear content.
/*    samples.add(
        new MediaItem.Builder()
            .setUri("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")
            .setTitle("Clear DASH: Tears")
            .setMimeType(MIME_TYPE_DASH)
            .build());
    samples.add(
        new MediaItem.Builder()
            .setUri("https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8")
            .setTitle("Clear HLS: Angel one")
            .setMimeType(MIME_TYPE_HLS)
            .build());
    samples.add(
        new MediaItem.Builder()
            .setUri("https://html5demos.com/assets/dizzy.mp4")
            .setTitle("Clear MP4: Dizzy")
            .setMimeType(MIME_TYPE_VIDEO_MP4)
            .build());*/

    /*
        samples.add(new Sample("http://radionz-ice.streamguys.com/national_aac64", "National AAC64",
        MimeTypes.AUDIO_AAC));
    samples.add(new Sample("https://radionz-ice.streamguys.com/national.mp3", "National Mp3",
        MimeTypes.AUDIO_MPEG_L2));
    samples.add(new Sample("http://ice4.somafm.com/digitalis-128-aac", "Digitalis",
        MimeTypes.AUDIO_AAC));
    samples.add(new Sample("http://ice4.somafm.com/u80s-128-aac", "UNderground 80S",
        MimeTypes.AUDIO_AAC));

     */
    samples.add(
        new MediaItem.Builder()
            .setUri("http://radionz-ice.streamguys.com/national_aac64")
            .setTitle("National AAC64")
            .setMimeType("audio/aacp")
            .build());


    samples.add(
        new MediaItem.Builder()
            .setUri("http://ice4.somafm.com/digitalis-128-aac")
            .setTitle("Digitalis")
            .setMimeType(AUDIO_AAC)
            .build());

    samples.add(
        new MediaItem.Builder()
            .setUri("http://ice4.somafm.com/u80s-128-aac")
            .setTitle("Underground 80s")
            .setMimeType(AUDIO_AAC)
            .build());

    samples.add(
        new MediaItem.Builder()
            .setUri("http://radionz-ice.streamguys.com/national.mp3")
            .setTitle("National MP3")
            .setMimeType(AUDIO_MPEG)
            .build());

    // DRM content.
    samples.add(
        new MediaItem.Builder()
            .setUri(Uri.parse("https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"))
            .setTitle("Widevine DASH cenc: Tears")
            .setMimeType(MIME_TYPE_DASH)
            .setDrmConfiguration(
                new DrmConfiguration(
                    C.WIDEVINE_UUID,
                    Uri.parse("https://proxy.uat.widevine.com/proxy?provider=widevine_test"),
                    Collections.emptyMap()))
            .build());
    samples.add(
        new MediaItem.Builder()
            .setUri(
                Uri.parse(
                    "https://storage.googleapis.com/wvmedia/cbc1/h264/tears/tears_aes_cbc1.mpd"))
            .setTitle("Widevine DASH cbc1: Tears")
            .setMimeType(MIME_TYPE_DASH)
            .setDrmConfiguration(
                new DrmConfiguration(
                    C.WIDEVINE_UUID,
                    Uri.parse("https://proxy.uat.widevine.com/proxy?provider=widevine_test"),
                    Collections.emptyMap()))
            .build());
    samples.add(
        new MediaItem.Builder()
            .setUri(
                Uri.parse(
                    "https://storage.googleapis.com/wvmedia/cbcs/h264/tears/tears_aes_cbcs.mpd"))
            .setTitle("Widevine DASH cbcs: Tears")
            .setMimeType(MIME_TYPE_DASH)
            .setDrmConfiguration(
                new DrmConfiguration(
                    C.WIDEVINE_UUID,
                    Uri.parse("https://proxy.uat.widevine.com/proxy?provider=widevine_test"),
                    Collections.emptyMap()))
            .build());

    SAMPLES = Collections.unmodifiableList(samples);
  }

  private DemoUtil() {}
}
