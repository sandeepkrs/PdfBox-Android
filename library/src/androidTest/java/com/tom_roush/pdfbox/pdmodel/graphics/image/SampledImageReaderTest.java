package com.tom_roush.pdfbox.pdmodel.graphics.image;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;

import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSBase;
import com.tom_roush.pdfbox.filter.DecodeOptions;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColorSpace;

import org.junit.Assert;
import org.junit.Test;

public class SampledImageReaderTest
{
    private static PDImage createDummyPDImage(final int width, final int height)
    {
        return new PDImage()
        {
            @Override
            public Bitmap getImage() throws IOException { return null; }

            @Override
            public Bitmap getImage(Rect region, int subsampling) throws IOException { return null; }

            @Override
            public Bitmap getStencilImage(Paint paint) throws IOException { return null; }

            @Override
            public InputStream createInputStream() throws IOException { return null; }

            @Override
            public InputStream createInputStream(DecodeOptions options) throws IOException { return null; }

            @Override
            public InputStream createInputStream(List<String> stopFilters) throws IOException { return null; }

            @Override
            public boolean isEmpty() { return false; }

            @Override
            public boolean isStencil() { return false; }

            @Override
            public void setStencil(boolean isStencil) {}

            @Override
            public int getBitsPerComponent() { return 8; }

            @Override
            public void setBitsPerComponent(int bpc) {}

            @Override
            public PDColorSpace getColorSpace() throws IOException { return null; }

            @Override
            public void setColorSpace(PDColorSpace cs) {}

            @Override
            public int getHeight() { return height; }

            @Override
            public void setHeight(int h) {}

            @Override
            public int getWidth() { return width; }

            @Override
            public void setWidth(int w) {}

            @Override
            public boolean getInterpolate() { return false; }

            @Override
            public void setInterpolate(boolean value) {}

            @Override
            public COSArray getDecode() { return null; }

            @Override
            public void setDecode(COSArray decode) {}

            @Override
            public String getSuffix() { return "png"; }

            @Override
            public COSBase getCOSObject() { return null; }
        };
    }

    @Test
    public void testClipRegionNull()
    {
        PDImage pdImage = createDummyPDImage(400, 300);

        Rect clipped = SampledImageReader.clipRegion(pdImage, null);
        Assert.assertNotNull(clipped);
        Assert.assertEquals(0, clipped.left);
        Assert.assertEquals(0, clipped.top);
        Assert.assertEquals(400, clipped.right);
        Assert.assertEquals(300, clipped.bottom);
    }

    @Test
    public void testClipRegionNonZeroOffset()
    {
        PDImage pdImage = createDummyPDImage(500, 500);

        // Rect(left, top, right, bottom)
        Rect sourceRegion = new Rect(50, 60, 250, 360);
        // sourceRegion width is 200, height is 300

        Rect clipped = SampledImageReader.clipRegion(pdImage, sourceRegion);
        Assert.assertNotNull(clipped);
        Assert.assertEquals(50, clipped.left);
        Assert.assertEquals(60, clipped.top);
        // right must be left + width = 50 + 200 = 250
        Assert.assertEquals(250, clipped.right);
        // bottom must be top + height = 60 + 300 = 360
        Assert.assertEquals(360, clipped.bottom);
    }
}
