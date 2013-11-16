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

* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.views;

import javax.microedition.lcdui.*;
import org.fbreader.Main;
import org.fbreader.localization.L10n;
import org.fbreader.settings.Settings;
import org.fbreader.models.BookInMyLibRMS;
import org.fbreader.uihelpers.Visual;
import org.fbreader.utils.mylib.MyLibUtils;
import org.tantalum.UITask;

/*
 * Class for displaying on the form parameters of the selected book
 * in My Library
 *  
 */

public class MyLibBookInfoViewForm 
       extends Form
       implements View, CommandListener, ItemCommandListener {
   private ViewMaster viewMaster;
   private MyLibUtils myLibUtils;
   private static BookInMyLibRMS selectedBookForRead;
   public static final Command readBookCmd = new Command("", Command.ITEM, 1); 
   private StringItem stringItemReadBookButton;
   
   String currentBookURL;
   
   //private Alert alertBookOpen; 
   private final Alert alertOpeningBook = new Alert(L10n.getMessage("MY_LIB_BOOK_INFO_OPENING_BOOK_ALERT_TITLE"), 
            L10n.getMessage("MY_LIB_BOOK_INFO_OPENING_BOOK_ALERT_CONTENT"), null, null);
 
    private UITask uiTask;

    public MyLibBookInfoViewForm() {
        super(L10n.getMessage("MY_LIB_BOOK_INFO_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        myLibUtils=MyLibUtils.getInstance();
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);
    }
    
    
    
    /**
     * Activate MyLibBookInfoViewForm and 
     * view info about selected book 
     * 
     */
    public void activate() {
        //show info about selected book
        showSelectedBookInfo();      
    }
    
    
    /**
     * Deactivate MyLibBookInfoViewForm
     */
    public void deactivate() {
        deleteAll();
    }
    
    
     
    /**
     * show info about selected book
    */
    public void showSelectedBookInfo() {
        
            //Get selected book info for read
            try {
                selectedBookForRead=myLibUtils.getSelectedBookForRead();
            }
            catch (Exception e) {
                //#debug
                Main.LOGGER.logError("MyLibBookInfoViewForm::activate()"+e.getMessage());
            }
//--- 1 --- Book Title
        StringItem stringItemBookTitle = 
                new StringItem(L10n.getMessage("BOOK_TITLE_STRING_ITEM_TITLE"),
                              selectedBookForRead.getBookTitle().toLowerCase());
        this.append(stringItemBookTitle);
        
//--- 2 --- Book Author
        StringItem stringItemBookAuthor = 
                new StringItem(L10n.getMessage("BOOK_AUTHOR_STRING_ITEM_TITLE"),
                              selectedBookForRead.getBookAuthor().toLowerCase());
        this.append(stringItemBookAuthor);         
        
//--- 3 --- Book Tags
        StringItem stringItemBookTags = 
                new StringItem(L10n.getMessage("BOOK_TAGS_STRING_ITEM_TITLE"),
                              selectedBookForRead.getBookTags().toLowerCase());
        this.append(stringItemBookTags);         

//--- 4 --- Book Language
        StringItem stringItemBookLang = 
                new StringItem(L10n.getMessage("BOOK_LANG_STRING_ITEM_TITLE"),
                              selectedBookForRead.getBookLang().toLowerCase());
        this.append(stringItemBookLang);
        
//--- 4 --- ReadBook Button
        stringItemReadBookButton = 
                new StringItem("", L10n.getMessage("READ_BOOK_BUTTON"), Item.BUTTON);
        stringItemReadBookButton.addCommand(readBookCmd);
        stringItemReadBookButton.setDefaultCommand(readBookCmd);
        stringItemReadBookButton.setItemCommandListener(this);
        stringItemReadBookButton.setFont(Visual.SMALL_FONT);
        stringItemReadBookButton.setPreferredSize(-1, stringItemReadBookButton.getPreferredHeight()+8);
        stringItemReadBookButton.setLayout(Item.LAYOUT_EXPAND);
        this.append(stringItemReadBookButton);
    }
    
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
        
        if (command == viewMaster.getHelpCmd()){
            
            
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("MY_LIB_BOOK_INFO_VIEW_FORM_HELP_TEXT_HEADER"),
                                            L10n.getMessage("MY_LIB_BOOK_INFO_VIEW_FORM_HELP_TEXT_CONTENT"), 
                                            false);
            
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);            
        }
          
         else if (command == viewMaster.getBackCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();
          }

         else if (displayable==alertOpeningBook && command == viewMaster.getCancelCmd()) {
               uiTask.cancel(true);
               Main.getInstance().switchDisplayable(null, this);
          }
      }
      
       /**
         * Handles the select command related to a list item
         * @param c
         * @param item
       */
       public void commandAction(Command c, Item item) {
           if (item==stringItemReadBookButton) {
//--- 1 --- set currentBookURL for reading books in bookCanvas
               currentBookURL=Settings.getInstance().getBooksFolderURL()
                                    +selectedBookForRead.getBookFileName()
                                    +"."
                                    +selectedBookForRead.getBookFileExt();
               
               Main.getInstance().setCurrentBookURL(currentBookURL);
//--- 2 --- set bookLastOpen field for selectedBookForRead (instance of BookInMyLibRMS class)
                selectedBookForRead.setBookLastOpen(System.currentTimeMillis());

//--- 3 --- update bookLastOpen field for record in myLibRMS 
                //with correspondent with selectedBookForRead
                //(record search on RecordID of the selectedBookForRead)
                myLibUtils.updateBookRecordInMyLibRMS(selectedBookForRead);
//--- 4 --- begin load selectedBookForRead 
               Main.getInstance().switchDisplayable(null, Main.getInstance().getLoadBook());
         }
       }

    public void showCancelableAlert() {

        // display a "busy" screen
        final Gauge gauge;

        // Creates an alert with an indeterminate gauge
        gauge = new Gauge(null, false, Gauge.INDEFINITE,
                Gauge.CONTINUOUS_RUNNING);
        alertOpeningBook.setIndicator(gauge);            
        alertOpeningBook.setTimeout(Alert.FOREVER);  
        alertOpeningBook.addCommand(viewMaster.getCancelCmd());
        alertOpeningBook.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertOpeningBook,this);         
    }

}
