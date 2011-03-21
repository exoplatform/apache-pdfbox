/*
 * @(#)DefaultFileFilter.java    1.12 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.pdfbox.util;

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
//import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 * Example - create a new filter that filerts out all files
 * but gif and jpg image files:
 *
 *     JFileChooser chooser = new JFileChooser();
 *     DefaultFileFilter filter = new DefaultFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *     chooser.addChoosableFileFilter(filter);
 *     chooser.showOpenDialog(this);
 *
 * @version $Revision$
 * @author Jeff Dinkins
 */
public class DefaultFileFilter extends FileFilter
{

    private static final String TYPE_UNKNOWN = "Type Unknown";
    private static final String HIDDEN_FILE = "Hidden File";

    private Hashtable filters = null;
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public DefaultFileFilter()
    {
        this.filters = new Hashtable();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new DefaultFileFilter("jpg");
     *
     * @see #addExtension
     */
    public DefaultFileFilter(String extension)
    {
        this(extension,null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new DefaultFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public DefaultFileFilter(String extension, String desc)
    {
        this();
        if(extension!=null)
        {
            addExtension(extension);
        }
        if(desc!=null)
        {
            setDescription(desc);
        }
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new DefaultFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see #addExtension
     */
    public DefaultFileFilter(String[] filterArray)
    {
        this(filterArray, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new DefaultFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public DefaultFileFilter(String[] filterArray, String desc)
    {
        this();
        for (int i = 0; i < filterArray.length; i++)
        {
            // add filters one by one
            addExtension(filterArray[i]);
        }
        if(desc!=null)
        {
            setDescription(desc);
        }
    }

    /**
     * Files that begin with "." are ignored.
     *
     * @param f The file to accept.
     * @return true if this file should be shown in the directory pane, false if it shouldn't.
     * @see #getExtension
     * @see FileFilter#accepts
     */
    public boolean accept(File f)
    {
        if(f != null)
        {
            if(f.isDirectory())
            {
                return true;
            }
            String extension = getExtension(f);
            if(extension != null && filters.get(getExtension(f)) != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the extension portion of the file's name .
     *
     * @param f The file to get the extension of.
     * @return The extension of a file.
     * @see #getExtension
     * @see FileFilter#accept
     */
     public String getExtension(File f)
     {
        if(f != null)
        {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if(i>0 && i<filename.length()-1)
            {
                return filename.substring(i+1).toLowerCase();
            }
        }
        return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against.
     *
     * For example: the following code will create a filter that filters
     * out all files except those that end in ".jpg" and ".tif":
     *
     *   DefaultFileFilter filter = new DefaultFileFilter();
     *   filter.addExtension("jpg");
     *   filter.addExtension("tif");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @param extension The new extension to add.
     */
    public void addExtension(String extension)
    {
        if(filters == null)
        {
            filters = new Hashtable(5);
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }


    /**
     * Returns the human readable description of this filter. For
     * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
     *
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     * @see FileFilter#getDescription
     *
     * @return The human readable description of this filter.
     */
    public String getDescription()
    {
        if(fullDescription == null)
        {
            if(description == null || isExtensionListInDescription())
            {
                fullDescription = description==null ? "(" : description + " (";
                // build the description from the extension list
                Enumeration extensions = filters.keys();
                if(extensions != null)
                {
                    fullDescription += "." + (String) extensions.nextElement();
                    while (extensions.hasMoreElements())
                    {
                        fullDescription += ", ." + (String) extensions.nextElement();
                    }
                }
                fullDescription += ")";
            }
            else
            {
                fullDescription = description;
            }
        }
        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For
     * example: filter.setDescription("Gif and JPG Images");
     *
     * @param desc the new description for the file.
     * @see setDescription
     * @see setExtensionListInDescription
     * @see isExtensionListInDescription
     */
    public void setDescription(String desc)
    {
        description = desc;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     *
     * @param b Tell if the extionsion shoud show up in human readable description.
     * @see getDescription
     * @see setDescription
     * @see isExtensionListInDescription
     */
    public void setExtensionListInDescription(boolean b)
    {
        useExtensionsInDescription = b;
        fullDescription = null;
    }

    /**
     * @return whether the extension list (.jpg, .gif, etc) should
     * show up in the human readable description.
     *
     * Only relevent if a description was provided in the constructor
     * or using setDescription();
     *
     *
     * @see getDescription
     * @see setDescription
     * @see setExtensionListInDescription
     */
    public boolean isExtensionListInDescription()
    {
        return useExtensionsInDescription;
    }
}