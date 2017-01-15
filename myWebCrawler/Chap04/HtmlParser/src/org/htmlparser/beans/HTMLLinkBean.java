// HTMLParser Library - A java-based parser for HTML
// http://htmlparser.org
// Copyright (C) 2006 Derrick Oswald
//
// Revision Control Information
//
// $URL: https://svn.sourceforge.net/svnroot/htmlparser/trunk/parser/src/main/java/org/htmlparser/beans/HTMLLinkBean.java $
// $Author: derrickoswald $
// $Date: 2006-09-16 10:44:17 -0400 (Sat, 16 Sep 2006) $
// $Revision: 4 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the Common Public License; either
// version 1.0 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// Common Public License for more details.
//
// You should have received a copy of the Common Public License
// along with this library; if not, the license is available from
// the Open Source Initiative (OSI) website:
//   http://opensource.org/licenses/cpl1.0.php

package org.htmlparser.beans;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JList;

/**
 * Display the links from a URL.
 * @author Derrick Oswald
 * Created on December 24, 2002, 3:49 PM
 */
public class HTMLLinkBean
    extends
        JList
    implements
        Serializable,
        PropertyChangeListener
{
    /**
     * The underlying bean that provides our htmlparser specific properties.
     */
    protected LinkBean mBean;

    /**
     * Creates a new HTMLTextBean.
     * This uses an underlying StringBean and displays the text.
     */
    public HTMLLinkBean ()
    {
        getBean ().addPropertyChangeListener (this);
    }

    /**
     * Return the underlying bean object.
     * Creates a new one if it hasn't been initialized yet.
     * @return The StringBean this bean uses to fetch text.
     */
    protected LinkBean getBean ()
    {
        if (null == mBean)
            mBean = new LinkBean ();

        return (mBean);
    }

    /**
     * Return the minimum dimension for this visible bean.
     * @return a minimum bounding box for this bean.
     */
    public Dimension getMinimumSize ()
    {
        FontMetrics met;
        int width;
        int height;

        met = getFontMetrics (getFont ());
        width = met.stringWidth ("http://localhost");
        height = met.getLeading () + met.getHeight () + met.getDescent ();

        return (new Dimension (width, height));
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param listener The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener (listener);
        getBean ().addPropertyChangeListener (listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a registered PropertyChangeListener.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener)
    {
        super.addPropertyChangeListener (listener);
        getBean ().removePropertyChangeListener (listener);
    }

    //
    // Properties
    //

    /**
     * Getter for property links.
     * <p><em>Delegates to the underlying StringBean</em>
     * @return Value of property links.
     */
    public URL[] getLinks ()
    {
        return (getBean ().getLinks ());
    }

    /**
     * Getter for property URL.
     * <p><em>Delegates to the underlying StringBean</em>
     * @return Value of property URL.
     */
    public String getURL ()
    {
        return (getBean ().getURL ());
    }

    /**
     * Setter for property URL.
     * <p><em>Delegates to the underlying StringBean</em>
     * @param url New value of property URL.
     */
    public void setURL (String url)
    {
        getBean ().setURL (url);
    }

    /**
     * Getter for property Connection.
     * @return Value of property Connection.
     */
    public URLConnection getConnection ()
    {
        return (getBean ().getConnection ());
    }

    /**
     * Setter for property Connection.
     * @param connection New value of property Connection.
     */
    public void setConnection (URLConnection connection)
    {
        getBean ().setConnection (connection);
    }

    //
    // PropertyChangeListener inteface
    //

    /**
     * Responds to changes in the underlying bean's properties.
     * @param event The event triggering this listener method call.
     */
    public void propertyChange (PropertyChangeEvent event)
    {
        if (event.getPropertyName ().equals (LinkBean.PROP_LINKS_PROPERTY))
        {
            setListData (getBean ().getLinks ());
        }
    }

//    /**
//     * Unit test.
//     */
//    public static void main (String[] args)
//    {
//        HTMLLinkBean lb = new HTMLLinkBean ();
//        lb.setURL ("http://cbc.ca");
//        javax.swing.JFrame frame = new javax.swing.JFrame ();
//        frame.getContentPane ().setLayout (new BorderLayout ());
//        frame.getContentPane ().add (new JScrollPane (lb),
//            BorderLayout.CENTER);
//        frame.addWindowListener (new java.awt.event.WindowListener () {
//            public void windowOpened (java.awt.event.WindowEvent e) {}
//            public void windowClosing (java.awt.event.WindowEvent e)
//            {
//                System.exit (0);
//            }
//            public void windowClosed (java.awt.event.WindowEvent e) {}
//            public void windowDeiconified (java.awt.event.WindowEvent e) {}
//            public void windowIconified (java.awt.event.WindowEvent e) {}
//            public void windowActivated (java.awt.event.WindowEvent e) {}
//            public void windowDeactivated (java.awt.event.WindowEvent e) {}
//        });
//        frame.setBounds (100, 100, 640, 480);
//        frame.show ();
//    }
}



