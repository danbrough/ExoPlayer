/*
 * Copyright 2020 The Android Open Source Project
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
package com.google.android.exoplayer2.source;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The default {@link MediaSourceFactory} implementation.
 *
 * <p>This implementation delegates calls to {@link #createMediaSource(MediaItem)} to the following
 * factories:
 *
 * <ul>
 *   <li>{@code DashMediaSource.Factory} if the item's {@link MediaItem.PlaybackProperties#sourceUri
 *       sourceUri} ends in '.mpd' or if its {@link MediaItem.PlaybackProperties#mimeType mimeType
 *       field} is explicitly set to {@link MimeTypes#APPLICATION_MPD} (Requires the <a
 *       href="https://exoplayer.dev/hello-world.html#add-exoplayer-modules">exoplayer-dash module
 *       to be added</a> to the app).
 *   <li>{@code HlsMediaSource.Factory} if the item's {@link MediaItem.PlaybackProperties#sourceUri
 *       sourceUri} ends in '.m3u8' or if its {@link MediaItem.PlaybackProperties#mimeType mimeType
 *       field} is explicitly set to {@link MimeTypes#APPLICATION_M3U8} (Requires the <a
 *       href="https://exoplayer.dev/hello-world.html#add-exoplayer-modules">exoplayer-hls module to
 *       be added</a> to the app).
 *   <li>{@code SsMediaSource.Factory} if the item's {@link MediaItem.PlaybackProperties#sourceUri
 *       sourceUri} ends in '.ism', '.ism/Manifest' or if its {@link
 *       MediaItem.PlaybackProperties#mimeType mimeType field} is explicitly set to {@link
 *       MimeTypes#APPLICATION_SS} (Requires the <a
 *       href="https://exoplayer.dev/hello-world.html#add-exoplayer-modules">
 *       exoplayer-smoothstreaming module to be added</a> to the app).
 *   <li>{@link ProgressiveMediaSource.Factory} serves as a fallback if the item's {@link
 *       MediaItem.PlaybackProperties#sourceUri sourceUri} doesn't match one of the above. It tries
 *       to infer the required extractor by using the {@link
 *       com.google.android.exoplayer2.extractor.DefaultExtractorsFactory}. An {@link
 *       UnrecognizedInputFormatException} is thrown if none of the available extractors can read
 *       the stream.
 * </ul>
 *
 * <h3>DrmSessionManager creation for protected content</h3>
 *
 * <p>For a media item with a valid {@link
 * com.google.android.exoplayer2.MediaItem.DrmConfiguration}, a {@link DefaultDrmSessionManager} is
 * created. The following setters can be used to optionally configure the creation:
 *
 * <ul>
 *   <li>{@link #setPlayClearContentWithoutKey(boolean)}: See {@link
 *       DefaultDrmSessionManager.Builder#setPlayClearSamplesWithoutKeys(boolean)} (default: {@code
 *       false}).
 *   <li>{@link #setUseDrmSessionForClearContent(int...)}: See {@link
 *       DefaultDrmSessionManager.Builder#setUseDrmSessionsForClearContent(int...)} (default: none).
 *   <li>{@link #setDrmHttpDataSourceFactory(HttpDataSource.Factory)}: Sets the data source factory
 *       to be used by the {@link HttpMediaDrmCallback} for network requests (default: {@link
 *       DefaultHttpDataSourceFactory}).
 * </ul>
 *
 * <p>For media items without a drm configuration {@link DrmSessionManager#DUMMY} is used. To use an
 * alternative dummy, apps can pass a drm session manager to {@link
 * #setDrmSessionManager(DrmSessionManager)} which will be used for all items without a drm
 * configuration.
 */
public final class DefaultMediaSourceFactory implements MediaSourceFactory {

  /**
   * Creates a new instance with the given {@link Context}.
   *
   * <p>This is functionally equivalent with calling {@code #newInstance(Context,
   * DefaultDataSourceFactory)}.
   *
   * @param context The {@link Context}.
   */
  public static DefaultMediaSourceFactory newInstance(Context context) {
    return newInstance(
        context,
        new DefaultDataSourceFactory(
            context, Util.getUserAgent(context, ExoPlayerLibraryInfo.VERSION_SLASHY)));
  }

  /**
   * Creates a new instance with the given {@link Context} and {@link DataSource.Factory}.
   *
   * @param context The {@link Context}.
   * @param dataSourceFactory A {@link DataSource.Factory} to be used to create media sources.
   */
  public static DefaultMediaSourceFactory newInstance(
      Context context, DataSource.Factory dataSourceFactory) {
    return new DefaultMediaSourceFactory(context, dataSourceFactory);
  }

  private final SparseArray<MediaSourceFactory> mediaSourceFactories;
  @C.ContentType private final int[] supportedTypes;
  private final String userAgent;

  private DrmSessionManager<?> drmSessionManager;
  private HttpDataSource.Factory drmHttpDataSourceFactory;
  private boolean playClearContentWithoutKey;
  private int[] useDrmSessionsForClearContentTrackTypes;
  @Nullable private List<StreamKey> streamKeys;

  private DefaultMediaSourceFactory(Context context, DataSource.Factory dataSourceFactory) {
    drmSessionManager = DrmSessionManager.getDummyDrmSessionManager();
    userAgent = Util.getUserAgent(context, ExoPlayerLibraryInfo.VERSION_SLASHY);
    drmHttpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
    useDrmSessionsForClearContentTrackTypes = new int[0];
    mediaSourceFactories = loadDelegates(dataSourceFactory);
    supportedTypes = new int[mediaSourceFactories.size()];
    for (int i = 0; i < mediaSourceFactories.size(); i++) {
      supportedTypes[i] = mediaSourceFactories.keyAt(i);
    }
  }

  /**
   * Sets the {@link HttpDataSource.Factory} to be used for creating {@link HttpMediaDrmCallback
   * HttpMediaDrmCallbacks} which executes key and provisioning requests over HTTP. If {@code null}
   * is passed the {@link DefaultHttpDataSourceFactory} is used.
   *
   * @param drmHttpDataSourceFactory The HTTP data source factory or {@code null} to use {@link
   *     DefaultHttpDataSourceFactory}.
   * @return This factory, for convenience.
   */
  public DefaultMediaSourceFactory setDrmHttpDataSourceFactory(
      @Nullable HttpDataSource.Factory drmHttpDataSourceFactory) {
    this.drmHttpDataSourceFactory =
        drmHttpDataSourceFactory != null
            ? drmHttpDataSourceFactory
            : new DefaultHttpDataSourceFactory(userAgent);
    return this;
  }

  /**
   * Used to create {@link DrmSessionManager DrmSessionManagers}. See {@link
   * DefaultDrmSessionManager.Builder#setPlayClearSamplesWithoutKeys(boolean)}.
   *
   * @return This factory, for convenience.
   */
  public DefaultMediaSourceFactory setPlayClearContentWithoutKey(
      boolean playClearContentWithoutKey) {
    this.playClearContentWithoutKey = playClearContentWithoutKey;
    return this;
  }

  /**
   * Used to create {@link DrmSessionManager DrmSessionManagers}. See {@link
   * DefaultDrmSessionManager.Builder#setUseDrmSessionsForClearContent(int...)}.
   *
   * @return This factory, for convenience.
   */
  public DefaultMediaSourceFactory setUseDrmSessionForClearContent(
      int... useDrmSessionsForClearContentTrackTypes) {
    for (int trackType : useDrmSessionsForClearContentTrackTypes) {
      Assertions.checkArgument(trackType == C.TRACK_TYPE_VIDEO || trackType == C.TRACK_TYPE_AUDIO);
    }
    this.useDrmSessionsForClearContentTrackTypes = useDrmSessionsForClearContentTrackTypes.clone();
    return this;
  }

  @Override
  public DefaultMediaSourceFactory setDrmSessionManager(
      @Nullable DrmSessionManager<?> drmSessionManager) {
    this.drmSessionManager =
        drmSessionManager != null
            ? drmSessionManager
            : DrmSessionManager.getDummyDrmSessionManager();
    return this;
  }

  public DefaultMediaSourceFactory setLoadErrorHandlingPolicy(
      @Nullable LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
    LoadErrorHandlingPolicy newLoadErrorHandlingPolicy =
        loadErrorHandlingPolicy != null
            ? loadErrorHandlingPolicy
            : new DefaultLoadErrorHandlingPolicy();
    for (int i = 0; i < mediaSourceFactories.size(); i++) {
      mediaSourceFactories.valueAt(i).setLoadErrorHandlingPolicy(newLoadErrorHandlingPolicy);
    }
    return this;
  }

  /**
   * @deprecated Use {@link MediaItem.Builder#setStreamKeys(List)} and {@link
   *     #createMediaSource(MediaItem)} instead.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public DefaultMediaSourceFactory setStreamKeys(@Nullable List<StreamKey> streamKeys) {
    this.streamKeys = streamKeys != null && !streamKeys.isEmpty() ? streamKeys : null;
    return this;
  }

  @Override
  public int[] getSupportedTypes() {
    return Arrays.copyOf(supportedTypes, supportedTypes.length);
  }

  @SuppressWarnings("deprecation")
  @Override
  public MediaSource createMediaSource(MediaItem mediaItem) {
    Assertions.checkNotNull(mediaItem.playbackProperties);
    @C.ContentType
    int type =
        inferContentType(
            mediaItem.playbackProperties.sourceUri, mediaItem.playbackProperties.mimeType);
    @Nullable MediaSourceFactory mediaSourceFactory = mediaSourceFactories.get(type);
    Assertions.checkNotNull(
        mediaSourceFactory, "No suitable media source factory found for content type: " + type);
    mediaSourceFactory.setDrmSessionManager(createDrmSessionManager(mediaItem));
    mediaSourceFactory.setStreamKeys(
        !mediaItem.playbackProperties.streamKeys.isEmpty()
            ? mediaItem.playbackProperties.streamKeys
            : streamKeys);
    return mediaSourceFactory.createMediaSource(mediaItem);
  }

  // internal methods

  private DrmSessionManager<?> createDrmSessionManager(MediaItem mediaItem) {
    Assertions.checkNotNull(mediaItem.playbackProperties);
    if (mediaItem.playbackProperties.drmConfiguration == null
        || mediaItem.playbackProperties.drmConfiguration.licenseUri == null
        || Util.SDK_INT < 18) {
      return drmSessionManager;
    }
    return new DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(
            mediaItem.playbackProperties.drmConfiguration.uuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
        .setMultiSession(mediaItem.playbackProperties.drmConfiguration.multiSession)
        .setPlayClearSamplesWithoutKeys(playClearContentWithoutKey)
        .setUseDrmSessionsForClearContent(useDrmSessionsForClearContentTrackTypes)
        .build(createHttpMediaDrmCallback(mediaItem.playbackProperties.drmConfiguration));
  }

  private MediaDrmCallback createHttpMediaDrmCallback(MediaItem.DrmConfiguration drmConfiguration) {
    Assertions.checkNotNull(drmConfiguration.licenseUri);
    HttpMediaDrmCallback drmCallback =
        new HttpMediaDrmCallback(drmConfiguration.licenseUri.toString(), drmHttpDataSourceFactory);
    for (Map.Entry<String, String> entry : drmConfiguration.requestHeaders.entrySet()) {
      drmCallback.setKeyRequestProperty(entry.getKey(), entry.getValue());
    }
    return drmCallback;
  }

  private static SparseArray<MediaSourceFactory> loadDelegates(
      DataSource.Factory dataSourceFactory) {
    SparseArray<MediaSourceFactory> factories = new SparseArray<>();
    // LINT.IfChange
    try {
      Class<? extends MediaSourceFactory> factoryClazz =
          Class.forName("com.google.android.exoplayer2.source.dash.DashMediaSource$Factory")
              .asSubclass(MediaSourceFactory.class);
      factories.put(
          C.TYPE_DASH,
          factoryClazz.getConstructor(DataSource.Factory.class).newInstance(dataSourceFactory));
    } catch (Exception e) {
      // Expected if the app was built without the dash module.
    }
    try {
      Class<? extends MediaSourceFactory> factoryClazz =
          Class.forName(
                  "com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory")
              .asSubclass(MediaSourceFactory.class);
      factories.put(
          C.TYPE_SS,
          factoryClazz.getConstructor(DataSource.Factory.class).newInstance(dataSourceFactory));
    } catch (Exception e) {
      // Expected if the app was built without the smoothstreaming module.
    }
    try {
      Class<? extends MediaSourceFactory> factoryClazz =
          Class.forName("com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory")
              .asSubclass(MediaSourceFactory.class);
      factories.put(
          C.TYPE_HLS,
          factoryClazz.getConstructor(DataSource.Factory.class).newInstance(dataSourceFactory));
    } catch (Exception e) {
      // Expected if the app was built without the hls module.
    }
    // LINT.ThenChange(../../../../../../../../proguard-rules.txt)
    factories.put(C.TYPE_OTHER, new ProgressiveMediaSource.Factory(dataSourceFactory));
    return factories;
  }

  private static int inferContentType(Uri sourceUri, @Nullable String mimeType) {
    if (mimeType == null) {
      return Util.inferContentType(sourceUri);
    }
    switch (mimeType) {
      case MimeTypes.APPLICATION_MPD:
        return C.TYPE_DASH;
      case MimeTypes.APPLICATION_M3U8:
        return C.TYPE_HLS;
      case MimeTypes.APPLICATION_SS:
        return C.TYPE_SS;
      default:
        return Util.inferContentType(sourceUri);
    }
  }
}
