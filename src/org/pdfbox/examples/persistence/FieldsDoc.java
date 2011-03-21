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

import java.util.Iterator;

import org.pdfbox.pdfparser.PDFParser;

import org.pdfbox.pdfwriter.COSWriter;

import org.pdfbox.cos.COSDocument;
import org.pdfbox.cos.COSString;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSObject;
import org.pdfbox.cos.COSName;

import org.pdfbox.exceptions.COSVisitorException;

/**
 * This example fills a field in a pdf document and writes it to new destination.
 *
 * @author Michael Traut
 * @version $Revision$
 */
public class FieldsDoc
{
    /**
     * Constructor.
     */
    public FieldsDoc()
    {
        super();
    }
    /**
     * fill a field in the pdf.
     *
     * @param in The template file
     * @param out The file to write the PDF to.
     * @param name The name of the PDF field (FDF field)
     * @param value The value to be used for the field
     *
     * @throws IOException If there is an error writing the data.
     * @throws COSVisitorException If there is an error generating the PDF document.
     */
    public void doIt(String in, String out, String name, String value) throws IOException, COSVisitorException
    {
        java.io.InputStream is = null;
        COSDocument doc = null;
        OutputStream os = null;
        COSWriter writer = null;
        try
        {
            is = new java.io.FileInputStream(in);
            PDFParser parser = new PDFParser(is);
            parser.parse();

            doc = parser.getDocument();

            setField(doc, new COSString(name), new COSString(value));

            os = new FileOutputStream(out);
            writer = new COSWriter(os);

            writer.write(doc);

        }
        finally
        {
            is.close();
            doc.close();
            os.close();
            writer.close();
        }
    }
    /**
     * This will fill a field in a PDF document.
     * <br />
     * see usage() for commandline
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        FieldsDoc app = new FieldsDoc();
        try
        {
            if( args.length != 4 )
            {
                app.usage();
            }
            else
            {
                app.doIt( args[0], args[1], args[2], args[3]);
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
     * @param name the name of the PDF Annotation field
     * @param value The desired value to be used for the field
     *
     */
    public void setField(COSDocument doc, COSString name, COSString value)
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
        System.err.println( "usage: " + this.getClass().getName() + " <input-file> <output-file> <name> <value>" );
    }
}