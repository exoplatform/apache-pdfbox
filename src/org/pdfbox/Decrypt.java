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
package org.pdfbox;

import java.io.IOException;

import org.pdfbox.pdmodel.PDDocument;

/**
 * This will read a document from the filesystem, decrypt it and and then write
 * the results to the filesystem. <br/><br/>
 *
 * usage: java org.pdfbox.Decrypt &lt;password&gt; &lt;inputfile&gt; &lt;outputfile&gt;
 *
 * @author  Ben Litchfield (ben@csh.rit.edu)
 * @version $Revision$
 */
public class Decrypt
{

    /**
     * This is the entry point for the application.
     *
     * @param args The command-line arguments.
     *
     * @throws Exception If there is an error decrypting the document.
     */
    public static void main( String[] args ) throws Exception
    {
        Decrypt decrypt = new Decrypt();
        decrypt.decrypt( args );
    }

    private void decrypt( String[] args ) throws Exception
    {
        if( args.length < 2 || args.length > 3 )
        {
            usage();
        }
        else
        {
            String password = args[0];
            String infile = args[1];
            String outfile = args[1];
            if( args.length == 3 )
            {
                outfile = args[2];
            }
            

            PDDocument document = null;

            try
            {
                document = PDDocument.load( infile );

                if( document.isEncrypted() )
                {
                    if( document.isOwnerPassword( password ) )
                    {
                        document.decrypt( password );
                        document.save( outfile );
                    }
                    else
                    {
                        throw new IOException(
                            "Error: You are only allowed to decrypt a document with the owner password." );
                    }
                }
                else
                {
                    System.err.println( "Error: Document is not encrypted." );
                }
            }
            finally
            {
                if( document != null )
                {
                    document.close();
                }
            }
        }
    }

    /**
     * This will print a usage message.
     */
    private static void usage()
    {
        System.err.println( "usage: java org.pdfbox.Decrypt " +
                            "<password> <inputfile> [outputfile]" );
    }

}