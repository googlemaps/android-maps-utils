/*
 * Copyright 2026 Google LLC
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
package com.google.maps.android.data.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.maps.android.data.parser.geojson.GeoJsonParser;
import com.google.maps.android.data.parser.gpx.GpxParser;
import com.google.maps.android.data.parser.kml.KmlParser;
import com.google.maps.android.data.parser.kml.KmzParser;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

/**
 * Verifies from Java that the parsers declare {@link IOException}. Each {@code catch (IOException)}
 * below only compiles if the called method declares it, so this test also guards the Java
 * signature.
 */
public class ParserCheckedExceptionTest {
  private static final String MESSAGE = "stream failure";

  private static InputStream failingStream() {
    return new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException(MESSAGE);
      }
    };
  }

  @Test
  public void kmzParserDeclaresIOException() {
    try {
      new KmzParser().parse(failingStream());
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals(MESSAGE, e.getMessage());
    }
  }

  @Test
  public void kmlParserDeclaresIOException() {
    try {
      new KmlParser().parse(failingStream());
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals(MESSAGE, e.getMessage());
    }
  }

  @Test
  public void kmlParserParseAsKmlDeclaresIOException() {
    try {
      new KmlParser().parseAsKml(failingStream());
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals(MESSAGE, e.getMessage());
    }
  }

  @Test
  public void gpxParserDeclaresIOException() {
    try {
      new GpxParser().parse(failingStream());
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals(MESSAGE, e.getMessage());
    }
  }

  @Test
  public void geoJsonParserDeclaresIOException() {
    try {
      new GeoJsonParser().parse(failingStream());
      fail("Expected IOException");
    } catch (IOException e) {
      assertEquals(MESSAGE, e.getMessage());
    }
  }
}
