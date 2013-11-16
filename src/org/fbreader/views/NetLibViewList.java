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
import javax.microedition.midlet.MIDlet;
import java.io.IOException;
import org.fbreader.utils.ImageLoader;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.localization.L10n;
import org.fbreader.utils.mylib.MyLibUtils;

public class NetLibViewList 
             extends FancyCustomList 
             implements View, CommandListener {
    
   private final int MAX_ITEMS = 2;
   private Image imageListIcon;
   public static Command selectNetLibListItemCmd; 
   
    /*
     * ****** arrays for NetLibViewList  *******
    */
    private final String[] TITLE_LIST = {
        L10n.getMessage("MANYBOOKS_ITEM_TITLE"), 
        L10n.getMessage("FEEDBOOKS_ITEM_TITLE") 
        
    };
    
    private final String[] SUBTITLE_LIST = {
        L10n.getMessage("MANYBOOKS_ITEM_SUBTITLE"), 
        L10n.getMessage("FEEDBOOKS_ITEM_SUBTITLE") 
        
    };
    
    private final String[] ICON_LIST = {
        "/icons/netlib_manybooks_38x38.png",
        "/icons/netlib_feedbooks_38x38.png"
    };
    
    private ViewMaster viewMaster;
    
    private MyLibUtils myLibUtils;
    

    public NetLibViewList(MIDlet parent) {
       super(L10n.getMessage("NET_LIB_VIEW_LIST_TITLE"));
       viewMaster = ViewMaster.getInstance();
       
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
        viewMaster.setCategoryBarSelectedIndex(2);
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getDisclaimerCmd());
        selectNetLibListItemCmd=viewMaster.getSelectListItemCmd();
        this.setSelectCommand(selectNetLibListItemCmd);
        this.setCommandListener(this);
    }

    
    /**
     * Activate MyLibViewList 
     */
    public void activate() {
        //show CategoryBar 
        viewMaster.setCategoryBarVisible(true);         
    }
    
    
    /**
     * Deactivate MyLibViewList 
     */
    public void deactivate() {
        
    }    
    
    
    public void commandAction(Command command, Displayable d) {
        if (command == selectNetLibListItemCmd) {
            if (this.getSelectedIndex()==0) {
                viewMaster.setNextViewTitle(L10n.getMessage("MANYBOOKS_ITEM_TITLE"));
                viewMaster.getNetLibManyBooksRSSFeedsViewList();
                viewMaster.openView(ViewMaster.VIEW_MANYBOOKS_RSS_FEEDS_LIST);        
            }
            else if (this.getSelectedIndex()==1) {
                viewMaster.setNextViewTitle(L10n.getMessage("FEEDBOOKS_ITEM_TITLE"));
                viewMaster.getNetLibFeedBooksViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_LIST);
            }
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("NET_LIB_VIEW_LIST_FORM_HELP_TEXT_HEADER"),
                                            L10n.getMessage("NET_LIB_VIEW_LIST_FORM_HELP_TEXT_CONTENT"), 
                                            true);
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);            
        }
        else if (command == viewMaster.getDisclaimerCmd()) {
            viewMaster.getUniversalHelpForm(L10n.getMessage("DISCLAIMER_FORM_TITLE"),
                                            L10n.getMessage("NET_LIB_VIEW_LIST_FORM_DISCLAIMER_TEXT_HEADER"),
                                            L10n.getMessage("NET_LIB_VIEW_LIST_FORM_DISCLAIMER_TEXT_CONTENT"), 
                                            true);
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);             
          }        
        
    }
    
}
