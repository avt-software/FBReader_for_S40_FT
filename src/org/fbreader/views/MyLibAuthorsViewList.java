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

import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import org.fbreader.Main;
import org.fbreader.utils.ImageLoader;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.utils.mylib.MyLibFilter;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.localization.L10n;
import org.fbreader.models.BookInMyLibRMS;

public class MyLibAuthorsViewList 
             extends FancyCustomList 
             implements View, CommandListener {
   
   private static MyLibAuthorsViewList self = null; 
   private Image imageListIcon;
   private static Command selectMyLibAuthorsListCmd;
   private Vector myLibRMSDataVector=null;
    private final String BY_AUTHORS_ICON = 
            "/icons/mylib_by_authors_38x38.png";
    private ViewMaster viewMaster;
    private MyLibUtils myLibUtils;
    private MyLibFilter myLibFilter;
    private String filterStringByAuthor;
    private final Alert alertAuthorsNotFound = new Alert(L10n.getMessage("AUTHORS_NOT_FOUND_ALERT_TITLE"), 
            L10n.getMessage("AUTHORS_NOT_FOUND_ALERT_CONTENT"), null, null);    
    

    public MyLibAuthorsViewList(MIDlet parent) {
       super(L10n.getMessage("MY_LIB_AUTHORS_VIEW_LIST_TITLE"));
       viewMaster = ViewMaster.getInstance();
       myLibUtils = MyLibUtils.getInstance(); 
       myLibFilter = MyLibFilter.getInstance();
       selectMyLibAuthorsListCmd=viewMaster.getSelectListItemCmd();
       this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
       try  {
              imageListIcon=ImageLoader.getInstance().loadImage(BY_AUTHORS_ICON, null);
            } 
       catch(IOException e) {
       }    
       this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
       this.addCommand(viewMaster.getHelpCmd());
       this.addCommand(viewMaster.getBackCmd());
       this.setSelectCommand(selectMyLibAuthorsListCmd);
       this.setCommandListener(this);       
    }
    
     /**
     * @return MyLibAuthorsViewList singleton
     */
    public static MyLibAuthorsViewList getInstance() {
        if (self == null) {
            self = new MyLibAuthorsViewList(Main.getInstance());
        }
        return self;
    } 

    
    /**
     * Activate MyLibViewList 
     */
    public void activate() {
        
       //read MyLibRMS 
       myLibUtils.readMyLibRMS();
       //get myLibRMSDataVector
       myLibRMSDataVector=myLibUtils.getMyLibRMSDataVector();

       if (myLibRMSDataVector !=null) {
              for (int i = 0; i < myLibRMSDataVector.size(); i++) {
              BookInMyLibRMS currentBook = (BookInMyLibRMS) myLibRMSDataVector.elementAt(i);
              this.append(currentBook.getBookAuthor().toLowerCase(),currentBook.getBookTitle().toLowerCase(),"",imageListIcon,
                             FancyElement.IMPORTANCE_NONE);
            }
       }
       else {
           showAuthorsNotFoundAlert();
       }
       
       //hide CategoryBar 
       viewMaster.setCategoryBarVisible(false);
    }

    /**
     * Deactivate MyLibViewList. 
     * Empties the List items.
     */
    public void deactivate() {
       int MyLibAuthorsListSize=this.size();
       for (int i=MyLibAuthorsListSize-1; i >=0 ; i--){
            try {
                this.delete(i);
            }
            catch (IndexOutOfBoundsException iobe) {
                //#debug
                Main.LOGGER.logError("MyLibAuthorsViewList::deactivate()"+iobe.getMessage());
            }
        }
    }    
    
    
    public void commandAction(Command command, Displayable displayable) {
        
        if (displayable==alertAuthorsNotFound && command == viewMaster.getDismissCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();            
        }
        if (command == viewMaster.getBackCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();
        }
        else if (command == selectMyLibAuthorsListCmd) {
            BookInMyLibRMS currentBook = (BookInMyLibRMS) myLibRMSDataVector.elementAt(this.getSelectedIndex());  
            filterStringByAuthor=currentBook.getBookAuthor();
            myLibFilter.setFilterStringByAuthor(filterStringByAuthor);
            viewMaster.getMyLibBooksByAuthorViewList();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_BOOKS_BY_AUTHOR_LIST);
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getMyLibAuthorsViewListHelpForm();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_AUTHORS_LIST_HELP_FORM);            
        }          
    }
    
    
    public void showAuthorsNotFoundAlert() {
        alertAuthorsNotFound.setTimeout(Alert.FOREVER); 
        alertAuthorsNotFound.addCommand(viewMaster.getDismissCmd());
        alertAuthorsNotFound.setCommandListener(this);         
        Main.getInstance().switchDisplayable(alertAuthorsNotFound,this);
    }    
    
}
