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
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import org.fbreader.Main;
import org.fbreader.localization.L10n;
import org.fbreader.utils.ImageLoader;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.views.model.parser.FeedBooksEbook;
import org.fbreader.views.model.parser.FeedBooksEbookModel;
import org.fbreader.models.BookInNetLib;

import org.tantalum.UITask;
import org.tantalum.Workable;
import org.tantalum.Worker;
import org.tantalum.net.StaticWebCache;
import org.tantalum.storage.DataTypeHandler;
import org.tantalum.util.L;
import org.tantalum.net.HttpGetter;

public final class NetLibFeedBooksPublicDomainFictionByCategoryViewList 
                    extends FancyCustomList 
                    implements View, CommandListener {
    private Image imageListIcon;

    private final String BOOKS_ICON = 
            "/icons/book_placeholder_38x38.png";
    private ViewMaster viewMaster;
    private static Command selectFeedBookPublicDomainFictionByCategoryListItemCmd;
    
    private final Alert alertLoadingFeedBooksData = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_CONTENT"), null, null);
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_CONTENT"), null, null);
    
    
    private final StaticWebCache feedCache = new StaticWebCache('3', new DataTypeHandler() {
        public Object convertToUseForm(final byte[] bytes) {
            try {
                feedBooksPublicDomainFictionByCategoryModel.removeAllElements();
                synchronized (Worker.LARGE_MEMORY_MUTEX) {
                    feedBooksPublicDomainFictionByCategoryModel.setXML(bytes);
                }
                return feedBooksPublicDomainFictionByCategoryModel;
            } catch (Exception e) {
                //#debug
                Main.LOGGER.logError("Error parsing XML" + feedBooksPublicDomainFictionByCategoryModel.toString());
                return null;
            }
        }
    });    

    private static BookInNetLib selectedBookForDownload;
    private final FeedBooksEbookModel feedBooksPublicDomainFictionByCategoryModel = new FeedBooksEbookModel(100);
    private String feedUrl;
    private UITask uiTask;
    private boolean pressCancelButton;

    public NetLibFeedBooksPublicDomainFictionByCategoryViewList(MIDlet parent) {
         super("");
         viewMaster = ViewMaster.getInstance();
         selectFeedBookPublicDomainFictionByCategoryListItemCmd=viewMaster.getSelectListItemCmd();
 
        this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        try  {
               imageListIcon=ImageLoader.getInstance().loadImage(BOOKS_ICON, null);
        } 
        catch(IOException e) {
        } 

        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectFeedBookPublicDomainFictionByCategoryListItemCmd);
        this.setCommandListener(this);          
    }
    
     /**
     * Activate NetLibFeedBooksPublicDomainFictionByCategoryViewList
     */
    public void activate() {
        
        //when activate current view set pressCancelButton to false;
        pressCancelButton=false;
        
        //set View Title
        this.setTitle(viewMaster.getSelectedFeedBookPublicDomainFictionCategoriesListItem().getTitle().toLowerCase());
        
        
        //hide CategoryBar 
        viewMaster.setCategoryBarVisible(false);
        
        
        //Runs the garbage collector
        System.gc(); 
       
        // Use StaticWebCache  
        if (viewMaster.getFirstLoadFeedBooksPublicDomainFictionByCategoryList()) {
            showCancelableAlert();
            clearCache();
            reload(true);
        }
        else {
            reload(false);
        }
    }
    
    /**
     * Deactivate NetLibFeedBooksPublicDomainFictionByCategoryViewList
     * 
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
        alertLoadingFeedBooksData.setIndicator(gauge);            
        alertLoadingFeedBooksData.setTimeout(Alert.FOREVER);  
        alertLoadingFeedBooksData.addCommand(viewMaster.getCancelCmd());
        alertLoadingFeedBooksData.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertLoadingFeedBooksData,this);         
    }    

    public void commandAction(final Command command, final Displayable displayable) {
        
        if (displayable==alertNetworkError && command==viewMaster.getDismissCmd()) {
            Main.getInstance().switchDisplayable(null,this);
            viewMaster.backView();
        }
        else if (displayable==alertLoadingFeedBooksData && command == viewMaster.getCancelCmd()) {
            pressCancelButton=true;
            uiTask.cancel(true);
        }         
        else if (command == viewMaster.getBackCmd()){
            viewMaster.backView();
        }
        else if (command == selectFeedBookPublicDomainFictionByCategoryListItemCmd) {
//--- 1 ---- create new bookInNetLib object
            selectedBookForDownload = new BookInNetLib();
//--- 2 --- 
            FeedBooksEbook selectedFeedBookPublicDomainFictionByCategoryListItem = 
                           feedBooksPublicDomainFictionByCategoryModel.getFeedBooksEbookByIdAt(this.getSelectedIndex());              
            selectedBookForDownload.setBookTitle(selectedFeedBookPublicDomainFictionByCategoryListItem.getTitle());
            selectedBookForDownload.setBookAuthor(selectedFeedBookPublicDomainFictionByCategoryListItem.getName());
            selectedBookForDownload.setBookTags(selectedFeedBookPublicDomainFictionByCategoryListItem.getCategory());
            selectedBookForDownload.setBookLang(selectedFeedBookPublicDomainFictionByCategoryListItem.getLanguage());

            String bookURL=selectedFeedBookPublicDomainFictionByCategoryListItem.getLink();
            selectedBookForDownload.setDownloadBookURL(bookURL);
            
            int downloadBookFileNameStartIndex=bookURL.lastIndexOf('/')+1;
            int downloadBookFileNameEndIndex=bookURL.length();

            String downloadBookFileName=bookURL.substring(downloadBookFileNameStartIndex,
                                   downloadBookFileNameEndIndex);
            selectedBookForDownload.setDownloadBookFileName(downloadBookFileName);
            viewMaster.setSelectedBookForDownload(selectedBookForDownload);
            viewMaster.getNetLibBookInfoViewForm();
            viewMaster.openView(ViewMaster.VIEW_NET_LIB_BOOK_INFO_FORM);
        }        
        
    }
    
    
    /**
     * Use StaticWebCache
     * For thread safety this is only called from the EDT
     *
     * @param forceNetLoad
     */
   
    public UITask reload(final boolean forceNetLoad) {
        feedUrl = viewMaster.getSelectedFeedBookPublicDomainFictionCategoriesListItem().getId();
        if (forceNetLoad) {
            uiTask = new UITask() {
                public void onPostExecute(final Object result) {
                    final int feedBooksPublicDomainFictionByCategoryModelSize = feedBooksPublicDomainFictionByCategoryModel.size();

                    //hide loading process window 
                    Main.getInstance().switchDisplayable(null,viewMaster.getNetLibFeedBooksPublicDomainFictionByCategoryViewList());

                    //check if the feedBooksPublicDomainCategoriesModelSize not empty
                    if (feedBooksPublicDomainFictionByCategoryModelSize!=0) {
                        //set FirstLoadFeedBooksPublicDomainFictionByCategoryList flag to false
                        viewMaster.setFirstLoadFeedBooksPublicDomainFictionByCategoryList(false);  

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
                    final int feedBooksPublicDomainFictionByCategoryModelSize = feedBooksPublicDomainFictionByCategoryModel.size();                    
                    //check if the rssModelSize not empty
                    if (feedBooksPublicDomainFictionByCategoryModelSize!=0) {
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
        final int feedBooksPublicDomainFictionByCategoryModelSize = feedBooksPublicDomainFictionByCategoryModel.size();
        for (int i = 0; i < feedBooksPublicDomainFictionByCategoryModelSize; i++) {
            addFeedBooksPublicDomainFictionByCategoryListItem(feedBooksPublicDomainFictionByCategoryModel.getFeedBooksEbookByIdAt(i));
        }
       /** 
       * Scroll to show the element with index 0.
       * 
       * @param elementNum 
       */
        this.scrollToElement(0);         
    }

    public void addFeedBooksPublicDomainFictionByCategoryListItem(final FeedBooksEbook feedBooksPublicDomainFictionByCategoryListItem) {

        final String bookTitle=feedBooksPublicDomainFictionByCategoryListItem.getTitle().toLowerCase();
        final String bookAuthor=feedBooksPublicDomainFictionByCategoryListItem.getName().toLowerCase();
        
        this.append(bookTitle,bookAuthor,"",imageListIcon,FancyElement.IMPORTANCE_NONE);        
    }
}
