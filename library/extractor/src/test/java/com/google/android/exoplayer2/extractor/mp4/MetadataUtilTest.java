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
package com.google.android.exoplayer2.extractor.mp4;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Test for {@link MetadataUtil}. */
@RunWith(AndroidJUnit4.class)
public final class MetadataUtilTest {

  @Test
  public void standardGenre_length_matchesNumberOfId3Genres() {
    // Check that we haven't forgotten a genre in the list.
    assertThat(MetadataUtil.STANDARD_GENRES).hasLength(192);
  }

  /**
   * Test case for https://github.com/google/ExoPlayer/issues/6774. The sample file contains an mdat
   * atom whose size indicates that it extends 8 bytes beyond the end of the file.
   */
  @Test
  public void testMp4SampleWithMdatTooLong() throws Exception {
    ExtractorAsserts.assertBehavior(Mp4Extractor::new, "mp4/sample_mdat_too_long.mp4");
  }

  @Test
  public void testMp4SampleWithAc4Track() throws Exception {
    ExtractorAsserts.assertBehavior(Mp4Extractor::new, "mp4/sample_ac4.mp4");
  }

  @Test
  public void testMp4SampleWithSlowMotionMetadata() throws Exception {
    ExtractorAsserts.assertBehavior(Mp4Extractor::new, "mp4/sample_android_slow_motion.mp4");
  }
}
