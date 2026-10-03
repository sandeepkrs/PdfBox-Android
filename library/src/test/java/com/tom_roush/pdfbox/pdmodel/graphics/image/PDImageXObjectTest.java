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

import android.graphics.Bitmap;

import java.io.IOException;

import junit.framework.TestCase;
import org.mockito.Mockito;

import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.cos.COSStream;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;

/**
 * Unit tests for PDImageXObject mask handling and resource inheritance.
 */
public class PDImageXObjectTest extends TestCase
{
    public void testApplyMaskNullSafety() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDImageXObject imageXObject = new PDImageXObject(doc);

        Bitmap mockImage = Mockito.mock(Bitmap.class);
        Bitmap mockMask = Mockito.mock(Bitmap.class);

        // When image is null, applyMask must return null without throwing NullPointerException
        assertNull(imageXObject.applyMask(null, mockMask, false, false, null));
        assertNull(imageXObject.applyMask(null, null, false, false, null));

        // When mask is null, applyMask must return the original image
        assertSame(mockImage, imageXObject.applyMask(mockImage, null, false, false, null));

        doc.close();
    }

    public void testMaskAndSoftMaskInheritResources() throws IOException
    {
        PDDocument doc = new PDDocument();
        PDResources resources = new PDResources();
        COSStream mainStream = new COSStream();

        COSStream maskStream = new COSStream();
        mainStream.setItem(COSName.MASK, maskStream);

        COSStream smaskStream = new COSStream();
        mainStream.setItem(COSName.SMASK, smaskStream);

        PDStream pdStream = new PDStream(mainStream);
        PDImageXObject image = new PDImageXObject(pdStream, resources);
        assertSame(resources, image.getResources());

        PDImageXObject mask = image.getMask();
        assertNotNull(mask);
        assertSame("Mask must inherit parent image resources", resources, mask.getResources());

        PDImageXObject smask = image.getSoftMask();
        assertNotNull(smask);
        assertSame("Soft mask must inherit parent image resources", resources, smask.getResources());

        doc.close();
    }
}
