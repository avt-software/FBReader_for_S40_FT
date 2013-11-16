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
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.fbreader.Main;
import org.fbreader.localization.L10n;
import org.fbreader.settings.Settings;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.models.BookInNetLib;
import org.fbreader.uihelpers.Visual;
import org.tantalum.net.HttpGetter;
import org.tantalum.Task;


/**
 * Class for displaying on the form parameters of the selected book
 * in Network Library
 * 
 */

public class NetLibBookInfoViewForm 
       extends Form
       implements View, CommandListener, ItemCommandListener {
    
   private ViewMaster viewMaster;
   private MyLibUtils myLibUtils;
   private static BookInNetLib selectedBookForDownload;
   public static final Command downloadBookCmd = new Command("", Command.ITEM, 1); 
    //public static final Command cancelCmd = new Command("CANCEL", Command.CANCEL, 0); 
   private StringItem stringItemDownloadBookButton;
   private HttpGetter httpGetter;
    String booksFolderURL;
    String downloadBookURL;
    String downloadBookURLAfterResponseCode302;
    String downloadBookFileName;
    String downloadBookForSaveURL;
    String locationHeader;
    int responseCode;
    FileConnection fcDownloadBookForSaveURL;
 
    private final Alert alertDownloadingBook = new Alert(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_TITLE"), 
            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_CONTENT"), null, null);               

    private final Alert alertDownloadedBookSuccess = new Alert(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_DOWNLOADED_BOOK_DIALOG_TITLE"), 
            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_DOWNLOADED_BOOK_DIALOG_CONTENT"), null, null);               
   

    private final Alert alertNetworkError = new Alert(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_NET_ERROR_CONTENT"), null, null);

    private final Alert alertMemoryCardNotFound = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_CONTENT"), null, null);
    
    
    private Alert alertBooksFolderAccessDenied;

    
    public NetLibBookInfoViewForm() {
        super(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        myLibUtils=MyLibUtils.getInstance();
        this.addCommand(viewMaster.getBackCmd());
        this.addCommand(viewMaster.getHelpCmd());
        setCommandListener(this);
    }
    
    
    
    /**
     * Activate NetLibBookInfoViewForm and 
     * view info about selected book 
     */
    public void activate() {
        //Runs the garbage collector
        System.gc();
        
        //show info about selected book
        showSelectedBookInfo();      
    }
    
    
    /**
     * Deactivate NetLibBookInfoViewForm 
     */
    public void deactivate() {
        //Empties the form
        deleteAll();
        //Runs the garbage collector
        System.gc();        
    }
    
    
     
    /**
     * show info about selected book
    */
    public void showSelectedBookInfo() {
        
            //Get selected book info for read
            try {
                selectedBookForDownload=viewMaster.getSelectedBookForDownload();
            }
            catch (Exception e) {
                //#debug
                Main.LOGGER.logError("MyLibBookInfoViewForm::activate()"+e.getMessage());
            }
//--- 1 --- Book Title
        StringItem stringItemBookTitle = 
                new StringItem(L10n.getMessage("BOOK_TITLE_STRING_ITEM_TITLE"),
                              selectedBookForDownload.getBookTitle().toLowerCase());
        this.append(stringItemBookTitle);
        
//--- 2 --- Book Author
        StringItem stringItemBookAuthor = 
                new StringItem(L10n.getMessage("BOOK_AUTHOR_STRING_ITEM_TITLE"),
                              selectedBookForDownload.getBookAuthor().toLowerCase());
        this.append(stringItemBookAuthor);         
        
//--- 3 --- Book Tags
        StringItem stringItemBookTags = 
                new StringItem(L10n.getMessage("BOOK_TAGS_STRING_ITEM_TITLE"),
                              selectedBookForDownload.getBookTags().toLowerCase());
        this.append(stringItemBookTags);         

//--- 4 --- Book Language
        StringItem stringItemBookLang = 
                new StringItem(L10n.getMessage("BOOK_LANG_STRING_ITEM_TITLE"),
                              selectedBookForDownload.getBookLang().toLowerCase());
        this.append(stringItemBookLang);
        
//--- 5 --- Download Book Button
        stringItemDownloadBookButton = 
                new StringItem("", L10n.getMessage("DOWNLOAD_BOOK_BUTTON"), Item.BUTTON);
        stringItemDownloadBookButton.addCommand(downloadBookCmd);
        stringItemDownloadBookButton.setDefaultCommand(downloadBookCmd);
        stringItemDownloadBookButton.setItemCommandListener(this);
        stringItemDownloadBookButton.setFont(Visual.SMALL_FONT);
        stringItemDownloadBookButton.setPreferredSize(-1, stringItemDownloadBookButton.getPreferredHeight()+8);
        stringItemDownloadBookButton.setLayout(Item.LAYOUT_EXPAND);        
        this.append(stringItemDownloadBookButton);
    }
    
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {

        if (displayable==alertBooksFolderAccessDenied && command==viewMaster.getDismissCmd()){
              Main.getInstance().switchDisplayable(null,this);
        }           
        if (displayable==alertMemoryCardNotFound && command==viewMaster.getDismissCmd()){
              Main.getInstance().switchDisplayable(null,this);
        }
        else if (displayable==alertNetworkError && command==viewMaster.getDismissCmd()) {
            Main.getInstance().switchDisplayable(null,this); 
        }        
        else if (displayable==alertDownloadedBookSuccess) {
            
            //Settings flag setMyLibDataIsChecked to false 
            //for scanning book folder 
            //and find new downloaded books.
            //When pressed "Yes" or "No" button.
            myLibUtils.setMyLibDataIsChecked(false);

            if (command==viewMaster.getYesCmd()){
                //hide dialog 
                Main.getInstance().switchDisplayable(null,this);
                
                //show MyLibViewList
                viewMaster.getMyLibViewList();
                viewMaster.openView(ViewMaster.VIEW_MY_LIB_LIST);                
            }
            else if(command==viewMaster.getNoCmd()) {
                Main.getInstance().switchDisplayable(null,this); 
            } 
             
         }          
         else if (command == viewMaster.getHelpCmd()) {
            viewMaster.getUniversalHelpForm(L10n.getMessage("HELP_FORM_TITLE"),
                                            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_HELP_TEXT_HEADER"),
                                            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_HELP_TEXT_CONTENT"), 
                                            false);
            
            viewMaster.openView(ViewMaster.VIEW_UNIVERSAL_HELP_FORM);  
          }         
         else if (command == viewMaster.getBackCmd()) {
            viewMaster.backView();
          }

         else if (command == viewMaster.getCancelCmd()) {
             httpGetter.cancel(true);
               try {
                    if (fcDownloadBookForSaveURL.fileSize()==0){
                        fcDownloadBookForSaveURL.delete();
                    }                                        

                    fcDownloadBookForSaveURL.close();               
               }
               catch (IOException ioe) {
                    System.out.println("IOException in MyLibBookInfoViewForm::commandAction()::command == viewMaster.getCancelCmd()"+ioe.getMessage());            
               }               
               
               //Runs the garbage collector
               System.gc();
               Main.getInstance().switchDisplayable(null, this);
          }
      }
      
       /**
         * Handles the select command related to a list item
         * @param c
         * @param item
       */
       public void commandAction(Command c, Item item) {
           if (item==stringItemDownloadBookButton) {
            //if for books folder in settings defined memory card, but
            //memory card not found in the device then return from checkMyLibData()
            //an break  check my lib data process

            //Check Localized names corresponding to available roots  
            //Series 40 return value: 
            //1) phone;memory card; if available phone memory & memory card;
            //2) phone;             if available only phone memory;                        

            String rootsNames = System.getProperty("fileconn.dir.roots.names");
            String booksFolderLocation = Settings.getInstance().getBooksFolderLocation();
            if(rootsNames.equals("phone;") && booksFolderLocation.equals("memory card")) {        
                showMemoryCardNotFoundAlert();
                return;
            }           
           booksFolderURL=Settings.getInstance().getBooksFolderURL();
           downloadBookFileName=selectedBookForDownload.getDownloadBookFileName();
           downloadBookForSaveURL=booksFolderURL+downloadBookFileName;  
           downloadBookURL=selectedBookForDownload.getDownloadBookURL();
           fcDownloadBookForSaveURL=null;
            try { 
                fcDownloadBookForSaveURL = (FileConnection)Connector.open(downloadBookForSaveURL);
                //if DownloadBookForSave file already exists in bookFolder,
                //inform user and break save process
                if (fcDownloadBookForSaveURL.exists() && fcDownloadBookForSaveURL.fileSize()!=0){
                    viewMaster.showTimedAlert(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_MSG_TITLE"), 
                                              L10n.getMessage("NET_LIB_BOOK_INFO_FORM_MSG_CONTENT_1")
                                              + "\""
                                              +downloadBookFileName.toLowerCase()
                                              +"\""
                                              + L10n.getMessage("NET_LIB_BOOK_INFO_FORM_MSG_CONTENT_2"), 
                                              AlertType.INFO, 3000);
                }
                else {
                    
                    //show downloading process window                         
                    showCancelableAlert();
                    // Create HTTP GET request
                    httpGetter = new HttpGetter(downloadBookURL);
                    
                    // Add a task which will run after the HTTP GET
                    //In other words:
                    // Chain callback code for handling the request response. 
                    //This code gets executed when the HttpGetter is finished
                    httpGetter.chain(
                         new Task() {
                            public Object doInBackground(final Object in) { 
                                // Perform an operation on byte[] “in” received from the net 
                                // This is called from a background Worker thread
                                // Handle server response code
                                responseCode=httpGetter.getResponseCode();
                                if (responseCode == 200) {
                                    try { 
                                        //create new book file
                                        fcDownloadBookForSaveURL.create();  
                                        //open DataOutputStream for write book file
                                        DataOutputStream dos = fcDownloadBookForSaveURL.openDataOutputStream();
                                        dos.write((byte[]) in);
                                        dos.close();
                                        //if file size DownloadBookForSave equal 0, delete it
                                        if (fcDownloadBookForSaveURL.fileSize()==0){
                                            fcDownloadBookForSaveURL.delete();
                                        }                                    
                                        fcDownloadBookForSaveURL.close();
                                        //hide downloading process window   
                                        Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());
                                        //show downloadedBookSuccessDialog()
                                        showDownloadedBookSuccessDialog();

                                    } catch (IOException ioe) {
                                       System.out.println("IOException: "+ioe.getMessage());            
                                    }                                     
   
                                }
                                //if Request Status = 302: "Moved Temporarily"
                                //Get location Header 
                                //(e.g. Location = [http://s3.amazonaws.com/manybooksepub_new/twainmar3232532325epub.epub])
                                //set downloadBookURL=location header
                                //and retry httpGetter.fork();
                                else if (responseCode == 302 ) {
                                    //Get a Hashtable of all HTTP headers recieved from the server
                                    Hashtable responseHeaders=httpGetter.getResponseHeaders();
                                    Enumeration listOfHeadersKeys = responseHeaders.keys();
                                    Enumeration listOfHeadersValues = responseHeaders.elements();
                                    String locationKeyName="Location".toLowerCase();
                                    locationHeader=(String)responseHeaders.get(locationKeyName);
                                    downloadBookURL=locationHeader;
                                    
                                    //set new downloadBookURL=location header
                                    // Create new HTTP GET request
                                    httpGetter = new HttpGetter(downloadBookURL);
                                    
                                    //Add a task which will run after the HTTP GET
                                    //In other words:
                                    //Chain callback code for handling the request response. 
                                    //This code gets executed when the HttpGetter is finished
                                     httpGetter.chain(
                                         new Task() {
                                            public Object doInBackground(final Object in) { 
                                                // Perform an operation on byte[] “in” received from the net 
                                                // This is called from a background Worker thread

                                                // Handle server response code
                                                responseCode=httpGetter.getResponseCode();

                                                if (responseCode == 200) {
                                                    try { 
                                                        
                                                        //create new book file
                                                        fcDownloadBookForSaveURL.create();  

                                                        //open DataOutputStream for write book file
                                                        DataOutputStream dos = fcDownloadBookForSaveURL.openDataOutputStream();
                                                        dos.write((byte[]) in);
                                                        dos.close();

                                                        //if file size DownloadBookForSave equal 0, delete it
                                                        if (fcDownloadBookForSaveURL.fileSize()==0){
                                                            fcDownloadBookForSaveURL.delete();
                                                        }                                    

                                                        fcDownloadBookForSaveURL.close();

                                                        //hide downloading process window   
                                                        Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());

                                                        //show downloadedBookSuccessDialog()
                                                        showDownloadedBookSuccessDialog();


                                                    } catch (IOException ioe) {
                                                       System.out.println("IOException: "+ioe.getMessage());            
                                                    }                                     

                                                }
                                                else {
                                                    //hide downloading process window   
                                                    Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());

                                                    //show alertNetworkError
                                                    showNetworkErrorAlert();                                      
                                                }

                                                return in; 
                                           } 
                                           protected void onCancelled() { 
                                               // On the UI thread, update the screen when the operation fails 

                                               //if file size DownloadBookForSave equal 0, delete it
                                                try {

                                                    if (fcDownloadBookForSaveURL.fileSize()==0){
                                                            fcDownloadBookForSaveURL.delete();
                                                    }                                        

                                                    fcDownloadBookForSaveURL.close();

                                                } catch (IOException ioe) {
                                                   System.out.println("IOException: "+ioe.getMessage());            
                                               }

                                                //hide downloading process window   
                                                Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());

                                                //show alertNetworkError
                                                showNetworkErrorAlert();                               
                                           }                            
                                         }

                                        );
                                        httpGetter.fork(); // Start task(downloading book) on a background Worker thread                                                            
                                    
                                }
                                else {
                                    //hide downloading process window   
                                    Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());

                                    //show alertNetworkError
                                    showNetworkErrorAlert();                                      
                                }

                                return in; 
                           } 
                           protected void onCancelled() { 
                               // On the UI thread, update the screen when the operation fails 
                               
                                //if file size DownloadBookForSave equal 0, delete it
                                try {
                                    
                                    if (fcDownloadBookForSaveURL.fileSize()==0){
                                            fcDownloadBookForSaveURL.delete();
                                    }                                        
                                    
                                    fcDownloadBookForSaveURL.close();
                                    
                                } catch (IOException ioe) {
                                   System.out.println("IOException: "+ioe.getMessage());            
                               }
                               
                                //hide downloading process window   
                                Main.getInstance().switchDisplayable(null, viewMaster.getNetLibBookInfoViewForm());

                                //show alertNetworkError
                                showNetworkErrorAlert();                               
                           }                            
                         }
       
                        );
                    httpGetter.fork(); // Start task(downloading book) on a background Worker thread
      
                }
            } 
            catch (SecurityException se) {
                showBooksFolderAccessDeniedAlert();
                System.out.println("SecurityException: "+se.getMessage());            
            }            
            
            catch (IOException ioe) {
               System.out.println("IOException: "+ioe.getMessage());            
            }
         }
       }
       
    
    public void showDownloadedBookSuccessDialog(){
        alertDownloadedBookSuccess.setTimeout(Alert.FOREVER);
        alertDownloadedBookSuccess.addCommand(viewMaster.getYesCmd());
        alertDownloadedBookSuccess.addCommand(viewMaster.getNoCmd());
        alertDownloadedBookSuccess.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertDownloadedBookSuccess,this);
        
    }       
       
       
    public void showNetworkErrorAlert() {
        alertNetworkError.setTimeout(Alert.FOREVER); 
        alertNetworkError.addCommand(viewMaster.getDismissCmd());
        alertNetworkError.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertNetworkError,this);
    }       
       
       
    public void showCancelableAlert() {
        // display a "busy" screen
        final Gauge gauge;
        // Creates an alert with an indeterminate gauge
        gauge = new Gauge(null, false, Gauge.INDEFINITE,
                Gauge.CONTINUOUS_RUNNING);
        alertDownloadingBook.setIndicator(gauge);            
        alertDownloadingBook.setTimeout(Alert.FOREVER);  
        alertDownloadingBook.addCommand(viewMaster.getCancelCmd());
        alertDownloadingBook.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertDownloadingBook,this);         
    }          
    
    public void showMemoryCardNotFoundAlert() {
        alertMemoryCardNotFound.setTimeout(Alert.FOREVER); 
        alertMemoryCardNotFound.addCommand(viewMaster.getDismissCmd());
        alertMemoryCardNotFound.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertMemoryCardNotFound,viewMaster.getMyLibViewList());
    }
    
    
    public void showBooksFolderAccessDeniedAlert() {
        alertBooksFolderAccessDenied = new Alert(L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_TITLE"), 
            L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_CONTENT_1")
            +"\""+Settings.getInstance().getBooksFolderName()+"\""
            + L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_CONTENT_2"), null, null);
        alertBooksFolderAccessDenied.setTimeout(Alert.FOREVER); 
        alertBooksFolderAccessDenied.addCommand(viewMaster.getDismissCmd());
        alertBooksFolderAccessDenied.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertBooksFolderAccessDenied,this);        
    }     
}
