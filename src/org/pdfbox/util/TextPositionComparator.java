/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.util;

import java.util.Comparator;

import org.pdfbox.pdmodel.PDPage;

/**
 * This class is a comparator for TextPosition operators.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision$
 */
public class TextPositionComparator implements Comparator
{
    private PDPage thePage = null;
    
    /**
     * Constuctor, comparison of TextPosition depends on the rotation
     * of the page.
     * @param page The page that the text position is on.
     */
    public TextPositionComparator( PDPage page )
    {
        thePage = page;
    }
    
    /**
     * @see Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2)
    {
        int retval = 0;
        TextPosition pos1 = (TextPosition)o1;
        TextPosition pos2 = (TextPosition)o2;
        int rotation = thePage.findRotation();
        float x1 = 0;
        float x2 = 0;
        float y1 = 0;
        float y2 = 0;
        if( rotation == 0 )
        {
            x1 = pos1.getX();
            x2 = pos2.getX();
            y1 = pos1.getY();
            y2 = pos2.getY();
        }
        else if( rotation == 90 )
        {
            x1 = pos1.getY();
            x2 = pos2.getX();
            y1 = pos1.getX();
            y2 = pos2.getY();
        }
        else if( rotation == 180 )
        {
            x1 = -pos1.getX();
            x2 = -pos2.getX();
            y1 = -pos1.getY();
            y2 = -pos2.getY();
        }
        else if( rotation == 270 )
        {
            x1 = -pos1.getY();
            x2 = -pos2.getY();
            y1 = -pos1.getX();
            y2 = -pos2.getX();
        }
        
        if( y1 < y2 )
        {
            retval = -1;
        }
        else if( y1 > y2 )
        {
            return 1;
        }
        else
        {
            if( x1 < x2 )
            {
                retval = -1;
            }
            else if( x1 > x2 )
            {
                retval = 1;
            }
            else
            {
                retval = 0;
            }
        }
        
        return retval;
    }
    
}