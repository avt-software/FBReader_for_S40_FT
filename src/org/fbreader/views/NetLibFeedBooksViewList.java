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
import org.fbreader.utils.ImageLoader;
import org.fbreader.views.list.CustomList;
import org.fbreader.views.list.FancyCustomList;
import org.fbreader.Main;
import org.fbreader.localization.L10n;

public class NetLibFeedBooksViewList 
             extends FancyCustomList 
             implements View, CommandListener {
    
   private static NetLibFeedBooksViewList self = null;
    
   private final int MAX_ITEMS = 2;
   private Image imageListIcon;
   public static Command selectFeedBooksListItemCmd;

    /*
     * ****** arrays for NetLibViewList  *******
    */
    private final String[] TITLE_LIST = {
        L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_ITEM_TITLE"), 
        L10n.getMessage("FEEDBOOKS_ORIGINAL_ITEM_TITLE") 
        
    };
    
    private final String[] SUBTITLE_LIST = {
        L10n.getMessage("FEEDBOOKS_PUBLIC_DOMAIN_ITEM_SUBTITLE"), 
        L10n.getMessage("FEEDBOOKS_ORIGINAL_ITEM_SUBTITLE") 
        
    };
    
    private final String FEEDBOOKS_ICON = 
            "/icons/netlib_feedbooks_38x38.png";
    
    private ViewMaster viewMaster;
    
    private String viewTitleStr;
    
    

    public NetLibFeedBooksViewList(MIDlet parent) {
       super("");
       this.viewTitleStr=ViewMaster.getInstance().getNextViewTitle();         
       viewMaster = ViewMaster.getInstance();
       selectFeedBooksListItemCmd=viewMaster.getSelectListItemCmd();

       this.setTheme(CustomList.createTheme(Display.getDisplay(parent)));

       try  {
              imageListIcon=ImageLoader.getInstance().loadImage(FEEDBOOKS_ICON, null);
            } 
       catch(IOException e) {
       }        
       
       for (int i = 0; i < MAX_ITEMS; i++) {
            this.append(TITLE_LIST[i],SUBTITLE_LIST[i],"",imageListIcon,
                        FancyElement.IMPORTANCE_NONE);
        }
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectFeedBooksListItemCmd);
        this.setCommandListener(this);
    }
    
    
    /**
     * @return NetLibFeedBooksViewList singleton
     */
    public static NetLibFeedBooksViewList getInstance() {
        if (self == null) {
            self = new NetLibFeedBooksViewList(Main.getInstance());
        }
        return self;
    }     

    
    /**
     * Activate NetLibFeedBooksViewList
     */
    public void activate() {
        //set View Title
        this.setTitle(this.viewTitleStr);
        //hide CategoryBar 
        viewMaster.setCategoryBarVisible(false);
    }
    
    /**
     *  Deactivate NetLibFeedBooksViewList
     */
    public void deactivate() {

    }    
    
    
    public void commandAction(Command command, Displayable d) {
        if (command == selectFeedBooksListItemCmd) {
            
            viewMaster.setNextViewTitle(TITLE_LIST[this.getSelectedIndex()]);

            if (this.getSelectedIndex()==0) {
                viewMaster.getNetLibFeedBooksPublicDomainCatalogViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_PUBLIC_DOMAIN_CATALOG_LIST);
            }
            else if (this.getSelectedIndex()==1) {
                viewMaster.getNetLibFeedBooksOriginalCatalogViewList();
                viewMaster.openView(ViewMaster.VIEW_FEEDBOOKS_ORIGINAL_CATALOG_LIST);
            }
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("FEEDBOOKS_VIEW_LIST_FORM_HELP_TEXT_HEADER"),
                                            L10n.getMessage("FEEDBOOKS_VIEW_LIST_FORM_HELP_TEXT_CONTENT"), 
                                            false);
            
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);            
        }
        

        else if (command == viewMaster.getBackCmd()) {
            viewMaster.backView();
        }        

    }

}
