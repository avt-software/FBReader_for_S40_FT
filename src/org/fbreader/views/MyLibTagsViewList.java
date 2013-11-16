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

public class MyLibTagsViewList 
             extends FancyCustomList 
             implements View, CommandListener {
    
   
   private Image imageListIcon;
   public static final Command selectMyLibTagsListCmd = 
           new Command("Select", Command.OK, 1);
   
   private Vector myLibRMSDataVector=null;

   private final String BY_TAGS_ICON = 
            "/icons/mylib_by_tags_38x38.png";
    
    private ViewMaster viewMaster;
    
    private MyLibUtils myLibUtils;
    
    private MyLibFilter myLibFilter;
    private String filterStringByTag;
    
    private final Alert alertTagsNotFound = new Alert(L10n.getMessage("TAGS_NOT_FOUND_ALERT_TITLE"), 
            L10n.getMessage("TAGS_NOT_FOUND_ALERT_CONTENT"), null, null);    
    
    

    public MyLibTagsViewList(MIDlet parent) {
       super(L10n.getMessage("MY_LIB_TAGS_VIEW_LIST_TITLE"));
       viewMaster = ViewMaster.getInstance();
       myLibUtils = MyLibUtils.getInstance();
       myLibFilter = MyLibFilter.getInstance();
          
       this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        
       try  {
              imageListIcon=ImageLoader.getInstance().loadImage(BY_TAGS_ICON, null);
            } 
       catch(IOException e) {
            
       }
 
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectMyLibTagsListCmd);
        this.setCommandListener(this);        

    }

    
    /**
     * Activate MyLibTagsViewList 
     */
    public void activate() {
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
        
       //read MyLibRMS 
       myLibUtils.readMyLibRMS();
       //get myLibRMSDataVector
       myLibRMSDataVector=myLibUtils.getMyLibRMSDataVector();        
        
       //get MyLibRMSDataVector
       myLibRMSDataVector=myLibUtils.getMyLibRMSDataVector();

       //View MyLibTags List
       if (myLibRMSDataVector !=null) {
            for (int i = 0; i < myLibRMSDataVector.size(); i++) {
              BookInMyLibRMS currentBook = (BookInMyLibRMS) myLibRMSDataVector.elementAt(i);
              this.append(currentBook.getBookTags().toLowerCase(),currentBook.getBookTitle().toLowerCase(),"",imageListIcon,
                             FancyElement.IMPORTANCE_NONE);
            }
       }
       else {
           showTagsNotFoundAlert();
       }    

        
    }
    
    /*
    * Deactivate MyLibTagsViewList 
    */
    public void deactivate() {
        //delete MyLibTagsList
        for (int i=this.size()-1; i >= 0; i--){
            this.delete(i);
        }
    }    
    
    
    public void commandAction(Command command, Displayable displayable) {
        if (displayable==alertTagsNotFound && command == viewMaster.getDismissCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();            
        }        
        if (command == viewMaster.getBackCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();
        }
        else if (command == selectMyLibTagsListCmd) {
            BookInMyLibRMS currentBook = (BookInMyLibRMS) myLibRMSDataVector.elementAt(this.getSelectedIndex());  
            filterStringByTag=currentBook.getBookTags();

            myLibFilter.setFilterStringByTag(filterStringByTag);
            
            viewMaster.getMyLibBooksByTagViewList();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_BOOKS_BY_TAG_LIST);
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getMyLibTagsViewListHelpForm();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_TAGS_LIST_HELP_FORM);            
        }        
    }
    
    public void showTagsNotFoundAlert() {
        alertTagsNotFound.setTimeout(Alert.FOREVER); 
        alertTagsNotFound.addCommand(viewMaster.getDismissCmd());
        alertTagsNotFound.setCommandListener(this);         
        Main.getInstance().switchDisplayable(alertTagsNotFound,this);
    }     
    
    
}
