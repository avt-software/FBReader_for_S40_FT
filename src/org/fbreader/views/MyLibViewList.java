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

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import org.fbreader.localization.L10n;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.utils.ImageLoader;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.tantalum.Workable;
import org.tantalum.Worker;


public class MyLibViewList 
             extends FancyCustomList 
             implements View, CommandListener {
    
   private final int MAX_ITEMS = 3;
   private Image imageListIcon;
   private static Command selectMyLibListCmd;
   
    /*
     * ****** arrays for MyLibViewList  *******
    */
    private final String[] TITLE_LIST = {
        L10n.getMessage("BY_AUTHORS_ITEM_TITLE"), 
        L10n.getMessage("BY_TAGS_ITEM_TITLE"), 
        L10n.getMessage("RECENT_BOOKS_ITEM_TITLE") 
    };
    
    private final String[] SUBTITLE_LIST = {
        L10n.getMessage("BY_AUTHORS_ITEM_SUBTITLE"), 
        L10n.getMessage("BY_TAGS_ITEM_SUBTITLE"), 
        L10n.getMessage("RECENT_BOOKS_ITEM_SUBTITLE") 
    };
    
    private final String[] ICON_LIST = {
        "/icons/mylib_by_authors_main_38x38.png",
        "/icons/mylib_by_tags_main_38x38.png",
        "/icons/mylib_recent_book_main_38x38.png"
    };
    
    private ViewMaster viewMaster;
    
    private MyLibUtils myLibUtils;
    

    public MyLibViewList(MIDlet parent) {
       super(L10n.getMessage("MY_LIB_VIEW_LIST_TITLE"));
       viewMaster = ViewMaster.getInstance();
       selectMyLibListCmd=viewMaster.getSelectListItemCmd();
       
       this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));
        
        for (int i = 0; i < MAX_ITEMS; i++) {
            
            try {
                imageListIcon=ImageLoader.getInstance().loadImage(ICON_LIST[i], null);
            } 
            catch(IOException e) {
            
            }    
            this.append(TITLE_LIST[i],SUBTITLE_LIST[i],"",imageListIcon,
                        FancyElement.IMPORTANCE_NONE);
        }
        
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getHelpCmd());
        
        this.setSelectCommand(selectMyLibListCmd);
        this.setCommandListener(this);
    }

    
    /**
     * Activate MyLibViewList 
     */
    public void activate() {
      viewMaster.setCategoryBarVisible(true);
      viewMaster.setCategoryBarSelectedIndex(1); 
        
       Worker.fork(new Workable() { 
            public Object exec(Object in) { 
                   // Do something on a background Worker thread 
                   openMyLib();
                   return in;
            } 
        });     
    }
    
    /**
     * Deactivate MyLibViewList 
     */
    public void deactivate() {
        
    }    
    
    
    private void openMyLib() {
        myLibUtils=MyLibUtils.getInstance();
        if (!myLibUtils.getMyLibDataIsChecked()) {
            myLibUtils.checkMyLibData();
        }   
    }
    
    
    public void commandAction(Command command, Displayable d) {
        if (command == selectMyLibListCmd) {
            if (this.getSelectedIndex()==0) {
                viewMaster.getMyLibAuthorsViewList();
                viewMaster.openView(ViewMaster.VIEW_MY_LIB_AUTHORS_LIST);
            }
            else if (this.getSelectedIndex()==1) {
                viewMaster.getMyLibTagsViewList();
                viewMaster.openView(ViewMaster.VIEW_MY_LIB_TAGS_LIST);
            }
            else if (this.getSelectedIndex()==2) {
                viewMaster.getMyLibRecentBooksViewList();
                viewMaster.openView(ViewMaster.VIEW_MY_LIB_RECENT_BOOKS_LIST);
            }            
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getMyLibViewListHelpForm();
            viewMaster.openView(ViewMaster.VIEW_MY_LIB_LIST_HELP_FORM);            
        }
    }
    
}
