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
import org.fbreader.views.model.parser.FeedBooksCatalogModel;
import org.fbreader.views.model.parser.FeedBooksCatalog;
import org.tantalum.Task;
import org.tantalum.net.HttpGetter;
import org.tantalum.Worker;


public final class NetLibFeedBooksOriginalCatalogViewList 
                    extends FancyCustomList 
                    implements View, CommandListener {

   private static NetLibFeedBooksOriginalCatalogViewList self = null;
   private Image imageListIcon;
   private final String FEEDBOOKS_ICON = 
            "/icons/netlib_feedbooks_38x38.png";
    private ViewMaster viewMaster;
    private static final String FEEDBOOKS_ORIGINAL_CATALOG_URL=
                        "http://www.feedbooks.com/original/catalog.atom";
    private static Command selectFeedBookOriginalCatalogListItemCmd;

    private final Alert alertLoadingFeedBooksData = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_CONTENT"), null, null);
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_CONTENT"), null, null);    

    private HttpGetter httpGetter;
    private String feedUrl;    

    private final FeedBooksCatalogModel feedBooksOriginalCatalogModel = new FeedBooksCatalogModel(10);
    
    FeedBooksCatalog selectedFeedBookOriginalCatalogListItem;
    
    private String viewTitleStr;


    public NetLibFeedBooksOriginalCatalogViewList(MIDlet parent) {
        super("");
        this.viewTitleStr=ViewMaster.getInstance().getNextViewTitle();
        viewMaster = ViewMaster.getInstance();
        selectFeedBookOriginalCatalogListItemCmd=viewMaster.getSelectListItemCmd();
        this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        try  {
               imageListIcon=ImageLoader.getInstance().loadImage(FEEDBOOKS_ICON, null);
        } 
        catch(IOException e) {
        } 
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectFeedBookOriginalCatalogListItemCmd);
        this.setCommandListener(this);          
        viewMaster.setFirsLoadFeedBooksOriginalCatalogList(true);
    }
    
  
    /**
     * @return NetLibFeedBooksOriginalCatalogViewList singleton
     */
    public static NetLibFeedBooksOriginalCatalogViewList getInstance() {
        if (self == null) {
            self = new NetLibFeedBooksOriginalCatalogViewList(Main.getInstance());
        }
        return self;
    }    
    
    
     /**
     * Activate  NetLibFeedBooksOriginalCatalogViewList 
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
        if (viewMaster.getFirsLoadFeedBooksOriginalCatalogList()) {
            reload();
        }         

    }

    

    /**
     * Deactivate  NetLibFeedBooksOriginalCatalogViewList 
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
        
        //display a "busy" screen
        final Gauge gauge;

        //Creates an alert with an indeterminate gauge
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
            //show CategoryBar 
            viewMaster.setCategoryBarVisible(true);            
            viewMaster.backView();
        }
        else if (command == selectFeedBookOriginalCatalogListItemCmd) {
//--- 1 --- choice selectedFeedBookOriginalListItem
            
            byte indexOriginalItem=(byte) this.getSelectedIndex();
            selectedFeedBookOriginalCatalogListItem = feedBooksOriginalCatalogModel.getFeedBooksCatalogByIdAt(indexOriginalItem);  
            if (indexOriginalItem >= 0 && indexOriginalItem <= 3) {
                //open NetLibFeedBooksOriginalViewList  (books list )
                viewMaster.setFirstLoadFeedBooksOriginalList(true);
                viewMaster.setSelectedFeedBookOriginalCatalogListItem(selectedFeedBookOriginalCatalogListItem);
                viewMaster.getNetLibFeedBooksOriginalViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_ORIGINAL_LIST);
            }
            else {
                //open NetLibFeedBooksPublicDomainCategoriesViewList (list of the category) 
                //For "category" item from "original book" section 
                //will use the same units as for (FeedBooks PublicDomain):
                //1.NetLibFeedBooksPublicDomainCategoriesViewList.java
                //2.NetLibFeedBooksPublicDomainFictionCategoriesViewList.java
                //3.NetLibFeedBooksPublicDomainFictionByCategoryViewList.java
                viewMaster.setSelectedFeedBookPublicDomainCatalogListItem(selectedFeedBookOriginalCatalogListItem);
                viewMaster.setFirsLoadFeedBooksPublicDomainCategoriesList(true);
                viewMaster.getNetLibFeedBooksPublicDomainCategoriesViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATEGORIES_LIST);                   
            }
            
        }        
    }
    
    /**
     * Don't use StaticWebCache.
     * For thread safety this is only called from the EDT
     * 
     */
    public void reload() {

       //show loading process window                         
       showCancelableAlert();
       feedUrl = FEEDBOOKS_ORIGINAL_CATALOG_URL;
        
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
                                   feedBooksOriginalCatalogModel.removeAllElements();
                                   synchronized (Worker.LARGE_MEMORY_MUTEX) {
                                       feedBooksOriginalCatalogModel.setXML((byte[]) in);
                                   }

                        } catch (Exception e) {
                                       //#debug
                                       Main.LOGGER.logError("Error parsing XML" + feedBooksOriginalCatalogModel.toString());
                        }

                        //hide loading process window 
                        Main.getInstance().switchDisplayable(null,viewMaster.getNetLibFeedBooksOriginalCatalogViewList()); 
                        final int feedBooksOriginalCatalogModelSize = feedBooksOriginalCatalogModel.size();

                        //check if the rssModelSize not empty
                        if (feedBooksOriginalCatalogModelSize!=0) {
                            //set FirsLoadFeedBooksPublicDomainMostPopularList flag to false
                            viewMaster.setFirsLoadFeedBooksOriginalCatalogList(false);                      

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
            httpGetter.fork(); // Start task(downloading book) on a background Worker thread        
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
        
        final int  feedBooksOriginalCatalogModelSize = feedBooksOriginalCatalogModel.size();
        for (int i = 0; i < feedBooksOriginalCatalogModelSize; i++) {
            addFeedBooksOriginalCatalogListItem(feedBooksOriginalCatalogModel.getFeedBooksCatalogByIdAt(i));
        }  
    }

    public void addFeedBooksOriginalCatalogListItem(final FeedBooksCatalog feedBooksOriginalCatalogListItem) {

        String feedBooksOriginalCatalogListItemTitle=feedBooksOriginalCatalogListItem.getTitle().toLowerCase();
        String feedBooksOriginalCatalogListItemSubtitle=feedBooksOriginalCatalogListItem.getContent().toLowerCase();
        
        this.append(feedBooksOriginalCatalogListItemTitle,feedBooksOriginalCatalogListItemSubtitle,
                    "",imageListIcon,FancyElement.IMPORTANCE_NONE);
    }
}
