/**
 * Copyright (c) 2003-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.font;

import java.awt.Graphics;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.pdmodel.common.PDRectangle;

import org.apache.log4j.Logger;

/**
 * This is implementation for the CIDFontType0/CIDFontType2 Fonts.
 *
 * @author Ben Litchfield (ben@csh.rit.edu)
 * @version $Revision$
 */
public abstract class PDCIDFont extends PDFont
{
    private static Logger log = Logger.getLogger(PDCIDFont.class);


    private Map widthCache = new HashMap();

    /**
     * Constructor.
     */
    public PDCIDFont()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDCIDFont( COSDictionary fontDictionary )
    {
        super( fontDictionary );
    }

    /**
     * @see PDFont#drawString( String, Graphics, float, float, float, float, float )
     */
    public void drawString( String string, Graphics g, float fontSize, 
        float xScale, float yScale, float x, float y )
    {
        throw new RuntimeException( "Not yet implemented" );
    }
    
    /**
     * This will get the fonts bouding box.
     *
     * @return The fonts bouding box.
     * 
     * @throws IOException If there is an error getting the font bounding box.
     */
    public PDRectangle getFontBoundingBox() throws IOException
    {
        throw new RuntimeException( "Not yet implemented" );
    }

    /**
     * This will get the default width.  The default value for the default width is 1000.
     *
     * @return The default width for the glyphs in this font.
     */
    public long getDefaultWidth()
    {
        long dw = 1000;
        COSNumber number = (COSNumber)font.getDictionaryObject( COSName.getPDFName( "DW" ) );
        if( number != null )
        {
            dw = number.intValue();
        }
        return dw;
    }

    /**
     * This will set the default width for the glyphs of this font.
     *
     * @param dw The default width.
     */
    public void setDefaultWidth( long dw )
    {
        font.setItem( COSName.getPDFName( "DW" ), new COSInteger( dw ) );
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getFontWidth( byte[] c, int offset, int length ) throws IOException
    {

        float retval = 0.0f;
        int code = getCodeFromArray( c, offset, length );

        Float widthFloat = (Float)widthCache.get( new Integer( code ) );
        if( widthFloat == null )
        {
            COSArray widths = (COSArray)font.getDictionaryObject( COSName.getPDFName( "W" ) );

            if( widths != null )
            {
                boolean foundWidth = false;
                for( int i=0; !foundWidth && i<widths.size(); i++ )
                {
                    COSNumber firstCode = (COSNumber)widths.getObject( i++ );
                    COSBase next = widths.getObject( i );
                    if( next instanceof COSArray )
                    {
                        COSArray array = (COSArray)next;
                        if( code >= firstCode.intValue() &&
                            code < firstCode.intValue() + array.size() )
                        {
                            COSNumber rangeWidth =
                                (COSNumber)array.get( code - firstCode.intValue() );
                            retval = rangeWidth.floatValue();
                            foundWidth = true;
                        }
                    }
                    else
                    {
                        COSNumber secondCode = (COSNumber)next;
                        i++;
                        COSNumber rangeWidth = (COSNumber)widths.getObject( i );
                        if( code >= firstCode.intValue() &&
                            code <= secondCode.intValue() )
                        {
                            retval = rangeWidth.floatValue();
                            foundWidth = true;
                        }
                    }
                }
                widthCache.put( new Integer( code ), new Float( retval ) );
            }
        }
        else
        {
            retval = widthFloat.floatValue();
        }

        if(log.isDebugEnabled() )
        {
            log.debug( "PDCIDFontType0Font.getFontWidth( code=" + code +" ) retval=" +retval );
        }
        return retval;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public float getAverageFontWidth() throws IOException
    {
        float totalWidths = 0.0f;
        float characterCount = 0.0f;
        float defaultWidth = getDefaultWidth();
        COSArray widths = (COSArray)font.getDictionaryObject( COSName.getPDFName( "W" ) );

        if( widths != null )
        {
            for( int i=0; i<widths.size(); i++ )
            {
                COSNumber firstCode = (COSNumber)widths.getObject( i++ );
                COSBase next = widths.getObject( i );
                float nextWidth=0.0f;
                if( next instanceof COSArray )
                {
                    COSArray array = (COSArray)next;
                    for( int j=0; j<array.size(); j++ )
                    {
                        COSNumber width = (COSNumber)array.get( j );
                        totalWidths+=width.floatValue();
                        characterCount += 1;
                    }
                }
                else
                {
                    COSNumber secondCode = (COSNumber)next;
                    i++;
                    COSNumber rangeWidth = (COSNumber)widths.getObject( i );
                    if( rangeWidth.floatValue() > 0 )
                    {
                        totalWidths += rangeWidth.floatValue();
                        characterCount += 1;
                    }
                }
            }
        }
        float average = totalWidths / characterCount;
        if( average <= 0 )
        {
            average = defaultWidth;
        }
        return average;
    }
}