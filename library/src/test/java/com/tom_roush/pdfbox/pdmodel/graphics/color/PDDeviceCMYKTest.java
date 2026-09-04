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

import junit.framework.TestCase;

/**
 * Test for power user creation of a custom default CMYK color space.
 *
 * @author John Hewson
 */
public class PDDeviceCMYKTest extends TestCase
{
   public void testCMYK() throws IOException
   {
      PDDeviceCMYK cmyk = PDDeviceCMYK.INSTANCE;

      float[] white = cmyk.toRGB(new float[] { 0, 0, 0, 0 });
      assertEquals(1.0f, white[0], 0.001f);
      assertEquals(1.0f, white[1], 0.001f);
      assertEquals(1.0f, white[2], 0.001f);

      float[] black = cmyk.toRGB(new float[] { 0, 0, 0, 1 });
      assertEquals(0.0f, black[0], 0.001f);
      assertEquals(0.0f, black[1], 0.001f);
      assertEquals(0.0f, black[2], 0.001f);

      float[] cyan = cmyk.toRGB(new float[] { 1, 0, 0, 0 });
      assertEquals(0.0f, cyan[0], 0.001f);
      assertEquals(1.0f, cyan[1], 0.001f);
      assertEquals(1.0f, cyan[2], 0.001f);

      float[] magenta = cmyk.toRGB(new float[] { 0, 1, 0, 0 });
      assertEquals(1.0f, magenta[0], 0.001f);
      assertEquals(0.0f, magenta[1], 0.001f);
      assertEquals(1.0f, magenta[2], 0.001f);

      float[] yellow = cmyk.toRGB(new float[] { 0, 0, 1, 0 });
      assertEquals(1.0f, yellow[0], 0.001f);
      assertEquals(1.0f, yellow[1], 0.001f);
      assertEquals(0.0f, yellow[2], 0.001f);

      PDDeviceCMYK.INSTANCE = new CustomDeviceCMYK();
   }

   private static class CustomDeviceCMYK extends PDDeviceCMYK
   {
      protected CustomDeviceCMYK() throws IOException
      {
      }
   }
}
