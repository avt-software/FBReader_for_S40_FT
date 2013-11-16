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
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import java.util.Vector;
import org.fbreader.localization.L10n;
import org.fbreader.Main;
import org.fbreader.settings.Settings;
import org.fbreader.utils.mylib.MyLibUtils;
import org.tantalum.j2me.RMSUtils;

public class SettingsBooksFolderViewForm 
             extends Form
             implements View, 
                        CommandListener, 
                        ItemStateListener {

    private ViewMaster viewMaster;
    private Settings settings;
    private Main main;
    private MyLibUtils myLibUtils;
    
    private boolean settingsStateChanged;
    
    //textFieldFolderName
    private final int MAX_CHARS = 256;
    private final int TEXT_FIELD_BOOKS_FOLDER_NANE_TYPE = TextField.ANY;
    private TextField textFieldBooksFolderName;
    private String    textFieldBooksFolderNameTitle;
    private String    textFieldBooksFolderNameText;
    
    //choiceGroupLocationBooksFolder
    private final int CHOICE_GROUP_LOCATION_BOOKS_FOLDER_ITEM_COUNT = 2;
    private ChoiceGroup choiceGroupLocationBooksFolder;
    private String choiceGroupLocationBooksFolderTitle;
    private final String[] CHOICE_GROUP_LOCATION_BOOKS_FOLDER_LIST_ITEMS = {
        L10n.getMessage("SETTINGS_BOOKS_FOLDER_CHOICE_GROUP_PHONE_MEMORY_ITEM"),
        L10n.getMessage("SETTINGS_BOOKS_FOLDER_CHOICE_GROUP_MEMORY_CARD_ITEM")
    };
    
    
    private final Alert alertMemoryCardNotFound = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_CONTENT"), null, null);
    
    private final Alert alertSaveChangesMade = new Alert(L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_TITLE"), 
            L10n.getMessage("SETTINGS_SAVE_CHANGES_DIALOG_CONTENT"), null, null); 

    private Alert alertFolderAlreadyExists;
    private Alert alertFolderCreated;

    private final Alert alertInvalidFolderName = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_INVALID_FOLDER_NAME_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_INVALID_FOLDER_NAME_CONTENT"), null, null); 
    
    
    private String booksFolderLocation;
    private String booksFolderPath;
    private String newBooksFolderURL;
    private String newBooksFolderName;
    private FileConnection fcBooksFolder;
    
    public SettingsBooksFolderViewForm() {
        //the text for the form title get from SettingsViewList 
        super(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ITEM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        settings=Settings.getInstance();
        main=Main.getInstance();
        myLibUtils=MyLibUtils.getInstance();
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(viewMaster.getOkCmd());
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);
        setItemStateListener(this);
    }
    
    
    /**
     * Activate SettingsBooksFolderViewForm and view form data
     */
    public void activate() {
            
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);

        //At the time when the form is active, 
        //the state of the controls have not changed
        settingsStateChanged=false;

        showSettingsBooksFolder();      
    }
    
    
    /**
     * Deactivate SettingsBooksFolderViewForm
     */
    public void deactivate() {
        //delete all elements on the form
        this.deleteAll();
    }
    
    
    //Displaying the controls on the form 
    //to configure the name and location of the folder to store books
    public void showSettingsBooksFolder() {

//--- 1 --- Add textFieldFolderName      
        textFieldBooksFolderNameTitle=L10n.getMessage("SETTINGS_BOOKS_FOLDER_TEXT_FIELD_TITLE");
        textFieldBooksFolderNameText=settings.getBooksFolderName();
        textFieldBooksFolderName = new TextField(textFieldBooksFolderNameTitle, textFieldBooksFolderNameText, MAX_CHARS, TEXT_FIELD_BOOKS_FOLDER_NANE_TYPE);
        this.append(textFieldBooksFolderName);
        
//--- 2 --- Add a choiceGroupLocationBooksFolder
        choiceGroupLocationBooksFolderTitle=L10n.getMessage("SETTINGS_BOOKS_FOLDER_CHOICE_GROUP_TITLE");
        choiceGroupLocationBooksFolder = new ChoiceGroup(choiceGroupLocationBooksFolderTitle, Choice.POPUP);
        choiceGroupLocationBooksFolder.addCommand(viewMaster.getSelectChoiceGroupItemCmd());
        for (int i = 0; i < CHOICE_GROUP_LOCATION_BOOKS_FOLDER_ITEM_COUNT; i++) {
            
            choiceGroupLocationBooksFolder.append(CHOICE_GROUP_LOCATION_BOOKS_FOLDER_LIST_ITEMS[i], null);
        }
        if (settings.getBooksFolderLocation().equals("phone memory")) {
            choiceGroupLocationBooksFolder.setSelectedIndex(0, true);
        }
        else if (settings.getBooksFolderLocation().equals("memory card")) {
            choiceGroupLocationBooksFolder.setSelectedIndex(1, true);
        }
        
        //get booksFolderLocation according to current active item 
        //in choiceGroupLocationBooksFolder (before changing)
        booksFolderLocation=choiceGroupLocationBooksFolder.getString(choiceGroupLocationBooksFolder.getSelectedIndex());
        this.append(choiceGroupLocationBooksFolder);         
    }
    

    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {

        if (displayable==this && command==viewMaster.getHelpCmd()) {
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("SETTINGS_BOOKS_FOLDER_HELP_TEXT_HEADER"),
                                            L10n.getMessage("SETTINGS_BOOKS_FOLDER_HELP_TEXT_CONTENT"), 
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
            }    
         }
         else if (displayable==this && command == viewMaster.getBackCmd()) {
            //if controls not changed exit SettingsBooksFolderViewForm
            //and back to previous screen
            if (!settingsStateChanged){
                //show CategoryBar
                //viewMaster.setCategoryBarVisible(true);                
                viewMaster.backView();
            }  
            else {
              showSaveChangesMadeDialog();   
            }  
        }        
        else if (displayable==alertMemoryCardNotFound && command==viewMaster.getOkCmd()) {
            main.switchDisplayable(null,this); 
        }
        else if (displayable==alertInvalidFolderName && command==viewMaster.getOkCmd()) {
            main.switchDisplayable(null,this); 
        }         
        else if (displayable==alertFolderAlreadyExists && command==viewMaster.getOkCmd()) {
            main.switchDisplayable(null,this); 
         }
         else if (displayable==alertFolderCreated && command==viewMaster.getOkCmd()) {
            main.switchDisplayable(null,this); 
            viewMaster.backView();
         }
         else if (displayable==alertSaveChangesMade) {
             if (command==viewMaster.getSaveChangesMadeYesCmd()){
                saveChangedSettings();
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

        newBooksFolderName=textFieldBooksFolderName.getString();
        //if newBooksFolderName="" don't save settings
        if (newBooksFolderName.equals("")){
            showInvalidFolderNameAlert();
            return false;
        }
        if (booksFolderLocation.equals("phone memory")) {
            booksFolderPath="file:///c:/predefgallery/predeffilereceived/";   
        }
        else if (booksFolderLocation.equals("memory card")) {
            String rootMC = System.getProperty("fileconn.dir.memorycard");  
            booksFolderPath=rootMC; 
        }

        //create new books folder
        newBooksFolderURL=booksFolderPath+newBooksFolderName+"/";                

        try {
            //Opens a file connection in  READ_WRITE mode
            fcBooksFolder = (FileConnection)Connector.open(newBooksFolderURL, Connector.READ_WRITE);

             //check if books folder not exists
             if (!fcBooksFolder.exists()){

                    fcBooksFolder.mkdir();

                    settings.setBooksFolderName(newBooksFolderName);
                    settings.setBooksFolderURL(newBooksFolderURL);

                    folderCreatedAlert(newBooksFolderName); 
             }
             else {
                   settings.setBooksFolderName(newBooksFolderName);
                   settings.setBooksFolderURL(newBooksFolderURL);
                   folderAlreadyExistsAlert(textFieldBooksFolderName.getString());                        
             }
             //delete BOOKS_FOLDER_STATE_CACHE_KEY record store cache
             //during save new settings in SettingsBooksFolderViewForm::saveChangedSettings()
             Vector cachedRecordStoreNamesDataVector =RMSUtils.getCachedRecordStoreNames();
             for (int i=0; i < cachedRecordStoreNamesDataVector.size(); i++) {
                  String cachedRecordStoreNameStr=(String)cachedRecordStoreNamesDataVector.elementAt(i);
                  if (cachedRecordStoreNameStr.indexOf(myLibUtils.getBooksFolderStateCacheKey())!=-1 ) {
                      RMSUtils.delete(cachedRecordStoreNameStr);
                      cachedRecordStoreNamesDataVector.removeElementAt(i);
                      break;
                   }
                }
             //delete MyLibRMS
             myLibUtils.deleteMyLibRMS();

             myLibUtils.setMyLibDataIsChecked(false);

             fcBooksFolder.close();

        }
        catch (IOException ioe) {
                 //#debug
                Main.LOGGER.logError("SettingsBooksFolderViewForm::saveChangedSettings()->IOException:: "+ioe.getMessage());  
        }
        catch (IllegalArgumentException iaex) {
            //#debug
            Main.LOGGER.logError("SettingsBooksFolderViewForm::saveChangedSettings()->IllegalArgumentException: "+iaex.getMessage());  
            showInvalidFolderNameAlert();
            return false;
        }              

        //set booksFolderLocation
        settings.setBooksFolderLocation(booksFolderLocation);

        //save all settings changes to RMS "fbreader_settings"
        settings.saveSettings();

        //loading a saved settings 
        settings.loadSettings();

        //after save changes set settingsStateChanged to false
        settingsStateChanged=false; 

        return true;        

     } 
      
      
    /**
     * Set settingsStateChanged in TRUE, if text input changes.     
     * @param item
     */
    public void itemStateChanged(Item item) {
        
        settingsStateChanged=true;

        if (item==choiceGroupLocationBooksFolder){
              
              if (choiceGroupLocationBooksFolder.getSelectedIndex()==0) {
                
                //Assign a value to a variable booksFolderLocation 
                //in selecting the appropriate item from choiceGroupLocationBooksFolder 
                booksFolderLocation="phone memory";  
                
              }
              else if (choiceGroupLocationBooksFolder.getSelectedIndex()==1) {
                  
                  String rootsNames = System.getProperty("fileconn.dir.roots.names");
                  
                  //Check Localized names corresponding to available roots  
                  //Series 40 return value: 
                  //1) phone;memory card; if available phone memory & memory card;
                  //2) phone;             if available only phone memory;
                  
                  if(rootsNames.equalsIgnoreCase("phone;memory card;")) {
                    //Assign a value to a variable booksFolderLocation 
                    //in selecting the appropriate item from choiceGroupLocationBooksFolder 
                    booksFolderLocation="memory card";                        
                   }  
                  //if root directory of a memory card is null,
                  //then a memory card is absent in the device
                  else {
                      choiceGroupLocationBooksFolder.setSelectedIndex(0, true);
                      booksFolderLocation="phone memory";
                      showMemoryCardNotFoundAlert();
                  }  
  
               }

        }        
    }
    
    public void showMemoryCardNotFoundAlert() {
        
        alertMemoryCardNotFound.setTimeout(Alert.FOREVER); 
        alertMemoryCardNotFound.addCommand(viewMaster.getOkCmd());
        alertMemoryCardNotFound.setCommandListener(this);
        main.switchDisplayable(alertMemoryCardNotFound,this);
    }   
    
    
    public void folderAlreadyExistsAlert(String booksFolderName) {
      
        alertFolderAlreadyExists = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALREADY_EXISTS_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALREADY_EXISTS_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALREADY_EXISTS_CONTENT_2")
            + CHOICE_GROUP_LOCATION_BOOKS_FOLDER_LIST_ITEMS[choiceGroupLocationBooksFolder.getSelectedIndex()], null, null);
        
        
        alertFolderAlreadyExists.setTimeout(Alert.FOREVER); 
        alertFolderAlreadyExists.addCommand(viewMaster.getOkCmd());
        alertFolderAlreadyExists.setCommandListener(this);
        main.switchDisplayable(alertFolderAlreadyExists,this);        
    }
    
    public void folderCreatedAlert(String booksFolderName) {
       
        alertFolderCreated = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_FOLDER_CREATED_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_FOLDER_CREATED_CONTENT_1")
            +"\""+booksFolderName+"\""
            +L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_FOLDER_CREATED_CONTENT_2")
            + CHOICE_GROUP_LOCATION_BOOKS_FOLDER_LIST_ITEMS[choiceGroupLocationBooksFolder.getSelectedIndex()], null, null);
        
        
        alertFolderCreated.setTimeout(Alert.FOREVER); 
        alertFolderCreated.addCommand(viewMaster.getOkCmd());
        alertFolderCreated.setCommandListener(this);
        main.switchDisplayable(alertFolderCreated,this);        
    }
    
    
    
    public void showSaveChangesMadeDialog(){
        alertSaveChangesMade.setTimeout(Alert.FOREVER);
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeYesCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeNoCmd());
        alertSaveChangesMade.addCommand(viewMaster.getSaveChangesMadeBackCmd());
        alertSaveChangesMade.setCommandListener(this);
        
        main.switchDisplayable(alertSaveChangesMade,this);
        
    }

    
    
    
    public void showInvalidFolderNameAlert() {
        
        alertInvalidFolderName.setTimeout(Alert.FOREVER); 
        alertInvalidFolderName.addCommand(viewMaster.getOkCmd());
        alertInvalidFolderName.setCommandListener(this);
        main.switchDisplayable(alertInvalidFolderName,this);
    }
    
    
}
