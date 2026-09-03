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

package com.tom_roush.pdfbox.cos;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tom_roush.pdfbox.filter.DecodeOptions;
import com.tom_roush.pdfbox.filter.DecodeResult;
import com.tom_roush.pdfbox.filter.Filter;
import com.tom_roush.pdfbox.io.RandomAccess;
import com.tom_roush.pdfbox.io.RandomAccessInputStream;
import com.tom_roush.pdfbox.io.RandomAccessOutputStream;
import com.tom_roush.pdfbox.io.ScratchFile;
/**
 * An InputStream which reads from an encoded COS stream.
 *
 * @author John Hewson
 */
public final class COSInputStream extends FilterInputStream
{

    /**
     * Creates a new COSInputStream from an encoded input stream.
     *
     * @param filters Filters to be applied.
     * @param parameters Filter parameters.
     * @param in Encoded input stream.
     * @param scratchFile Scratch file to use, or null.
     * @param options decode options for the encoded stream
     * @return Decoded stream.
     * @throws IOException If the stream could not be read.
     */
    static COSInputStream create(List<Filter> filters, COSDictionary parameters, InputStream in,
                                 ScratchFile scratchFile, DecodeOptions options) throws IOException
    {
        InputStream input = in;
        if (filters.isEmpty())
        {
            return new COSInputStream(in, null);
        }

        List<DecodeResult> results = new ArrayList<DecodeResult>(filters.size());
        if (filters.size() > 1)
        {
            Set<Filter> filterSet = new HashSet<Filter>(filters);
            if (filterSet.size() != filters.size())
            {
                List<Filter> reducedFilterList = new ArrayList<Filter>();
                for (Filter filter : filters)
                {
                    if (!reducedFilterList.contains(filter))
                    {
                        reducedFilterList.add(filter);
                    }
                }
                // replace origin list with the reduced one
                filters = reducedFilterList;
                Log.w("PdfBox-Android", "Removed duplicated filter entries");
            }
        }
        // apply filters
        for (int i = 0; i < filters.size(); i++)
        {
            if (scratchFile != null)
            {
                // scratch file
                final RandomAccess buffer = scratchFile.createBuffer();
                DecodeResult result = filters.get(i).decode(input, new RandomAccessOutputStream(buffer), parameters, i, options);
                results.add(result);
                input = new RandomAccessInputStream(buffer)
                {
                    @Override
                    public void close() throws IOException
                    {
                        buffer.close();
                    }
                };
            }
            else
            {
                // in-memory
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DecodeResult result = filters.get(i).decode(input, output, parameters, i, options);
                results.add(result);
                input = new ByteArrayInputStream(output.toByteArray());
            }
        }
        if (results.isEmpty())
        {
            return new COSInputStream(in, null);
        }
        return new COSInputStream(input, results.get(results.size() - 1));
    }

    private final DecodeResult decodeResult;

    /**
     * Constructor.
     *
     * @param input decoded stream
     * @param decodeResult result of decoding
     */
    private COSInputStream(InputStream input, DecodeResult decodeResult)
    {
        super(input);
        this.decodeResult = decodeResult;
    }

    /**
     * Returns the result of the last filter, for use by repair mechanisms.
     *
     * @return the result of the last filter
     */
    public DecodeResult getDecodeResult()
    {
        if (decodeResult == null)
        {
            return DecodeResult.DEFAULT;
        }
        return decodeResult;
    }
}
