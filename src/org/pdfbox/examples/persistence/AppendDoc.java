/**
 * Copyright (c) 2003-2004, www.pdfbox.org
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
package org.pdfbox.examples.persistence;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSStream;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.pdmodel.PDPage;

import org.pdfbox.pdmodel.common.PDStream;

import org.pdfbox.exceptions.COSVisitorException;

/**
 * This example concatenates two documents and writes the result.
 *
 * @author Michael Traut
 * @version $Revision$
 */
public class AppendDoc
{
    /**
     * Constructor.
     */
    public AppendDoc()
    {
        super();
    }
    /**
     * append all pages from source to destination.
     *
     * todo: this method will go to the pdfmodel package one day
     *
     * @param destination the document to receive the pages
     * @param source the document originating the new pages
     *
     * @throws IOException If there is an error accessing data from either document.
     */
    public void appendDocument(PDDocument destination, PDDocument source) throws IOException
    {
        if( destination.isEncrypted() )
        {
            throw new IOException( "Error: destination PDF is encrypted, can't append encrypted PDF documents." );
        }
        if( source.isEncrypted() )
        {
            throw new IOException( "Error: destination PDF is encrypted, can't append encrypted PDF documents." );
        }
        PDDocumentInformation destInfo = destination.getDocumentInformation();
        PDDocumentInformation srcInfo = source.getDocumentInformation();
        destInfo.getDictionary().mergeInto( srcInfo.getDictionary() );
        
        COSDictionary destTrailer = destination.getDocument().getTrailer();
        COSDictionary destRoot = (COSDictionary)destTrailer.getDictionaryObject( COSName.ROOT );

        COSDictionary srcTrailer = source.getDocument().getTrailer();
        COSDictionary srcRoot = (COSDictionary)srcTrailer.getDictionaryObject( COSName.ROOT );

        COSName openAction = COSName.getPDFName( "OpenAction" );
        if( destRoot.getDictionaryObject( openAction ) == null )
        {
            COSBase open = srcRoot.getDictionaryObject( openAction );
            if( open != null )
            {
                destRoot.setItem( openAction, copyStreamsIntoDocument( destination, open ) );
            }
        }

        COSName acroForm = COSName.getPDFName( "AcroForm" );
        COSDictionary destAcroForm = (COSDictionary)destRoot.getDictionaryObject( acroForm );
        COSDictionary srcAcroForm = (COSDictionary)srcRoot.getDictionaryObject( acroForm );
        if( srcAcroForm != null )
        {
            if( destAcroForm == null )
            {
                destRoot.setItem( acroForm, copyStreamsIntoDocument( destination, srcAcroForm ) );
            }
            else
            {
                //****************need to do proper merge***************
                //destAcroForm.addAll( (COSArray)copyStreamsIntoDocument( destination, srcAcroForm ) );
            }            
        }

        COSName threads = COSName.getPDFName( "Threads" );
        COSArray destThreads = (COSArray)destRoot.getDictionaryObject( threads );
        COSArray srcThreads = (COSArray)srcRoot.getDictionaryObject( threads );
        if( srcThreads != null )
        {
            if( destThreads == null )
            {
                destRoot.setItem( threads, copyStreamsIntoDocument( destination, srcThreads ) );
            }
            else
            {
                destThreads.addAll( (COSArray)copyStreamsIntoDocument( destination, srcThreads ) );
            }
        }

        COSName names = COSName.getPDFName( "Names" );
        COSDictionary destNames = (COSDictionary)destRoot.getDictionaryObject( names );
        COSDictionary srcNames = (COSDictionary)srcRoot.getDictionaryObject( names );
        if( srcNames != null )
        {
            if( destNames == null )
            {
                destRoot.setItem( names, copyStreamsIntoDocument( destination, srcNames ) );
            }
            else
            {
                //warning, potential for collision here!!
                destNames.mergeInto( (COSDictionary)copyStreamsIntoDocument( destination, srcNames ) );
            }
        }
        
        COSName outlines = COSName.getPDFName( "Outlines" );
        COSDictionary destOutlines = (COSDictionary)destRoot.getDictionaryObject( outlines );
        COSDictionary srcOutlines = (COSDictionary)srcRoot.getDictionaryObject( outlines );
        if( srcOutlines != null && destOutlines == null )
        {
            destRoot.setItem( outlines, copyStreamsIntoDocument( destination, srcOutlines ) );
        }
        
        COSName pagemode = COSName.getPDFName( "PageMode" );
        COSBase srcPageMode = srcRoot.getDictionaryObject( pagemode );
        if( srcOutlines != null && destOutlines == null )
        {
            destRoot.setItem( pagemode, copyStreamsIntoDocument( destination, srcPageMode ) );
        }

        COSName pageLabels = COSName.getPDFName( "PageLabels" );
        COSDictionary destLabels = (COSDictionary)destRoot.getDictionaryObject( pageLabels );
        COSDictionary srcLabels = (COSDictionary)srcRoot.getDictionaryObject( pageLabels );
        if( srcLabels != null )
        {
            int destPageCount = destination.getNumberOfPages();
            COSArray destNums = null;
            if( destLabels == null )
            {
                destLabels = new COSDictionary();
                destNums = new COSArray();
                destLabels.setItem( COSName.getPDFName( "Nums" ), destNums );
                destRoot.setItem( pageLabels, destLabels );
            }
            else
            {
                destNums = (COSArray)destLabels.getDictionaryObject( COSName.getPDFName( "Nums" ) );
            }
            COSArray srcNums = (COSArray)srcLabels.getDictionaryObject( COSName.getPDFName( "Nums" ) );
            for( int i=0; i<srcNums.size(); i+=2 )
            {
                COSNumber labelIndex = (COSNumber)srcNums.getObject( i );
                long labelIndexValue = labelIndex.intValue();
                destNums.add( new COSInteger( labelIndexValue + destPageCount ) );
                destNums.add( copyStreamsIntoDocument( destination, srcNums.getObject( i+1 ) ) );
            }
        }
        
        COSName metadata = COSName.getPDFName( "Metadata" );
        COSStream destMetadata = (COSStream)destRoot.getDictionaryObject( metadata );
        COSStream srcMetadata = (COSStream)srcRoot.getDictionaryObject( metadata );
        if( destMetadata == null && srcMetadata != null )
        {
            PDStream newStream = new PDStream( destination, srcMetadata.getUnfilteredStream(), false );
            newStream.getStream().mergeInto( srcMetadata );
            newStream.addCompression();
            destRoot.setItem( metadata, newStream );
        }

        //finally append the pages
        List pages = source.getDocumentCatalog().getAllPages();
        Iterator pageIter = pages.iterator();
        while( pageIter.hasNext() )
        {
            PDPage page = (PDPage)pageIter.next();
            PDPage newPage = 
                new PDPage( (COSDictionary)copyStreamsIntoDocument( destination, page.getCOSDictionary() ) );
            destination.addPage( newPage );
        }
    }
    Map clonedVersion = new HashMap();
    
    private COSBase copyStreamsIntoDocument( PDDocument destination, COSBase base ) throws IOException
    {
        
        COSBase retval = (COSBase)clonedVersion.get( base );
        if( retval != null )
        {
            return retval;
        }
        if( base instanceof COSObject )
        {
            COSObject object = (COSObject)base; 
            retval = new COSObject(copyStreamsIntoDocument( destination, object.getObject() ) );
        }
        else if( base instanceof COSArray )
        {
            retval = new COSArray();
            COSArray array = (COSArray)base;
            for( int i=0; i<array.size(); i++ )
            {
                ((COSArray)retval).add( copyStreamsIntoDocument( destination, array.get( i ) ) );
            }
        }
        else if( base instanceof COSDictionary )
        {
            COSDictionary dic = (COSDictionary)base;
            List keys = dic.keyList();
            if( base instanceof COSStream )
            {
                COSStream originalStream = (COSStream)base;
                PDStream stream = new PDStream( destination, originalStream.getFilteredStream(), true );
                clonedVersion.put( base, stream.getStream() );
                for( int i=0; i<keys.size(); i++ )
                {
                    COSName key = (COSName)keys.get( i );
                    stream.getStream().setItem( key, copyStreamsIntoDocument(destination,dic.getItem(key)));
                }
                retval = stream.getStream();
            }
            else
            {     
                retval = new COSDictionary();
                clonedVersion.put( base, retval );
                for( int i=0; i<keys.size(); i++ )
                {
                    COSName key = (COSName)keys.get( i );
                    ((COSDictionary)retval).setItem( key, copyStreamsIntoDocument(destination,dic.getItem(key)));
                }
            }
        }
        else
        {
            retval = base;
        }
        clonedVersion.put( base, retval );
        return retval;
    }

    /**
     * concat two pdf documents.
     *
     * @param in1 The first template file
     * @param in2 The second template file
     * @param out The created fiel with all pages from document one and document two
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error generating the PDF document.
     */
    public void doIt(String in1, String in2, String out) throws IOException, COSVisitorException
    {
        PDDocument doc1 = null;
        PDDocument doc2 = null;
        try
        {
            doc1 = PDDocument.load( in1 );
            doc2 = PDDocument.load( in2 );

            appendDocument(doc1, doc2);
            doc1.save( out );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            close( doc1 );
            close( doc2 );
        }
    }

    private void close( PDDocument doc ) throws IOException
    {
        if( doc != null )
        {
            doc.close();
        }
    }

    /**
     * This will concat two pdf documents.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        AppendDoc app = new AppendDoc();
        try
        {
            if( args.length != 3 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1], args[2]);
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() + " <input-file1> <input-file2> <output-file>" );
    }
}