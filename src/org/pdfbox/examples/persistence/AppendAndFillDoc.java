/**
 * Copyright (c) 2003, www.pdfbox.org
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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Iterator;

import org.pdfbox.pdfparser.PDFParser;

import org.pdfbox.pdfwriter.COSWriter;

import org.pdfbox.cos.COSDocument;
import org.pdfbox.cos.COSString;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSNumber;
import org.pdfbox.cos.COSInteger;

import org.pdfbox.exceptions.COSVisitorException;

/**
 * This concatenates two documents with fields and fills the fields in the two templates using different
 * values.
 *
 * @author Michael Traut
 * @version $Revision$
 */
public class AppendAndFillDoc
{
    /**
     * Constructor.
     */
    public AppendAndFillDoc()
    {
        super();
    }

    /**
     * Append all pages from source to destination.
     *
     * todo: this method will go to the pdfmodel package one day
     *
     * @param destination the document to receive the pages
     * @param source the document originating the new pages
     *
     */
    public void appendDocument(COSDocument destination, COSDocument source)
    {
        COSDictionary pages2 = getPages(source);
        COSArray kids = (COSArray)pages2.getItem(COSName.getPDFName("Kids"));
        for (Iterator i = kids.iterator(); i.hasNext();)
        {
            COSDictionary page = (COSDictionary)((COSObject)i.next()).getObject();
            appendPage(destination, page);
        }
    }
    /**
     * append a page dict to destination.
     *
     * todo: this method will go to the pdfmodel package one day
     *
     * @param destination the document to receive the page
     * @param page the page to append to the document
     *
     */
    public void appendPage(COSDocument destination, COSDictionary page)
    {
        // get the parent object (pages dictionary)
        COSDictionary pages = getPages(destination);
        // and set in the page object
        page.setItem(COSName.PARENT, pages);
        // add new page to kids entry in the pages dictionary
        COSArray kids = (COSArray) pages.getItem(COSName.getPDFName("Kids"));
        kids.add(page);
        // and increase count
        COSNumber count = (COSNumber) pages.getItem(COSName.COUNT);
        pages.setItem(COSName.COUNT, new COSInteger(count.intValue() + 1));
    }
    /**
     * concat two pdf documents and fill fields in both templates
     * this is a bit tricky as one has to rename the fields if we use the same template two times.
     * here we don't user a clever algorithm to create dynamic fieldnames - this is left to the user..
     *
     * @param in1 The first template file
     * @param in2 The second template file
     * @param out The created fiel with all pages from document one and document two
     * @param name1 The name of the PDF field (FDF field) in the first template
     * @param value1 The value to be used for the field in the first template
     * @param name2 The name of the PDF field (FDF field) in the second template
     * @param value2 The value to be used for the field in the second template
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error generating the PDF document.
     */
    public void doIt( String in1,
                      String in2,
                      String out,
                      String name1,
                      String value1,
                      String name2,
                      String value2) throws IOException, COSVisitorException
    {
        COSDocument doc1 = null;
        COSDocument doc2 = null;
        InputStream is1 = null;
        InputStream is2 = null;
        PDFParser parser1 = null;
        PDFParser parser2 = null;

        OutputStream os = null;
        COSWriter writer = null;
        try
        {
            is1 = new FileInputStream(in1);
            parser1 = new PDFParser(is1);
            parser1.parse();
            doc1 = parser1.getDocument();

            is2 = new FileInputStream(in2);
            parser2 = new PDFParser(is2);
            parser2.parse();
            doc2 = parser2.getDocument();

            setField(doc1, "doc1", new COSString(name1), new COSString(value1));
            setField(doc2, "doc2", new COSString(name2), new COSString(value2));

            appendDocument(doc1, doc2);

            os = new FileOutputStream(out);
            writer = new COSWriter(os);
            writer.write(doc1);
        }
        finally
        {
            is1.close();
            doc1.close();

            is2.close();
            doc2.close();

            os.close();
            writer.close();
        }
    }

    /**
     * Lookup the pages dictionary in a document.
     *
     * todo: this method will go to the pdfmodel package one day
     *
     * @param doc the document where the pages dict is searched
     *
     * @return The Pages dictionary.
     */
    public COSDictionary getPages(COSDocument doc)
    {
        // todo should access via catalog instead!
        for (Iterator i = doc.getObjects().iterator(); i.hasNext();)
        {
            COSObject obj = (COSObject) i.next();
            COSBase base = obj.getObject();
            if (base instanceof COSDictionary)
            {
                COSDictionary dict = (COSDictionary) base;
                COSBase type = dict.getItem(COSName.TYPE);
                if (type != null && type.equals(COSName.getPDFName("Pages")))
                {
                    return dict;
                }
            }
        }
        return null;
    }
    /**
     * This will concat two pdf documents and fill fields in both.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        AppendAndFillDoc app = new AppendAndFillDoc();
        try
        {
            if( args.length != 7 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * lookup and fill the field.
     *
     * todo: this method will go to the pdfmodel package one day
     *
     * @param doc the document where the field resides
     * @param prefix a prefix to use to make the field name unique in the new document
     * @param name the name of the PDF Annotation field
     * @param value The desired value to be used for the field
     *
     */
    public void setField(COSDocument doc, String prefix, COSString name, COSString value)
    {
        for (Iterator i = doc.getObjects().iterator(); i.hasNext();)
        {
            COSObject obj = (COSObject) i.next();
            COSBase base = obj.getObject();
            if (base instanceof COSDictionary)
            {
                COSDictionary dict = (COSDictionary) base;
                COSBase type = dict.getItem(COSName.TYPE);
                if (type != null && type.equals(COSName.getPDFName("Annot")))
                {
                    COSBase subtype = dict.getItem(COSName.getPDFName("Subtype"));
                    if (subtype != null && subtype.equals(COSName.getPDFName("Widget")))
                    {
                        // we found the field
                        COSBase fname = dict.getItem(COSName.getPDFName("T"));
                        if (fname != null && fname.equals(name))
                        {
                            dict.setItem(COSName.getPDFName("V"), value);
                            dict.setItem(COSName.getPDFName("T"), new COSString(prefix  + name.getString()));
                        }
                    }
                }
            }
        }
    }
    /**
     * This will print out a message telling how to use this example.
     */
    private void usage()
    {
        System.err.println( "usage: " + this.getClass().getName() +
        " <input-file1> <input-file2> <output-file> <name1> <value1> <name2> <value2>" );
    }
}