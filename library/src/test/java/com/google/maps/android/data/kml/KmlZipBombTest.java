/*
 * Copyright 2026 Google Inc.
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
package com.google.maps.android.data.kml;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class KmlZipBombTest {

    @Test
    public void testValidKmz() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry("doc.kml"));
        zos.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document></Document></kml>".getBytes());
        zos.closeEntry();
        zos.close();

        Context context = ApplicationProvider.getApplicationContext();
        KmlLayer layer = new KmlLayer(null, new ByteArrayInputStream(baos.toByteArray()), context);
        assertNotNull(layer);
    }

    @Test
    public void testMaxEntriesLimit() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (int i = 0; i < 202; i++) {
            zos.putNextEntry(new ZipEntry("entry" + i + ".txt"));
            zos.write("data".getBytes());
            zos.closeEntry();
        }
        zos.close();

        Context context = ApplicationProvider.getApplicationContext();
        try {
            new KmlLayer(null, new ByteArrayInputStream(baos.toByteArray()), context);
            fail("Should have thrown IOException due to too many entries");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test
    public void testMaxSizeLimit() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry("large_entry.kml"));
        // 50MB + 1 byte
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < 51; i++) {
            zos.write(largeData);
        }
        zos.closeEntry();
        zos.close();

        Context context = ApplicationProvider.getApplicationContext();
        try {
            new KmlLayer(null, new ByteArrayInputStream(baos.toByteArray()), context);
            fail("Should have thrown IOException due to size limit");
        } catch (IOException e) {
            // Expected
        }
    }
}
