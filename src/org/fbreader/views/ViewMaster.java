/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
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

* Changes and additions to the author's code:
* --------------------------------------------
* 1.Numerous changes in the source code 
* 2.Adopted for FBReader for S40FT project
*    
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.views;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import java.io.IOException;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Gauge;
import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.ElementListener;
import com.nokia.mid.ui.IconCommand;
import org.fbreader.Main;
import org.fbreader.utils.ImageLoader;
import org.fbreader.utils.ViewStack;
import org.fbreader.views.model.parser.FeedBooksCatalog;
import org.fbreader.views.model.parser.ManyBooksCategory;
import org.fbreader.localization.L10n;
import org.fbreader.models.BookInNetLib;

/**
 * Singleton class for handling application views. 
 * 1. Handles view switching
 * 2. Base Commands set for all views
 * 3. Show simple Alerts
 * etc.
 * 
 */
public class ViewMaster
        implements ElementListener  {

    public static final int VIEW_NOVIEW = -1;
    public static final int VIEW_INFO_CATEGORY = 0;
    public static final int VIEW_MY_LIB_CATEGORY = 1;
    public static final int VIEW_NET_LIB_CATEGORY = 2;
    public static final int VIEW_SETTINGS_CATEGORY = 3;
    
    public static final int VIEW_ABOUT_FORM = 4;
    public static final int VIEW_LICENSE_FORM = 5;
    
    public static final int VIEW_MY_LIB_LIST = 6;
    public static final int VIEW_MY_LIB_LIST_HELP_FORM = 7;
    public static final int VIEW_MY_LIB_AUTHORS_LIST = 8;
    public static final int VIEW_MY_LIB_AUTHORS_LIST_HELP_FORM = 9;
    public static final int VIEW_MY_LIB_TAGS_LIST = 10;
    public static final int VIEW_MY_LIB_TAGS_LIST_HELP_FORM = 11;
    public static final int VIEW_MY_LIB_RECENT_BOOKS_LIST = 12;
    public static final int VIEW_MY_LIB_RECENT_BOOKS_LIST_HELP_FORM = 13;
    public static final int VIEW_MY_LIB_BOOKS_BY_AUTHOR_LIST = 14;
    public static final int VIEW_MY_LIB_BOOKS_BY_TAG_LIST = 15;
    public static final int VIEW_MY_LIB_BOOK_INFO_FORM = 16;
    
    public static final int VIEW_BOOK_CANVAS = 17;
    public static final int VIEW_BOOK_CANVAS_BOOK_INFO_FORM = 18;
    public static final int VIEW_BOOK_CANVAS_HELP_FORM = 19;
    
    public static final int VIEW_NET_LIB_LIST = 20;
    public static final int VIEW_MANYBOOKS_RSS_FEEDS_LIST = 21;
    public static final int VIEW_MANYBOOKS_NEW_TITLES_LIST = 22;
    
    public static final int VIEW_NET_LIB_BOOK_INFO_FORM = 23;
    
    
    public static final int VIEW_MANYBOOKS_CATEGORIES_LIST = 24;
    public static final int VIEW_MANYBOOKS_NEW_TITLES_BY_CATEGORY_LIST = 25;
    
    public static final int VIEW_FEEDBOOKS_LIST = 26;
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_LIST = 27;
    public static final int VIEW_FEEDBOOKS_ORIGINAL_CATALOG_LIST = 28;
    public static final int VIEW_FEEDBOOKS_ORIGINAL_LIST = 29;
    
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_MOST_POPULAR_LIST = 30;
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_RECENTLY_ADDED_LIST = 31;
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATEGORIES_LIST=32;
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_FICTION_CATEGORIES_LIST=33;
    public static final int VIEW_FEEDBOOKS_PUBLIC_DOMAIN_FICTION_BY_CATEGORY_LIST=34;
    
    public static final int VIEW_SETTINGS_LIST = 35;
    public static final int VIEW_SETTINGS_BOOKS_FOLDER_FORM = 36;
    public static final int VIEW_SETTINGS_TEXT_FORM = 37;
    public static final int VIEW_SETTINGS_STATUS_BAR_FORM = 38;
    public static final int VIEW_SETTINGS_SCROLLING_FORM = 39;
    
     public static final int VIEW_UNIVERSAL_HELP_FORM = 40;
    

    private static ViewMaster self = null;
    
    private int activeView = VIEW_NOVIEW;
    
    //base commands set
    private final Command okCmd = new Command(L10n.getMessage("OK_COMMAND_BUTTON"), 
                                                Command.OK, 1);

    private final Command helpCmd = new Command(L10n.getMessage("HELP_COMMAND"), 
                                                Command.SCREEN, 1);

    private final Command disclaimerCmd = new Command(L10n.getMessage("DISCLAIMER_COMMAND"), 
                                                Command.SCREEN, 1);
    
    private final Command dismissCmd = new Command(L10n.getMessage("DISMISS_COMMAND_BUTTON"), 
                                                Command.OK, 1);

    private final Command backCmd = new Command("BACK", Command.BACK, 1);

    private final Command yesCmd = new Command(L10n.getMessage("YES_COMMAND_BUTTON"), 
                                                Command.OK, 1);
    private final Command noCmd = new Command(L10n.getMessage("NO_COMMAND_BUTTON"), 
                                                Command.BACK, 1);
    
    private final Command saveChangesMadeYesCmd = new Command(L10n.getMessage("SAVE_CHANGES_YES_COMMAND_BUTTON"), 
                                                            Command.OK, 1);
    private final Command saveChangesMadeNoCmd = new Command(L10n.getMessage("SAVE_CHANGES_NO_COMMAND_BUTTON"), 
                                                            Command.HELP, 1);
    private final Command saveChangesMadeBackCmd = new Command(L10n.getMessage("SAVE_CHANGES_BACK_COMMAND_BUTTON"), 
                                                            Command.BACK, 1);
    
   private final Command cancelCmd = new Command(L10n.getMessage("CANCEL_COMMAND_BUTTON"), 
                                                 Command.CANCEL, 1);
    
    private final Command selectListItemCmd = new Command(L10n.getMessage("SELECT_COMMAND"), 
                                                Command.OK, 1);
    
    private final Command selectChoiceGroupItemCmd = new Command(L10n.getMessage("SELECT_COMMAND"), 
                                                Command.OK, 1);
    
    private Display display;
    
    //all base views
    private InfoViewForm infoViewForm;
    private AboutViewForm aboutViewForm;
    private LicenseViewForm licenseViewForm;
    
    private MyLibViewList myLibViewList;
    private MyLibViewListHelpForm myLibViewListHelpForm;
    
    private MyLibAuthorsViewList myLibAuthorsViewList;
    private MyLibAuthorsViewListHelpForm myLibAuthorsViewListHelpForm; 
    
    private MyLibTagsViewList myLibTagsViewList;
    private MyLibTagsViewListHelpForm myLibTagsViewListHelpForm;
    
    private MyLibRecentBooksViewList myLibRecentBooksViewList;
    private MyLibRecentBooksViewListHelpForm myLibRecentBooksViewListHelpForm;
    
    private MyLibBooksByAuthorViewList myLibBooksByAuthorViewList;
    private MyLibBooksByTagViewList myLibBooksByTagViewList;
    private MyLibBookInfoViewForm myLibBookInfoViewForm;
    
    private BookCanvas bookCanvas;
    private BookCanvasBookInfoViewForm bookCanvasBookInfoViewForm;
    private BookCanvasHelpForm bookCanvasHelpForm;
    
    private NetLibViewList netLibViewList;
    private NetLibManyBooksRSSFeedsViewList netLibManyBooksRSSFeedsViewList;
    private NetLibManyBooksNewTitlesViewList netLibManyBooksNewTitlesViewList;
    //Flag for NetLibManyBooksNewTitlesViewList.java 
    private boolean firsLoadManyBooksNewTitlesList=true;
    
    private NetLibBookInfoViewForm netLibBookInfoViewForm;
    private BookInNetLib selectedBookForDownload;
    
    private NetLibManyBooksCategoriesViewList netLibManyBooksCategoriesViewList;
    
    private NetLibManyBooksNewTitlesByCategoryViewList netLibManyBooksNewTitlesByCategoryViewList;
    //Flag for NetLibManyBooksNewTitlesByCategoryViewList.java 
    private boolean firsLoadManyBooksNewTitlesByCategoryList=true;
    private ManyBooksCategory selectedManyBooksCategoryListItem;
    
    
    private NetLibFeedBooksViewList netLibFeedBooksViewList;

    private NetLibFeedBooksPublicDomainCatalogViewList netLibFeedBooksPublicDomainCatalogViewList; 
    private boolean firsLoadFeedBooksPublicDomainCatalogList=true;
    private FeedBooksCatalog selectedFeedBookPublicDomainCatalogListItem;
    
    private NetLibFeedBooksOriginalCatalogViewList netLibFeedBooksOriginalCatalogViewList; 
    private boolean firsLoadFeedBooksOriginalCatalogList=true;
    private FeedBooksCatalog selectedFeedBookOriginalCatalogListItem;
    
    private NetLibFeedBooksOriginalViewList netLibFeedBooksOriginalViewList; 
    private boolean firstLoadFeedBooksOriginalList=true;
    
    private NetLibFeedBooksPublicDomainMostPopularViewList netLibFeedBooksPublicDomainMostPopularViewList;
    private boolean firsLoadFeedBooksPublicDomainMostPopularList=true;
    
    private NetLibFeedBooksPublicDomainRecentlyAddedViewList netLibFeedBooksPublicDomainRecentlyAddedViewList;
    private boolean firsLoadFeedBooksPublicDomainRecentlyAddedList=true;
    
    private NetLibFeedBooksPublicDomainCategoriesViewList netLibFeedBooksPublicDomainCategoriesViewList; 
    private FeedBooksCatalog selectedFeedBookPublicDomainCategoriesListItem;
    private boolean firsLoadFeedBooksPublicDomainCategoriesList=true;

    private NetLibFeedBooksPublicDomainFictionCategoriesViewList netLibFeedBooksPublicDomainFictionCategoriesViewList; 
    private boolean firstLoadFeedBooksPublicDomainFictionCategoriesList=true;
    private FeedBooksCatalog selectedFeedBookPublicDomainFictionCategoriesListItem;
    
    private NetLibFeedBooksPublicDomainFictionByCategoryViewList netLibFeedBooksPublicDomainFictionByCategoryViewList; 
    private boolean firstLoadFeedBooksPublicDomainFictionByCategoryList=true;
    
    private SettingsViewList settingsViewList;
    private SettingsBooksFolderViewForm settingsBooksFolderViewForm;
    private SettingsTextViewForm settingsTextViewForm;
    private SettingsStatusBarViewForm settingsStatusBarViewForm;
    private SettingsScrollingViewForm settingsScrollingViewForm;
    
    private UniversalHelpViewForm universalHelpForm;
    
    private String nextViewTitle;
    
    private ViewStack viewStack = new ViewStack();
    
    //declarations for Category Bar
    private CategoryBar categoryBar;
    
    public static final String CATEGORY_INFO_ICON_PATH = "/icons/cb_info_36x36.png";
    public static final String CATEGORY_MYLIB_ICON_PATH = "/icons/cb_my_lib_36x36.png";
    public static final String CATEGORY_NETLIB_ICON_PATH = "/icons/cb_net_lib_36x36.png";
    public static final String CATEGORY_SETTINGS_ICON_PATH = "/icons/cb_settings_36x36.png";    
    
    //images
    private ImageLoader imageLoader;
    
    private Image infoImageIcon;
    private Image myLibImageIcon;
    private Image netLibImageIcon;
    private Image settingsImageIcon;
    

    private ViewMaster() {
    }

    /**
     * Returns and/or instantiates ViewMaster object
     * @return
     */
    public static ViewMaster getInstance() {
        if (self == null) {
            self = new ViewMaster();
            self.initialize();
        }
        return self;
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display d) {
        this.display = d;
    }

    /**
     * Returns view instance according to given ID
     * @param id view ID
     * @return View
     */
    public Displayable getView(int id) {

        switch (id) {
            case VIEW_INFO_CATEGORY:
                return infoViewForm;
            case VIEW_ABOUT_FORM:
                return aboutViewForm;
            case VIEW_LICENSE_FORM:
                return licenseViewForm;

            case VIEW_MY_LIB_LIST:
                return myLibViewList;
            case VIEW_MY_LIB_LIST_HELP_FORM:
                return myLibViewListHelpForm;
           case VIEW_MY_LIB_AUTHORS_LIST:
                return myLibAuthorsViewList;
           case VIEW_MY_LIB_AUTHORS_LIST_HELP_FORM:
                return myLibAuthorsViewListHelpForm;
           case VIEW_MY_LIB_TAGS_LIST:
                return myLibTagsViewList;
           case VIEW_MY_LIB_TAGS_LIST_HELP_FORM:
                return myLibTagsViewListHelpForm;
           case VIEW_MY_LIB_RECENT_BOOKS_LIST:
                return myLibRecentBooksViewList;
           case VIEW_MY_LIB_RECENT_BOOKS_LIST_HELP_FORM:
                return myLibRecentBooksViewListHelpForm;
           case VIEW_MY_LIB_BOOKS_BY_AUTHOR_LIST:
                return myLibBooksByAuthorViewList;
           case VIEW_MY_LIB_BOOKS_BY_TAG_LIST:
                return myLibBooksByTagViewList; 
           case VIEW_MY_LIB_BOOK_INFO_FORM:
                return myLibBookInfoViewForm; 
           
           case VIEW_BOOK_CANVAS:
                return bookCanvas; 
           case VIEW_BOOK_CANVAS_BOOK_INFO_FORM:
                return bookCanvasBookInfoViewForm; 
           case VIEW_BOOK_CANVAS_HELP_FORM:
                return bookCanvasHelpForm; 
               
           case VIEW_NET_LIB_LIST:
                return netLibViewList;
           case VIEW_MANYBOOKS_RSS_FEEDS_LIST:
                return netLibManyBooksRSSFeedsViewList;
           case VIEW_MANYBOOKS_NEW_TITLES_LIST:
                return netLibManyBooksNewTitlesViewList;

           case VIEW_NET_LIB_BOOK_INFO_FORM:
                return netLibBookInfoViewForm; 
               
           case VIEW_MANYBOOKS_CATEGORIES_LIST:
                return netLibManyBooksCategoriesViewList; 
           case VIEW_MANYBOOKS_NEW_TITLES_BY_CATEGORY_LIST:
                return netLibManyBooksNewTitlesByCategoryViewList;
           
           case VIEW_FEEDBOOKS_LIST:
                return netLibFeedBooksViewList;
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_LIST:
                return netLibFeedBooksPublicDomainCatalogViewList;
           case VIEW_FEEDBOOKS_ORIGINAL_CATALOG_LIST:
                return netLibFeedBooksOriginalCatalogViewList;
           case VIEW_FEEDBOOKS_ORIGINAL_LIST:
                return netLibFeedBooksOriginalViewList;               
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_MOST_POPULAR_LIST:
                return netLibFeedBooksPublicDomainMostPopularViewList;
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_RECENTLY_ADDED_LIST:
                return netLibFeedBooksPublicDomainRecentlyAddedViewList;
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATEGORIES_LIST:
                return netLibFeedBooksPublicDomainCategoriesViewList; 
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_FICTION_CATEGORIES_LIST:
                return netLibFeedBooksPublicDomainFictionCategoriesViewList; 
           case VIEW_FEEDBOOKS_PUBLIC_DOMAIN_FICTION_BY_CATEGORY_LIST:
                return netLibFeedBooksPublicDomainFictionByCategoryViewList;
               
           case VIEW_SETTINGS_LIST:
                return settingsViewList; 
           case VIEW_SETTINGS_BOOKS_FOLDER_FORM:
                return settingsBooksFolderViewForm; 
           case VIEW_SETTINGS_TEXT_FORM:
                return settingsTextViewForm; 
           case VIEW_SETTINGS_STATUS_BAR_FORM:
                return settingsStatusBarViewForm; 
           case VIEW_SETTINGS_SCROLLING_FORM:
                return settingsScrollingViewForm; 

           case VIEW_UNIVERSAL_HELP_FORM:
                return universalHelpForm; 
               
           default:
                return infoViewForm;

        }
    }

    /**
     * Activates previous view
     */
    public synchronized void backView() {
        setView(viewStack.popView());
    }

    /**
     * Opens a view according to ID and pushes it to view stack
     * @param id View ID
     */
    public synchronized void openView(int id) {
        if (id != activeView) {
            viewStack.pushView(activeView);
            setView(id);
        }
    }

    /**
     * Handles command actions
     * @param command Selected command
     * @param dispayable The Displayable on which this event has occurred
     */
    public void commandAction(Command command, Displayable displayable) {

    }
    
    /**
     * Handles CategoryBar events, tells the currently visible CategoryBarView
     * to switch view to whatever item is tapped
     * @param categoryBar
     * @param selectedIndex 
     */
    public void notifyElementSelected(CategoryBar categoryBar, int selectedIndex) {
        
        switch (selectedIndex) {
            
            case VIEW_INFO_CATEGORY:
                // Initialized in the constructor, no need to nullcheck
                openView(VIEW_INFO_CATEGORY);
                break;
            case VIEW_MY_LIB_CATEGORY:
                // Initialized in the constructor, no need to nullcheck
                myLibViewList=getMyLibViewList();
                openView(VIEW_MY_LIB_LIST);
                break;                
            case VIEW_NET_LIB_CATEGORY:
                // Initialized in the constructor, no need to nullcheck
                netLibViewList=getNetLibViewList();
                openView(VIEW_NET_LIB_LIST);
                break;                
            case VIEW_SETTINGS_CATEGORY:
                // Initialized in the constructor, no need to nullcheck
                settingsViewList=getSettingsViewList();
                openView(VIEW_SETTINGS_LIST);
                break;

            case ElementListener.BACK:
                Main.getInstance().exitMIDlet();
                break;
        }
    }     

    /**
     * Displays an alert
     * @param title Title of the alert
     * @param alertText The actual content of the alert
     * @param type Type of the alert
     */
    public void showAlert(String title, String alertText, AlertType type) {
            final Alert alert = new Alert(title, alertText, null, type);
            display.setCurrent(alert, getView(activeView));
    }
    
    
    
    /**
     * Displays an alert
     * @param title Title of the alert
     * @param alertText The actual content of the alert
     * @param type Type of the alert
     */
    public void showTimedAlert(String title, String alertText, AlertType type, int timeOut) {
            final Alert alert = new Alert(title, alertText, null, type);
            alert.setTimeout(timeOut);
            display.setCurrent(alert, getView(activeView));
    }

    /**
     * Instantiates all the views and sets commands to them
     */
    private void initialize() {
        //create CategoryBar
        try {
            
            imageLoader=ImageLoader.getInstance();
            
            
            infoImageIcon = imageLoader.loadImage(
                             CATEGORY_INFO_ICON_PATH, null);

            myLibImageIcon = imageLoader.loadImage(
                    CATEGORY_MYLIB_ICON_PATH, null);
            
            netLibImageIcon = imageLoader.loadImage(
                CATEGORY_NETLIB_ICON_PATH, null);  
            
            settingsImageIcon = imageLoader.loadImage(
                CATEGORY_SETTINGS_ICON_PATH, null);             
            
        }
        catch (IOException e) {
        }
        
        
        IconCommand[] iconCommands = {
            new IconCommand("info", infoImageIcon, null, Command.SCREEN, 1),
            new IconCommand("my library", myLibImageIcon, null, Command.SCREEN,
            1),
            new IconCommand("network lib", netLibImageIcon, null, Command.SCREEN, 1),
            new IconCommand("settings", settingsImageIcon, null, Command.SCREEN, 1)
                
        };
        
        categoryBar = new CategoryBar(iconCommands, true);
        categoryBar.setElementListener(this);
        
        //create InfoViewForm() instance
        infoViewForm = getInfoViewForm();
    }

    /**
     * Sets a view to be displayed on the screen
     * @param id View ID
     */
    private synchronized void setView(int id) {
        // Deactivate previous one
        View current = (View) getView(activeView);
        current.deactivate();

        final Displayable next = getView(id);

        display.setCurrent(next);

        // Activate the next one
        activeView = id;
        ((View) next).activate();
    }
    
    public void setCategoryBarVisible(boolean visibleState) {
        categoryBar.setVisibility(visibleState);
    }
    
    public void setCategoryBarSelectedIndex(int index) {
        categoryBar.setSelectedIndex(index);
    }
    
    
    public InfoViewForm getInfoViewForm() {
        if (infoViewForm == null) {
            infoViewForm = new InfoViewForm();
        }
        return infoViewForm;
    }    
    
    public AboutViewForm getAboutViewForm() {
        if (aboutViewForm == null) {
            aboutViewForm = new AboutViewForm();
        }
        return aboutViewForm;
    }
    
    public LicenseViewForm getLicenseViewForm() {
        if (licenseViewForm == null) {
            licenseViewForm = new LicenseViewForm();
        }
        return licenseViewForm;
    }
    
    public MyLibViewList getMyLibViewList() {
        if (myLibViewList == null) {
            myLibViewList = new MyLibViewList(Main.getInstance());
        }
        return myLibViewList;
    }
    
    
    public MyLibViewListHelpForm getMyLibViewListHelpForm() {
        if (myLibViewListHelpForm == null) {
            myLibViewListHelpForm = new MyLibViewListHelpForm();
        }
        return myLibViewListHelpForm;
    }    

    
    public MyLibAuthorsViewList getMyLibAuthorsViewList() {
        if (myLibAuthorsViewList == null) {
            myLibAuthorsViewList = new MyLibAuthorsViewList(Main.getInstance());
        }
        return myLibAuthorsViewList;
    }
    
    public MyLibAuthorsViewListHelpForm getMyLibAuthorsViewListHelpForm() {
        if (myLibAuthorsViewListHelpForm == null) {
            myLibAuthorsViewListHelpForm = new MyLibAuthorsViewListHelpForm();
        }
        return myLibAuthorsViewListHelpForm;
    }
    
    
    public MyLibTagsViewList getMyLibTagsViewList() {
        if (myLibTagsViewList == null) {
            myLibTagsViewList = new MyLibTagsViewList(Main.getInstance());
        }
        return myLibTagsViewList;
    }
    

    public MyLibTagsViewListHelpForm getMyLibTagsViewListHelpForm() {
        if (myLibTagsViewListHelpForm == null) {
            myLibTagsViewListHelpForm = new MyLibTagsViewListHelpForm();
        }
        return myLibTagsViewListHelpForm;
    }    
    
    
    
    public MyLibRecentBooksViewList getMyLibRecentBooksViewList() {
        if (myLibRecentBooksViewList == null) {
            myLibRecentBooksViewList = new MyLibRecentBooksViewList(Main.getInstance());
        }
        return myLibRecentBooksViewList;
    }
    
    
    public MyLibRecentBooksViewListHelpForm getMyLibRecentBooksViewListHelpForm() {
        if (myLibRecentBooksViewListHelpForm == null) {
            myLibRecentBooksViewListHelpForm = new MyLibRecentBooksViewListHelpForm();
        }
        return myLibRecentBooksViewListHelpForm;
    }      
    
    
    public MyLibBooksByAuthorViewList getMyLibBooksByAuthorViewList() {
        if (myLibBooksByAuthorViewList == null) {
            myLibBooksByAuthorViewList = new MyLibBooksByAuthorViewList(Main.getInstance());
        }
        return myLibBooksByAuthorViewList;
    }
    
    public MyLibBooksByTagViewList getMyLibBooksByTagViewList() {
        if (myLibBooksByTagViewList == null) {
            myLibBooksByTagViewList = new MyLibBooksByTagViewList(Main.getInstance());
        }
        return myLibBooksByTagViewList;
    }
    
    public MyLibBookInfoViewForm getMyLibBookInfoViewForm() {
        if (myLibBookInfoViewForm == null) {
            myLibBookInfoViewForm = new MyLibBookInfoViewForm();
        }
        return myLibBookInfoViewForm;
    }    
    
    
    public BookCanvas getBookCanvas() {
        if (bookCanvas == null) {
   
            bookCanvas = new BookCanvas(Main.getInstance());
            bookCanvas.setTitle(L10n.getMessage("BOOK_CANVAS_TITLE"));
            bookCanvas.setFullScreenMode(false);
        
            /* RMS */
            Main.getInstance().openRMSAndLoadData();
        
            /*
             * The BookCanvas must be initialized before usage. This is because
             * of the fact, that it wouldn't have correct metrics, i.e.
             * wouldn't be in fullscreenmode when looked at from the constructor
             */
             bookCanvas.initialize();            
            
        }
        return bookCanvas;
    }
    
    
    public BookCanvasBookInfoViewForm getBookCanvasBookInfoViewForm() {
        bookCanvasBookInfoViewForm = new BookCanvasBookInfoViewForm();
        return bookCanvasBookInfoViewForm;
    } 
    
    public BookCanvasHelpForm getBookCanvasHelpForm() {
        if (bookCanvasHelpForm == null) {
            bookCanvasHelpForm = new BookCanvasHelpForm();
        }
        return bookCanvasHelpForm;
    } 
    
    
    public NetLibViewList getNetLibViewList() {
        if (netLibViewList == null) {
            netLibViewList = new NetLibViewList(Main.getInstance());
        }
        return netLibViewList;
    }    
    
    
    public NetLibManyBooksRSSFeedsViewList getNetLibManyBooksRSSFeedsViewList() {
        if (netLibManyBooksRSSFeedsViewList == null) {
            netLibManyBooksRSSFeedsViewList = new NetLibManyBooksRSSFeedsViewList(Main.getInstance());
        }
        return netLibManyBooksRSSFeedsViewList;
    }    

    public NetLibManyBooksNewTitlesViewList getNetLibManyBooksNewTitlesViewList() {
        if (netLibManyBooksNewTitlesViewList == null) {
            netLibManyBooksNewTitlesViewList = new NetLibManyBooksNewTitlesViewList(Main.getInstance());
        }
        return netLibManyBooksNewTitlesViewList;
    }
    
    
    /**
     * Get firsLoadManyBooksNewTitlesList value
     * 
     * @return firsLoadManyBooksNewTitlesList
     */
    public boolean getFirsLoadManyBooksNewTitlesList() {
        return firsLoadManyBooksNewTitlesList;
    }
    
    
    /**
     * Set firsLoadManyBooksNewTitlesList value
     * 
    */
    public void setFirsLoadManyBooksNewTitlesList(boolean firsLoadManyBooksNewTitlesList) {
        this.firsLoadManyBooksNewTitlesList=firsLoadManyBooksNewTitlesList;
    }     
    
    
    
    public NetLibBookInfoViewForm getNetLibBookInfoViewForm() {
        if (netLibBookInfoViewForm == null) {
            netLibBookInfoViewForm = new NetLibBookInfoViewForm();
        }
        return netLibBookInfoViewForm;
    }
    
    
    /**
     * Set selected book for download as BookInMyLibRMS object
     * 
     * @return 
     */
     public void setSelectedBookForDownload(BookInNetLib selectedBookForDownload) {
        this.selectedBookForDownload=selectedBookForDownload;
     } 
    
    
    
    /**
     * Get selected book for download as BookInNetLib object
     * 
     * @return selectedBook
     */
    public BookInNetLib getSelectedBookForDownload() {
        return selectedBookForDownload;
    }     
    
    
    
    public NetLibManyBooksCategoriesViewList getNetLibManyBooksCategoriesViewList() {
        if (netLibManyBooksCategoriesViewList == null) {

            String manyBooksCategoriesListTitle=             
                   L10n.getMessage("MANYBOOKS_CATEGORIES_VIEW_LIST_TITLE");
            int manyBooksCategoriesListType=Choice.IMPLICIT;
          
            netLibManyBooksCategoriesViewList = 
                new NetLibManyBooksCategoriesViewList(manyBooksCategoriesListTitle,manyBooksCategoriesListType);
        }
        return netLibManyBooksCategoriesViewList;
    }
    
    
    public NetLibManyBooksNewTitlesByCategoryViewList getNetLibManyBooksNewTitlesByCategoryViewList() {
        if (netLibManyBooksNewTitlesByCategoryViewList == null) {
            netLibManyBooksNewTitlesByCategoryViewList = new NetLibManyBooksNewTitlesByCategoryViewList(Main.getInstance());
        }
        return netLibManyBooksNewTitlesByCategoryViewList;
    }
    
    
    /**
     * Get selectedManyBooksCategoryListItem 
     * 
     * @return selectedManyBooksCategoryListItem
     */
    public ManyBooksCategory getSelectedManyBooksCategoryListItem() {
        return selectedManyBooksCategoryListItem;
    }
    
    
    /**
     * Set selectedManyBooksCategoryListItem
     * 
    */
    public void setSelectedManyBooksCategoryListItem(ManyBooksCategory selectedManyBooksCategoryListItem) {
        this.selectedManyBooksCategoryListItem=selectedManyBooksCategoryListItem;
    }    
    
    
    
    
    
    /**
     * Get firsLoadManyBooksNewTitlesList value
     * 
     * @return firsLoadManyBooksNewTitlesByCategoryList
     */
    public boolean getFirsLoadManyBooksNewTitlesByCategoryList() {
        return firsLoadManyBooksNewTitlesByCategoryList;
    }
    
    
    /**
     * Set firsLoadManyBooksNewTitlesByCategoryList value
     * 
    */
    public void setFirsLoadManyBooksNewTitlesByCategoryList(boolean firsLoadManyBooksNewTitlesByCategoryList) {
        this.firsLoadManyBooksNewTitlesByCategoryList=firsLoadManyBooksNewTitlesByCategoryList;
    }
    
    
    public NetLibFeedBooksViewList getNetLibFeedBooksViewList() {
        if (netLibFeedBooksViewList == null) {
            netLibFeedBooksViewList = new NetLibFeedBooksViewList(Main.getInstance());
        }
        return netLibFeedBooksViewList;
    }
    
    
    
    
    public NetLibFeedBooksPublicDomainCatalogViewList getNetLibFeedBooksPublicDomainCatalogViewList() {
        if (netLibFeedBooksPublicDomainCatalogViewList == null) {
            netLibFeedBooksPublicDomainCatalogViewList = new NetLibFeedBooksPublicDomainCatalogViewList(Main.getInstance());
        }
        return netLibFeedBooksPublicDomainCatalogViewList;
    }
    
    
    /**
     * Get firsLoadFeedBooksPublicDomainCatalogList value
     * 
     * @return firsLoadFeedBooksPublicDomainCatalogList
     */
    public boolean getFirsLoadFeedBooksPublicDomainCatalogList() {
        return firsLoadFeedBooksPublicDomainCatalogList;
    }
    
    
    /**
     * Set firsLoadFeedBooksPublicDomainCatalogList value
     * 
    */
    public void setFirsLoadFeedBooksPublicDomainCatalogList(boolean firsLoadFeedBooksPublicDomainCatalogList) {
        this.firsLoadFeedBooksPublicDomainCatalogList=firsLoadFeedBooksPublicDomainCatalogList;
    }  
    
    
    /**
     * Get selectedFeedBookPublicDomainCatalogListItem 
     * 
     * @return selectedFeedBookPublicDomainCatalogListItem
     */
    public FeedBooksCatalog getSelectedFeedBookPublicDomainCatalogListItem() {
        return selectedFeedBookPublicDomainCatalogListItem;
    }
    
    
    /**
     * Set selectedFeedBookPublicDomainCatalogListItem
     * 
    */
    public void setSelectedFeedBookPublicDomainCatalogListItem(FeedBooksCatalog selectedFeedBookPublicDomainCatalogListItem) {
        this.selectedFeedBookPublicDomainCatalogListItem=selectedFeedBookPublicDomainCatalogListItem;
    }
    
    
    public NetLibFeedBooksOriginalCatalogViewList getNetLibFeedBooksOriginalCatalogViewList() {
        if (netLibFeedBooksOriginalCatalogViewList == null) {
            netLibFeedBooksOriginalCatalogViewList = new NetLibFeedBooksOriginalCatalogViewList(Main.getInstance());
        }
        return netLibFeedBooksOriginalCatalogViewList;
    }
    
    
    /**
     * Get firsLoadFeedBooksOriginalCatalogList value
     * 
     * @return firsLoadFeedBooksOriginalCatalogList
     */
    public boolean getFirsLoadFeedBooksOriginalCatalogList() {
        return firsLoadFeedBooksOriginalCatalogList;
    }
    
    
    /**
     * Set firsLoadFeedBooksOriginalCatalogList value
     * 
    */
    public void setFirsLoadFeedBooksOriginalCatalogList(boolean firsLoadFeedBooksOriginalCatalogList) {
        this.firsLoadFeedBooksOriginalCatalogList=firsLoadFeedBooksOriginalCatalogList;
    }  
    
    
    /**
     * Get selectedFeedBookOriginalCatalogListItem 
     * 
     * @return selectedFeedBookOriginalCatalogListItem
     */
    public FeedBooksCatalog getSelectedFeedBookOriginalCatalogListItem() {
        return selectedFeedBookOriginalCatalogListItem;
    }
    
    
    /**
     * Set selectedFeedBookOriginalCatalogListItem
     * 
    */
    public void setSelectedFeedBookOriginalCatalogListItem(FeedBooksCatalog selectedFeedBookOriginalCatalogListItem) {
        this.selectedFeedBookOriginalCatalogListItem=selectedFeedBookOriginalCatalogListItem;
    }     
    
    
   
    public NetLibFeedBooksOriginalViewList getNetLibFeedBooksOriginalViewList() {
        if (netLibFeedBooksOriginalViewList == null) {
            netLibFeedBooksOriginalViewList = new NetLibFeedBooksOriginalViewList(Main.getInstance());
        }
        return netLibFeedBooksOriginalViewList;
    }
    
    
    /**
     * Get firstLoadFeedBooksOriginalList value
     * 
     * @return firsLoadFeedBooksOriginalList
     */
    public boolean getFirstLoadFeedBooksOriginalList() {
        return firstLoadFeedBooksOriginalList;
    }
    
    
    /**
     * Set firstLoadFeedBooksOriginalList value
     * 
    */
    public void setFirstLoadFeedBooksOriginalList(boolean firstLoadFeedBooksOriginalList) {
        this.firstLoadFeedBooksOriginalList=firstLoadFeedBooksOriginalList;
    }     
    
    
    
    
     
     
    public NetLibFeedBooksPublicDomainMostPopularViewList getNetLibFeedBooksPublicDomainMostPopularViewList() {
        if (netLibFeedBooksPublicDomainMostPopularViewList == null) {
            netLibFeedBooksPublicDomainMostPopularViewList = new NetLibFeedBooksPublicDomainMostPopularViewList(Main.getInstance());
        }
        return netLibFeedBooksPublicDomainMostPopularViewList;
    }
    
    
    /**
     * Get firsLoadFeedBooksPublicDomainMostPopularList value
     * 
     * @return firsLoadFeedBooksPublicDomainMostPopularList
     */
    public boolean getFirsLoadFeedBooksPublicDomainMostPopularList() {
        return firsLoadFeedBooksPublicDomainMostPopularList;
    }
    
    
    /**
     * Set firsLoadFeedBooksPublicDomainMostPopularList value
     * 
    */
    public void setFirsLoadFeedBooksPublicDomainMostPopularList(boolean firsLoadFeedBooksPublicDomainMostPopularList) {
        this.firsLoadFeedBooksPublicDomainMostPopularList=firsLoadFeedBooksPublicDomainMostPopularList;
    }      
     
    
    public NetLibFeedBooksPublicDomainRecentlyAddedViewList getNetLibFeedBooksPublicDomainRecentlyAddedViewList() {
        if (netLibFeedBooksPublicDomainRecentlyAddedViewList == null) {
            netLibFeedBooksPublicDomainRecentlyAddedViewList = new NetLibFeedBooksPublicDomainRecentlyAddedViewList(Main.getInstance());
        }
        return netLibFeedBooksPublicDomainRecentlyAddedViewList;
    }
    
    
    /**
     * Get firsLoadFeedBooksPublicDomainRecentlyAddedList value
     * 
     * @return firsLoadFeedBooksPublicDomainRecentlyAddedList
     */
    public boolean getFirsLoadFeedBooksPublicDomainRecentlyAddedList() {
        return firsLoadFeedBooksPublicDomainRecentlyAddedList;
    }
    
    
    /**
     * Set firsLoadFeedBooksPublicDomainRecentlyAddedList value
     * 
    */
    public void setFirsLoadFeedBooksPublicDomainRecentlyAddedList(boolean firsLoadFeedBooksPublicDomainRecentlyAddedList) {
        this.firsLoadFeedBooksPublicDomainRecentlyAddedList=firsLoadFeedBooksPublicDomainRecentlyAddedList;
    }

    public NetLibFeedBooksPublicDomainCategoriesViewList getNetLibFeedBooksPublicDomainCategoriesViewList() {
        if (netLibFeedBooksPublicDomainCategoriesViewList == null) {
            String feedBooksPublicDomainCategoriesListTitle=this.getNextViewTitle();
            int feedBooksPublicDomainCategoriesListType=Choice.IMPLICIT;
            netLibFeedBooksPublicDomainCategoriesViewList = 
                new NetLibFeedBooksPublicDomainCategoriesViewList(feedBooksPublicDomainCategoriesListTitle,feedBooksPublicDomainCategoriesListType);
        }
        return netLibFeedBooksPublicDomainCategoriesViewList;
    }
    
   
    /**
     * Get firsLoadFeedBooksPublicDomainCategoriesList value
     * 
     * @return firsLoadFeedBooksPublicDomainCategoriesList
     */
    public boolean getFirsLoadFeedBooksPublicDomainCategoriesList() {
        return firsLoadFeedBooksPublicDomainCategoriesList;
    }
    
    
    /**
     * Set firsLoadFeedBooksPublicDomainCategoriesList value
     * 
    */
    public void setFirsLoadFeedBooksPublicDomainCategoriesList(boolean firsLoadFeedBooksPublicDomainCategoriesList) {
        this.firsLoadFeedBooksPublicDomainCategoriesList=firsLoadFeedBooksPublicDomainCategoriesList;
    }    
    
    
    
    /**
     * Get selectedFeedBookPublicDomainCategoriesListItem 
     * 
     * @return selectedFeedBookPublicDomainCategoriesListItem
     */
    public FeedBooksCatalog getSelectedFeedBookPublicDomainCategoriesListItem() {
        return selectedFeedBookPublicDomainCategoriesListItem;
    }
    
    
    /**
     * Set selectedFeedBookPublicDomainCategoriesListItem
     * 
    */
    public void setSelectedFeedBookPublicDomainCategoriesListItem(FeedBooksCatalog selectedFeedBookPublicDomainCategoriesListItem) {
        this.selectedFeedBookPublicDomainCategoriesListItem=selectedFeedBookPublicDomainCategoriesListItem;
    }
    
    
    
    
    /**
     *  Methods for NetLibFeedBooksPublicDomainFictionCategoriesViewList class
     */    
    public NetLibFeedBooksPublicDomainFictionCategoriesViewList getNetLibFeedBooksPublicDomainFictionCategoriesViewList() {
        if (netLibFeedBooksPublicDomainFictionCategoriesViewList == null) {
            String feedBooksPublicDomainFictionCategoriesListTitle=this.getNextViewTitle();
            int feedBooksPublicDomainFictionCategoriesListType=Choice.IMPLICIT;
          
            netLibFeedBooksPublicDomainFictionCategoriesViewList = 
                new NetLibFeedBooksPublicDomainFictionCategoriesViewList(feedBooksPublicDomainFictionCategoriesListTitle,feedBooksPublicDomainFictionCategoriesListType);
        }
        return netLibFeedBooksPublicDomainFictionCategoriesViewList;
    }      
    
    
    /**
     * Get firsLoadFeedBooksPublicDomainRecentlyAddedList value
     * 
     * @return firsLoadFeedBookPublicDomainFictionCategoriesList
     */
    public boolean getFirstLoadFeedBooksPublicDomainFictionCategoriesList() {
        return firstLoadFeedBooksPublicDomainFictionCategoriesList;
    }
    
    
    /**
     * Set firstLoadFeedBookPublicDomainFictionCategoriesList value
     * 
    */
    public void setFirstLoadFeedBooksPublicDomainFictionCategoriesList(boolean firstLoadFeedBookPublicDomainFictionCategoriesList) {
        this.firstLoadFeedBooksPublicDomainFictionCategoriesList=firstLoadFeedBookPublicDomainFictionCategoriesList;
    }    
    
    
    
    
    /**
     * Get selectedFeedBookPublicDomainFictionCategoriesListItem 
     * 
     * @return selectedFeedBookPublicDomainFictionCategoriesListItem
     */
    public FeedBooksCatalog getSelectedFeedBookPublicDomainFictionCategoriesListItem() {
        return selectedFeedBookPublicDomainFictionCategoriesListItem;
    }
    
    
    /**
     * Set selectedFeedBookPublicDomainFictionCategoriesListItem
     * 
    */
    public void setSelectedFeedBookPublicDomainFictionCategoriesListItem(FeedBooksCatalog selectedFeedBookPublicDomainFictionCategoriesListItem) {
        this.selectedFeedBookPublicDomainFictionCategoriesListItem=selectedFeedBookPublicDomainFictionCategoriesListItem;
    }

    
    
    /**
     *  Methods for NetLibFeedBooksPublicDomainFictionByCategoryViewList class
     */     
     public NetLibFeedBooksPublicDomainFictionByCategoryViewList getNetLibFeedBooksPublicDomainFictionByCategoryViewList() {
        if (netLibFeedBooksPublicDomainFictionByCategoryViewList == null) {
            netLibFeedBooksPublicDomainFictionByCategoryViewList = new NetLibFeedBooksPublicDomainFictionByCategoryViewList(Main.getInstance());
        }
        return netLibFeedBooksPublicDomainFictionByCategoryViewList;
    }
     
     
    /**
     * Get firsLoadFeedBooksPublicDomainFictionByCategoryList value
     * 
     * @return firsLoadFeedBooksPublicDomainFictionByCategoryList
     */
    public boolean getFirstLoadFeedBooksPublicDomainFictionByCategoryList() {
        return firstLoadFeedBooksPublicDomainFictionByCategoryList;
    }
    
    
    /**
     * Set firsLoadFeedBooksPublicDomainFictionByCategoryList value
     * 
    */
    public void setFirstLoadFeedBooksPublicDomainFictionByCategoryList(boolean firstLoadFeedBooksPublicDomainFictionByCategoryList) {
        this.firstLoadFeedBooksPublicDomainFictionByCategoryList=firstLoadFeedBooksPublicDomainFictionByCategoryList;
    }     
     
     
     
     
     
    /**
     *  Methods for SettingsViewList class
     */    
    public SettingsViewList getSettingsViewList() {
        if (settingsViewList == null) {

            String settingsListTitle=             
                   L10n.getMessage("SETTINGS_VIEW_LIST_TITLE");
            int settingsListType=Choice.IMPLICIT;
          
            settingsViewList = 
                new SettingsViewList(settingsListTitle,settingsListType);
        }
        return settingsViewList;
    }
    
    
    public SettingsBooksFolderViewForm getSettingsBooksFolderViewForm() {
        if (settingsBooksFolderViewForm == null) {
            settingsBooksFolderViewForm = new SettingsBooksFolderViewForm();
        }
        return settingsBooksFolderViewForm;
    } 
    
    
    public SettingsTextViewForm getSettingsTextViewForm() {
        if (settingsTextViewForm == null) {
            settingsTextViewForm = new SettingsTextViewForm();
        }
        return settingsTextViewForm;
    } 
    
    
    public SettingsStatusBarViewForm getSettingsStatusBarViewForm() {
        if (settingsStatusBarViewForm == null) {
            settingsStatusBarViewForm = new SettingsStatusBarViewForm();
        }
        return settingsStatusBarViewForm;
    }    
    
    public SettingsScrollingViewForm getSettingsScrollingViewForm() {
        if (settingsScrollingViewForm == null) {
            settingsScrollingViewForm = new SettingsScrollingViewForm();
        }
        return settingsScrollingViewForm;
    } 
    
     /**
     *  Methods for UniversalHelpViewForm class
     */ 
    public UniversalHelpViewForm getUniversalHelpForm(String formTitle, String helpTextHeader, String helpTextContent,boolean showCategoryBar) {
        universalHelpForm = new UniversalHelpViewForm(formTitle,helpTextHeader,helpTextContent,showCategoryBar);
        return universalHelpForm;
    }
    

    /**
     *  Methods for managing titles during transitions between 
     *  the screens
    */
    
    /**
     * Get nextViewTitle value
     * 
     * @return nextViewTitle
     */
    public String getNextViewTitle() {
        return nextViewTitle;
    }
    
    
    /**
     * Set nextViewTitle value
     * 
    */
    public void setNextViewTitle(String nextViewTitle) {
        this.nextViewTitle=nextViewTitle;
    }    
    

    //Getters for base commands set
    
    /**
     * @return exitCmd
    */
    public final Command getOkCmd() {
        return okCmd;
    } 
    
    /**
     * @return backCmd
    */
    public final Command getBackCmd() {
        return backCmd;
    } 
    
    
    /**
     * @return dismissCmd
    */
    public final Command getDismissCmd() {
        return dismissCmd;
    } 

    
    /**
    * @return yesCmd
    */
    public final Command getYesCmd() {
        return yesCmd;
    } 

    /**
    * @return noCmd
    */
    public final Command getNoCmd() {
        return noCmd;
    } 

    
    /**
     * @return saveChangesMadeYesCmd
    */
    public final Command getSaveChangesMadeYesCmd() {
        return saveChangesMadeYesCmd;
    } 
    
    /**
     * @return saveChangesMadeNoCmd
    */
    public final Command getSaveChangesMadeNoCmd() {
        return saveChangesMadeNoCmd;
    } 
    
    
    /**
     * @return saveChangesMadeBackCmd
    */
    public final Command getSaveChangesMadeBackCmd() {
        return saveChangesMadeBackCmd;
    }
    
    
    
    /**
     * @return helpCmd
    */
    public final Command getHelpCmd() {
        return helpCmd;
    } 
    
    
    /**
     * @return disclaimerCmd
    */
    public final Command getDisclaimerCmd() {
        return disclaimerCmd;
    }     
    
    /**
     * @return cancelCmd
    */
    public final Command getCancelCmd() {
        return cancelCmd;
    }     
    
    
    /**
     * @return selectCmd
    */
    public final Command getSelectListItemCmd() {
        return selectListItemCmd;
    }  
    
    /**
     * @return  selectChoiceGroupItemCmd
    */
    public final Command getSelectChoiceGroupItemCmd() {
        return selectChoiceGroupItemCmd;
    }  
}
