/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.google.android.exoplayer2.video;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Surface;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RendererConfiguration;
import com.google.android.exoplayer2.decoder.SimpleDecoder;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.testutil.FakeSampleStream;
import com.google.android.exoplayer2.testutil.FakeSampleStream.FakeSampleStreamItem;
import com.google.android.exoplayer2.util.MimeTypes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** Unit test for {@link SimpleDecoderVideoRenderer}. */
@RunWith(AndroidJUnit4.class)
public final class SimpleDecoderVideoRendererTest {
  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private static final Format BASIC_MP4_1080 =
      Format.createVideoSampleFormat(
          /* id= */ null,
          /* sampleMimeType= */ MimeTypes.VIDEO_MP4,
          /* codecs= */ null,
          /* bitrate= */ Format.NO_VALUE,
          /* maxInputSize= */ Format.NO_VALUE,
          /* width= */ 1920,
          /* height= */ 1080,
          /* frameRate= */ Format.NO_VALUE,
          /* initializationData= */ null,
          /* rotationDegrees= */ 0,
          /* pixelWidthHeightRatio= */ 1f,
          /* drmInitData= */ null);

  private SimpleDecoderVideoRenderer renderer;
  @Mock private VideoRendererEventListener eventListener;

  @Before
  public void setUp() {
    renderer =
        new SimpleDecoderVideoRenderer(
            /* allowedJoiningTimeMs= */ 0,
            new Handler(),
            eventListener,
            /* maxDroppedFramesToNotify= */ -1) {
          @C.VideoOutputMode private int outputMode;

          @Override
          @Capabilities
          public int supportsFormat(Format format) {
            return RendererCapabilities.create(FORMAT_HANDLED);
          }

          @Override
          protected void setDecoderOutputMode(@C.VideoOutputMode int outputMode) {
            this.outputMode = outputMode;
          }

          @Override
          protected void renderOutputBufferToSurface(
              VideoDecoderOutputBuffer outputBuffer, Surface surface) {
            // Do nothing.
          }

          @Override
          protected SimpleDecoder<
                  VideoDecoderInputBuffer,
                  ? extends VideoDecoderOutputBuffer,
                  ? extends VideoDecoderException>
              createDecoder(Format format, @Nullable ExoMediaCrypto mediaCrypto) {
            return new SimpleDecoder<
                VideoDecoderInputBuffer, VideoDecoderOutputBuffer, VideoDecoderException>(
                new VideoDecoderInputBuffer[10], new VideoDecoderOutputBuffer[10]) {
              @Override
              protected VideoDecoderInputBuffer createInputBuffer() {
                return new VideoDecoderInputBuffer();
              }

              @Override
              protected VideoDecoderOutputBuffer createOutputBuffer() {
                return new VideoDecoderOutputBuffer(this::releaseOutputBuffer);
              }

              @Override
              protected VideoDecoderException createUnexpectedDecodeException(Throwable error) {
                return new VideoDecoderException("error", error);
              }

              @Nullable
              @Override
              protected VideoDecoderException decode(
                  VideoDecoderInputBuffer inputBuffer,
                  VideoDecoderOutputBuffer outputBuffer,
                  boolean reset) {
                outputBuffer.init(inputBuffer.timeUs, outputMode, /* supplementalData= */ null);
                return null;
              }

              @Override
              public String getName() {
                return "TestDecoder";
              }
            };
          }
        };
    renderer.setOutputSurface(new Surface(new SurfaceTexture(/* texName= */ 0)));
  }

  @Test
  public void enable_withMayRenderStartOfStream_rendersFirstFrameBeforeStart() throws Exception {
    FakeSampleStream fakeSampleStream =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME));

    renderer.enable(
        RendererConfiguration.DEFAULT,
        new Format[] {BASIC_MP4_1080},
        fakeSampleStream,
        /* positionUs= */ 0,
        /* joining= */ false,
        /* mayRenderStartOfStream= */ true,
        /* offsetUs */ 0);
    for (int i = 0; i < 10; i++) {
      renderer.render(/* positionUs= */ 0, SystemClock.elapsedRealtime() * 1000);
    }

    verify(eventListener).onRenderedFirstFrame(any());
  }

  @Test
  public void enable_withoutMayRenderStartOfStream_doesNotRenderFirstFrameBeforeStart()
      throws Exception {
    FakeSampleStream fakeSampleStream =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME));

    renderer.enable(
        RendererConfiguration.DEFAULT,
        new Format[] {BASIC_MP4_1080},
        fakeSampleStream,
        /* positionUs= */ 0,
        /* joining= */ false,
        /* mayRenderStartOfStream= */ false,
        /* offsetUs */ 0);
    for (int i = 0; i < 10; i++) {
      renderer.render(/* positionUs= */ 0, SystemClock.elapsedRealtime() * 1000);
    }

    verify(eventListener, never()).onRenderedFirstFrame(any());
  }

  @Test
  public void enable_withoutMayRenderStartOfStream_rendersFirstFrameAfterStart() throws Exception {
    FakeSampleStream fakeSampleStream =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME));

    renderer.enable(
        RendererConfiguration.DEFAULT,
        new Format[] {BASIC_MP4_1080},
        fakeSampleStream,
        /* positionUs= */ 0,
        /* joining= */ false,
        /* mayRenderStartOfStream= */ false,
        /* offsetUs */ 0);
    renderer.start();
    for (int i = 0; i < 10; i++) {
      renderer.render(/* positionUs= */ 0, SystemClock.elapsedRealtime() * 1000);
    }

    verify(eventListener).onRenderedFirstFrame(any());
  }

  // TODO: First frame of replaced stream are not yet reported.
  @Ignore
  @Test
  public void replaceStream_whenStarted_rendersFirstFrameOfNewStream() throws Exception {
    FakeSampleStream fakeSampleStream1 =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME),
            FakeSampleStreamItem.END_OF_STREAM_ITEM);
    FakeSampleStream fakeSampleStream2 =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME),
            FakeSampleStreamItem.END_OF_STREAM_ITEM);
    renderer.enable(
        RendererConfiguration.DEFAULT,
        new Format[] {BASIC_MP4_1080},
        fakeSampleStream1,
        /* positionUs= */ 0,
        /* joining= */ false,
        /* mayRenderStartOfStream= */ true,
        /* offsetUs */ 0);
    renderer.start();

    boolean replacedStream = false;
    for (int i = 0; i < 200; i += 10) {
      renderer.render(/* positionUs= */ i * 10, SystemClock.elapsedRealtime() * 1000);
      if (!replacedStream && renderer.hasReadStreamToEnd()) {
        renderer.replaceStream(
            new Format[] {BASIC_MP4_1080}, fakeSampleStream2, /* offsetUs= */ 100);
        replacedStream = true;
      }
    }

    verify(eventListener, times(2)).onRenderedFirstFrame(any());
  }

  @Test
  public void replaceStream_whenNotStarted_doesNotRenderFirstFrameOfNewStream() throws Exception {
    FakeSampleStream fakeSampleStream1 =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME),
            FakeSampleStreamItem.END_OF_STREAM_ITEM);
    FakeSampleStream fakeSampleStream2 =
        new FakeSampleStream(
            /* format= */ BASIC_MP4_1080,
            /* eventDispatcher= */ null,
            /* firstSampleTimeUs= */ 0,
            /* timeUsIncrement= */ 50,
            new FakeSampleStreamItem(new byte[] {0}, C.BUFFER_FLAG_KEY_FRAME),
            FakeSampleStreamItem.END_OF_STREAM_ITEM);
    renderer.enable(
        RendererConfiguration.DEFAULT,
        new Format[] {BASIC_MP4_1080},
        fakeSampleStream1,
        /* positionUs= */ 0,
        /* joining= */ false,
        /* mayRenderStartOfStream= */ true,
        /* offsetUs */ 0);

    boolean replacedStream = false;
    for (int i = 0; i < 200; i += 10) {
      renderer.render(/* positionUs= */ i * 10, SystemClock.elapsedRealtime() * 1000);
      if (!replacedStream && renderer.hasReadStreamToEnd()) {
        renderer.replaceStream(
            new Format[] {BASIC_MP4_1080}, fakeSampleStream2, /* offsetUs= */ 100);
        replacedStream = true;
      }
    }

    verify(eventListener).onRenderedFirstFrame(any());
  }
}
