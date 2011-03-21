/**
 * Copyright (c) 2004, www.pdfbox.org
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

import org.pdfbox.pdmodel.PDDocument;

import org.pdfbox.pdmodel.encryption.PDStandardEncryption;
import org.pdfbox.pdmodel.encryption.PDEncryptionDictionary;

/**
 * This will read a document from the filesystem, encrypt it and and then write
 * the results to the filesystem. <br/><br/>
 *
 * @author  Ben Litchfield (ben@csh.rit.edu)
 * @version $Revision$
 */
public class Encrypt
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
        Encrypt encrypt = new Encrypt();
        encrypt.encrypt( args );
    }

    private void encrypt( String[] args ) throws Exception
    {
        if( args.length < 1 )
        {
            usage();
        }
        else
        {
            String infile = null;
            String outfile = null;
            String userPassword = "";
            String ownerPassword = "";

            PDDocument document = null;

            try
            {

                PDStandardEncryption enc = new PDStandardEncryption();
                enc.setVersion( PDEncryptionDictionary.VERSION1_40_BIT_ALGORITHM );
                enc.setRevision( PDStandardEncryption.REVISION2 );
                for( int i=0; i<args.length; i++ )
                {
                    String key = args[i];
                    if( key.equals( "-O" ) )
                    {
                        ownerPassword = args[++i];
                    }
                    else if( key.equals( "-U" ) )
                    {
                        userPassword = args[++i];
                    }
                    else if( key.equals( "-canAssemble" ) )
                    {
                        enc.setCanAssembleDocument( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canExtractContent" ) )
                    {
                        enc.setCanExtractContent( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canExtractForAccessibility" ) )
                    {
                        enc.setCanExtractForAccessibility( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canFillInForm" ) )
                    {
                        enc.setCanFillInForm( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canModify" ) )
                    {
                        enc.setCanModify( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canModifyAnnotations" ) )
                    {
                        enc.setCanModifyAnnotations( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canPrint" ) )
                    {
                        enc.setCanPrint( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-canPrintDegraded" ) )
                    {
                        enc.setCanPrintDegraded( args[++i].equalsIgnoreCase( "true" ) );
                    }
                    else if( key.equals( "-keyLength" ) )
                    {
                        try
                        {
                            enc.setLength( Integer.parseInt( args[++i] ) );
                        }
                        catch( NumberFormatException e )
                        {
                            throw new NumberFormatException(
                                "Error: -keyLength is not an integer '" + args[i] + "'" );
                        }
                    }
                    else if( key.equals( "-version" ) )
                    {
                        try
                        {
                            enc.setVersion( Integer.parseInt( args[++i] ) );
                        }
                        catch( NumberFormatException e )
                        {
                            throw new NumberFormatException( "Error: -version is not an integer '" + args[i] + "'" );
                        }
                    }
                    else if( key.equals( "-revision" ) )
                    {
                        try
                        {
                            enc.setRevision( Integer.parseInt( args[++i] ) );
                        }
                        catch( NumberFormatException e )
                        {
                            throw new NumberFormatException( "Error: -version is not an integer '" + args[i] + "'" );
                        }
                    }
                    else if( infile == null )
                    {
                        infile = key;
                    }
                    else if( outfile == null )
                    {
                        outfile = key;
                    }
                    else
                    {
                        usage();
                    }
                }
                if( infile == null )
                {
                    usage();
                }
                if( outfile == null )
                {
                    outfile = infile;
                }
                document = PDDocument.load( infile );

                if( !document.isEncrypted() )
                {
                    document.setEncryptionDictionary( enc );
                    document.encrypt( ownerPassword, userPassword );
                    document.save( outfile );
                }
                else
                {
                    System.err.println( "Error: Document is already encrypted." );
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
        System.err.println( "usage: java org.pdfbox.Encrypt [options] <inputfile> [outputfile]" );
        System.err.println( "   -O <password>                            Set the owner password" );
        System.err.println( "   -U <password>                            Set the user password" );
        System.err.println( "   -canAssemble <true|false>                Set the assemble permission" );
        System.err.println( "   -canExtractContent <true|false>          Set the extraction permission" );
        System.err.println( "   -canExtractForAccessibility <true|false> Set the extraction permission" );
        System.err.println( "   -canFillInForm <true|false>              Set the fill in form permission" );
        System.err.println( "   -canModify <true|false>                  Set the modify permission" );
        System.err.println( "   -canModifyAnnotations <true|false>       Set the modify annots permission" );
        System.err.println( "   -canPrint <true|false>                   Set the print permission" );
        System.err.println( "   -canPrintDegraded <true|false>           Set the print degraded permission" );
        System.err.println( "   -keyLength <length>                      The length of the key in bits(40)" );
        System.err.println( "   -version <0|1|2|3|4>                     The encryption version(1)" );
        System.err.println( "       0 - Undocumented/Unpublished" );
        System.err.println( "       1 - Allows for 40 bit encryption" );
        System.err.println( "       2 - Allows for variable length encryption" );
        System.err.println( "       3 - Unpublished but allows for key lengths from 40 to 128" );
        System.err.println( "       4 - Security handler defines encryption" );
        System.err.println( "   -revision <2|3|4>                        The encryption revision(2)" );
        System.err.println( "       2 - version less than 2" );
        System.err.println( "       3 - version 2 or 3 or uses a revision 3 permission" );
        System.err.println( "       4 - version of 4" );
        System.err.println( "\nNote: By default all permissions are set to true!" );
        System.exit( 1 );
    }

}