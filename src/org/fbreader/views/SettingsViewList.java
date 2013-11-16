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
import org.fbreader.localization.L10n;


public final class SettingsViewList 
                    extends List 
                    implements View, CommandListener {

   private static SettingsViewList self = null;
    
   private final int MAX_ITEMS = 4;
   public static Command selectSettingsListItemCmd;
   

    /*
     * ****** arrays for SettingsViewList  *******
    */
    private final String[] SETTINGS_TITLE_LIST = {
        L10n.getMessage("SETTINGS_BOOKS_FOLDER_ITEM_TITLE"), 
        L10n.getMessage("SETTINGS_TEXT_ITEM_TITLE"),
        L10n.getMessage("SETTINGS_STATUS_BAR_ITEM_TITLE"),
        L10n.getMessage("SETTINGS_SCROLLING_ITEM_TITLE"),
    };

   private static String settingsListTitle;
   private static int settingsListType;
   private ViewMaster viewMaster;

   public SettingsViewList(String settingsListTitle, int settingsListType) {
        super(settingsListTitle, settingsListType); 
        this.settingsListTitle=settingsListTitle;
        this.settingsListType=settingsListType;
          viewMaster = ViewMaster.getInstance();
        for (int i = 0; i < MAX_ITEMS; i++) {
            this.append(SETTINGS_TITLE_LIST[i],null);
        }        
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getHelpCmd());
        selectSettingsListItemCmd=viewMaster.getSelectListItemCmd();
        this.setSelectCommand(selectSettingsListItemCmd);
        this.setCommandListener(this);          
    }
    
    

    /**
     * @return SettingsViewList singleton
     */
   
    public static SettingsViewList getInstance() {
        if (self == null) {
            self = new SettingsViewList(settingsListTitle,settingsListType);
        }
        return self;
    }     
    
    

    
     /**
     * Activate SettingsViewList 
     */
    public void activate() {
        //show CategoryBar 
        viewMaster.setCategoryBarVisible(true);
        viewMaster.setCategoryBarSelectedIndex(3); 
    }
    

    /**
     * Deactivate SettingsViewList 
     * 
     */
    public void deactivate() {
    }
    


    public void commandAction(final Command command, final Displayable displayable) {
        if (command == selectSettingsListItemCmd) {
            if (this.getSelectedIndex()==0) {
                viewMaster.getSettingsBooksFolderViewForm();
                viewMaster.openView(ViewMaster.VIEW_SETTINGS_BOOKS_FOLDER_FORM);
            }
            else if (this.getSelectedIndex()==1) {
                viewMaster.getSettingsTextViewForm();
                viewMaster.openView(ViewMaster.VIEW_SETTINGS_TEXT_FORM);
            }
            else if (this.getSelectedIndex()==2) {
                viewMaster.getSettingsStatusBarViewForm();
                viewMaster.openView(ViewMaster.VIEW_SETTINGS_STATUS_BAR_FORM);
            }
            else if (this.getSelectedIndex()==3) {
                viewMaster.getSettingsScrollingViewForm();
                viewMaster.openView(ViewMaster.VIEW_SETTINGS_SCROLLING_FORM);
            }             
        }
        else if (command == viewMaster.getHelpCmd()){
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("SETTINGS_VIEW_LIST_HELP_TEXT_HEADER"),
                                            L10n.getMessage("SETTINGS_VIEW_LIST_HELP_TEXT_CONTENT"), 
                                            true);
            
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);             
        }        
 
    }

}
