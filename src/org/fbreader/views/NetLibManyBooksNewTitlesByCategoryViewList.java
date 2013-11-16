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
 * 
 * 
 * @author vand
 * 
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

*    Changes and additions to the author's code:
*   --------------------------------------------
*   The multiple changes and optimization logic 
*   to implement tasks processing of the Network libraries 
*   in FBReader for S40FT
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.views;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import org.fbreader.Main;
import org.fbreader.localization.L10n;
import org.fbreader.utils.ImageLoader;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.models.BookInNetLib;
import org.tantalum.UITask;
import org.tantalum.Workable;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.net.xml.RSSItem;
import org.tantalum.net.xml.RSSModel;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.util.L;


public final class NetLibManyBooksNewTitlesByCategoryViewList 
                    extends FancyCustomList 
                    implements View, CommandListener {

    private static NetLibManyBooksNewTitlesByCategoryViewList self = null;
    private Image imageListIcon;
    private final String BOOKS_ICON = "/icons/book_placeholder_38x38.png";
    private ViewMaster viewMaster;
    private MyLibUtils myLibUtils;
    private static Command selectBookItemCmd;
    private static Command reloadCmd;
    
    private final Alert alertLoadingNewTitlesByCategory = new Alert(L10n.getMessage("MANYBOOKS_NEW_TITLES_ALERT_TITLE"), 
            L10n.getMessage("MANYBOOKS_NEW_TITLES_ALERT_CONTENT"), null, null);
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("MANYBOOKS_NEW_TITLES_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("MANYBOOKS_NEW_TITLES_ALERT_NET_ERROR_CONTENT"), null, null);

    private static BookInNetLib selectedBookForDownload;
    private UITask uiTask;
    private boolean pressCancelButton;
    
    private final StaticWebCache feedCache = new StaticWebCache('0', new DataTypeHandler() {
        public Object convertToUseForm(final byte[] bytes) {
            try {
                rssModel.removeAllElements();
                synchronized (Worker.LARGE_MEMORY_MUTEX) {
                    rssModel.setXML(bytes);
                }
                return rssModel;
            } catch (Exception e) {
                //#debug
                Main.LOGGER.logError("Error parsing XML" + rssModel.toString());
                return null;
            }
        }
    });
    private final RSSModel rssModel = new RSSModel(100);
    private Vector netLibManyBooksNewTitlesByCategoryDataVector=null;

    public NetLibManyBooksNewTitlesByCategoryViewList(MIDlet parent) {
         super("");
         viewMaster = ViewMaster.getInstance();
         myLibUtils=MyLibUtils.getInstance();
         selectBookItemCmd=viewMaster.getSelectListItemCmd();
 
        this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        try  {
               imageListIcon=ImageLoader.getInstance().loadImage(BOOKS_ICON, null);
        } 
        catch(IOException e) {
        } 

        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectBookItemCmd);
        this.setCommandListener(this);          
    }
    
    
    /**
     * @return NetLibManyBooksNewTitlesByCategoryViewList singleton
     */
    public static NetLibManyBooksNewTitlesByCategoryViewList getInstance() {
        if (self == null) {
            self = new NetLibManyBooksNewTitlesByCategoryViewList(Main.getInstance());
        }
        return self;
    }    
    
    
     /**
     * Activate NetLibManyBooksNewTitlesByCategoryViewList
     */
    public void activate() {
        //when activate current view set pressCancelButton to false;
        pressCancelButton=false;

        //hide CategoryBar 
        viewMaster.setCategoryBarVisible(false);

        //Runs the garbage collector
        System.gc();   
        
        //Use StaticWebCache        
        if (viewMaster.getFirsLoadManyBooksNewTitlesByCategoryList()) {
            this.setTitle(viewMaster.getSelectedManyBooksCategoryListItem().getCategoryName());
            showCancelableAlert();
            clearCache();
            reload(true);
        }
        else {
            reload(false);
        }        
    }
    

    /**
     * Deactivate NetLibManyBooksNewTitlesViewList
     * Empties the List items
     */
    public void deactivate() {
    }
    
  
    protected final void clearCache() {
        Worker.fork(new Workable() {
            public Object exec(final Object in) {
                try {
                    doClearCache();
                } catch (Exception e) {
                    //#debug
                    L.e("Can not clear cache", "", e);
                }

                return in;
            }
        }, Worker.HIGH_PRIORITY);
    }

    protected void doClearCache() {
        feedCache.clear();
    }    
 
    
    
    public void showNetworkErrorAlert() {
       alertNetworkError.setTimeout(Alert.FOREVER); 
       alertNetworkError.addCommand(viewMaster.getDismissCmd());
       alertNetworkError.setCommandListener(this);
       Main.getInstance().switchDisplayable(alertNetworkError,this);
    }
    
    public void showCancelableAlert() {
        // display a "busy" screen
        final Gauge gauge;

        // Creates an alert with an indeterminate gauge
        gauge = new Gauge(null, false, Gauge.INDEFINITE,
                Gauge.CONTINUOUS_RUNNING);
        alertLoadingNewTitlesByCategory.setIndicator(gauge);            
        alertLoadingNewTitlesByCategory.setTimeout(Alert.FOREVER);  
        alertLoadingNewTitlesByCategory.addCommand(viewMaster.getCancelCmd());
        alertLoadingNewTitlesByCategory.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertLoadingNewTitlesByCategory,this);         
    }    

    public void commandAction(final Command command, final Displayable displayable) {
        
        if (displayable==alertNetworkError && command==viewMaster.getDismissCmd()) {
            Main.getInstance().switchDisplayable(null,this);
            viewMaster.backView();
        }
        else if (displayable==alertLoadingNewTitlesByCategory && command == viewMaster.getCancelCmd()) {
            pressCancelButton=true;
            uiTask.cancel(true);
        }        
        else if (command == viewMaster.getBackCmd()){
            viewMaster.backView();
        }
        else if (command == selectBookItemCmd) {
//--- 1 --- choice selectedBook
            selectedBookForDownload = (BookInNetLib) netLibManyBooksNewTitlesByCategoryDataVector.elementAt(this.getSelectedIndex());  
            viewMaster.setSelectedBookForDownload(selectedBookForDownload);
            viewMaster.getNetLibBookInfoViewForm();
            viewMaster.openView(ViewMaster.VIEW_NET_LIB_BOOK_INFO_FORM);
        }        
    }
    
    
    /**
     * If use StaticWebCache.
     * For thread safety this is only called from the EDT
     *
     * @param forceNetLoad
     */
    
   
    public UITask reload(final boolean forceNetLoad) {
        
        //work, but initialization above in activate()
        String feedUrl = viewMaster.getSelectedManyBooksCategoryListItem().getFileUrl();

        if (forceNetLoad) {
            uiTask = new UITask() {
                public void onPostExecute(final Object result) {
                        
                            final int rssModelSize = rssModel.size();

                           //hide loading process window 
                           Main.getInstance().switchDisplayable(null,viewMaster.getNetLibManyBooksNewTitlesByCategoryViewList()); 

                           //check if the rssModelSize not empty
                           if (rssModelSize!=0) {
                               //set FirsLoadManyBooksNewTitlesList flag to false
                               viewMaster.setFirsLoadManyBooksNewTitlesByCategoryList(false);                        

                               //create books list 
                               paint();
                           }
                           else {
                             showNetworkErrorAlert();  
                           }                        

                }

                protected void onCanceled() {
                    if (pressCancelButton) {
                       viewMaster.backView(); 
                    }
                    else {
                      showNetworkErrorAlert();  
                    }
                }
            };
            feedCache.get(feedUrl, Worker.HIGH_PRIORITY, StaticWebCache.GET_WEB, uiTask);
        } else {
            uiTask = new UITask() {
                public void onPostExecute(final Object result) {
                    final int rssModelSize = rssModel.size();
                    //check if the rssModelSize not empty
                    if (rssModelSize!=0) {
                        //create books list 
                        paint();
                    }
                    else {
                      showNetworkErrorAlert();  
                    }                    
                   
                }
                
                protected void onCanceled() {
                    showNetworkErrorAlert();
                }                
            };
            feedCache.get(feedUrl, Worker.HIGH_PRIORITY, StaticWebCache.GET_ANYWHERE, uiTask);
        }
        return uiTask;
    }

    
    /**
     * Update the display
     *
     * Only call this from the UI thread
     */
    private void paint() {
        //delete NetLibManyBooksNewTitlesViewList
        for (int i=this.size()-1; i >= 0; i--){
            this.delete(i);
        }        
        if (netLibManyBooksNewTitlesByCategoryDataVector!=null) {
            netLibManyBooksNewTitlesByCategoryDataVector.removeAllElements();
        }
        else {
             netLibManyBooksNewTitlesByCategoryDataVector=new Vector();
        }     

        final int  rssModelSize = rssModel.size();
        for (int i = 0; i < rssModelSize; i++) {
                addNewTitlesListItem((RSSItem) rssModel.elementAt(i));
        }
        
       /** 
       * Scroll to show the element with index 0.
       * 
       * @param elementNum 
       */
        this.scrollToElement(0);
    }

    public void addNewTitlesListItem(final RSSItem rssItem) {

//--- 1 ---- create new bookInMyLibRMS for write in myLibRMSDataVector
        BookInNetLib bookInNetLib = new BookInNetLib();        
    
    
//--- 2 --- get bookTitle 
        final String bookTitle=rssItem.getTitle().toLowerCase();

        //bookInMyLibRMS field
        bookInNetLib.setBookTitle(bookTitle);

        
//--- 3 --- get bookDescription 
        final String bookDescription=rssItem.getDescription();
        
//--- 3.1 get bookAuthor from bookDescription        
        //get bookAuthor substring from bookDescription string
        String authorSubStr="Author:";
        
        int bookAuthorStartIndex=bookDescription.indexOf(authorSubStr)+authorSubStr.length();
        
        int bookAuthorEndIndex=bookDescription.indexOf("</p>",bookAuthorStartIndex);
        
        final String bookAuthor=bookDescription.substring(bookAuthorStartIndex,bookAuthorEndIndex).toLowerCase();
        
        //bookInMyLibRMS field
        bookInNetLib.setBookAuthor(bookAuthor);
        
        
//--- 3.2  get bookLang from bookDescription        

        //get bookAuthor substring from bookDescription string
        String langSubStr="Language:";        
        
        int bookLangStartIndex=bookDescription.indexOf(langSubStr)+langSubStr.length();
        
        int bookLangEndIndex=bookDescription.indexOf("</p>",bookLangStartIndex);
        
        final String bookLang=bookDescription.substring(bookLangStartIndex,bookLangEndIndex).toLowerCase();
        
        //bookInMyLibRMS field
        bookInNetLib.setBookLang(bookLang);
        
//--- 4 --- get bookURL
        final String bookURL=rssItem.getLink();

//---4.1 --- create downloadBookURL from bookURL
        //for ePub
        String downloadBookURLPrefix="http://manybooks.net/_scripts/send.php?tid=";
        
        int downloadBookURLBaseStartIndex=bookURL.lastIndexOf('/')+1;  
        int downloadBookURLBaseEndIndex=bookURL.lastIndexOf('.');
        
        //e.g. breckenridgeg3957439574-8 from http://manybooks.net/titles/breckenridgeg3957439574-8.html 
        String downloadBookURLBase=bookURL.substring(downloadBookURLBaseStartIndex,
                                   downloadBookURLBaseEndIndex); 
        
        //ePub format     
        String downloadBookURLSuffix="&book=1:epub:.epub:epub";
        
        //e.g. http://manybooks.net/_scripts/send.php?tid=breckenridgeg3957439574-8&book=1:epub:.epub:epub
        String downloadBookURL=downloadBookURLPrefix+downloadBookURLBase+downloadBookURLSuffix;

        //bookInMyLibRMS field
        bookInNetLib.setDownloadBookURL(downloadBookURL);


//---4.2 --- create downloadBookFileName from bookURL for ePub format
        //e.g. breckenridgeg3957439574-8.epub
        String downloadBookFileName=downloadBookURLBase+".epub";
        
        //bookInMyLibRMS field
        bookInNetLib.setDownloadBookFileName(downloadBookFileName);        
        
        
//--- 5 --- set bookTags as "unknown tag" for manybooks.net 
        
        //set bookTags as "category" from list of the categories in 
        //NetLibManyBooksCategoriesViewList.java  
        final String bookTags=viewMaster.getSelectedManyBooksCategoryListItem().getCategoryName();

        //bookInMyLibRMS field
        bookInNetLib.setBookTags(bookTags);        
        
        
//--- 6 --- add bookInMyLibRMS data in netLibManyBooksNewTitlesDataVector
        netLibManyBooksNewTitlesByCategoryDataVector.addElement(bookInNetLib);

//--- 7 --- add new item in addNewTitlesListItem        
        this.append(bookTitle,bookAuthor,"",imageListIcon,FancyElement.IMPORTANCE_NONE);

    }

}
