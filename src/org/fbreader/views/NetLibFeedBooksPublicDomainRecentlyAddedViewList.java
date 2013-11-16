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
import org.fbreader.models.BookInNetLib;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.views.model.parser.FeedBooksEbookModel;
import org.fbreader.views.model.parser.FeedBooksEbook;
import org.tantalum.Worker;
import org.tantalum.Task;
import org.tantalum.net.HttpGetter;

public final class NetLibFeedBooksPublicDomainRecentlyAddedViewList 
                    extends FancyCustomList 
                    implements View, CommandListener {

    private static NetLibFeedBooksPublicDomainRecentlyAddedViewList self = null;
    
    private Image imageListIcon;
    
    private final String BOOKS_ICON = 
            "/icons/book_placeholder_38x38.png";
    
    private ViewMaster viewMaster;
    
    private static Command selectBookItemCmd;
    
    private final Alert alertLoadingFeedBooksData = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_CONTENT"), null, null);
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_CONTENT"), null, null);
    
    private static BookInNetLib selectedBookForDownload;
    
    private HttpGetter httpGetter;
    private String feedUrl; 

    private final FeedBooksEbookModel feedBooksPublicDomainRecentlyAddedModel = new FeedBooksEbookModel(100);

    private String viewTitleStr;
    

    public NetLibFeedBooksPublicDomainRecentlyAddedViewList(MIDlet parent) {
         super("");
         this.viewTitleStr=ViewMaster.getInstance().getNextViewTitle();
         viewMaster = ViewMaster.getInstance();
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
        viewMaster.setFirsLoadFeedBooksPublicDomainRecentlyAddedList(true);
   }
    
    
    /**
     * @return NetLibFeedBooksPublicDomainRecentlyAddedViewList singleton
     */
    public static NetLibFeedBooksPublicDomainRecentlyAddedViewList getInstance() {
        if (self == null) {
            self = new NetLibFeedBooksPublicDomainRecentlyAddedViewList(Main.getInstance());
        }
        return self;
    }    
    
    
     /**
     * Activate NetLibFeedBooksPublicDomainRecentlyAddedViewList
     */
    public void activate() {
        
        //set View Title
        this.setTitle(this.viewTitleStr);         
        
        //hide CategoryBar 
        viewMaster.setCategoryBarVisible(false);
        
        //Runs the garbage collector
        System.gc(); 
        
        //StaticWebCache not use.
        //Don't repeat requests to the server
        //every time to open(activate) this view
        if (viewMaster.getFirsLoadFeedBooksPublicDomainRecentlyAddedList()) {
            reload();
        }
        else if (feedBooksPublicDomainRecentlyAddedModel.size()!=0) {
            /** 
            * Scroll to show the element with index 0.
            * 
            * @param elementNum 
            */
             this.scrollToElement(0);             
        }         
    }
    

    /**
     * Deactivate NetLibFeedBooksPublicDomainRecentlyAddedViewList
     * 
     */
    public void deactivate() {
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
            httpGetter.cancel(true);
        }          
        else if (command == viewMaster.getBackCmd()){
            viewMaster.backView();
        }
        else if (command == selectBookItemCmd) {
//--- 1 ---- create new bookInNetLib object
            selectedBookForDownload = new BookInNetLib();
//--- 2 --- 
            FeedBooksEbook selectedFeedBooksPublicDomainRecentlyAddedListItem=                   
                           feedBooksPublicDomainRecentlyAddedModel.getFeedBooksEbookByIdAt(this.getSelectedIndex());
            selectedBookForDownload.setBookTitle(selectedFeedBooksPublicDomainRecentlyAddedListItem.getTitle());
            selectedBookForDownload.setBookAuthor(selectedFeedBooksPublicDomainRecentlyAddedListItem.getName());
            selectedBookForDownload.setBookTags(selectedFeedBooksPublicDomainRecentlyAddedListItem.getCategory());
            selectedBookForDownload.setBookLang(selectedFeedBooksPublicDomainRecentlyAddedListItem.getLanguage());
            String bookURL=selectedFeedBooksPublicDomainRecentlyAddedListItem.getLink();
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
     * Don't use StaticWebCache.
     * For thread safety this is only called from the EDT
     */
    public void reload() {
        
        //show loading process window                         
        showCancelableAlert();
                
        feedUrl = viewMaster.getSelectedFeedBookPublicDomainCatalogListItem().getId();                
        
        //Sending a request to the server
        httpGetter = new HttpGetter(feedUrl);
        
        // Add a task which will run after the HTTP GET
        httpGetter.chain(
             new Task() {
                public Object doInBackground(final Object in) { 
                    // Perform an operation on byte[] “in” received from the net 
                    // This is called from a background Worker thread
                   if (!httpGetter.isCanceled()) {
                        try {
                                   feedBooksPublicDomainRecentlyAddedModel.removeAllElements();

                                   synchronized (Worker.LARGE_MEMORY_MUTEX) {
                                       feedBooksPublicDomainRecentlyAddedModel.setXML((byte[]) in);
                                   }
                        } catch (Exception e) {
                                       //#debug
                                       Main.LOGGER.logError("Error parsing XML" + feedBooksPublicDomainRecentlyAddedModel.toString());
                        }

                        //hide loading process window 
                        Main.getInstance().switchDisplayable(null,viewMaster.getNetLibFeedBooksPublicDomainRecentlyAddedViewList());
                        final int feedBooksPublicDomainCatalogModelSize = feedBooksPublicDomainRecentlyAddedModel.size();

                        //check if the rssModelSize not empty
                        if (feedBooksPublicDomainCatalogModelSize!=0) {
                            //set FirsLoadFeedBooksPublicDomainMostPopularList flag to false
                            viewMaster.setFirsLoadFeedBooksPublicDomainRecentlyAddedList(false);                          

                            //create books list 
                            paint();
                        }
                        else {
                          showNetworkErrorAlert();  
                        }
                        
                    }
                  return in; 
               } 
               protected void onCancelled() { 
                   // On the UI thread, update the screen when the operation fails 
                  showNetworkErrorAlert();                    
               }
               public boolean cancel(final boolean mayInterruptIfNeeded) {
                    viewMaster.backView(); 
                    return false;
                }               
             }
            );
            httpGetter.setRetriesRemaining(0);
            httpGetter.fork(); // Start task on a background Worker thread        
   
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

        final int  feedBooksPublicDomainRecentlyAddedModelSize = feedBooksPublicDomainRecentlyAddedModel.size();
        for (int i = 0; i < feedBooksPublicDomainRecentlyAddedModelSize; i++) {
            addFeedBooksPublicDomainRecentlyAddedListItem(feedBooksPublicDomainRecentlyAddedModel.getFeedBooksEbookByIdAt(i));
        }
        
       /** 
       * Scroll to show the element with index 0.
       * 
       * @param elementNum 
       */
        this.scrollToElement(0);          
    }

    public void addFeedBooksPublicDomainRecentlyAddedListItem(final FeedBooksEbook feedBooksPublicDomainRecentlyAddedListItem) {
        final String bookTitle=feedBooksPublicDomainRecentlyAddedListItem.getTitle().toLowerCase();
        final String bookAuthor=feedBooksPublicDomainRecentlyAddedListItem.getName().toLowerCase();
        this.append(bookTitle,bookAuthor,"",imageListIcon,FancyElement.IMPORTANCE_NONE);
    }

}
