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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * Unit tests for JPEGFactory SOF component detection and color space selection.
 */
public class JPEGFactoryTest extends TestCase
{
    private byte[] createSyntheticJpeg(int width, int height, int numComponents) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // SOI
        out.write(0xFF);
        out.write(0xD8);

        // SOF0 (Baseline DCT)
        out.write(0xFF);
        out.write(0xC0);

        int length = 8 + (3 * numComponents);
        out.write((length >> 8) & 0xFF);
        out.write(length & 0xFF);

        out.write(8); // Precision: 8 bits
        out.write((height >> 8) & 0xFF);
        out.write(height & 0xFF);
        out.write((width >> 8) & 0xFF);
        out.write(width & 0xFF);
        out.write(numComponents);

        for (int i = 1; i <= numComponents; i++)
        {
            out.write(i);    // Component ID
            out.write(0x11); // Sampling factors
            out.write(0);    // Quantization table number
        }

        // EOI
        out.write(0xFF);
        out.write(0xD9);

        return out.toByteArray();
    }

    public void testGrayscaleJpegDetectedAsDeviceGray() throws IOException
    {
        PDDocument doc = new PDDocument();
        byte[] grayJpeg = createSyntheticJpeg(40, 25, 1);

        PDImageXObject image = JPEGFactory.createFromByteArray(doc, grayJpeg);
        assertNotNull(image);
        assertEquals(40, image.getWidth());
        assertEquals(25, image.getHeight());
        assertEquals(PDDeviceGray.INSTANCE, image.getColorSpace());
        assertEquals(1, image.getColorSpace().getNumberOfComponents());

        doc.close();
    }

    public void testRgbJpegDetectedAsDeviceRGB() throws IOException
    {
        PDDocument doc = new PDDocument();
        byte[] rgbJpeg = createSyntheticJpeg(50, 30, 3);

        PDImageXObject image = JPEGFactory.createFromByteArray(doc, rgbJpeg);
        assertNotNull(image);
        assertEquals(50, image.getWidth());
        assertEquals(30, image.getHeight());
        assertEquals(PDDeviceRGB.INSTANCE, image.getColorSpace());
        assertEquals(3, image.getColorSpace().getNumberOfComponents());

        doc.close();
    }

    public void testCmykJpegDetectedAsDeviceCMYK() throws IOException
    {
        PDDocument doc = new PDDocument();
        byte[] cmykJpeg = createSyntheticJpeg(60, 35, 4);

        PDImageXObject image = JPEGFactory.createFromByteArray(doc, cmykJpeg);
        assertNotNull(image);
        assertEquals(60, image.getWidth());
        assertEquals(35, image.getHeight());
        assertEquals(PDDeviceCMYK.INSTANCE, image.getColorSpace());
        assertEquals(4, image.getColorSpace().getNumberOfComponents());

        doc.close();
    }
}
