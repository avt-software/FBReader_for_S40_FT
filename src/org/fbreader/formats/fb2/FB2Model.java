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

* 1.The module FB2Module.java create on base RSSModel.java 
*   from Tantalum 4 library and adopted for the FB2 documents 
*   processing
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.formats.fb2;


import java.util.Vector;
import org.tantalum.util.L;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.tantalum.net.xml.XMLModel;
import org.tantalum.net.xml.XMLAttributes;
import org.fbreader.localization.L10n;


/**
 * Class FB2Model for parsing FB2Book
 *
 */
public class FB2Model extends XMLModel {

    /**
     * The collection of FB2Book tags parsed from XML
     */
    protected final Vector fb2BookDataVector = new Vector();
    
    //1.1.1 Element <title-info>
    protected TitleInfo titleInfo;    
    

    /**
     * The maximum length of the model. Items after this length is reached will
     * not be parsed, thereby limiting the total memory consumption.
     */
    protected final int maxLength;

    /**
     * Create a new FB2Book data model which will never parse more than the
     * specified maxLength items.
     *
     * @param maxLength
     */
    public FB2Model(final int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * When parsing this Locale XML document, this indicates the beginning of an
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

        if (qName.equals("title-info")) {
            titleInfo = new  TitleInfo();
        }
        try {
             if (titleInfo!= null) {
                    synchronized (titleInfo) {
                        if (qName.equals("author")) {
                            titleInfo.setFlagAuthor(true);
 
                        }

                    }
                }   
            } catch (Exception e) {
                //#debug
                L.e("FB2Book::TitleInfo:: Element parsing error", "qName=" + qName, e);
            }  
        
        

  
    }

    /**
     * When parsing this Locale XML document, this indicates the body of an XML tag
     *
     * @param qname
     * @param chars
     * @param attributes
     */
    protected synchronized void parseElement(final String qname, final String chars, final XMLAttributes attributes) {
        try {
            if (titleInfo!= null) {
                synchronized (titleInfo) {

                    if (qname.equals("genre") && chars.length()!=0) {
                        titleInfo.setGenre(L10n.getMessage(chars));
                    }
                    else if (qname.equals("first-name") || 
                             qname.equals("middle-name") ||
                             qname.equals("last-name")) {
                             if (titleInfo.isAuthor() && chars.length()!=0) {
                                  titleInfo.setAuthor(chars);
                              }
                    }
                    else if (qname.equals("book-title") && chars.length()!=0) {
                        titleInfo.setBookTitle(chars);
                    }
                    else if (qname.equals("lang") && chars.length()!=0) {
                            titleInfo.setBookLang(chars);
                     }                    
                  }    
            }
            
        } catch (Exception e) {
            //#debug
            L.e("FB2Book::Parsing error", "qname=" + qname + " - chars=" + chars, e);
        }
        
        
    }

    /**
     * When parsing this Locale XML document, this indicates the end of an XML tag
     *
     * @param uri
     * @param localName
     * @param qname
     * @throws SAXException
     */
    public void endElement(final String uri, final String localName, final String qname) throws SAXException {
        super.endElement(uri, localName, qname);
        
        if (qname.equals("author")) {
            try {
                if (titleInfo!= null) {
                    synchronized (titleInfo) {
                      titleInfo.setFlagAuthor(false);
                    }
                }   
            } catch (Exception e) {
                //#debug
                L.e("FB2Book::TitleInfo:: Element parsing error", "qname=" + qname, e);
            }             
        }       
        
        
        if (qname.equals("title-info")) {
            if (fb2BookDataVector.size() < maxLength) {
                //check wrong content of the data string about genre 
                String genreStr=titleInfo.getGenre();
                String findStr="Error. Not find localized message.";
                if (genreStr.equals("") || genreStr.indexOf(findStr)!=-1) {
                    titleInfo.setUnknownGenre(L10n.getMessage("UNKNOWN_GENRE"));
                }

                //check wrong content of the data string about author
                String authorStr=titleInfo.getAuthor();
                if (authorStr.equals("")) {
                    titleInfo.setUnknownAuthor(L10n.getMessage("UNKNOWN_AUTHOR"));
                }
                
                //check wrong content of the data string about Book Title
                String bookTitleStr=titleInfo.getBookTitle();
                if (bookTitleStr.equals("")) {
                    titleInfo.setBookTitle(L10n.getMessage("UNKNOWN_BOOK_TITLE"));
                }                
                
                //check wrong content of the data string about Book Lang
                String bookLangStr=titleInfo.getBookLang();
                if (bookLangStr.equals("")) {
                    titleInfo.setBookLang(L10n.getMessage("UNKNOWN_BOOK_LANG"));
                }                
                
                fb2BookDataVector.addElement(titleInfo);
                
                //break parsing current book
                throw new SAXException("\n The necessary data about the current book is extracted." 
                                      + "Complete parsing of the current book.");                
            }
            titleInfo = null;
                                         

        }       
         
    }

    
    /**
     * Get fb2BookDataVector
     *
     * @return
     */
    public synchronized Vector getFB2BookDataVector() {
        return fb2BookDataVector;
    }
    
    
    /**
     * Empty the data model
     *
     */
    public synchronized void removeAllElements() {
        fb2BookDataVector.removeAllElements();
    }

    /**
     * Return the number of elements fb2BookDataVector in the model
     *
     * @return
     */
    public synchronized int size() {
        return fb2BookDataVector.size();
    }

    /**
     * Return the TitleInfo object from fb2BookDataVector 
     * at the specified position by id
     *
     * @param i
     * @return
     */
    
    public synchronized TitleInfo getTitleInfoByIdAt(final int i) {
        return (TitleInfo) fb2BookDataVector.elementAt(i);
    }
    
   
    
    /*
    public synchronized TitleInfo elementTitleInfoAt(final int i) {
        return (TitleInfo) fb2BookDataVector.elementAt(i);
    }
    */ 
    
    // --- 1 --- Slower, but reliable way
   
    public synchronized TitleInfo getTitleInfo() {
        int i;
        for (i = 0; i < fb2BookDataVector.size(); i++) {
            if (fb2BookDataVector.elementAt(i).getClass().getName().equals("org.fbreader.formats.fb2.TitleInfo")){
                break;
            }
        }
        return (TitleInfo) fb2BookDataVector.elementAt(i);
    }
   


    /**
     * Copy the current list into a working array which can safely be used
     * outside of synchronized blocks. This guards against simultaneous changes
     * to the list on another thread.
     *
     * @param copy
     * @return
     */
    public final synchronized FB2Model[] copy(FB2Model[] copy) {
        if (copy == null || copy.length != size()) {
            copy = new FB2Model[size()];
        }
        fb2BookDataVector.copyInto(copy);

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
    public synchronized  FB2Model itemNextTo(final FB2Model item, final boolean before) {
        FB2Model adjacentItem = null;
        int i = fb2BookDataVector.indexOf(item);

        if (before) {
            if (i > 0) {
                adjacentItem = (FB2Model) fb2BookDataVector.elementAt(--i);
            }
        } else if (i < size() - 1) {
            adjacentItem = (FB2Model) fb2BookDataVector.elementAt(++i);
        }

        return adjacentItem;
    }
}
