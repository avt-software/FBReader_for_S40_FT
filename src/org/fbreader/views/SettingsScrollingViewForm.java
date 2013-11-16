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


public class SettingsScrollingViewForm 
             extends Form
             implements View, 
                        CommandListener, 
                        ItemStateListener {

    private ViewMaster viewMaster;
    private Settings settings;
    private Main main;
    private boolean settingsStateChanged;
    private boolean scrollingDirection;
    private String scrollingDirectionStr;
    private boolean useTapForScroll;
    private String useTapForScrollStr;
    private boolean useFlickAndSwipeForScroll;
    private String useFlickAndSwipeForScrollStr;
    
    //choiceGroupScrollingDirection
    private final int CHOICE_GROUP_SCROLLING_DIRECTION_ITEM_COUNT = 2;
    private ChoiceGroup choiceGroupScrollingDirection;
    private String choiceGroupScrollingDirectionTitle;
    private final String[] CHOICE_GROUP_SCROLLING_DIRECTION_LIST_ITEMS = {
        L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_SCROLLING_DIRECTION_HORIZONTAL_ITEM"),
        L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_SCROLLING_DIRECTION_VERTICAL_ITEM")
    };
    
    
    //choiceGroupGesturesForScrolling
    private final int CHOICE_GROUP_GESTURES_FOR_SCROLLING_ITEM_COUNT = 2;
    private ChoiceGroup choiceGroupGesturesForScrolling;
    private String choiceGroupGesturesForScrollingTitle;
    private final String[] CHOICE_GROUP_GESTURES_FOR_SCROLLING_LIST_ITEMS = {
        L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_GESTURES_FOR_SCROLLING_TAP_ITEM"),
        L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_GESTURES_FOR_SCROLLING_FLICK_AND_SWIPE_ITEM")
    };    

    
    private final Alert alertSaveChangesMade = new Alert(L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_TITLE"), 
            L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_CONTENT"), null, null); 
    
    private final Alert alertOnSelectedGesturesForScrolling = new Alert(L10n.getMessage("SETTINGS_SCROLLING_ALERT_ON_SELECTED_GESTURES_TITLE"), 
            L10n.getMessage("SETTINGS_SCROLLING_ALERT_ON_SELECTED_GESTURES_CONTENT"), null, null); 
    


    public SettingsScrollingViewForm() {
        //the text for the form title get from SettingsViewList 
        super(L10n.getMessage("SETTINGS_SCROLLING_ITEM_TITLE"));
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
     * Activate SettingsScrollingViewForm and view form data
     */
    public void activate() {
            
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
        
        //control loading a saved settings
        settings.loadSettings();
        
        //At the time when the form is active, 
        //the state of the controls have not changed
        settingsStateChanged=false;

        showSettingsScrolling();      
    }
    
    
    /**
     * Deactivate SettingsScrollingViewForm
     */
    public void deactivate() {
        //delete all elements on the form
        this.deleteAll();
    }
    
    
    //Displaying the controls on the form 
    //to configure ways to scroll 
    //for base text on BookCanvas.java
    public void showSettingsScrolling() {

        
//--- 1 --- Add a choiceGroupScrollingDirection
        
        choiceGroupScrollingDirectionTitle=L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_SCROLLING_DIRECTION_TITLE");
        choiceGroupScrollingDirection = new ChoiceGroup(choiceGroupScrollingDirectionTitle, Choice.POPUP);
        choiceGroupScrollingDirection.addCommand(viewMaster.getSelectChoiceGroupItemCmd());
        for (int i = 0; i < CHOICE_GROUP_SCROLLING_DIRECTION_ITEM_COUNT; i++) {
            
            choiceGroupScrollingDirection.append(CHOICE_GROUP_SCROLLING_DIRECTION_LIST_ITEMS[i], null);
        }
        
        //set current scrollingDirection  as active item
        if (settings.getScrollingDirection()) {
          choiceGroupScrollingDirection.setSelectedIndex(0, true);
          scrollingDirection=true;
        }
        else {
          choiceGroupScrollingDirection.setSelectedIndex(1, true);    
          scrollingDirection=false;
        }
        //get scrollingDirectionStr according to current active item 
        //in choiceGroupScrollingDirection (before changing)      
        scrollingDirectionStr=choiceGroupScrollingDirection.getString(choiceGroupScrollingDirection.getSelectedIndex());
        this.append(choiceGroupScrollingDirection);

        
//--- 2 --- Add a choiceGroupGesturesForScrolling
        
        choiceGroupGesturesForScrollingTitle=L10n.getMessage("SETTINGS_SCROLLING_CHOICE_GROUP_GESTURES_FOR_SCROLLING_TITLE");
        choiceGroupGesturesForScrolling = new ChoiceGroup(choiceGroupGesturesForScrollingTitle, Choice.MULTIPLE);
        choiceGroupGesturesForScrolling.addCommand(viewMaster.getSelectChoiceGroupItemCmd());
        for (int i = 0; i < CHOICE_GROUP_GESTURES_FOR_SCROLLING_ITEM_COUNT; i++) {
            
            choiceGroupGesturesForScrolling.append(CHOICE_GROUP_GESTURES_FOR_SCROLLING_LIST_ITEMS[i], null);
        }
        
//--- 2.1. ---- settings useTapForScrolling 
        if (settings.getUseTapForScroll()) {
          choiceGroupGesturesForScrolling.setSelectedIndex(0, true);
          useTapForScroll=true;
        }
        else if (!settings.getUseTapForScroll()) {
          choiceGroupGesturesForScrolling.setSelectedIndex(0, false);    
          useTapForScroll=true;
        }
        
        //get scrollingDirectionStr according to current active item 
        //in choiceGroupScrollingDirection (before changing)      
        useTapForScrollStr=choiceGroupGesturesForScrolling.getString(0);

        
//--- 2.2. ---- settings useFlickAndSwipeForScrolling 
        if (settings.getUseFlickAndSwipeForScroll()) {
          choiceGroupGesturesForScrolling.setSelectedIndex(1, true);
          useFlickAndSwipeForScroll=true;
        }
        else if (!settings.getUseTapForScroll()) {
          choiceGroupGesturesForScrolling.setSelectedIndex(1, false);    
          useFlickAndSwipeForScroll=true;
        }

        //get scrollingDirectionStr according to current active item 
        //in choiceGroupScrollingDirection (before changing)      
        useFlickAndSwipeForScrollStr=choiceGroupGesturesForScrolling.getString(1);

        this.append(choiceGroupGesturesForScrolling);        
        
        
    }
    

    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {

         if (displayable==this && command==viewMaster.getHelpCmd()) {
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("SETTINGS_SCROLLING_HELP_TEXT_HEADER"),
                                            L10n.getMessage("SETTINGS_SCROLLING_HELP_TEXT_CONTENT"), 
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
                  if (saveChangedSettings()) {
                      viewMaster.backView();
                  }
                  
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
                main.switchDisplayable(null,this); 
                if (saveChangedSettings()) {
                    viewMaster.backView();
                }            
             }
             else if (command==viewMaster.getSaveChangesMadeNoCmd()){
                main.switchDisplayable(null,this); 
                viewMaster.backView();
             }
             else if (command==viewMaster.getSaveChangesMadeBackCmd()){
                main.switchDisplayable(null,this); 
             }
             
         }
         else if(displayable==alertOnSelectedGesturesForScrolling && command==viewMaster.getOkCmd()) {
           main.switchDisplayable(null,this);   
         }
          
  
      }
      
     
      
      
     /**
      * Save changed settings
     */ 
     public boolean saveChangedSettings() {
         
        //If the user has not selected of gestures for scrolling, 
        //a warning and abort the save settings
        if (!useTapForScroll && !useFlickAndSwipeForScroll) {
            showSelectedGesturesForScrollingAlert();
            return false;
        }
        else {
//--- 1 --- scrollingDirection

            //set new scrollingDirection
            settings.setScrollingDirection(scrollingDirection);       

//--- 2 --- useTapForScroll

            //set new useTapForScroll
            settings.setUseTapForScroll(useTapForScroll);        


//--- 3 --- useFlickAndSwipeForScroll

            //set new useFlickAndSwipeForScroll
            settings.setUseFlickAndSwipeForScroll(useFlickAndSwipeForScroll);

            //save all settings changes to RMS "fbreader_settings"
            settings.saveSettings();

            //loading a saved settings 
            settings.loadSettings();

            //after save changes set settingsStateChanged to false
            settingsStateChanged=false; 

            return true;              
        } 
      
                
     } 
      
    /**
     * Set settingsStateChanged in TRUE, if text input changes.     
     * @param item
     */
    public void itemStateChanged(Item item) {
        
        settingsStateChanged=true;
        
        if (item==choiceGroupScrollingDirection) {
            //get scrollingDirection according to current active item 
            //in choiceGroupScrollingDirection (after changing)

            if (choiceGroupScrollingDirection.getSelectedIndex()==0){
              scrollingDirection=true;  
            }
            else {
              scrollingDirection=false;   
            }

            //get scrollingDirectionStr according to current active item 
            //in choiceGroupScrollingDirection (after changing)
            scrollingDirectionStr=choiceGroupScrollingDirection.getString(choiceGroupScrollingDirection.getSelectedIndex());

        }
        else if (item==choiceGroupGesturesForScrolling){

//--- 1 ---- useTapForScroll
            //get useTapForScroll according to current active item 
            //in choiceGroupGesturesForScrolling (after changing)
            if (choiceGroupGesturesForScrolling.isSelected(0)){
              useTapForScroll=true;  
            }
            else if (!choiceGroupGesturesForScrolling.isSelected(0)){
              useTapForScroll=false;   
            }

            //get scrollingDirectionStr according to current active item 
            //in choiceGroupScrollingDirection (after changing)
            useTapForScrollStr=choiceGroupGesturesForScrolling.getString(0);


//--- 2 ---- useFlickAndSwipeForScroll
            //get useFlickAndSwipeForScroll according to current active item 
            //in choiceGroupGesturesForScrolling (after changing)
            if (choiceGroupGesturesForScrolling.isSelected(1)){
              useFlickAndSwipeForScroll=true;  
            }
            else if (!choiceGroupGesturesForScrolling.isSelected(1)){
              useFlickAndSwipeForScroll=false;   
            }

            //get useFlickAndSwipeForScrollStr according to current active item 
            //in choiceGroupGesturesForScrolling (after changing)
            useFlickAndSwipeForScrollStr=choiceGroupGesturesForScrolling.getString(1);
            
        }
    
    }
    
    
    public void showSaveChangesMadeDialog(){
        alertSaveChangesMade.setTimeout(Alert.FOREVER);
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeYesCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeNoCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeBackCmd());
        alertSaveChangesMade.setCommandListener(this);
        
        main.switchDisplayable(alertSaveChangesMade,this);
        
    }
    
    
    public void showSelectedGesturesForScrollingAlert() {
        
        alertOnSelectedGesturesForScrolling.setTimeout(Alert.FOREVER); 
        alertOnSelectedGesturesForScrolling.addCommand(viewMaster.getOkCmd());
        alertOnSelectedGesturesForScrolling.setCommandListener(this);
        main.switchDisplayable(alertOnSelectedGesturesForScrolling,this);
    } 
    
}
