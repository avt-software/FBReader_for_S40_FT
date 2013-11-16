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
import org.fbreader.utils.ImageLoader;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.utils.mylib.MyLibFilter;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.localization.L10n;
import org.fbreader.models.BookInMyLibRMS;

public class MyLibBooksByAuthorViewList 
             extends FancyCustomList 
             implements View, CommandListener {
    
   private Image imageListIcon;
   public static final Command selectMyLibAuthorListCmd = 
           new Command("Select", Command.OK, 1);
   private Vector myLibRMSByAuthorFilterDataVector=null;
    private final String BOOKS_BY_AUTHOR_ICON = 
            "/icons/book_placeholder_38x38.png";
    private ViewMaster viewMaster;
    private MyLibUtils myLibUtils;
    private MyLibFilter myLibFilter;
    private String filterStringByAuthor;
    private static BookInMyLibRMS selectedBook;

    public MyLibBooksByAuthorViewList(MIDlet parent) {
       super(L10n.getMessage("MY_LIB_BOOKS_BY_AUTHOR_VIEW_LIST_TITLE"));
       viewMaster = ViewMaster.getInstance();
       myLibFilter = MyLibFilter.getInstance();
       myLibUtils = MyLibUtils.getInstance();
 
       this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
       try  {
              imageListIcon=ImageLoader.getInstance().loadImage(BOOKS_BY_AUTHOR_ICON, null);
       } 
       catch(IOException e) {
       } 
        
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectMyLibAuthorListCmd);
        this.setCommandListener(this);    
    }
    
    
    /**
     * Activate MyLibBooksByAuthorViewList 
     */
    public void activate() {
       //hide CategoryBar
       viewMaster.setCategoryBarVisible(false);
        
       // set filterStringByAuthor parameters 
       filterStringByAuthor=myLibFilter.getFilterStringByAuthor();
       myLibFilter.filterMyLibRMSByAuthor();
       myLibRMSByAuthorFilterDataVector=myLibFilter.getMyLibRMSByAuthorFilterDataVector();
       if (myLibRMSByAuthorFilterDataVector !=null) {
            for (int i = 0; i < myLibRMSByAuthorFilterDataVector.size(); i++) {
              BookInMyLibRMS currentBook = (BookInMyLibRMS) myLibRMSByAuthorFilterDataVector.elementAt(i);
              this.append(currentBook.getBookTitle().toLowerCase(),currentBook.getBookTags().toLowerCase(),
                          "",imageListIcon,FancyElement.IMPORTANCE_NONE);
            }
       }
       else {
           this.append(L10n.getMessage("NOT_FOUND_BOOKS_BY_AUTHOR"),"","",null,FancyElement.IMPORTANCE_NONE);
       }    
    }
    
    
    /**
     * Deactivate MyLibBooksByAuthorViewList
     * Empties the List items
     */
    public void deactivate() {
        //delete MyLibBooksByAuthorList
        for (int i=this.size()-1; i >= 0; i--){
            this.delete(i);
        }
    }    
    
    
    public void commandAction(Command command, Displayable d) {
        if (command == viewMaster.getBackCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();
        }
        else if (command == selectMyLibAuthorListCmd) {

//--- 1 --- choice selectedBook
            selectedBook = (BookInMyLibRMS) myLibRMSByAuthorFilterDataVector.elementAt(this.getSelectedIndex());  
            
//--- 2 --- settings selectedBook for read in myLibUtils
            myLibUtils.setSelectedBookForRead(selectedBook);
           
//--- 3 --- Open MyLibBookInfoViewForm            
            viewMaster.getMyLibBookInfoViewForm();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_BOOK_INFO_FORM);
        }           
    }
    
}
