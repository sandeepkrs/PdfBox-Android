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
package com.tom_roush.pdfbox.pdmodel.graphics.color;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSInteger;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.cos.COSStream;
import com.tom_roush.pdfbox.cos.COSString;

/**
 * Unit tests for PDIndexed color space.
 */
public class PDIndexedTest extends TestCase
{
    public void testDefaultConstructor()
    {
        PDIndexed indexed = new PDIndexed();
        assertEquals("Indexed", indexed.getName());
        assertEquals(1, indexed.getNumberOfComponents());
        float[] decode = indexed.getDefaultDecode(8);
        assertEquals(0.0f, decode[0]);
        assertEquals(255.0f, decode[1]);
    }

    public void testRgbLookupFromString() throws IOException
    {
        // 3 entries: Red (255, 0, 0), Green (0, 255, 0), Blue (0, 0, 255)
        byte[] lookup = new byte[] {
            (byte) 255, 0, 0,
            0, (byte) 255, 0,
            0, 0, (byte) 255
        };

        COSArray array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(2)); // hival = 2 (3 colors: 0, 1, 2)
        array.add(new COSString(lookup));

        PDColorSpace cs = PDColorSpace.create(array);
        assertTrue(cs instanceof PDIndexed);

        PDIndexed indexed = (PDIndexed) cs;
        assertEquals(1, indexed.getNumberOfComponents());
        assertEquals(PDDeviceRGB.INSTANCE, indexed.getBaseColorSpace());

        int[] rgbTable = indexed.getRgbColorTable();
        assertNotNull(rgbTable);
        assertEquals(3, rgbTable.length);

        // Check packed ARGB values
        assertEquals(0xFFFF0000, indexed.getRgbColor(0));
        assertEquals(0xFF00FF00, indexed.getRgbColor(1));
        assertEquals(0xFF0000FF, indexed.getRgbColor(2));

        // Test toRGB
        float[] rgb0 = indexed.toRGB(new float[] { 0 });
        assertEquals(1.0f, rgb0[0], 0.001f);
        assertEquals(0.0f, rgb0[1], 0.001f);
        assertEquals(0.0f, rgb0[2], 0.001f);

        float[] rgb1 = indexed.toRGB(new float[] { 1 });
        assertEquals(0.0f, rgb1[0], 0.001f);
        assertEquals(1.0f, rgb1[1], 0.001f);
        assertEquals(0.0f, rgb1[2], 0.001f);

        float[] rgb2 = indexed.toRGB(new float[] { 2 });
        assertEquals(0.0f, rgb2[0], 0.001f);
        assertEquals(0.0f, rgb2[1], 0.001f);
        assertEquals(1.0f, rgb2[2], 0.001f);

        // Test index clamping (out-of-bounds index should clamp safely)
        assertEquals(0xFF0000FF, indexed.getRgbColor(10));
        assertEquals(0xFFFF0000, indexed.getRgbColor(-5));
    }

    public void testGrayLookupFromString() throws IOException
    {
        // 2 entries: Black (0), White (255)
        byte[] lookup = new byte[] { 0, (byte) 255 };

        COSArray array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICEGRAY);
        array.add(COSInteger.get(1)); // hival = 1 (2 colors)
        array.add(new COSString(lookup));

        PDIndexed indexed = new PDIndexed(array);
        assertEquals(2, indexed.getRgbColorTable().length);

        // Black (0) -> ARGB 0xFF000000
        assertEquals(0xFF000000, indexed.getRgbColor(0));
        // White (255) -> ARGB 0xFFFFFFFF
        assertEquals(0xFFFFFFFF, indexed.getRgbColor(1));
    }

    public void testLookupFromStream() throws IOException
    {
        byte[] lookup = new byte[] {
            (byte) 200, (byte) 100, 50
        };

        COSStream stream = new COSStream();
        OutputStream out = stream.createOutputStream();
        out.write(lookup);
        out.close();

        COSArray array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(0)); // hival = 0 (1 color)
        array.add(stream);

        PDIndexed indexed = new PDIndexed(array);
        assertEquals(1, indexed.getRgbColorTable().length);

        int color = indexed.getRgbColor(0);
        assertEquals(200, (color >> 16) & 0xFF);
        assertEquals(100, (color >> 8) & 0xFF);
        assertEquals(50, color & 0xFF);
    }
}
