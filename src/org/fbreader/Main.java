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

* 1.In the module Main.java used some fields and methods 
*   from AlbiteMIDlet.java (AlbiteReader) 
*   by Svetlin Ankov <galileostudios@gmail.com>
* 
* 2. Some of the methods that have been modified and optimized for 
*   the needs of FBReader for S40FT
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

/**
 *
 * @author Svetlin Ankov <galileostudios@gmail.com>
 * 
 */

package org.fbreader;

import org.fbreader.views.ViewMaster;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.netbeans.microedition.lcdui.WaitScreen;
import org.netbeans.microedition.util.SimpleCancellableTask;
import org.tantalum.Worker;
import org.tantalum.j2me.TantalumMIDlet;
import org.tantalum.j2me.RMSUtils;
//#debug
import org.albite.util.*;
import org.albite.book.model.book.Book;
import org.albite.io.decoders.Encodings;
import org.albite.util.RMSHelper;
import org.fbreader.views.SplashCanvas;
import org.fbreader.views.BookCanvas;
import org.fbreader.localization.L10n;
import org.fbreader.settings.Settings;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.utils.ImageLoader;

/**
 * Main Main MIDlet
 */


public final class Main
    extends TantalumMIDlet 
    implements CommandListener {

    private static Main instance;
    private Display display = null;
    private SplashCanvas splashCanvas;
    private BookCanvas bookCanvas;
    
    
    private ViewMaster viewMaster;
    
    
    //#mdebug
        public static final Logger LOGGER = new ConsoleLogger();
    //#enddebug 
    
    
    
    /*
     * App
     */
    private boolean                 firstTime               = false;
    private RecordStore             rs;

    /*
     * Book
     */
    private String                  bookURL;
    
    private String                  baseBookEncoding = Encodings.DEFAULT;
    
    
    /*
     * Chapter
     */
    private int                     selectedChapterIndex;
    
    private List toc;
    private WaitScreen loadBook;
    private SimpleCancellableTask loadBookTask;
    private Font loadingFont;
    private Alert bookError;
    
    private WaitScreen reflowChapterScreen;
    private SimpleCancellableTask goToChapterTask;
    
    private Image imageChapterListIcon;
    private final String CHAPTER_ICON =  "/icons/chapter_38x38.png";
    
    private Settings settings;
    private FileConnection fcBooksFolder;
    private String booksFolderURL;
       

    /**
     * @see MIDlet#startApp() 
     */
    public final void startApp() {
        
        //early initL10n() for correct localization SplashCanvas text info
        initL10n();
        
        if (display == null) {
            instance = this;
            display = Display.getDisplay(this);
            
            
            // Set up the first view of the application.
            splashCanvas = SplashCanvas.getInstance();
            display.setCurrent(splashCanvas);
            
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                public void run() {

                     settings=Settings.getInstance();

                     try {
                        RecordStore rs = RecordStore.openRecordStore("fbreader_settings", true);
                        
                        try {
                            if (rs.getNumRecords()!=0) {
                                rs.closeRecordStore();
                                settings.loadSettings();
                            }
                            else {
                              settings.setBooksFolderLocation(null);
                              settings.setBooksFolderURL(null);
                              rs.closeRecordStore();   
                            }
                        }
                        catch (RecordStoreNotOpenException rsnop) {
                            //#debug
                            Main.LOGGER.logError("RecordStoreNotOpenException in Main::startApp()->"+rsnop.getMessage());
                        }
                    }
                    catch (RecordStoreNotFoundException rsnfex) {
                        //#debug
                        Main.LOGGER.logError("RecordStoreNotFoundException in Main::startApp()->"+rsnfex.getMessage());
                    }
                    catch (RecordStoreFullException rsfex) {
                        //#debug
                        Main.LOGGER.logError("RecordStoreFullException in Main::startApp()->"+rsfex.getMessage());
                    }
                    catch (RecordStoreException rsex) {
                        //ex.printStackTrace();
                        //#debug
                        Main.LOGGER.logError("RecordStoreException in Main::startApp()->"+rsex.getMessage());
                    }                    
                    
                    if (settings.getBooksFolderURL()==null && settings.getBooksFolderLocation()==null) {
                    
                        //create new books folder
                        try {

                            //Check Localized names corresponding to available roots  
                            //Series 40 return value: 
                            //1) phone;memory card; if available phone memory & memory card;
                            //2) phone;             if available only phone memory;                        
                            String booksFolderPath;
                            String rootsNames = System.getProperty("fileconn.dir.roots.names");
                            if(rootsNames.equalsIgnoreCase("phone;memory card;")) {
                              settings.setBooksFolderLocation("memory card");

                              String rootMC = System.getProperty("fileconn.dir.memorycard");  
                              booksFolderPath=rootMC;
                              booksFolderURL=booksFolderPath+settings.getBooksFolderName()+"/";
                              
                              settings.setBooksFolderURL(booksFolderURL);

                            }
                            else {
                                settings.setBooksFolderLocation("phone memory");
                                booksFolderPath="file:///c:/predefgallery/predeffilereceived/";

                                booksFolderURL=booksFolderPath+settings.getBooksFolderName()+"/";
                                settings.setBooksFolderURL(booksFolderURL);
                            }

                            settings.saveSettings();

                            //control loading a saved settings
                            settings.loadSettings();

                            //Opens a file connection in READ mode

                            fcBooksFolder = (FileConnection)Connector.open(booksFolderURL, Connector.READ_WRITE);
                             //check if books folder not exists
                             if (!fcBooksFolder.exists()){
                                 fcBooksFolder.mkdir();
                             }
                             fcBooksFolder.close();

                        }
                        catch (IOException ioe) {
                                 //#debug
                                Main.LOGGER.logError("Main::startApp()->IOException: "+ioe.getMessage());  
                        }                    
                   
                    }
                   
                    //initL10n();
                    initBookCanvas(); 
                    initViewMaster();
                    preDeleteCachedRecordStore();
  
                }
            }, 100);
             
            
            timer.schedule(new TimerTask() {

                public void run() {
                    closeSplash();
                }
            }, 1000); // minimum time the splash is shown
        }
    }
    
 
    private void preDeleteCachedRecordStore(){
        MyLibUtils myLibUtils = MyLibUtils.getInstance();
        String booksFolderStateCacheKey=myLibUtils.getBooksFolderStateCacheKey();
        //check if the cache is record matching the current BooksFolderState
        Vector cachedRecordStoreNamesDataVector=RMSUtils.getCachedRecordStoreNames();
        for (int i=0; i < cachedRecordStoreNamesDataVector.size(); i++) {
            String cachedRecordStoreNameStr=(String)cachedRecordStoreNamesDataVector.elementAt(i);
            //delete all record stores whose name indiates that they are caches,
            //except BOOKS_FOLDER_STATE_CACHE_KEY
            if (cachedRecordStoreNameStr.indexOf(booksFolderStateCacheKey) == -1 ) {
                RMSUtils.delete(cachedRecordStoreNameStr);
                //cachedRecordStoreNamesDataVector.removeElementAt(i);                        
            }
        }
        
        //after deleting Cached RMS, delete all elements from cachedRecordStoreNamesDataVector
        if (!cachedRecordStoreNamesDataVector.isEmpty()) {
            cachedRecordStoreNamesDataVector.removeAllElements();
        }
        cachedRecordStoreNamesDataVector=RMSUtils.getCachedRecordStoreNames();

        //control output Cached RMS after delete
        for (int i=0; i < cachedRecordStoreNamesDataVector.size(); i++) {
            String cachedRecordStoreNameStr=(String)cachedRecordStoreNamesDataVector.elementAt(i);
        }   
    }
    
    private void initL10n() {
        if (L10n.getDeviceLocale().equals("ru-RU")) {
            L10n.initLocalizationSupport("ru_RU");
        }
        else {
         L10n.initLocalizationSupport("en_US");   
        }
    } 

    private void initViewMaster() {
        viewMaster = ViewMaster.getInstance();
    }
    
    public void initBookCanvas() {
        bookCanvas = new BookCanvas(this);
        bookCanvas.setTitle(L10n.getMessage("BOOK_CANVAS_TITLE"));
        bookCanvas.setFullScreenMode(false);
        
        /* RMS */
        openRMSAndLoadData();
        
        /*
         * The BookCanvas must be initialized before usage. This is because
         * of the fact, that it wouldn't have correct metrics, i.e.
         * wouldn't be in fullscreenmode when looked at from the constructor
         */
        bookCanvas.initialize();
    } 


    private void closeSplash() {
        
        viewMaster.setDisplay(display);
        
        //set Category "InfoView" on Category Bar
        viewMaster.setCategoryBarSelectedIndex(0); 
        viewMaster.setCategoryBarVisible(true);
        viewMaster.openView(ViewMaster.VIEW_INFO_CATEGORY);
        
    }

 
    /**
     * @see MIDlet#pauseApp() 
     */
    public final void pauseApp() {
    }

    
     /**
     * Exits MIDlet.
     * @see TantalumMIDlet#exitMIDlet() 
     */
    public void exitMIDlet() {
        Worker.shutdown(true);
    }
    
    
        /**
     * @return MIDlet object
     */
    public static Main getInstance() {
        return instance;
    }
    
    /**
     * Returns a display instance.
     * @return the display instance.
     */
    public final Display getDisplay () {
        return Display.getDisplay(this);
    }  
    
    
    
    /**
     * Switches a current displayable in a display. The <code>display</code> instance is taken from <code>getDisplay</code> method. This method is used by all actions in the design for switching displayable.
     * @param alert the Alert which is temporarily set to the display; if <code>null</code>, then <code>nextDisplayable</code> is set immediately
     * @param nextDisplayable the Displayable to be set
     */
    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {
        //Display display = getDisplay();
        display = getDisplay();
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }
    }   


    /**
     * Show alert message on the screen.
     * @param title
     * @param alertText
     * @param type 
     */
    public final void showAlertMessage(String title, String alertText,
        AlertType type) {
        Alert alert = new Alert(title, alertText, null, type);
        display.setCurrent(alert, display.getCurrent());
    }

    /**
     * @return name of the MIDlet.
     */
    public final String getName() {
        return getAppProperty("MIDlet-Name");
    }

    /**
     * @return vendor of the MIDlet.
     */
    public final String getVendor() {
        return getAppProperty("MIDlet-Vendor");
    }

    /**
     * @return version of the MIDlet.
     */
    public final String getVersion() {
        return getAppProperty("MIDlet-Version");
    }
    
    
   /******************************************************************
    * From AlbiteReader
    *
    *****************************************************************/ 
    /**
     *
     * @author Svetlin Ankov <galileostudios@gmail.com>
     * 
     */    
    
    public void openRMSAndLoadData() {
        try {
            rs = RecordStore.openRecordStore("application", true);

            if (rs.getNumRecords() > 0) {
                //deserialize first record
                byte[] data = rs.getRecord(1);
                DataInputStream din =
                        new DataInputStream(new ByteArrayInputStream(data));
                try {
                    RMSHelper.checkValidity(this, din);
                    
                    //load last book open
                    bookURL     = din.readUTF();
                    //dictsFolder = din.readUTF();
                } catch (IOException ioe) {
                    //#debug
                    LOGGER.log(ioe);
                }

            } else {
                /*
                 * No records found, so it must be the first time
                 * the app starts on this device.
                 */
                firstTime = true;
            }

        } catch (RecordStoreException rse) {
            //#debug
            LOGGER.log(rse);
        }
    }

    public final void saveOptionsToRMS() {
        if (bookURL != null && !bookURL.equalsIgnoreCase("")) {
            /*
             * A book has been opened successfully
             */
            try {
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(boas);
                try {
                    RMSHelper.writeVersionNumber(this, dout);

                    //save last book open
                    dout.writeUTF(bookURL);
                    //dout.writeUTF(dictsFolder);

                    byte[] data = boas.toByteArray();

                    //serialize first record
                    if (rs.getNumRecords() > 0) {
                        rs.setRecord(1, data, 0, data.length);
                    } else {
                        rs.addRecord(data, 0, data.length);
                    }
                } catch (IOException ioe) {}
            } catch (RecordStoreException rse) {}
        }
    }

    private void closeRMS() {
        try {
            rs.closeRecordStore();
        } catch (RecordStoreException rse) {}
    }
    
    
    public String getCurrentBookURL() {
  
        if (bookURL == null) {
            return null;
        }
        else {
         return bookURL;
        }

    }
    
    
    public void setCurrentBookURL(String bookURL) {
        this.bookURL=bookURL;
    }
    
    
 /**
     * Returns an initiliazed instance of loadBook component.
     * @return the initialized component instance
     */
    public WaitScreen getLoadBook() {
        if (loadBook == null) {

            loadBook = new WaitScreen(getDisplay());

            loadBook.setTitle(null);

            loadBook.setCommandListener(this);

            loadBook.setFullScreenMode(true);

            loadBook.setImage(null);

            loadBook.setText(L10n.getMessage("OPENING_BOOK_MESSAGE"));

            loadBook.setTask(getLoadBookTask());

            loadBook.setBookCanvas(bookCanvas);
        }
        return loadBook;
    }
    
    /**
     * Returns an initiliazed instance of loadBookTask component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getLoadBookTask() {
        if (loadBookTask == null) {
            
            loadBookTask = new SimpleCancellableTask();
            loadBookTask.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {
                    /*
                     * bookURL already loaded before calling this task
                     */
                    bookCanvas.openBook(bookURL);
                    bookCanvas.setScrollingOptions(bookCanvas.getScrollingSpeed()/100F, bookCanvas.getSmoothScrolling(), settings.getScrollingDirection());                       
                }
            });
        }
        return loadBookTask;
    }
    
    /**
     * Returns an initiliazed instance of loadingFont component.
     * @return the initialized component instance
     */
    public Font getLoadingFont() {
        if (loadingFont == null) {
            loadingFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
        }
        return loadingFont;
    }
    
    
  /**
     * Called by a system to indicated that a command has been invoked on a particular displayable.
     * @param command the Command that was invoked
     * @param displayable the Displayable where the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {
        
        if (displayable == loadBook) {
            if (command == WaitScreen.FAILURE_COMMAND) {
                // write pre-action user code here
                switchDisplayable(null, getBookError());
                // write post-action user code here
            } 
            else if (command == WaitScreen.SUCCESS_COMMAND) {
                switchDisplayable(null, bookCanvas);
             }
        }
        else if (displayable == bookError && command == viewMaster.getDismissCmd()){
            Main.getInstance().switchDisplayable(null, viewMaster.getMyLibBookInfoViewForm()); 
        } 
        else if (displayable == toc) {
           if (command == viewMaster.getBackCmd()) {
               switchDisplayable(null, bookCanvas);
           }
           else if (command == List.SELECT_COMMAND) {                                             
                 goToChapter();
            }
        }
    
    }
    
    
    /**
     * Returns an initiliazed instance of bookError component.
     * @return the initialized component instance
     */
    public Alert getBookError() {
        if (bookError == null) {
            bookError = new Alert(L10n.getMessage("ERROR_OPENING_BOOKS_MSG_TITLE"), L10n.getMessage("ERROR_OPENING_BOOKS_MSG_CONTENT"), null, AlertType.WARNING);
            bookError.addCommand(viewMaster.getDismissCmd());
            bookError.setCommandListener(this);
            bookError.setTimeout(Alert.FOREVER);
        }
        return bookError;
    }

    
    /**
     * Return base Encoding for current Book
     * @return 
    */
    public String getBaseBookEncoding() {
        return baseBookEncoding;
    }
    
    /**
     * Set base Encoding for current Book
     * @return 
    */
    public void setBaseBookEncoding(String baseBookEncoding) {
        this.baseBookEncoding=baseBookEncoding;
    }

    
    /**
     * Performs an action assigned to the showToc entry-point.
     */
    public void showToc() {                                    
        switchDisplayable(null, getToc());                                      
    }                         
    
    /**
     * Returns an initiliazed instance of toc component.
     * @return the initialized component instance
     */
    public List getToc() {
        if (toc == null) {                                   
            
            toc = new List(L10n.getMessage("TOC_VIEW_LIST_TITLE"), Choice.IMPLICIT);                                      
            toc.addCommand(viewMaster.getBackCmd());
            toc.setCommandListener(this);
            toc.setFitPolicy(Choice.TEXT_WRAP_DEFAULT);
            try  {
                   imageChapterListIcon=ImageLoader.getInstance().loadImage(CHAPTER_ICON, null);
              } 
              catch(IOException e) {
            }             
            
            final Book book = bookCanvas.getCurrentBook();
            final int count = book.getChaptersCount();
            for (int i = 0; i < count; i++) {
                toc.append(book.getChapter(i).getTitle(), imageChapterListIcon);
            }
        }                          
        return toc;
    }
    
    public void resetToc(){
        toc=null;
    } 

 
    
    /**
     * Performs an action assigned to the goToChapter entry-point.
     */
    public void goToChapter() {                                    
        switchDisplayable(null, getReflowChapterScreen());                                      
    }    

    /**
     * Returns an initiliazed instance of reflowChapterScreen component.
     * @return the initialized component instance
     */
    public WaitScreen getReflowChapterScreen() {
        if (reflowChapterScreen == null) {
            reflowChapterScreen = new WaitScreen(getDisplay());
            reflowChapterScreen.setTitle(null);
            reflowChapterScreen.setCommandListener(this);
            reflowChapterScreen.setFullScreenMode(true);
            reflowChapterScreen.setImage(null);
            reflowChapterScreen.setText(L10n.getMessage("LAYING_OUT_CHAPTER_MESSAGE"));
            reflowChapterScreen.setTextFont(getLoadingFont());
            reflowChapterScreen.setTask(getGoToChapterTask());
            reflowChapterScreen.setBookCanvas(bookCanvas);
        }
        return reflowChapterScreen;
    }
    
    
    /**
     * Returns an initiliazed instance of goToChapterTask component.
     * @return the initialized component instance
     */
    public SimpleCancellableTask getGoToChapterTask() {
        if (goToChapterTask == null) {                                     
            goToChapterTask = new SimpleCancellableTask();                                       
            goToChapterTask.setExecutable(new org.netbeans.microedition.util.Executable() {
                public void execute() throws Exception {                                     
                    selectedChapterIndex=getToc().getSelectedIndex();
                    bookCanvas.goToFirstPage(selectedChapterIndex);
                    switchDisplayable(null, bookCanvas);
                }                                        
            });                                      
        }                           
        return goToChapterTask;
    }    
    

    
    /**
     * Returns an initiliazed instance of bookCanvas component.
     * 
     * @return bookCanvas
     */
    public BookCanvas getbookCanvas() {
        return bookCanvas;
    }
}
