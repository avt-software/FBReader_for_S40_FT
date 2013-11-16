/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

/*
 Copyright 2013 Sole Proprietorship Vita Tolstikova
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

* 1.The module FeedBooksCatalogModel.java create on base RSSModel.java 
*   from Tantalum 4 library and adopted for parsing catalog.atom structures
*   from http://www.feedbooks.com/
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.views.model.parser;

import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.tantalum.net.xml.XMLModel;
import org.tantalum.net.xml.XMLAttributes;
import org.fbreader.Main;


/**
 * 
 * FeedBooksCatalogModel Class for parsing catalog.atom structures
 * from http://www.feedbooks.com/
 *  
 */
public class FeedBooksCatalogModel extends XMLModel {

    /**
     * The collection of <entry> tags parsed from XML
     */
    protected final Vector feedBooksEntriesDataVector = new Vector();
    
    //Object for describe FeedBooksCatalog(catalog.atom) structure.
    //In particular <entry> elements
    protected FeedBooksCatalog feedBooksCatalog;  

    /**
     * The maximum length of the model. Items after this length is reached will
     * not be parsed, thereby limiting the total memory consumption.
     */
    protected final int maxLength;

    /**
     * Create a new FeedBooksCatalogModel data model which will 
     * never parse more than the specified maxLength items.
     *
     * @param maxLength
     */
    public FeedBooksCatalogModel(final int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * When parsing this XML document, this indicates the beginning of an
     * XML tag
     *
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    public synchronized void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("entry")) {
            feedBooksCatalog = new  FeedBooksCatalog();
        }
    }

    /**
     * When parsing this XML document, this indicates the body of an XML tag
     *
     * @param qname
     * @param chars
     * @param attributes
     */
    protected synchronized void parseElement(final String qname, final String chars, final XMLAttributes attributes) {
        try {
            if (feedBooksCatalog!= null) {
                synchronized (feedBooksCatalog) {
                    
                    if (qname.equals("title")) {
                        feedBooksCatalog.setTitle(chars);
                    }
                    else if (qname.equals("id")) {
                        feedBooksCatalog.setId(chars);
                    }
                    else if (qname.equals("content")) {
                        feedBooksCatalog.setContent(chars);
                    }
                  }    
            }
            
        } catch (Exception e) {
            //#debug
            Main.LOGGER.log("FeedBooksCatalogModel::Parsing error-qname=" + qname + " - chars=" + chars + e);
        }
        
        
    }

    /**
     * When parsing this XML document, this indicates the end of an XML tag
     *
     * @param uri
     * @param localName
     * @param qname
     * @throws SAXException
     */
    public void endElement(final String uri, final String localName, final String qname) throws SAXException {
        super.endElement(uri, localName, qname);
        if (qname.equals("entry")) {
            if (feedBooksEntriesDataVector.size() < maxLength) {
                feedBooksEntriesDataVector.addElement(feedBooksCatalog);
            }
            feedBooksCatalog = null;
        }
    
    }

    
    
    /**
     * Get feedBooksEntriesDataVector
     *
     * @return
     */
    public synchronized Vector getFeedBooksEntriesDataVector() {
        return feedBooksEntriesDataVector;
    }
    
    
    /**
     * Empty the data model
     *
     */
    public synchronized void removeAllElements() {
        feedBooksEntriesDataVector.removeAllElements();
    }

    /**
     * Return the number of elements feedBooksEntriesDataVector in the model
     *
     * @return
     */
    public synchronized int size() {
        return feedBooksEntriesDataVector.size();
    }

    /**
     * Return the FeedBooksCatalog object from feedBooksEntriesDataVector 
     * at the specified position by id
     *
     * @param i
     * @return
     */
    
    public synchronized FeedBooksCatalog getFeedBooksCatalogByIdAt(final int i) {
        return (FeedBooksCatalog) feedBooksEntriesDataVector.elementAt(i);
    }
    

    /**
     * Copy the current list into a working array which can safely be used
     * outside of synchronized blocks. This guards against simultaneous changes
     * to the list on another thread.
     *
     * @param copy
     * @return
     */
    public final synchronized FeedBooksCatalogModel[] copy(FeedBooksCatalogModel[] copy) {
        if (copy == null || copy.length != size()) {
            copy = new FeedBooksCatalogModel[size()];
        }
        feedBooksEntriesDataVector.copyInto(copy);

        return copy;
    }

    /**
     * Return the item before or after the specified item.
     *
     * null is returned if the item is not found, or there are no more items in
     * the specified direction.
     *
     * @param item
     * @param before
     * @return
     */
    public synchronized  FeedBooksCatalogModel itemNextTo(final FeedBooksCatalogModel item, final boolean before) {
        FeedBooksCatalogModel adjacentItem = null;
        int i = feedBooksEntriesDataVector.indexOf(item);

        if (before) {
            if (i > 0) {
                adjacentItem = (FeedBooksCatalogModel) feedBooksEntriesDataVector.elementAt(--i);
            }
        } else if (i < size() - 1) {
            adjacentItem = (FeedBooksCatalogModel) feedBooksEntriesDataVector.elementAt(++i);
        }

        return adjacentItem;
    }
}
