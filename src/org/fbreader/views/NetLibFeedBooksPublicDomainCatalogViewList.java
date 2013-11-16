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
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.views.model.parser.FeedBooksCatalogModel;
import org.fbreader.views.model.parser.FeedBooksCatalog;
import org.fbreader.uihelpers.IElement;

import org.tantalum.Worker;
import org.tantalum.Task;
import org.tantalum.net.HttpGetter;

public final class NetLibFeedBooksPublicDomainCatalogViewList 
                    extends FancyCustomList 
                    implements View, CommandListener {

    private static NetLibFeedBooksPublicDomainCatalogViewList self = null;
    
    private Image imageListIcon;
    
  
   private final String FEEDBOOKS_ICON = 
            "/icons/netlib_feedbooks_38x38.png";
    
    private ViewMaster viewMaster;
    private NetLibFeedBooksViewList netLibFeedBooksViewList;
    private static final String FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_URL=
                        "http://www.feedbooks.com/publicdomain/catalog.atom";
    
    private static Command selectFeedBookPublicDomainCatalogListItemCmd;
    
    private final Alert alertLoadingFeedBooksData = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_CONTENT"), null, null);
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_ALERT_NET_ERROR_CONTENT"), null, null);

    private HttpGetter httpGetter;
    private String feedUrl;      

    private final FeedBooksCatalogModel feedBooksPublicDomainCatalogModel = new FeedBooksCatalogModel(10);
    
    FeedBooksCatalog selectedFeedBookPublicDomainCatalogListItem;
    
    private String viewTitleStr;

    public NetLibFeedBooksPublicDomainCatalogViewList(MIDlet parent) {
        super("");
        this.viewTitleStr=ViewMaster.getInstance().getNextViewTitle();
        viewMaster = ViewMaster.getInstance();
        netLibFeedBooksViewList=NetLibFeedBooksViewList.getInstance();
        selectFeedBookPublicDomainCatalogListItemCmd=viewMaster.getSelectListItemCmd();
 
        this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        try  {
               imageListIcon=ImageLoader.getInstance().loadImage(FEEDBOOKS_ICON, null);
        } 
        catch(IOException e) {
        } 

        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectFeedBookPublicDomainCatalogListItemCmd);
        this.setCommandListener(this);          
        viewMaster.setFirsLoadFeedBooksPublicDomainCatalogList(true);
    }
    
    
    /**
     * @return NetLibFeedBooksPublicDomainCatalogViewList singleton
     */
    public static NetLibFeedBooksPublicDomainCatalogViewList getInstance() {
        if (self == null) {
            self = new NetLibFeedBooksPublicDomainCatalogViewList(Main.getInstance());
        }
        return self;
    }    
    
    
     /**
     * Activate NetLibFeedBooksPublicDomainCatalogViewList
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
        if (viewMaster.getFirsLoadFeedBooksPublicDomainCatalogList()) {
            reload();
        }

    }
    

    /**
     * Deactivate NetLibFeedBooksPublicDomainCatalogViewList
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
        else if (command == selectFeedBookPublicDomainCatalogListItemCmd) {
//--- 1 --- choice selectedFeedBookPublicDomainListItem
            selectedFeedBookPublicDomainCatalogListItem = feedBooksPublicDomainCatalogModel.getFeedBooksCatalogByIdAt(this.getSelectedIndex());  
            viewMaster.setSelectedFeedBookPublicDomainCatalogListItem(selectedFeedBookPublicDomainCatalogListItem);
            viewMaster.setNextViewTitle(((IElement)this.elements.elementAt(getSelectedIndex())).getStringPart());
            
            //most popular
            if (this.getSelectedIndex()==0) {
                viewMaster.getNetLibFeedBooksPublicDomainMostPopularViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_PUBLIC_DOMAIN_MOST_POPULAR_LIST);                
            }
            //recently added
            else if (this.getSelectedIndex()==1) {
                viewMaster.getNetLibFeedBooksPublicDomainRecentlyAddedViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_PUBLIC_DOMAIN_RECENTLY_ADDED_LIST);   
            //categories    
            }
            else if (this.getSelectedIndex()==2) {
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
     * 
     */
    public void reload() {
        
       //show loading process window                         
       showCancelableAlert();
       feedUrl = FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_URL;

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
                                   feedBooksPublicDomainCatalogModel.removeAllElements();

                                   synchronized (Worker.LARGE_MEMORY_MUTEX) {
                                       feedBooksPublicDomainCatalogModel.setXML((byte[]) in);
                                   }
                                   //return feedBooksPublicDomainFictionByCategoryModel;
                        } catch (Exception e) {
                                       //#debug
                                       Main.LOGGER.logError("Error parsing XML" + feedBooksPublicDomainCatalogModel.toString());
                                       //return null;
                        }

                        //hide loading process window (alertLoadingNewTitles box)
                        Main.getInstance().switchDisplayable(null,viewMaster.getNetLibFeedBooksPublicDomainCatalogViewList()); 
                        

                        final int feedBooksPublicDomainCatalogModelSize = feedBooksPublicDomainCatalogModel.size();

                        //check if the rssModelSize not empty
                        if (feedBooksPublicDomainCatalogModelSize!=0) {
                            //set FirsLoadFeedBooksPublicDomainCatalogList flag to false
                            viewMaster.setFirsLoadFeedBooksPublicDomainCatalogList(false);                           

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
        final int  feedBooksPublicDomainCatalogModelSize = feedBooksPublicDomainCatalogModel.size();
        for (int i = 0; i < feedBooksPublicDomainCatalogModelSize; i++) {
            addFeedBooksPublicDomainCatalogListItem(feedBooksPublicDomainCatalogModel.getFeedBooksCatalogByIdAt(i));
        }         
    }

    public void addFeedBooksPublicDomainCatalogListItem(final FeedBooksCatalog feedBooksPublicDomainCatalogListItem) {

        String feedBooksPublicDomainCatalogListItemTitle=feedBooksPublicDomainCatalogListItem.getTitle().toLowerCase();
        String feedBooksPublicDomainCatalogListItemSubtitle=feedBooksPublicDomainCatalogListItem.getContent().toLowerCase();
        
        this.append(feedBooksPublicDomainCatalogListItemTitle,feedBooksPublicDomainCatalogListItemSubtitle,
                    "",imageListIcon,FancyElement.IMPORTANCE_NONE);
    }
}
