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
package test.pdfbox.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

import org.pdfbox.encryption.PDFEncryption;

/**
 * This will test the encryption algorithms in PDFBox.
 *
 * @author Ben Litchfield (ben@csh.rit.edu)
 * @version $Revision$
 */
public class TestEncryption extends TestCase
{
    private static Logger log = Logger.getLogger(TestEncryption.class);

    /**
     * Constructor.
     *
     * @param name The name of the test to run.
     */
    public TestEncryption( String name )
    {
        super( name );
    }

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite( TestEncryption.class );
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        String[] arg = {TestEncryption.class.getName() };
        junit.textui.TestRunner.main( arg );
    }

    /**
     * This will test some simple encryption.
     *
     * @throws Exception If there is an exception while encrypting.
     */
    public void testEncryption() throws Exception
    {
        byte[] key={0x65, 0x3d, 0x4f, 0x70, 0x0c };
        byte[] data={0x31, 0x20, 0x30, 0x20, 0x30, 0x20, 0x72, 0x67, 0x20, 0x30,
                    0x20, 0x30, 0x20, 0x33, 0x30, 0x38, 0x2e, 0x34, 0x37, 0x34,
                    0x37, 0x20, 0x35, 0x37, 0x2e, 0x36, 0x32, 0x37, 0x31, 0x20,
                    0x72, 0x65, 0x20, 0x66, 0x20, 0x30, 0x20, 0x47, 0x20, 0x31,
                    0x20, 0x77, 0x20, 0x30, 0x2e, 0x35, 0x20, 0x30, 0x2e, 0x35,
                    0x20, 0x33, 0x30, 0x37, 0x2e, 0x34, 0x37, 0x34, 0x37, 0x20,
                    0x35, 0x36, 0x2e, 0x36, 0x32, 0x37, 0x31, 0x20, 0x72, 0x65,
                    0x20, 0x73, 0x20, 0x2f, 0x54, 0x78, 0x20, 0x42, 0x4d, 0x43,
                    0x20, 0x71, 0x20, 0x31, 0x20, 0x31, 0x20, 0x33, 0x30, 0x36,
                    0x2e, 0x34, 0x37, 0x34, 0x37, 0x20, 0x35, 0x35, 0x2e, 0x36,
                    0x32, 0x37, 0x31, 0x20, 0x72, 0x65, 0x20, 0x57, 0x20, 0x6e,
                    0x20, 0x30, 0x20, 0x67, 0x20, 0x42, 0x54, 0x0a, 0x2f, 0x48,
                    0x65, 0x6c, 0x76, 0x20, 0x31, 0x30, 0x20, 0x54, 0x66, 0x0a,
                    0x32, 0x20, 0x32, 0x35, 0x2e, 0x31, 0x30, 0x33, 0x35, 0x20,
                    0x54, 0x64, 0x0a, 0x31, 0x31, 0x2e, 0x35, 0x35, 0x39, 0x39,
                    0x20, 0x54, 0x4c, 0x0a, 0x28, 0x2d, 0x2d, 0x5c, 0x30, 0x34,
                    0x30, 0x44, 0x65, 0x66, 0x61, 0x75, 0x6c, 0x74, 0x5c, 0x30,
                    0x34, 0x30, 0x66, 0x69, 0x65, 0x6c, 0x64, 0x5c, 0x30, 0x34,
                    0x30, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x5c, 0x30, 0x34, 0x30,
                    0x2d, 0x2d, 0x29, 0x20, 0x54, 0x6a, 0x0a, 0x45, 0x54, 0x0a,
                    0x20, 0x51, 0x20, 0x45, 0x4d, 0x43, (byte)0x8a, 0x0d, 0x0a
                    };
        PDFEncryption enc = new PDFEncryption();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        enc.encryptData( 43, 0, key, new ByteArrayInputStream( data ), output );

        byte[] encrypted = output.toByteArray();
        printHexString( encrypted );

        ByteArrayOutputStream sameAsInput = new ByteArrayOutputStream();
        enc.encryptData( 43, 0, key, new ByteArrayInputStream( encrypted ), sameAsInput );
        byte[] dataAgain = sameAsInput.toByteArray();
        cmpArray( data, dataAgain );
    }

    /**
     * This will compare a couple of arrays and fail if they do not match.
     *
     * @param firstArray The first array.
     * @param secondArray The second array.
     */
    private void cmpArray( byte[] firstArray, byte[] secondArray )
    {
        if( firstArray.length != secondArray.length )
        {
            fail( "The array lengths do not match for " +
                  ", firstArray length was: " + firstArray.length +
                  ", secondArray length was: " + secondArray.length);
        }

        for( int i=0; i<firstArray.length; i++ )
        {
            if( firstArray[i] != secondArray[i] )
            {
                fail( "Array data does not match "  );
            }
        }
    }

    /**
     * This will print a byte array as a hex string to standard output.
     *
     * @param data The array to print.
     */
    private void printHexString( byte[] data )
    {
        for( int i=0; i<data.length; i++ )
        {
            int nextByte = (data[i] + 256)%256;
            String hexString = Integer.toHexString( nextByte );
            if( hexString.length() < 2 )
            {
                hexString = "0" + hexString;
            }
            System.out.print( hexString );
            if( i != 0 && (i+1) % 2 == 0 )
            {
                System.out.print( " " );
            }
            else if( i!= 0 &&i % 20 == 0 )
            {
                System.out.println();
            }
        }
        System.out.println();
    }
}