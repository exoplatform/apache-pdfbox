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

import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSStream;
import org.pdfbox.cos.COSString;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.common.PDMetadata;
import org.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.pdfbox.pdmodel.interactive.action.type.PDAction;
import org.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.pdfbox.pdmodel.interactive.form.PDAcroForm;

import org.pdfbox.pdmodel.interactive.pagenavigation.PDThread;
import org.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;

/**
 * This class represents the acroform of a PDF document.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision$
 */
public class PDDocumentCatalog implements COSObjectable
{
    private COSDictionary root;
    private PDDocument document;

    private PDAcroForm acroForm = null;

    /**
     * Constructor.
     *
     * @param doc The document that this catalog is part of.
     */
    public PDDocumentCatalog( PDDocument doc )
    {
        document = doc;
        root = new COSDictionary();
        root.setItem( COSName.TYPE, new COSString( "Catalog" ) );
        document.getDocument().getTrailer().setItem( COSName.ROOT, root );
    }

    /**
     * Constructor.
     *
     * @param doc The document that this catalog is part of.
     * @param rootDictionary The root dictionary that this object wraps.
     */
    public PDDocumentCatalog( PDDocument doc, COSDictionary rootDictionary )
    {
        document = doc;
        root = rootDictionary;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return root;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return root;
    }

    /**
     * This will get the documents acroform.  This will return null if
     * no acroform is part of the document.
     *
     * @return The documents acroform.
     */
    public PDAcroForm getAcroForm()
    {
        if( acroForm == null )
        {
            COSDictionary acroFormDic =
                (COSDictionary)root.getDictionaryObject( COSName.ACRO_FORM );
            if( acroFormDic == null )
            {
                acroForm = new PDAcroForm( document );
                root.setItem( COSName.ACRO_FORM, acroForm.getDictionary() );
            }
            else
            {
                acroForm = new PDAcroForm( document, acroFormDic );
            }
        }
        return acroForm;
    }

    /**
     * This will get the root node for the pages.
     *
     * @return The parent page node.
     */
    public PDPageNode getPages()
    {
        return new PDPageNode( (COSDictionary)root.getDictionaryObject( COSName.PAGES ) );
    }

    /**
     * The PDF document contains a hierarchical structure of PDPageNode and PDPages, which
     * is mostly just a way to store this information.  This method will return a flat list
     * of all PDPage objects in this document.
     *
     * @return A list of PDPage objects.
     */
    public List getAllPages()
    {
        List retval = new ArrayList();
        PDPageNode rootNode = getPages();
        //old (slower):
        //getPageObjects( rootNode, retval );
        rootNode.getAllKids(retval);
        return retval;
    }
    
    /**
     * Get the viewer preferences associated with this document or null if they
     * do not exist.
     * 
     * @return The document's viewer preferences.
     */
    public PDViewerPreferences getViewerPreferences()
    {
        PDViewerPreferences retval = null;
        COSDictionary dict = (COSDictionary)root.getDictionaryObject( "ViewerPreferences" );
        if( dict != null )
        {
            retval = new PDViewerPreferences( dict );
        }
        
        return retval;
    }
    
    /**
     * Set the viewer preferences.
     * 
     * @param prefs The new viewer preferences.
     */
    public void setViewerPreferences( PDViewerPreferences prefs )
    {
        root.setItem( "ViewerPreferences", prefs );
    }
    
    /**
     * Get the outline associated with this document or null if it
     * does not exist.
     * 
     * @return The document's outline.
     */
    public PDDocumentOutline getDocumentOutline()
    {
        PDDocumentOutline retval = null;
        COSDictionary dict = (COSDictionary)root.getDictionaryObject( "Outlines" );
        if( dict != null )
        {
            retval = new PDDocumentOutline( dict );
        }
        
        return retval;
    }
    
    /**
     * Set the document outlines.
     * 
     * @param outlines The new document outlines.
     */
    public void setDocumentOutline( PDDocumentOutline outlines )
    {
        root.setItem( "Outlines", outlines );
    }
    
    /**
     * Get the list threads for this pdf document.
     * 
     * @return A list of PDThread objects.
     */
    public List getThreads()
    {
        COSArray array = (COSArray)root.getDictionaryObject( "Threads" );
        if( array == null )
        {
            array = new COSArray();
            root.setItem( "Threads", array );
        }
        List pdObjects = new ArrayList();
        for( int i=0; i<array.size(); i++ )
        {
            pdObjects.add( new PDThread( (COSDictionary)array.getObject( i ) ) );
        }
        return new COSArrayList( pdObjects, array );
    }
    
    /**
     * Set the list of threads for this pdf document.
     * 
     * @param threads The list of threads, or null to clear it.
     */
    public void setThreads( List threads )
    {
        root.setItem( "Threads", COSArrayList.converterToCOSArray( threads ) );
    }
    
    /**
     * Get the metadata that is part of the document catalog.  This will 
     * return null if there is no meta data for this object.
     * 
     * @return The metadata for this object.
     */
    public PDMetadata getMetadata()
    {
        PDMetadata retval = null;
        COSStream stream = (COSStream)root.getDictionaryObject( "Metadata" );
        if( stream != null )
        {
            retval = new PDMetadata( stream );
        }
        return retval;
    }
    
    /**
     * Set the metadata for this object.  This can be null.
     * 
     * @param meta The meta data for this object.
     */
    public void setMetadata( PDMetadata meta )
    {
        root.setItem( "Metadata", meta );
    }
    
    /**
     * Set the Document Open Action for this object.
     * 
     * @param action The action you want to perform.
     */
    public void setOpenAction( PDAction action )
    {
        root.setItem( COSName.getPDFName("OpenAction"), action );
    }

    /**
     * Get the Document Open Action for this object.
     *
     * @return The action to perform when the document is opened.
     */
    public PDAction getOpenAction()
    {
        PDAction action = null;
        COSDictionary actionDic = (COSDictionary) root.getDictionaryObject("OpenAction");
        
        action = PDActionFactory.createAction(actionDic);
        
        return action;
    }    
    /**
     * @return The Additional Actions for this Document 
     */
    public PDDocumentCatalogAdditionalActions getActions()
    {
        COSDictionary addAct = (COSDictionary) root.getDictionaryObject("AA");
        if (addAct == null)
        {
            addAct = new COSDictionary();
            root.setItem("AA", addAct);
        }       
        return new PDDocumentCatalogAdditionalActions(addAct);
    }
    
    /**
     * Set the additional actions for the document.
     * 
     * @param actions The actions that are associated with this document.
     */
    public void setActions( PDDocumentCatalogAdditionalActions actions )
    {
        root.setItem("AA", actions );
    }
    
    /**
     * @return The names dictionary for this document or null if none exist.
     */
    public PDDocumentNameDictionary getNames()
    {
        PDDocumentNameDictionary nameDic = null;
        COSDictionary names = (COSDictionary) root.getDictionaryObject("Names");
        if(names != null)
        {
            nameDic = new PDDocumentNameDictionary(this,names);
        }       
        return nameDic;
    }
    
    /**
     * Set the names dictionary for the document.
     * 
     * @param names The names dictionary that is associated with this document.
     */
    public void setNames( PDDocumentNameDictionary names )
    {
        root.setItem("Names", names );
    }
    
    /**
     * Get info about doc's usage of tagged features.  This will return
     * null if there is no information.
     * 
     * @return The new mark info. 
     */
    public PDMarkInfo getMarkInfo()
    {
        PDMarkInfo retval = null;
        COSDictionary dic = (COSDictionary)root.getDictionaryObject( "MarkInfo" );
        if( dic != null )
        {
            retval = new PDMarkInfo( dic );
        }
        return retval;
    }
    
    /**
     * Set information about the doc's usage of tagged features.
     * 
     * @param markInfo The new MarkInfo data.
     */
    public void setMarkInfo( PDMarkInfo markInfo )
    {
        root.setItem( "MarkInfo", markInfo );
    }
}