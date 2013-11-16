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
import org.fbreader.Main;
import org.fbreader.settings.Settings;


public class SettingsStatusBarViewForm 
             extends Form
             implements View, 
                        CommandListener, 
                        ItemStateListener {

    private ViewMaster viewMaster;
    private Settings settings;
    private Main main;
    private boolean settingsStateChanged;
    private int showChapterNum;
    private String showChapterNumStr;
    
    //choiceGroupShowChapterNum
    private final int CHOICE_GROUP_SHOW_CHAPTER_NUM_ITEM_COUNT = 2;
    private ChoiceGroup choiceGroupShowChapterNum;
    private String choiceGroupShowChapterNumTitle;
    private final String[] CHOICE_GROUP_SHOW_CHAPTER_NUM_LIST_ITEMS = {
        L10n.getMessage("SETTINGS_SHOW_CHAPTER_NUM_CHOICE_GROUP_HIDE_ITEM"),
        L10n.getMessage("SETTINGS_SHOW_CHAPTER_NUM_CHOICE_GROUP_SHOW_ITEM")
    };

    
    private final Alert alertSaveChangesMade = new Alert(L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_TITLE"), 
            L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_CONTENT"), null, null); 


    public SettingsStatusBarViewForm() {
        //the text for the form title get from SettingsViewList 
        super(L10n.getMessage("SETTINGS_STATUS_BAR_ITEM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        settings=Settings.getInstance();
        main=Main.getInstance();
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getOkCmd());
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);
        setItemStateListener(this);
    }
    
    
    /**
     * Activate SettingsStatusBarViewForm and view form data
     */
    public void activate() {
            
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
        
        //control loading a saved settings
        settings.loadSettings();

        //At the time when the form is active, 
        //the state of the controls have not changed
        settingsStateChanged=false;

        showSettingsStatusBar();      
    }
    
    
    /**
     * Deactivate SettingsStatusBarViewForm
     */
    public void deactivate() {
        //delete all elements on the form
        this.deleteAll();
    }
    
    
    //Displaying the controls on the form 
    //to configure font size and margin width 
    //for base text on BookCanvas.java
    public void showSettingsStatusBar() {
        
//--- 1 --- Add a choiceGroupShowChapterNum
        
        choiceGroupShowChapterNumTitle=L10n.getMessage("SETTINGS_SHOW_CHAPTER_NUM_CHOICE_GROUP_TITLE");
        choiceGroupShowChapterNum = new ChoiceGroup(choiceGroupShowChapterNumTitle, Choice.POPUP);
        choiceGroupShowChapterNum.addCommand(viewMaster.getSelectChoiceGroupItemCmd());
        for (int i = 0; i < CHOICE_GROUP_SHOW_CHAPTER_NUM_ITEM_COUNT; i++) {
            choiceGroupShowChapterNum.append(CHOICE_GROUP_SHOW_CHAPTER_NUM_LIST_ITEMS[i], null);
        }
        
        //set current showChapterNum  as active item
        choiceGroupShowChapterNum.setSelectedIndex(settings.getShowChapterNumber(), true);
      
        //get showChapterNum according to current active item 
        //in choiceGroupShowChapterNum (before changing)
        showChapterNum=choiceGroupShowChapterNum.getSelectedIndex();
        
        //get showChapterNumStr according to current active item 
        //in choiceGroupShowChapterNum (before changing)      
        showChapterNumStr=choiceGroupShowChapterNum.getString(choiceGroupShowChapterNum.getSelectedIndex());
        
        this.append(choiceGroupShowChapterNum);

    }
    

    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {

         if (displayable==this && command==viewMaster.getHelpCmd()) {
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("SETTINGS_SHOW_CHAPTER_NUM_HELP_TEXT_HEADER"),
                                            L10n.getMessage("SETTINGS_SHOW_CHAPTER_NUM_HELP_TEXT_CONTENT"), 
                                            false);
            
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);               
         } 

         else if (displayable==this && command==viewMaster.getOkCmd()) {
            //if controls not changed exit SettingsBooksFolderViewForm
            //and back to previous screen
            if (!settingsStateChanged){
                viewMaster.backView();
            }  
            else {
                  saveChangedSettings();
                  viewMaster.backView();
            }    
                           
         }
         
         else if (displayable==this && command == viewMaster.getBackCmd()) {
            //if controls not changed exit SettingsFontSizeViewForm
            //and back to previous screen
            if (!settingsStateChanged){
                viewMaster.backView();
            }  
            else {
              showSaveChangesMadeDialog();   
            }  
        }
         else if (displayable==alertSaveChangesMade) {
             if (command==viewMaster.getSaveChangesMadeYesCmd()){
                saveChangedSettings();
                main.switchDisplayable(null,this); 
                viewMaster.backView();                
             }
             else if (command==viewMaster.getSaveChangesMadeNoCmd()){
                main.switchDisplayable(null,this); 
                viewMaster.backView();
             }
             else if (command==viewMaster.getSaveChangesMadeBackCmd()){
                main.switchDisplayable(null,this); 
             }
             
         }
          
  
      }
      
      
     /**
      * Save changed settings
     */ 
     public boolean saveChangedSettings() {

        //set new showChapterNum
        settings.setShowChapterNumber(showChapterNum);

        //save all settings changes to RMS "fbreader_settings"
        settings.saveSettings();

        //loading a saved settings 
        settings.loadSettings();

        //after save changes set settingsStateChanged to false
        settingsStateChanged=false; 

        //after save changes set settingsStateSaved to true
        //settingsStateSaved=true;                 
        return true;        
                
     } 
      
      
    /**
     * Set settingsStateChanged in TRUE, if text input changes.     
     * @param item
     */
    public void itemStateChanged(Item item) {
        
        settingsStateChanged=true;
        
        //get showChapterNum according to current active item 
        //in choiceGroupShowChapterNum (after changing)
        showChapterNum=choiceGroupShowChapterNum.getSelectedIndex();
        
        //get showChapterNumStr according to current active item 
        //in choiceGroupShowChapterNum (after changing)
        showChapterNumStr=choiceGroupShowChapterNum.getString(choiceGroupShowChapterNum.getSelectedIndex());
    }
    
    
    public void showSaveChangesMadeDialog(){
        alertSaveChangesMade.setTimeout(Alert.FOREVER);
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeYesCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeNoCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeBackCmd());
        alertSaveChangesMade.setCommandListener(this);
        
        main.switchDisplayable(alertSaveChangesMade,this);
        
    }

    
    
}
