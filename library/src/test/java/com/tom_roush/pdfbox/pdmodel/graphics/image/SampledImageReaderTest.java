/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tom_roush.pdfbox.pdmodel.graphics.image;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * Unit tests for SampledImageReader bounds checks and stream validation.
 */
public class SampledImageReaderTest extends TestCase
{
    public void testZeroWidthThrowsIOException()
    {
        byte[] data = new byte[] { 1, 2, 3 };
        try
        {
            SampledImageReader.createBitmapFromRawStream(
                new ByteArrayInputStream(data), 0, 3, 1, PDDeviceRGB.INSTANCE);
            fail("Expected IOException for zero width");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("Invalid image dimensions"));
        }
    }

    public void testZeroComponentsThrowsIOException()
    {
        byte[] data = new byte[] { 1, 2, 3 };
        try
        {
            SampledImageReader.createBitmapFromRawStream(
                new ByteArrayInputStream(data), 10, 0, 1, PDDeviceRGB.INSTANCE);
            fail("Expected IOException for zero components");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("Invalid image dimensions"));
        }
    }

    public void testInsufficientStreamDataThrowsIOException()
    {
        // 10 width * 3 components = 30 bytes required for a single row, but only 5 provided
        byte[] truncated = new byte[5];
        try
        {
            SampledImageReader.createBitmapFromRawStream(
                new ByteArrayInputStream(truncated), 10, 3, 1, PDDeviceRGB.INSTANCE);
            fail("Expected IOException for insufficient stream data");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("insufficient data"));
        }
    }

    public void testEmptyStreamThrowsIOException()
    {
        byte[] empty = new byte[0];
        try
        {
            SampledImageReader.createBitmapFromRawStream(
                new ByteArrayInputStream(empty), 10, 3, 1, PDDeviceRGB.INSTANCE);
            fail("Expected IOException for empty stream");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("insufficient data"));
        }
    }

    public void testUnsupportedComponentsThrowsIOException()
    {
        // 5 components (not 1, 3, or 4 and not indexed)
        byte[] data = new byte[100];
        try
        {
            SampledImageReader.createBitmapFromRawStream(
                new ByteArrayInputStream(data), 2, 5, 1, PDDeviceRGB.INSTANCE);
            fail("Expected IOException for unsupported components");
        }
        catch (IOException expected)
        {
            assertTrue(expected.getMessage().contains("Unsupported number of components"));
        }
    }
}
