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
package org.pdfbox.pdmodel.encryption;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSInteger;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;

/**
 * This represents the base class for encryption dictionaries.  All PDF implementations
 * are expected to implement the Standard encryption algorithm, but others can be plugged in.
 *
 * See PDF Reference 1.4 section "3.5 Encryption"
 *
 * @author  Ben Litchfield (ben@csh.rit.edu)
 * @version $Revision$
 */
public abstract class PDEncryptionDictionary
{
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION0_UNDOCUMENTED_UNSUPPORTED = 0;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION1_40_BIT_ALGORITHM = 1;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION2_VARIABLE_LENGTH_ALGORITHM = 2;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION3_UNPUBLISHED_ALGORITHM = 3;
    /**
     * See PDF Reference 1.4 Table 3.13.
     */
    public static final int VERSION4_SECURITY_HANDLER = 4;

    /**
     * The default security handler.
     */
    public static final String DEFAULT_NAME = "Standard";

    /**
     * The default length for the encryption key.
     */
    public static final int DEFAULT_LENGTH = 40;

    /**
     * The default version, according to the PDF Reference.
     */
    public static final int DEFAULT_VERSION = VERSION0_UNDOCUMENTED_UNSUPPORTED;

    /**
     * The cos model wrapped object.
     */
    protected COSDictionary encryptionDictionary = null;

    /**
     * Constructor.
     *
     * @param dictionary The pre-existing encryption dictionary.
     */
    protected PDEncryptionDictionary( COSDictionary dictionary )
    {
        encryptionDictionary = dictionary;
    }

    /**
     * Constructor, sub classes need to fill out the required fields.
     */
    protected PDEncryptionDictionary()
    {
        encryptionDictionary = new COSDictionary();
        setLength( DEFAULT_LENGTH );
        setVersion( DEFAULT_VERSION );
    }

    /**
     * This will get the dictionary associated with this encryption dictionary.
     *
     * @return The COS dictionary that this object wraps.
     */
    public COSDictionary getCOSDictionary()
    {
        return encryptionDictionary;
    }

    /**
     * Read-only field of the encryption filter name.  The default value is
     * "Standard" for the built in security handler.
     *
     * @return The name of the encryption handler.
     */
    public String getFilter()
    {
        String filter = DEFAULT_NAME;
        COSName cosFilter = (COSName)encryptionDictionary.getDictionaryObject( COSName.FILTER );
        if( cosFilter != null )
        {
            filter = cosFilter.getName();
        }
        return filter;
    }

    /**
     * This will return the V entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.13.
     *
     * @return The encryption version to use.
     */
    public int getVersion()
    {
        int version = DEFAULT_VERSION;
        COSNumber cosVersion = (COSNumber)encryptionDictionary.getDictionaryObject( COSName.getPDFName( "V" ) );
        if( cosVersion != null )
        {
            version = cosVersion.intValue();
        }
        return version;
    }

    /**
     * This will set the V entry of the encryption dictionary.<br /><br />
     * See PDF Reference 1.4 Table 3.13.  <br /><br/>
     * <b>Note: This value is used to decrypt the pdf document.  If you change this when
     * the document is encrypted then decryption will fail!.</b>
     *
     * @param version The new encryption version.
     */
    public void setVersion( int version )
    {
        encryptionDictionary.setItem( COSName.getPDFName( "V" ), new COSInteger( version ) );
    }

    /**
     * This will return the Length entry of the encryption dictionary.<br /><br />
     * The length in <b>bits</b> for the encryption algorithm.  This will return a multiple of 8.
     *
     * @return The length in bits for the encryption algorithm
     */
    public int getLength()
    {
        int length = DEFAULT_LENGTH;
        COSNumber cosLength = (COSNumber)encryptionDictionary.getDictionaryObject( COSName.LENGTH );
        if( cosLength != null )
        {
            length = cosLength.intValue();
        }
        return length;
    }

    /**
     * This will set the number of bits to use for the encryption algorithm.
     *
     * @param length The new key length.
     */
    public void setLength( int length )
    {
        encryptionDictionary.setItem( COSName.LENGTH, new COSInteger( length ) );
    }
}