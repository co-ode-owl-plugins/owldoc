package org.coode.browser.test;

import junit.framework.TestCase;
import org.coode.html.util.HTMLUtil;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 *
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jun 11, 2007<br><br>
 * <p/>
 */
public class HyperlinkTestCase extends TestCase {

    public void testRelativeURLBothEndingWithPage(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/Beef.html");
            URL target = new URL("http://www.co-ode.org/ontologies/properties/hasMeat.html");
            assertEquals("../properties/hasMeat.html", HTMLUtil.createRelativeURL(current, target));
            assertEquals("../classes/Beef.html", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLSubdir(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/Beef.html");
            URL target = new URL("http://www.co-ode.org/ontologies/classes/subdir/Gibbon.html");
            assertEquals("subdir/Gibbon.html", HTMLUtil.createRelativeURL(current, target));
            assertEquals("../Beef.html", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLSuperdir(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/Beef.html");
            URL target = new URL("http://www.co-ode.org/ontologies/Tree.html");
            assertEquals("../Tree.html", HTMLUtil.createRelativeURL(current, target));
            assertEquals("classes/Beef.html", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLIndexToNamedPage(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/");
            URL target = new URL("http://www.co-ode.org/ontologies/classes/Tree.html");
            assertEquals("Tree.html", HTMLUtil.createRelativeURL(current, target));
            assertEquals("/", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLQuerySameLevel(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/Beef.html");
            URL target = new URL("http://www.co-ode.org/ontologies/classes/?name=Monkey&base=http://www.co-ode.org/ontologies/");
            assertEquals("./?name=Monkey&base=http://www.co-ode.org/ontologies/", HTMLUtil.createRelativeURL(current, target));
            assertEquals("Beef.html", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLTwoQueriesSameLevel(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/?name=Domain&base=http://www.co-ode.org/ontologies/");
            URL target = new URL("http://www.co-ode.org/ontologies/classes/?name=Monkey&base=http://www.co-ode.org/ontologies/");
            assertEquals("./?name=Monkey&base=http://www.co-ode.org/ontologies/", HTMLUtil.createRelativeURL(current, target));
            assertEquals("./?name=Domain&base=http://www.co-ode.org/ontologies/", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLEndingWithSlash(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/");
            URL target = new URL("http://www.co-ode.org/ontologies/properties/");

            assertEquals("../properties/", HTMLUtil.createRelativeURL(current, target));
            assertEquals("../classes/", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testRelativeURLEndingWithSlashAndQuery(){
        try {
            URL current = new URL("http://www.co-ode.org/ontologies/classes/?name=Monkey&base=http://www.co-ode.org/ontologies/");
            URL target = new URL("http://www.co-ode.org/ontologies/properties/?name=hasMonkey&base=http://www.co-ode.org/ontologies/");

            assertEquals("../properties/?name=hasMonkey&base=http://www.co-ode.org/ontologies/", HTMLUtil.createRelativeURL(current, target));
            assertEquals("../classes/?name=Monkey&base=http://www.co-ode.org/ontologies/", HTMLUtil.createRelativeURL(target, current));
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
    }
}
