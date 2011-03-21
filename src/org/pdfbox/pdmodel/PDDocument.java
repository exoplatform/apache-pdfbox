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
package org.pdfbox.pdmodel;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSDocument;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;

import org.pdfbox.encryption.PDFEncryption;
import org.pdfbox.encryption.DocumentEncryption;

import org.pdfbox.exceptions.COSVisitorException;
import org.pdfbox.exceptions.CryptographyException;
import org.pdfbox.exceptions.InvalidPasswordException;

import org.pdfbox.pdfparser.PDFParser;

import org.pdfbox.pdfwriter.COSWriter;

import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.common.PDStream;

import org.pdfbox.pdmodel.encryption.PDEncryptionDictionary;
import org.pdfbox.pdmodel.encryption.PDEncryptionManager;
import org.pdfbox.pdmodel.encryption.PDStandardEncryption;

/**
 * This is the in-memory representation of the PDF document.  You need to call
 * close() on this object when you are done using it!!
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision$
 */
public class PDDocument implements Pageable
{
    private COSDocument document;
    private boolean encryptOnSave = false;
    private String encryptUserPassword = null;
    private String encryptOwnerPassword = null;

    //cached values
    private PDDocumentInformation documentInformation;
    private PDDocumentCatalog documentCatalog;

    //The encParameters will be cached here.  When the document is decrypted then
    //the COSDocument will not have an "Encrypt" dictionary anymore and this object
    //must be used.
    private PDEncryptionDictionary encParameters = null;
    /**
     * This will tell if the document was decrypted with the master password.
     */
    private boolean decryptedWithOwnerPassword = false;

    /**
     * Constructor, creates a new PDF Document with no pages.  You need to add
     * at least one page for the document to be valid.
     *
     * @throws IOException If there is an error creating this document.
     */
    public PDDocument() throws IOException
    {
        document = new COSDocument();

        //First we need a trailer
        COSDictionary trailer = new COSDictionary();
        document.setTrailer( trailer );

        //Next we need the root dictionary.
        COSDictionary rootDictionary = new COSDictionary();
        trailer.setItem( COSName.ROOT, rootDictionary );
        rootDictionary.setItem( COSName.TYPE, COSName.CATALOG );
        rootDictionary.setItem( COSName.VERSION, COSName.getPDFName( "1.4" ) );

        //next we need the pages tree structure
        COSDictionary pages = new COSDictionary();
        rootDictionary.setItem( COSName.PAGES, pages );
        pages.setItem( COSName.TYPE, COSName.PAGES );
        COSArray kidsArray = new COSArray();
        pages.setItem( COSName.KIDS, kidsArray );
        pages.setItem( COSName.COUNT, new COSInteger( 0 ) );
    }

    /**
     * This will add a page to the document.  This is a convenience method, that
     * will add the page to the root of the hierarchy and set the parent of the
     * page to the root.
     *
     * @param page The page to add to the document.
     */
    public void addPage( PDPage page )
    {
        PDPageNode rootPages = getDocumentCatalog().getPages();
        rootPages.getKids().add( page );
        page.setParent( rootPages );
        rootPages.updateCount();
    }
    
    /**
     * Remove the page from the document.
     *
     * @param page The page to remove from the document.
     * 
     * @return true if the page was found false otherwise.
     */
    public boolean removePage( PDPage page )
    {
        PDPageNode parent = page.getParent();
        boolean retval = parent.getKids().remove( page );
        if( retval )
        {
            //do a recursive updateCount starting at the root
            //of the document
            getDocumentCatalog().getPages().updateCount();
        }
        return retval;
    }
    
    /**
     * Remove the page from the document.
     * 
     * @param pageNumber 0 based index to page number.
     * @return true if the page was found false otherwise.
     */
    public boolean removePage( int pageNumber )
    {
        boolean removed = false;
        List allPages = getDocumentCatalog().getAllPages();
        if( allPages.size() > pageNumber)
        {
            PDPage page = (PDPage)allPages.get( pageNumber );
            removed = removePage( page );
        }
        return removed;
    }

    /**
     * This will import and copy the contents from another location.  Currently
     * the content stream is stored in a scratch file.  The scratch file is
     * associated with the document.  If you are adding a page to this document
     * from another document and want to copy the contents to this document's
     * scratch file then use this method otherwise just use the addPage method.
     *
     * @param page The page to import.
     * @return The page that was imported.
     *
     * @throws IOException If there is an error copying the page.
     */
    public PDPage importPage( PDPage page ) throws IOException
    {
        PDPage importedPage = new PDPage( new COSDictionary( page.getCOSDictionary() ) );
        InputStream is = null;
        OutputStream os = null;
        try
        {
            PDStream src = page.getContents();
            PDStream dest = new PDStream( new COSStream( src.getStream(), document.getScratchFile() ) );
            importedPage.setContents( dest );
            os = dest.createOutputStream();

            byte[] buf = new byte[10240];
            int amountRead = 0;
            is = src.createInputStream();
            while((amountRead = is.read(buf,0,10240)) > -1)
            {
                os.write(buf, 0, amountRead);
            }
            addPage( importedPage );
        }
        finally
        {
            if( is != null )
            {
                is.close();
            }
            if( os != null )
            {
                os.close();
            }
        }
        return importedPage;

    }

    /**
     * Constructor that uses an existing document.  The COSDocument that
     * is passed in must be valid.
     *
     * @param doc The COSDocument that this document wraps.
     */
    public PDDocument( COSDocument doc )
    {
        document = doc;
    }

    /**
     * This will get the low level document.
     *
     * @return The document that this layer sits on top of.
     */
    public COSDocument getDocument()
    {
        return document;
    }

    /**
     * This will get the document info dictionary.  This is guaranteed to not return null.
     *
     * @return The documents /Info dictionary
     */
    public PDDocumentInformation getDocumentInformation()
    {
        if( documentInformation == null )
        {
            COSDictionary trailer = document.getTrailer();
            COSDictionary infoDic = (COSDictionary)trailer.getDictionaryObject( COSName.INFO );
            if( infoDic == null )
            {
                infoDic = new COSDictionary();
                trailer.setItem( COSName.INFO, infoDic );
            }
            documentInformation = new PDDocumentInformation( infoDic );
        }
        return documentInformation;
    }

    /**
     * This will set the document information for this document.
     *
     * @param info The updated document information.
     */
    public void setDocumentInformation( PDDocumentInformation info )
    {
        documentInformation = info;
        document.getTrailer().setItem( COSName.INFO, info.getDictionary() );
    }

    /**
     * This will get the document CATALOG.  This is guaranteed to not return null.
     *
     * @return The documents /Root dictionary
     */
    public PDDocumentCatalog getDocumentCatalog()
    {
        if( documentCatalog == null )
        {
            COSDictionary trailer = document.getTrailer();
            COSDictionary infoDic = (COSDictionary)trailer.getDictionaryObject( COSName.ROOT );
            if( infoDic == null )
            {
                documentCatalog = new PDDocumentCatalog( this );
            }
            else
            {
                documentCatalog = new PDDocumentCatalog( this, infoDic );
            }

        }
        return documentCatalog;
    }

    /**
     * This will tell if this document is encrypted or not.
     *
     * @return true If this document is encrypted.
     */
    public boolean isEncrypted()
    {
        return document.isEncrypted();
    }

    /**
     * This will get the encryption dictionary for this document.  This will still
     * return the parameters if the document was decrypted.  If the document was
     * never encrypted then this will return null.  As the encryption architecture
     * in PDF documents is plugable this returns an abstract class, but the only
     * supported subclass at this time is a PDStandardEncryption object.
     *
     * @return The encryption dictionary(most likely a PDStandardEncryption object)
     *
     * @throws IOException If there is an error determining which security handler to use.
     */
    public PDEncryptionDictionary getEncryptionDictionary() throws IOException
    {
        if( encParameters == null )
        {
            encParameters = PDEncryptionManager.getEncryptionDictionary( document.getEncryptionDictionary() );
        }
        return encParameters;
    }

    /**
     * This will set the encryption dictionary for this document.
     *
     * @param encDictionary The encryption dictionary(most likely a PDStandardEncryption object)
     *
     * @throws IOException If there is an error determining which security handler to use.
     */
    public void setEncryptionDictionary( PDEncryptionDictionary encDictionary ) throws IOException
    {
        encParameters = encDictionary;
    }

    /**
     * This will determine if this is the user password.  This only applies when
     * the document is encrypted and uses standard encryption.
     *
     * @param password The plain text user password.
     *
     * @return true If the password passed in matches the user password used to encrypt the document.
     *
     * @throws IOException If there is an error determining if it is the user password.
     * @throws CryptographyException If there is an error in the encryption algorithms.
     */
    public boolean isUserPassword( String password ) throws IOException, CryptographyException
    {
        boolean retval = false;
        if( password == null )
        {
            password = "";
        }
        PDFEncryption encryptor = new PDFEncryption();
        PDEncryptionDictionary encryptionDictionary = getEncryptionDictionary();
        if( encryptionDictionary == null )
        {
            throw new IOException( "Error: Document is not encrypted" );
        }
        else
        {
            if( encryptionDictionary instanceof PDStandardEncryption )
            {
                COSString documentID = (COSString)document.getDocumentID().get(0);
                PDStandardEncryption standard = (PDStandardEncryption)encryptionDictionary;
                retval = encryptor.isUserPassword(
                    password.getBytes(),
                    standard.getUserKey(),
                    standard.getOwnerKey(),
                    standard.getPermissions(),
                    documentID.getBytes(),
                    standard.getRevision(),
                    standard.getLength()/8 );
            }
            else
            {
                throw new IOException( "Error: Encyption dictionary is not 'Standard'" +
                    encryptionDictionary.getClass().getName() );
            }
        }
        return retval;
    }

    /**
     * This will determine if this is the owner password.  This only applies when
     * the document is encrypted and uses standard encryption.
     *
     * @param password The plain text owner password.
     *
     * @return true If the password passed in matches the owner password used to encrypt the document.
     *
     * @throws IOException If there is an error determining if it is the user password.
     * @throws CryptographyException If there is an error in the encryption algorithms.
     */
    public boolean isOwnerPassword( String password ) throws IOException, CryptographyException
    {
        boolean retval = false;
        if( password == null )
        {
            password = "";
        }
        PDFEncryption encryptor = new PDFEncryption();
        PDEncryptionDictionary encryptionDictionary = getEncryptionDictionary();
        if( encryptionDictionary == null )
        {
            throw new IOException( "Error: Document is not encrypted" );
        }
        else
        {
            if( encryptionDictionary instanceof PDStandardEncryption )
            {
                COSString documentID = (COSString)document.getDocumentID().get( 0 );
                PDStandardEncryption standard = (PDStandardEncryption)encryptionDictionary;
                retval = encryptor.isOwnerPassword(
                    password.getBytes(),
                    standard.getUserKey(),
                    standard.getOwnerKey(),
                    standard.getPermissions(),
                    documentID.getBytes(),
                    standard.getRevision(),
                    standard.getLength()/8 );
            }
            else
            {
                throw new IOException( "Error: Encyption dictionary is not 'Standard'" +
                    encryptionDictionary.getClass().getName() );
            }
        }
        return retval;
    }

    /**
     * This will decrypt a document.
     *
     * @param password Either the user or owner password.
     *
     * @throws CryptographyException If there is an error decrypting the document.
     * @throws IOException If there is an error getting the stream data.
     * @throws InvalidPasswordException If the password is not a user or owner password.
     */
    public void decrypt( String password ) throws CryptographyException, IOException, InvalidPasswordException
    {
        decryptedWithOwnerPassword = isOwnerPassword( password );
        DocumentEncryption decryptor = new DocumentEncryption( this );
        decryptor.decryptDocument( password );
        document.dereferenceObjectStreams();
    }
    
    /**
     * This will tell if the document was decrypted with the master password.  This
     * entry is invalid if the PDF was not decrypted.
     * 
     * @return true if the pdf was decrypted with the master password.
     */
    public boolean wasDecryptedWithOwnerPassword()
    {
        return decryptedWithOwnerPassword;
    }

    /**
     * This will <b>mark</b> a document to be encrypted.  The actual encryption
     * will occur when the document is saved.
     *
     * @param ownerPassword The owner password to encrypt the document.
     * @param userPassword The user password to encrypt the document.
     *
     * @throws CryptographyException If an error occurs during encryption.
     * @throws IOException If there is an error accessing the data.
     */
    public void encrypt( String ownerPassword, String userPassword )
        throws CryptographyException, IOException
    {
        encryptOnSave = true;
        encryptOwnerPassword = ownerPassword;
        encryptUserPassword = userPassword;
    }
    
    
    /**
     * The owner password that was passed into the encrypt method.  You should
     * never use this method.  This will not longer be valid once encryption
     * has occured.
     * 
     * @return The owner password passed to the encrypt method.
     */
    public String getOwnerPasswordForEncryption()
    {
        return encryptOwnerPassword;
    }
    
    /**
     * The user password that was passed into the encrypt method.  You should
     * never use this method.  This will not longer be valid once encryption
     * has occured.
     * 
     * @return The user password passed to the encrypt method.
     */
    public String getUserPasswordForEncryption()
    {
        return encryptUserPassword;
    }
    
    /**
     * Internal method do determine if the document will be encrypted when it is saved.
     * 
     * @return True if encrypt has been called and the document 
     *              has not been saved yet.
     */
    public boolean willEncryptWhenSaving()
    {
        return encryptOnSave;
    }
    
    /**
     * This shoule only be called by the COSWriter after encryption has completed.
     *
     */
    public void clearWillEncryptWhenSaving()
    {
        encryptOnSave = false;
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( String filename ) throws IOException
    {
        return load( new BufferedInputStream( new FileInputStream( filename ) ) );
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( File file ) throws IOException
    {
        return load( new BufferedInputStream( new FileInputStream( file ) ) );
    }

    /**
     * This will load a document from an input stream.
     *
     * @param input The stream that contains the document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static PDDocument load( InputStream input ) throws IOException
    {
        PDFParser parser = new PDFParser( input );
        parser.parse();
        return parser.getPDDocument();
    }

    /**
     * This will save this document to the filesystem.
     *
     * @param fileName The file to save as.
     *
     * @throws IOException If there is an error saving the document.
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void save( String fileName ) throws IOException, COSVisitorException
    {
        save( new FileOutputStream( fileName ) );
    }

    /**
     * This will save the document to an output stream.
     *
     * @param output The stream to write to.
     *
     * @throws IOException If there is an error writing the document.
     * @throws COSVisitorException If an error occurs while generating the data.
     */
    public void save( OutputStream output ) throws IOException, COSVisitorException
    {
        //update the count in case any pages have been added behind the scenes.
        getDocumentCatalog().getPages().updateCount();
        COSWriter writer = null;
        try
        {
            writer = new COSWriter( output );
            writer.write( this );
            writer.close();
        }
        finally
        {
            if( writer != null )
            {
                writer.close();
            }
        }

    }

    /**
     * This will return the total page count of the PDF document.  Note: This method
     * is deprecated in favor of the getNumberOfPages method.  The getNumberOfPages is
     * a required interface method of the Pageable interface.  This method will
     * be removed in a future version of PDFBox!!
     *
     * @return The total number of pages in the PDF document.
     * @deprecated Use the getNumberOfPages method instead!
     */
    public int getPageCount()
    {
        return getNumberOfPages();
    }
    
    /**
     * @see Pageable#getNumberOfPages()
     */
    public int getNumberOfPages()
    {
        PDDocumentCatalog cat = getDocumentCatalog();
        return (int)cat.getPages().getCount();
    }
    
    /**
     * @see Pageable#getPageFormat(int)
     */
    public PageFormat getPageFormat(int pageIndex)
    {
        PDPage page = (PDPage)getDocumentCatalog().getAllPages().get( pageIndex );
        PDRectangle mediaBox = page.findMediaBox();
        PageFormat format = new PageFormat();
        Paper paper = new Paper();
        //hmm the imageable area might need to be the CropBox instead
        //of the media box???
        paper.setImageableArea( 0,0,mediaBox.getWidth(),mediaBox.getHeight());
        paper.setSize( mediaBox.getWidth(), mediaBox.getHeight() );
        format.setPaper( paper );
        return format;
    }
    
    /**
     * @see Pageable#getPrintable(int)
     */
    public Printable getPrintable(int pageIndex)
    {
        return (Printable)getDocumentCatalog().getAllPages().get( pageIndex );
    }
    
    /**
     * This will send the PDF document to a printer.  The printing functionality
     * depends on the org.pdfbox.pdfviewer.PageDrawer functionality.  The PageDrawer
     * is a work in progress and some PDFs will print correctly and some will
     * not.  This is a convenience method to create the java.awt.print.PrinterJob.  
     * The PDDocument implements the java.awt.print.Pageable interface and 
     * PDPage implementes the java.awt.print.Printable interface, so advanced printing
     * capabilities can be done by using those interfaces instead of this method. 
     * 
     * @throws PrinterException If there is an error while sending the PDF to
     * the printer, or you do not have permissions to print this document.
     */
    public void print() throws PrinterException
    {
        PDEncryptionDictionary encDictionary = null;
        try
        {
            encDictionary = getEncryptionDictionary();
        }
        catch( IOException io )
        {
            throw new PrinterIOException( io );
        }

        //only care about standard encryption and if it was decrypted with the
        //user password
        if( encDictionary instanceof PDStandardEncryption && 
            !wasDecryptedWithOwnerPassword() )
        {
            PDStandardEncryption stdEncryption = (PDStandardEncryption)encDictionary;
            if( !stdEncryption.canPrint() )
            {
                throw new PrinterException( "You do not have permission to print this document." );
            }
        }
        
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPageable(this);
        if( printJob.printDialog() )
        {
            printJob.print();
        }
    }
    
    /**
     * This will close the underlying COSDocument object.
     *
     * @throws IOException If there is an error releasing resources.
     */
    public void close() throws IOException
    {
        document.close();
    }
}