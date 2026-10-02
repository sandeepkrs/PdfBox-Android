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

import android.graphics.Bitmap;

import java.io.IOException;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.cos.COSInteger;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.cos.COSNull;
import com.tom_roush.pdfbox.cos.COSNumber;
import com.tom_roush.pdfbox.cos.COSStream;
import com.tom_roush.pdfbox.cos.COSString;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;

/**
 * An Indexed colour space specifies that an area is to be painted using a colour table
 * of arbitrary colours from another color space.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDIndexed extends PDSpecialColorSpace
{
    private final PDColor initialColor = new PDColor(new float[] { 0 }, this);

    private PDColorSpace baseColorSpace = null;

    // cached lookup data
    private byte[] lookupData;
    private float[][] colorTable;
    private int actualMaxIndex;
    private int[] rgbColorTable;

    public PDIndexed()
    {
        array = new COSArray();
        array.add(COSName.INDEXED);
        array.add(COSName.DEVICERGB);
        array.add(COSInteger.get(255));
        array.add(COSNull.NULL);
    }

    public PDIndexed(COSArray indexedArray) throws IOException
    {
        this(indexedArray, null);
    }

    public PDIndexed(COSArray indexedArray, PDResources resources) throws IOException
    {
        array = indexedArray;
        baseColorSpace = PDColorSpace.create(array.get(1), resources);
        readColorTable();
        initRgbColorTable();
    }

    @Override
    public String getName()
    {
        return COSName.INDEXED.getName();
    }

    @Override
    public int getNumberOfComponents()
    {
        return 1;
    }

    @Override
    public float[] getDefaultDecode(int bitsPerComponent)
    {
        return new float[] { 0, (float) Math.pow(2, bitsPerComponent) - 1 };
    }

    @Override
    public PDColor getInitialColor()
    {
        return initialColor;
    }

    private void initRgbColorTable() throws IOException
    {
        if (baseColorSpace == null)
        {
            rgbColorTable = new int[actualMaxIndex + 1];
            return;
        }

        int numBaseComponents = baseColorSpace.getNumberOfComponents();
        rgbColorTable = new int[actualMaxIndex + 1];

        float[] base = new float[numBaseComponents];
        for (int i = 0; i <= actualMaxIndex; i++)
        {
            for (int c = 0; c < numBaseComponents; c++)
            {
                base[c] = colorTable[i][c];
            }
            float[] rgb = baseColorSpace.toRGB(base);
            int r = Math.round(rgb[0] * 255f);
            int g = Math.round(rgb[1] * 255f);
            int b = Math.round(rgb[2] * 255f);
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            rgbColorTable[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
    }

    @Override
    public float[] toRGB(float[] value)
    {
        if (value.length != 1)
        {
            throw new IllegalArgumentException("Indexed color spaces must have one color value");
        }

        int index = Math.round(value[0]);
        index = Math.max(0, Math.min(index, actualMaxIndex));

        int argb = rgbColorTable[index];
        return new float[] {
            ((argb >> 16) & 0xFF) / 255f,
            ((argb >> 8) & 0xFF) / 255f,
            (argb & 0xFF) / 255f
        };
    }

    /**
     * Returns the ARGB packed integer color for a given palette index.
     *
     * @param index the palette index
     * @return 0xAARRGGBB packed color
     */
    public int getRgbColor(int index)
    {
        if (rgbColorTable == null || rgbColorTable.length == 0)
        {
            return 0xFF000000;
        }
        index = Math.max(0, Math.min(index, actualMaxIndex));
        return rgbColorTable[index];
    }

    public int[] getRgbColorTable()
    {
        return rgbColorTable;
    }

    @Override
    public Bitmap toRGBImage(Bitmap raster) throws IOException
    {
        int width = raster.getWidth();
        int height = raster.getHeight();
        int[] src = new int[width];
        int[] out = new int[width];

        Bitmap rgbImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++)
        {
            raster.getPixels(src, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++)
            {
                int index = (src[x] >> 24) & 0xFF;
                if (raster.getConfig() != Bitmap.Config.ALPHA_8)
                {
                    index = src[x] & 0xFF;
                }
                out[x] = getRgbColor(index);
            }
            rgbImage.setPixels(out, 0, width, 0, y, width, 1);
        }
        return rgbImage;
    }

    public PDColorSpace getBaseColorSpace()
    {
        return baseColorSpace;
    }

    private int getHival()
    {
        COSBase base = array.getObject(2);
        if (base instanceof COSNumber)
        {
            return ((COSNumber) base).intValue();
        }
        return 255;
    }

    private void readLookupData() throws IOException
    {
        if (lookupData == null)
        {
            COSBase lookupTable = array.getObject(3);
            if (lookupTable instanceof COSString)
            {
                lookupData = ((COSString) lookupTable).getBytes();
            }
            else if (lookupTable instanceof COSStream)
            {
                lookupData = new PDStream((COSStream) lookupTable).toByteArray();
            }
            else if (lookupTable == null)
            {
                lookupData = new byte[0];
            }
            else
            {
                throw new IOException("Error: Unknown type for lookup table " + lookupTable);
            }
        }
    }

    private void readColorTable() throws IOException
    {
        readLookupData();

        int maxIndex = Math.min(getHival(), 255);
        int numComponents = baseColorSpace != null ? baseColorSpace.getNumberOfComponents() : 3;

        if (numComponents > 0 && lookupData.length / numComponents < maxIndex + 1)
        {
            maxIndex = lookupData.length / numComponents - 1;
        }
        actualMaxIndex = Math.max(0, maxIndex);

        colorTable = new float[actualMaxIndex + 1][numComponents];
        for (int i = 0, offset = 0; i <= actualMaxIndex; i++)
        {
            for (int c = 0; c < numComponents; c++)
            {
                if (offset < lookupData.length)
                {
                    colorTable[i][c] = (lookupData[offset] & 0xff) / 255f;
                    offset++;
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "Indexed{base:" + baseColorSpace + " " +
                "hival:" + getHival() + " " +
                "lookup:(" + (colorTable != null ? colorTable.length : 0) + " entries)}";
    }
}
