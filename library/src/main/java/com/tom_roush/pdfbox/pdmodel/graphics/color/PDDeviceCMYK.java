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

import com.tom_roush.pdfbox.cos.COSName;

/**
 * Allows colors to be specified according to the subtractive CMYK (cyan, magenta, yellow, black)
 * model typical of printers and other paper-based output devices.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public class PDDeviceCMYK extends PDDeviceColorSpace
{
   /**  The single instance of this class. */
   public static PDDeviceCMYK INSTANCE;
   static
   {
      INSTANCE = new PDDeviceCMYK();
   }

   private final PDColor initialColor = new PDColor(new float[] { 0, 0, 0, 1 }, this);

   protected PDDeviceCMYK()
   {
   }

   @Override
   public String getName()
   {
      return COSName.DEVICECMYK.getName();
   }

   @Override
   public int getNumberOfComponents()
   {
      return 4;
   }

   @Override
   public float[] getDefaultDecode(int bitsPerComponent)
   {
      return new float[] { 0, 1, 0, 1, 0, 1, 0, 1 };
   }

   @Override
   public PDColor getInitialColor()
   {
      return initialColor;
   }

   @Override
   public float[] toRGB(float[] value) throws IOException
   {
      float c = value[0];
      float m = value[1];
      float y = value[2];
      float k = value[3];

      float r = (1 - c) * (1 - k);
      float g = (1 - m) * (1 - k);
      float b = (1 - y) * (1 - k);
      return new float[] { r, g, b };
   }

   @Override
   public Bitmap toRGBImage(Bitmap raster) throws IOException
   {
      int width = raster.getWidth();
      int height = raster.getHeight();
      final int maxChunkPixels = 262144;
      final int chunkRows = Math.min(height, Math.max(1, maxChunkPixels / width));
      final int bufferSize = chunkRows * width;
      int[] imgPixels = new int[bufferSize];
      int[] outPixels = new int[bufferSize];
      Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

      for (int y = 0; y < height; y += chunkRows)
      {
         int currentChunkRows = Math.min(chunkRows, height - y);
         int currentPixelCount = currentChunkRows * width;
         raster.getPixels(imgPixels, 0, width, 0, y, width, currentChunkRows);
         for (int i = 0; i < currentPixelCount; i++)
         {
            int pixel = imgPixels[i];
            float c = android.graphics.Color.red(pixel) / 255.0f;
            float m = android.graphics.Color.green(pixel) / 255.0f;
            float yVal = android.graphics.Color.blue(pixel) / 255.0f;
            float k = android.graphics.Color.alpha(pixel) / 255.0f;
            int r = Math.round(255 * (1 - c) * (1 - k));
            int g = Math.round(255 * (1 - m) * (1 - k));
            int b = Math.round(255 * (1 - yVal) * (1 - k));
            outPixels[i] = android.graphics.Color.argb(255, r, g, b);
         }
         image.setPixels(outPixels, 0, width, 0, y, width, currentChunkRows);
      }
      return image;
   }
}
