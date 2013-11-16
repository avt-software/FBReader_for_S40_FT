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
package org.fbreader.utils.mylib;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import org.fbreader.Main;
import org.fbreader.formats.fb2.FB2Model;
import org.fbreader.models.BookInMyLibRMS;
import org.fbreader.settings.Settings;
import org.fbreader.views.ViewMaster;
import org.fbreader.localization.L10n;
import org.tantalum.j2me.RMSUtils;
import org.tantalum.storage.FlashDatabaseException;
import org.albite.book.model.book.BookException;
import org.albite.io.RandomReadingFile;
import org.albite.io.decoders.AlbiteStreamReader;
import org.albite.io.decoders.Encodings;
import org.albite.util.archive.ArchiveEntry;
import org.albite.util.archive.zip.ArchiveZip;
import org.xml.sax.SAXException;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Class for Creates My Library RMS, Saving and Loading Data from RMS
 * Refresh Data in My Library.
 *   
 */
public final class MyLibUtils implements CommandListener {

    private static MyLibUtils self = null;

    private static final char FILE_NAME_EXT_SEPARATOR = '.';
    
    private final static String MY_LIB_RMS_NAME="my_lib";
    private final static String BOOKS_FOLDER_STATE_CACHE_KEY="books_folder_state";
    
    private final Alert alertSynchronizeMyLib = new Alert(L10n.getMessage("ALERT_SYNCHRONIZE_MY_LIB_MSG_TITLE"), 
            L10n.getMessage("ALERT_SYNCHRONIZE_MY_LIB_MSG_CONTENT"), null, null);
    
    private final Alert alertMemoryCardNotFound = new Alert(L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_TITLE"), 
            L10n.getMessage("SETTINGS_BOOKS_FOLDER_ALERT_MC_NOT_FOUND_CONTENT"), null, null);
    
    
    private Alert alertBooksFolderIsEmpty;
    private Alert alertInvalidBooksFolderName;
    private Alert alertBooksFolderNotFound; 
    private Alert alertBooksFolderAccessDenied;
    
    private FileConnection fcBooksFolder;
    private FileConnection fcBookFile;
    
    
    //booksFolder parameters from Cache
    private long booksFolderSizeFromCache;
    private long booksFolderLastModifiedFromCache;
    
    //current booksFolder parameters
    private long booksFolderSize;
    private long booksFolderLastModified;
    
    private Vector myLibRMSDataVector=new Vector();
    private Vector myLibFSDataVector=null;
    
    private BookInMyLibRMS selectedBookForRead;
    
    private Settings settings;
    private String booksFolderURL;
    private String booksFolderName;
    private String booksFolderLocation;
    
    private String bookFileURL;
    
    private ViewMaster viewMaster;
    
    private final FB2Model fb2Model=new FB2Model(100);
     
    private boolean successParseFB2BookResult=false;
    private boolean successParseEpubBookResult=false;
    
    private boolean myLibDataIsChecked=false;
    
    private ArchiveZip bookArchive;
    private BookInMyLibRMS epubModel;
    

    public MyLibUtils() {
        viewMaster = ViewMaster.getInstance();
        settings=Settings.getInstance();
    }
    
    /**
     * @return MyLibUtils singleton
     */
    public static MyLibUtils getInstance() {
        if (self == null) {
            self = new MyLibUtils();
        }
        return self;
    }
    
    
    /**
     * Check current State My Library Data 
     * (comparision current booksFolder params with params saved in Cache)
     */
    public synchronized void checkMyLibData(){

        //load settings 
        settings.loadSettings();
        
        booksFolderName=settings.getBooksFolderName();

        booksFolderURL=settings.getBooksFolderURL();
        
        
        //if for books folder in settings defined memory card, but
        //memory card not found in the device then return from checkMyLibData()
        //and break checkMyLibData() process
        
        //Check Localized names corresponding to available roots  
        //Series 40 return value: 
        //1) phone;memory card; if available phone memory & memory card;
        //2) phone;             if available only phone memory;                        
        
        String rootsNames = System.getProperty("fileconn.dir.roots.names");
        booksFolderLocation = settings.getBooksFolderLocation();
        if(rootsNames.equals("phone;") && booksFolderLocation.equals("memory card")) {        
            showMemoryCardNotFoundAlert();
            return;
        }
        
        // display a "busy" screen
        final Gauge gauge;

        // Creates an alert with an indeterminate gauge
        gauge = new Gauge(null, false, Gauge.INDEFINITE,
                Gauge.CONTINUOUS_RUNNING);
        alertSynchronizeMyLib.setIndicator(gauge);            

        alertSynchronizeMyLib.setTimeout(Alert.FOREVER);  

        alertSynchronizeMyLib.addCommand(viewMaster.getCancelCmd());
        
        Main.getInstance().switchDisplayable(alertSynchronizeMyLib, 
                                                 viewMaster.getMyLibViewList()); 
        
        //if booksFolder is exists and not empty, 
        //read BooksFolderState from Cache. 
        //If the cache is record matching the current BooksFolderState
        if (checkBooksFolder()==0){
            try {

                boolean cachedRecordStoreNameFound=false;
                //check if the cache is record matching the current BooksFolderState
                Vector cachedRecordStoreNamesDataVector = RMSUtils.getCachedRecordStoreNames();
                for (int i=0; i < cachedRecordStoreNamesDataVector.size(); i++) {
                    String cachedRecordStoreNameStr=(String)cachedRecordStoreNamesDataVector.elementAt(i);

                    if (cachedRecordStoreNameStr.indexOf(BOOKS_FOLDER_STATE_CACHE_KEY)!=-1 ) {
                        cachedRecordStoreNameFound=true;
                        break;
                    }
                    
                }
                //if cachedRecordStoreName not found, then createMyLibData() & refreshMyLibData();
                if (!cachedRecordStoreNameFound){
                    createMyLibData();
                    refreshMyLibData();
                }
                //if cachedRecordStoreName found, then casheReadBooksFolderState() & refreshMyLibData()
                else {
                    casheReadBooksFolderState();
                    
                    //if booksFolder changed by user, begin scan booksFolder 
                    //and create new MyLibRMS
                    if (booksFolderSize!=booksFolderSizeFromCache || 
                        booksFolderLastModified!=booksFolderLastModifiedFromCache){
                        refreshMyLibData();
                    }
                    else {
                        readMyLibRMS();
                    }                    
                    
                }

            }
            catch (FlashDatabaseException fdbe){
                 //#debug
                 Main.LOGGER.logError("MyLibUtils::checkMyLibData() Can not read cache" + fdbe);                 
            }
            
            //success check myLibData 
            myLibDataIsChecked=true;
            Main.getInstance().switchDisplayable(null,viewMaster.getMyLibViewList()); 
        }

        //invalid booksFolder name
        else if(checkBooksFolder()==1) {
            showInvalidBooksFolderNameAlert();
        }
        
        //current booksFolder is empty
        else if(checkBooksFolder()==2) {
            showBooksFolderIsEmptyAlert();
        }
        //booksFolder not found
        else if (checkBooksFolder()==3) {
            showBooksFolderNotFoundAlert();
           
        }

    }    
    
   
    /**
     * 
     * @return resultCheckBooksFolder
     *         0 - booksFolder is exists and not empty
     *         1 - invalid booksFolder name
     *         2 - booksFolder is empty
     *         3 - booksFolder not found
     */
    
    public int checkBooksFolder(){
       
       //booksFolder is exists and not empty
       int resultCheckBooksFolder=0; 
       
       //FileConnection fc;
       try { 
            //Opens a file connection in READ mode
           fcBooksFolder = (FileConnection)Connector.open(booksFolderURL, Connector.READ);
   
           //check if books folder not exists
           if (fcBooksFolder.exists()){
                //Size and Last Modified Data for current booksFolder
                boolean includeSubDirs=true; 
                booksFolderSize=fcBooksFolder.directorySize(includeSubDirs);
                booksFolderLastModified=fcBooksFolder.lastModified();                
                //check if books folder not is Directory 
               if (!fcBooksFolder.isDirectory()) {
                    resultCheckBooksFolder=1;
                }
               //check if books folder is empty
               else if (booksFolderSize == 0) {
                    resultCheckBooksFolder=2;
                }
           }
           else {
             resultCheckBooksFolder=3;
           }
           fcBooksFolder.close();
        }
        catch (IOException ioe) {
            System.out.println("IOException: "+ioe.getMessage());            
        }
        catch (SecurityException se) {
            showBooksFolderAccessDeniedAlert();
            System.out.println("SecurityException: "+se.getMessage());            
        }
        finally {
           //if booksFolder is empty or not found then delete MyLibRMS
           if (resultCheckBooksFolder==2 || resultCheckBooksFolder==3){
               deleteMyLibRMS();
           }
           return resultCheckBooksFolder;
        }
    }
    
    
    
    /*
     * Create needed for MyLib cache and RMS data
     * 
     */
    public void createMyLibData(){
        
//--- 1 --- prepare myLibRMSDataVector for writing new BookInMyLibRMS objects
        if (myLibRMSDataVector!=null) {
            myLibRMSDataVector.removeAllElements();
        }
        else {
             myLibRMSDataVector=new Vector();
        }  
        
        try { 
//--- 2 --- Opens a file connection in READ mode
           fcBooksFolder = (FileConnection)Connector.open(booksFolderURL, Connector.READ);
           
//--- 3 ---
           //write books folder size and last modified date
           //in cashe as BOOKS_FOLDER_STATE_CASHE_KEY
           casheWriteBooksFolderState(fcBooksFolder);

           fcBooksFolder.close();
       }
        catch (IOException ioe) {
            System.out.println("IOException: "+ioe.getMessage());            
        }
        catch (SecurityException se) {
            showBooksFolderAccessDeniedAlert();
            System.out.println("SecurityException: "+se.getMessage());            
        }
    }
    
    
    /**
    *
    * @author Svetlin Ankov <galileostudios@gmail.com>
    *
    */
    
    /* 
    * Copyright 2013 Sole Proprietorship Vita Tolstikova
    * Adopted for FBReader for S40FT project
    * 
    */
    public void parseEpubBook() 
           throws BookException, IOException {
        
        InputStream in;
        String opfFileName = null;
        final String opfFilePath;
        
        bookArchive = new ArchiveZip(bookFileURL);
        
        successParseEpubBookResult=false;
        
        /*
         * first load META-INF/container.xml
        */
        ArchiveEntry container =
                bookArchive.getEntry("META-INF/container.xml");

        if (container == null) {
            throw new BookException("Missing manifest");
        }

        in = container.openInputStream();
        
        try {
            KXmlParser parser = null;
            Document doc = null;
            Element root;
            Element kid;
            try {
                parser = new KXmlParser();
                parser.setInput(new AlbiteStreamReader(
                        in, Encodings.DEFAULT));

                doc = new Document();
                doc.parse(parser);
                parser = null;

                root = doc.getRootElement();

                Element rfile = root
                        .getElement(KXmlParser.NO_NAMESPACE, "rootfiles")
                        .getElement(KXmlParser.NO_NAMESPACE, "rootfile");

                opfFileName = rfile.getAttributeValue(
                        KXmlParser.NO_NAMESPACE, "full-path");

                if (opfFileName == null) {
                    throw new BookException("Missing opf file");
                }

                opfFilePath = RandomReadingFile.getPathFromURL(opfFileName);

            } catch (XmlPullParserException xppe) {
                
                parser = null;
                doc = null;
                throw new BookException(
                    "container.xml is invalid");
            }
        } finally {
            in.close();
        }

        /*
         * now the opf file
         */
        ArchiveEntry opfFile = bookArchive.getEntry(opfFileName);

        if (opfFile == null) {
            throw new BookException("Missing opf");
        }

        in = opfFile.openInputStream();

        try {
            KXmlParser parser = null;
            Document doc = null;
            Element root;
            Element kid;

            try {
                parser = new KXmlParser();

                try {
                    parser.setFeature(
                            KXmlParser.FEATURE_PROCESS_NAMESPACES, true);
                } catch (XmlPullParserException e) {}

                parser.setInput(new AlbiteStreamReader(
                        in, Encodings.DEFAULT));

                doc = new Document();
                doc.parse(parser);
                parser = null;

                root = doc.getRootElement();

                try {
                    /*
                     * try to get the metadata
                     */
                    Element metadata = getElement(root, "metadata");
                    
                    Element dcmetadata = getElement(metadata, "dc-metadata");
                    
                    if (dcmetadata != null) {
                        metadata = dcmetadata;
                    }

                    if (metadata != null) {
                        
                        epubModel=new BookInMyLibRMS();
                        
                        //temporary set bookTags to empty string ("")
                        String bookTags="";
                        
                        for (int i = 0; i < metadata.getChildCount(); i++) {
                            if (metadata.getType(i) != Node.ELEMENT) {
                                continue;
                            }

                            kid = metadata.getElement(i);

                            if (kid.getName().equalsIgnoreCase("title")) {
                                epubModel.setBookTitle(text(kid));
                                continue;
                            }

                            if (kid.getName().equalsIgnoreCase("creator")) {
                                epubModel.setBookAuthor(text(kid));
                                continue;
                            }

                            if (kid.getName().equalsIgnoreCase("language")) {
                                String language = text(kid);
                                /*
                                 * squash it to a 2-letter tag
                                 */
                                if (language.length() > 2) {
                                    language = language.substring(0, 2);
                                }

                                /*
                                 * set currentLanguage to the default value
                                 * afterward (in loadUserFile) it will
                                 * be overwritten
                                 */
                                epubModel.setBookLang(language);

                                continue;
                            }
                            
                            if (kid.getName().equalsIgnoreCase("subject")) {
                                bookTags+=text(kid)+" ";
                                continue;
                            }                            
                            
                        }
                        if (bookTags.equals("")) {
                            epubModel.setBookTags(L10n.getMessage("UNKNOWN_GENRE"));
                        }
                        else {
                            epubModel.setBookTags(bookTags);
                        }
                        successParseEpubBookResult=true;
                    }
                } catch (Exception e) {
                    /*
                     * If there is a problem with the metadata,
                     * it's not worth bothering
                     */
                    //#debug
                    Main.LOGGER.log(e);
                }

            } catch (XmlPullParserException xppe) {
                parser = null;
                doc = null;
                //#debug
                Main.LOGGER.log(xppe);
                throw new BookException(
                    "the opf file is invalid");
            }
        } finally {
            in.close();
        }        
    }
    
    
    //helper method for parseEpubBook()
    private String text(final Element kid) {

        String res = null;

        try {
            res = kid.getText(0);
        } catch (Exception e) {}

        if (res == null) {
            return "";
        }

        return res;
    }

    //helper method for parseEpubBook()
    private Element getElement(final Node node, final String name) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getType(i) != Node.ELEMENT) {
                continue;
            }

            Element element = node.getElement(i);
            if (name.equalsIgnoreCase(element.getName())) {
                return element;
            }
        }

        return null;
    }    
   
    
    
    /**
     * Refresh data in MyLib
     */
    public void refreshMyLibData() {
   
    

//--- 1 --- prepare myLibFSDataVector for writing new bookFileURL objects
        //          from booksFolder
        if (myLibFSDataVector!=null) {
            myLibFSDataVector.removeAllElements();
        }
        else {
             myLibFSDataVector=new Vector();
        }  
        

//--- 2 ---  list all html, htm, fb2, epub files(don't show hidden files)
        //           and add it FileName to myLibFSDataVector       
        try { 
//--- 2.1 --- Opens a file connection in READ mode
           fcBooksFolder = (FileConnection)Connector.open(booksFolderURL, Connector.READ);

//--- 2.2 --- create myLibFSDataVector for *.html files
           Enumeration listOfHTMLFiles = fcBooksFolder.list("*.html", false);
            while (listOfHTMLFiles.hasMoreElements()) {
                String currentFile = (String) listOfHTMLFiles.nextElement();
                myLibFSDataVector.addElement(currentFile);
             }  
            
//--- 2.3 --- create myLibFSDataVector for *.htm files
           Enumeration listOfHTMFiles = fcBooksFolder.list("*.htm", false);
            while (listOfHTMFiles.hasMoreElements()) {
                String currentFile = (String) listOfHTMFiles.nextElement();
                myLibFSDataVector.addElement(currentFile);
             } 
            
//--- 2.4 --- create myLibFSDataVector for *.txt files
           Enumeration listOfTXTFiles = fcBooksFolder.list("*.txt", false);
            while (listOfTXTFiles.hasMoreElements()) {
                String currentFile = (String) listOfTXTFiles.nextElement();
                myLibFSDataVector.addElement(currentFile);
             }            

//--- 2.5 --- create myLibFSDataVector for *.fb2 files
           Enumeration listOfFB2Files = fcBooksFolder.list("*.fb2", false);
            while (listOfFB2Files.hasMoreElements()) {
                String currentFile = (String) listOfFB2Files.nextElement();
                myLibFSDataVector.addElement(currentFile);
             }
            
//--- 2.6 --- create myLibFSDataVector for *.epub files
           Enumeration listOfEpubFiles = fcBooksFolder.list("*.epub", false);
            while (listOfEpubFiles.hasMoreElements()) {
                String currentFile = (String) listOfEpubFiles.nextElement();
                myLibFSDataVector.addElement(currentFile);
            }            
            fcBooksFolder.close();
        }
        catch (IOException ioe) {
            System.out.println("IOException: "+ioe.getMessage());            
        }
        catch (SecurityException se) {
            showBooksFolderAccessDeniedAlert();
            System.out.println("SecurityException: "+se.getMessage());            
        }
        finally {
  
        }
        
//--- 3 ---  Open MyLibRMS
        RecordStore recordStore=openMyLibRMS(); 
        
//--- 4 --- If MyLibRMS is empty, closeMyLibRMS and call createMyLibData() method
        try {
//--- 5 --- If MyLib Folder changed, start synchronization MyLibRMS with myLibFSDataVector            
            //else if (recordStore.getNumRecords() != myLibFSDataVector.size()) {
            if (recordStore.getNumRecords() != myLibFSDataVector.size()) {
//--- 5.1 ---
                //write books folder size and last modified date
                //in cashe as BOOKS_FOLDER_STATE_CASHE_KEY
               
//--- 5.1.1 --- Opens a file connection in READ mode
                fcBooksFolder = (FileConnection)Connector.open(booksFolderURL, Connector.READ);

//--- 5.1.2 --- write BOOKS_FOLDER_STATE_CASHE_KEY
                 casheWriteBooksFolderState(fcBooksFolder);
                 
//--- 5.1.3 ---  close fcBooksFolder connection
                fcBooksFolder.close();

//--- 5.2 --- Read MyLibRMS and synchronize it with myLibFSDataVector   
               
                RecordEnumeration re = recordStore.enumerateRecords(null, null, true);
               
                while (re.hasNextElement()) {
                    int id = re.nextRecordId();
                    ByteArrayInputStream bais = new ByteArrayInputStream(recordStore.getRecord(id));
                    DataInputStream dis = new DataInputStream(bais);
                    try {
                         BookInMyLibRMS bookInMyLibRMS = BookInMyLibRMS.readFrom(dis);
                         String bookFileNameFromMyLibRMS=bookInMyLibRMS.getBookFileName()+"."+bookInMyLibRMS.getBookFileExt();
                         
                         //if bookFileNameFromMyLibRMS not found in myLibFSDataVector, 
                         //then delete current record from MyLibRMS
                         if (myLibFSDataVector.indexOf(bookFileNameFromMyLibRMS)==-1) {
                             recordStore.deleteRecord(id);
                          }
                         //else if bookFileNameFromMyLibRMS found in myLibFSDataVector,
                         //then delete object with same FileName from myLibFSDataVector
                         else {
                              myLibFSDataVector.removeElement(bookFileNameFromMyLibRMS);
                         }

                    }
                    catch (EOFException eofe) {
                        eofe.printStackTrace();
                    }
                }
                
//--- 5.3 --- Close MyLibRMS
                closeMyLibRMS(recordStore);                
                
//---5.4--- Read elements from myLibFSDataVector and add it to MyLibRMS
                if (myLibFSDataVector.size()>0) {
                    for (int i=0; i < myLibFSDataVector.size(); i++){
                        String currentFile = (String) myLibFSDataVector.elementAt(i);
                        bookFileURL=booksFolderURL+currentFile;
                        fcBookFile = (FileConnection)Connector.open(bookFileURL, Connector.READ);

                        //processing *.html file
                        if (currentFile.endsWith(".html")) {
//--- 6.4.1 ---
                            //adding book data into MyLibRMS without 
                            //parse the file in the format txt to extract 
                            //data about the author, book title,tags (genres) and language
                            addHTMLRecordInMyLibRMS(true);
                        }                        

                        //processing *.htm file
                        if (currentFile.endsWith(".htm")) {
//--- 6.4.2 ---
                            //adding book data into MyLibRMS without 
                            //parse the file in the format txt to extract 
                            //data about the author, book title,tags (genres) and language
                            addHTMLRecordInMyLibRMS(true);
                        }                           
                        
                        
                        //processing *.txt file
                        if (currentFile.endsWith(".txt")) {
//--- 6.4.3 ---
                            //adding book data into MyLibRMS without 
                            //parse the file in the format txt to extract 
                            //data about the author, book title and tags (genres)
                            addTXTRecordInMyLibRMS(true);
                        }
                        
                        //processing *.fb2 file
                        if (currentFile.endsWith(".fb2")) {
//--- 6.4.4 ---
                            //before adding book data into MyLibRMS:
                            //parse the file in the format fb2 to extract 
                            //data about the author, book title and tags (genres)
                            parseFB2Book();
                            //if parsing successfull complete, than add record to MyLibRMS
                            if (successParseFB2BookResult) {
                                System.gc();
                                addFB2RecordInMyLibRMS(true);
                            }                               
                        }
                        
                        //processing *.epub file
                        if (currentFile.endsWith(".epub")) {
//--- 6.4.5 ---
                            //before adding book data into MyLibRMS:
                            //parse the file in the format epub to extract 
                            //data about the author, book title and tags (genres)
                            parseEpubBook();

                            //if parsing successfull complete, than add record to MyLibRMS
                            if (successParseEpubBookResult) {
                                addEpubRecordInMyLibRMS(true);
                            }                               
                        }                        
                    }
                    fcBookFile.close();
                }
                readMyLibRMS();
            }

        }
        catch (SecurityException se) {
            showBooksFolderAccessDeniedAlert();
            System.out.println("SecurityException: "+se.getMessage());            
        }
        catch (Exception e){
            //#debug
            Main.LOGGER.logError("MyLibUtils::refreshMyLibData()->Error:" + e.getMessage());
        }
    }
    
    
    
   /*
    * write books folder size and it last modified date
    * in cashe as BOOKS_FOLDER_STATE_CASHE_KEY
    * @parameter fc
    */ 
   public void casheWriteBooksFolderState(FileConnection fc) {
        try {
             //write books folder size and last modified date
             //in cashe as BOOKS_FOLDER_STATE_CASHE_KEY
             ByteArrayOutputStream byteArrayOutputStream = null;
             byteArrayOutputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
             dataOutputStream.writeLong(fc.directorySize(true));
             dataOutputStream.writeLong(fc.lastModified());
             byte[] byteArray = byteArrayOutputStream.toByteArray();
             try { 
                 RMSUtils.cacheDelete(BOOKS_FOLDER_STATE_CACHE_KEY);
                 RMSUtils.cacheWrite(BOOKS_FOLDER_STATE_CACHE_KEY,byteArray);
             }
             catch (RecordStoreFullException rsfe){
                 System.out.println("RecordStoreFullException: "+rsfe.getMessage());            
             }
             dataOutputStream.close();
             byteArrayOutputStream.close();
        }
        catch (IOException ioe) {
             System.out.println("IOException: "+ioe.getMessage());            
        }       
   }
   
   
   /*
    * read books folder size and last modified date
    * from cashe as BOOKS_FOLDER_STATE_CASHE_KEY
    * 
    */ 
   public void casheReadBooksFolderState() throws FlashDatabaseException {
       
       byte[] byteArrayBooksFolderState=null; 
       
       try {
             //read books folder size and last modified date
             //from cashe as BOOKS_FOLDER_STATE_CASHE_KEY
             try { 
                 
                 byteArrayBooksFolderState=RMSUtils.cacheRead(BOOKS_FOLDER_STATE_CACHE_KEY);
             }
             catch (FlashDatabaseException fdbe){
                 //#debug
                 Main.LOGGER.logError("MyLibUtils::casheReadBooksFolderState() Can not read cache" + fdbe);
             }
            ByteArrayInputStream bin = null;
            bin = new ByteArrayInputStream(byteArrayBooksFolderState);
            DataInputStream din = new DataInputStream(bin);
            booksFolderSizeFromCache=din.readLong();
            booksFolderLastModifiedFromCache=din.readLong();
            din.close();
            bin.close();
        }
        catch (IOException ioe) {
             System.out.println("IOException: "+ioe.getMessage());            
        }
   }
   
    /**
     * Add record for *.txt book file to MyLibRMS 
     * if refreshMode=true, then method call from refreshMyLibRMS()
     * if refreshMode=false, then method call from createMyLibRMS()
     * 
     * @param createMode 
     * 
     */
    private void addTXTRecordInMyLibRMS(boolean refreshMode){
    
        RecordStore recordStore=openMyLibRMS(); 
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            String bookFileName=fcBookFile.getName();
            //Separation file name and extension to individual substring
            int separatorPos = bookFileName.lastIndexOf(FILE_NAME_EXT_SEPARATOR);
            if (separatorPos == -1) {
               System.err.println("ERROR: Separator character not found in "+bookFileName);
            }
            String  fileName = bookFileName.substring(0, separatorPos);
            String  fileExt = bookFileName.substring(separatorPos + 1);
           
            //create new bookInMyLibRMS for write in myLibRMSDataVector
            BookInMyLibRMS bookInMyLibRMS = new BookInMyLibRMS();

//--- 1 --- field "bookRecordId"  
            int bookRecordId=0; //0-temporary value
            dos.writeInt(bookRecordId);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookRecordId(bookRecordId);
            
//--- 2 --- field "bookFileName Name" in MyLibRMS 
            dos.writeUTF(fileName);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileName(fileName);
     
//--- 3 --- field "bookFileName Extension"  
            dos.writeUTF(fileExt);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileExt(fileExt);
            
//--- 4 --- field "bookFileName Size"  
            long bookFileSize=fcBookFile.fileSize();
            dos.writeLong(bookFileSize);
            //bookInMyLibRMS field
             bookInMyLibRMS.setBookFileSize(bookFileSize);
            
//--- 5 --- field "bookFileName LastModified"  
            long bookFileLastModified=fcBookFile.lastModified();
            dos.writeLong(bookFileLastModified);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileLastModified(bookFileLastModified);

//--- 6 --- field "genre"  
            String genre=L10n.getMessage("UNKNOWN_GENRE");
            dos.writeUTF(genre);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTags(genre);
            
//--- 7 --- field "author"  
            String author=L10n.getMessage("UNKNOWN_AUTHOR");
            dos.writeUTF(author);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookAuthor(author);
            
//--- 8 --- field "bookTitle"  
            String bookTitle=L10n.getMessage("UNKNOWN_BOOK_TITLE");
            dos.writeUTF(bookTitle);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTitle(bookTitle);
            
//--- 9 --- field "bookLang"  
            String bookLang=L10n.getMessage("UNKNOWN_BOOK_LANG");
            dos.writeUTF(bookLang);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLang(bookLang);
            
//--- 10 --- field "bookLastOpen"  
            long bookLastOpen=0;
            dos.writeLong(bookLastOpen);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLastOpen(bookLastOpen);
            
            byte[] recordData = baos.toByteArray();
            int addedIndex =recordStore.addRecord(recordData, 0, recordData.length);

            //add bookInMyLibRMS object in myLibRMSDataVector
            if (!refreshMode) {
                myLibRMSDataVector.addElement(bookInMyLibRMS);
            }    
            dos.close();
            baos.close();
        
            closeMyLibRMS(recordStore);
        
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }
   
   
    /**
     * Add record for *.html book file to MyLibRMS 
     * if refreshMode=true, then method call from refreshMyLibRMS()
     * if refreshMode=false, then method call from createMyLibRMS()
     * 
     * @param refreshMode 
     * 
     */
    private void addHTMLRecordInMyLibRMS(boolean refreshMode){
        
        RecordStore recordStore=openMyLibRMS();
        
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
                        
            String bookFileName=fcBookFile.getName();

            //Separation file name and extension to individual substring
            int separatorPos = bookFileName.lastIndexOf(FILE_NAME_EXT_SEPARATOR);
            if (separatorPos == -1) {
               System.err.println("ERROR: Separator character not found in "+bookFileName);
            }
            String  fileName = bookFileName.substring(0, separatorPos);
            String  fileExt = bookFileName.substring(separatorPos + 1);
           
            //create new bookInMyLibRMS for write in myLibRMSDataVector
            BookInMyLibRMS bookInMyLibRMS = new BookInMyLibRMS();
            

//--- 1 --- field "bookRecordId"  
            int bookRecordId=0; //0-temporary value
            dos.writeInt(bookRecordId);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookRecordId(bookRecordId);
            
            
//--- 2 --- field "bookFileName Name" in MyLibRMS 
            dos.writeUTF(fileName);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileName(fileName);
     
//--- 3 --- field "bookFileName Extension"  
            dos.writeUTF(fileExt);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileExt(fileExt);
            
            
//--- 4 --- field "bookFileName Size"  
            long bookFileSize=fcBookFile.fileSize();
            dos.writeLong(bookFileSize);
            //bookInMyLibRMS field
             bookInMyLibRMS.setBookFileSize(bookFileSize);
            
//--- 5 --- field "bookFileName LastModified"  

            long bookFileLastModified=fcBookFile.lastModified();
            dos.writeLong(bookFileLastModified);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileLastModified(bookFileLastModified);


//--- 6 --- field "genre"  
            String genre=L10n.getMessage("UNKNOWN_GENRE");
            dos.writeUTF(genre);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTags(genre);
            
            
//--- 7 --- field "author"  
            String author=L10n.getMessage("UNKNOWN_AUTHOR");
            dos.writeUTF(author);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookAuthor(author);
            
//--- 8 --- field "bookTitle"  
            //String bookTitle=fb2Model.getTitleInfo().getBookTitle();
            String bookTitle=L10n.getMessage("UNKNOWN_BOOK_TITLE");
            dos.writeUTF(bookTitle);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTitle(bookTitle);
            
//--- 9 --- field "bookLang"  
            //String bookLang=fb2Model.getTitleInfo().getBookLang();
            String bookLang=L10n.getMessage("UNKNOWN_BOOK_LANG");
            dos.writeUTF(bookLang);            
            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLang(bookLang);
            
//--- 10 --- field "bookLastOpen"  
            long bookLastOpen=0;
            dos.writeLong(bookLastOpen);
            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLastOpen(bookLastOpen);
            
            
            byte[] recordData = baos.toByteArray();
            int addedIndex =recordStore.addRecord(recordData, 0, recordData.length);

            //add bookInMyLibRMS object in myLibRMSDataVector
            if (!refreshMode) {
                myLibRMSDataVector.addElement(bookInMyLibRMS);
            }    
             
            dos.close();
            baos.close();
        
            closeMyLibRMS(recordStore);
        
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }   
   
   
   
   
    /**
     * parse FB2Book on the following steps: 
     * 1. load the fcBookFile  from Book Folder to the byte[] array (FB2BookAsByteArray) 
     * 2. parsing XML Data from FB2BookAsByteArray and return result to fb2Model
     * 
     * @return Object (fb2Model or null)
     */   
    private Object parseFB2Book(){

      try {


        int size = (int)fcBookFile.fileSize();
        byte FB2BookAsByteArray[] = new byte[size];

        InputStream is = fcBookFile.openInputStream();

        int bytesRead = 0;
        while (bytesRead < size) {
             bytesRead += is.read(FB2BookAsByteArray, bytesRead, size - bytesRead);
         }                    


        fb2Model.removeAllElements();
        try {
            fb2Model.setXML(FB2BookAsByteArray);
        }
        catch(SAXException se) {
            System.out.println(se.getMessage());
        }
        
        successParseFB2BookResult=true;
        return fb2Model;

    }

    catch (Exception e) {
        
        successParseFB2BookResult=false;
        showFB2BookParseErrorAlert();
        //#debug
        Main.LOGGER.logError("Error in parsing XML"+fb2Model.toString());
        
        //#debug
        Main.LOGGER.logError("Exception: "+e.getMessage());
        return null;
    }
   }  
     
  
    
    /**
     * Add record to MyLibRMS 
     * if refreshMode=true, then method call from refreshMyLibRMS()
     * if refreshMode=false, then method call from createMyLibRMS()
     * 
     * @param createMode 
     * 
     */
    private void addFB2RecordInMyLibRMS(boolean refreshMode){
        
        RecordStore recordStore=openMyLibRMS(); 
        
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
                        
            String bookFileName=fcBookFile.getName();
            
            //Separation file name and extension to individual substring
            //int separatorPos = bookFileName.indexOf(FILE_NAME_EXT_SEPARATOR);
            int separatorPos = bookFileName.lastIndexOf(FILE_NAME_EXT_SEPARATOR);
            if (separatorPos == -1) {
               System.err.println("ERROR: Separator character not found in "+bookFileName);
            }
            String  fileName = bookFileName.substring(0, separatorPos);
            String  fileExt = bookFileName.substring(separatorPos + 1);
           
            //create new bookInMyLibRMS for write in myLibRMSDataVector
            BookInMyLibRMS bookInMyLibRMS = new BookInMyLibRMS();
            

//--- 1 --- field "bookRecordId"  
            int bookRecordId=0; //0-temporary value
            dos.writeInt(bookRecordId);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookRecordId(bookRecordId);
            
//--- 2 --- field "bookFileName Name" in MyLibRMS 
            dos.writeUTF(fileName);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileName(fileName);
     
//--- 3 --- field "bookFileName Extension"  
            dos.writeUTF(fileExt);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileExt(fileExt);
            
            
//--- 4 --- field "bookFileName Size"  
            long bookFileSize=fcBookFile.fileSize();
            dos.writeLong(bookFileSize);
            //bookInMyLibRMS field
             bookInMyLibRMS.setBookFileSize(bookFileSize);
            
//--- 5 --- field "bookFileName LastModified"  
            long bookFileLastModified=fcBookFile.lastModified();
            dos.writeLong(bookFileLastModified);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileLastModified(bookFileLastModified);

//--- 6 --- field "genre"  
            String genre=fb2Model.getTitleInfo().getGenre();
            dos.writeUTF(genre);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTags(genre);
            
//--- 7 --- field "author"  
            String author=fb2Model.getTitleInfo().getAuthor();
            dos.writeUTF(author);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookAuthor(author);
            
            
//--- 8 --- field "bookTitle"  
            String bookTitle=fb2Model.getTitleInfo().getBookTitle();
            dos.writeUTF(bookTitle);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTitle(bookTitle);
            
//--- 9 --- field "bookLang"  
            String bookLang=fb2Model.getTitleInfo().getBookLang();
            dos.writeUTF(bookLang);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLang(bookLang);
            
//--- 10 --- field "bookLastOpen"  
            long bookLastOpen=0;
            dos.writeLong(bookLastOpen);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLastOpen(bookLastOpen);
            
            byte[] recordData = baos.toByteArray();
            
            int addedIndex =recordStore.addRecord(recordData, 0, recordData.length);

            //add bookInMyLibRMS object in myLibRMSDataVector
            if (!refreshMode) {
                myLibRMSDataVector.addElement(bookInMyLibRMS);
            }    
             
            dos.close();
            baos.close();
        
            closeMyLibRMS(recordStore);
        
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }
    
    
    
    /**
     * Add record to MyLibRMS 
     * if refreshMode=true, then method call from refreshMyLibRMS()
     * if refreshMode=false, then method call from createMyLibRMS()
     * 
     * @param createMode 
     * 
     */
    private void addEpubRecordInMyLibRMS(boolean refreshMode){
        
        RecordStore recordStore=openMyLibRMS(); 
        
        try {
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
                        
            String bookFileName=fcBookFile.getName();
            
            //Separation file name and extension to individual substring
            //int separatorPos = bookFileName.indexOf(FILE_NAME_EXT_SEPARATOR);
            int separatorPos = bookFileName.lastIndexOf(FILE_NAME_EXT_SEPARATOR);
            if (separatorPos == -1) {
               System.err.println("ERROR: Separator character not found in "+bookFileName);
            }
            String  fileName = bookFileName.substring(0, separatorPos);
            String  fileExt = bookFileName.substring(separatorPos + 1);
            //create new bookInMyLibRMS for write in myLibRMSDataVector
            BookInMyLibRMS bookInMyLibRMS = new BookInMyLibRMS();
            

//--- 1 --- field "bookRecordId"  
            int bookRecordId=0; //0-temporary value
            dos.writeInt(bookRecordId);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookRecordId(bookRecordId);
            
//--- 2 --- field "bookFileName Name" in MyLibRMS 
            dos.writeUTF(fileName);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileName(fileName);
     
//--- 3 --- field "bookFileName Extension"  
            dos.writeUTF(fileExt);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileExt(fileExt);
            
//--- 4 --- field "bookFileName Size"  
            long bookFileSize=fcBookFile.fileSize();
            dos.writeLong(bookFileSize);
            //bookInMyLibRMS field
             bookInMyLibRMS.setBookFileSize(bookFileSize);
            
//--- 5 --- field "bookFileName LastModified"  
            long bookFileLastModified=fcBookFile.lastModified();
            dos.writeLong(bookFileLastModified);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookFileLastModified(bookFileLastModified);


//--- 6 --- field "genre"  
            String genre=epubModel.getBookTags();
            dos.writeUTF(genre);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTags(genre);
            
            
//--- 7 --- field "author"  
            String author=epubModel.getBookAuthor();
            dos.writeUTF(author);
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookAuthor(author);
            
//--- 8 --- field "bookTitle"  
            String bookTitle=epubModel.getBookTitle();
            dos.writeUTF(bookTitle);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookTitle(bookTitle);
            
//--- 9 --- field "bookLang"  
            String bookLang=epubModel.getBookLang();
            dos.writeUTF(bookLang);            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLang(bookLang);
            
//--- 10 --- field "bookLastOpen"  
            long bookLastOpen=0;
            dos.writeLong(bookLastOpen);
            
            //bookInMyLibRMS field
            bookInMyLibRMS.setBookLastOpen(bookLastOpen);
            
            
            byte[] recordData = baos.toByteArray();
            
            int addedIndex =recordStore.addRecord(recordData, 0, recordData.length);

            //add bookInMyLibRMS object in myLibRMSDataVector
            if (!refreshMode) {
                myLibRMSDataVector.addElement(bookInMyLibRMS);
            }    
             
            dos.close();
            baos.close();
        
            closeMyLibRMS(recordStore);
        
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }    
    
    /**
    * Read MyLibRMS 
    */ 
    public void readMyLibRMS() {
      //open MyLibRMS
      RecordStore rs=openMyLibRMS();

      try {
            if(rs.getNumRecords() > 0){
                //prepare myLibRMSDataVector for writing new BookInMyLibRMS objects
                if (myLibRMSDataVector!=null) {
                    myLibRMSDataVector.removeAllElements();
                }
                else {
                     myLibRMSDataVector=new Vector();
                }                 
                RecordEnumeration re = rs.enumerateRecords(null, null, true);
                while (re.hasNextElement()) {
                    int id = re.nextRecordId();
                    ByteArrayInputStream bais = new ByteArrayInputStream(rs.getRecord(id));
                    DataInputStream dis = new DataInputStream(bais);
                    try {
                         myLibRMSDataVector.addElement(BookInMyLibRMS.readFrom(dis));                        
                    }
                    catch (EOFException eofe) {
                        eofe.printStackTrace();
                    }
                }
                //close MyLibRMS
                closeMyLibRMS(rs);
            }
            //in MyLibRMS no records
            else {
                  showBooksFolderIsEmptyAlert();                  
            }
       }
       catch (Exception e) {
            e.printStackTrace();
       }
       
    }
    
    
    
    /**
    * Get myLibRMSDataVector
    */     
    public Vector getMyLibRMSDataVector() {
        return myLibRMSDataVector;
    }
    
  
    /*
    * Open MyLibRMS 
    */ 
    public RecordStore openMyLibRMS() {
        
        RecordStore rs=null;
        
        try {
            rs = RecordStore.openRecordStore(MY_LIB_RMS_NAME, true);
        }
        catch (RecordStoreNotFoundException rsnfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreNotFoundException in MyLibUtils::openMyLibRMS()->"+rsnfex.getMessage());
        }
        catch (RecordStoreFullException rsfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreFullException in MyLibUtils::openMyLibRMS()->"+rsfex.getMessage());
        }
        catch (RecordStoreException rsex) {
            //ex.printStackTrace();
            //#debug
            Main.LOGGER.logError("RecordStoreException in MyLibUtils::openMyLibRMS()->"+rsex.getMessage());
        }        
         
        return rs;
    }
    
    /*
    * Close MyLibRMS 
    */     
     public void closeMyLibRMS(RecordStore rs){
        try {
            rs.closeRecordStore();
        } 
        catch (RecordStoreNotOpenException rsnoex) {
            //#debug
            Main.LOGGER.logError("RecordStoreNotOpenException in MyLibUtils::closeMyLibRMS()->"+rsnoex.getMessage());
        } 
        catch (RecordStoreException rsex) {
            //ex.printStackTrace();
            //#debug
            Main.LOGGER.logError("RecordStoreException in MyLibUtils::closeMyLibRMS()->"+rsex.getMessage());
        }
  
    }    
    
    
    
    /*
    * Delete MyLibRMS 
    */     
     
     public void deleteMyLibRMS(){
      try {
            // Clear data
            RecordStore.deleteRecordStore(MY_LIB_RMS_NAME);
       }
      catch (RecordStoreNotFoundException rsnfex) {
            /* Nothing to delete */ 
            //#debug
            Main.LOGGER.logError("RecordStoreNotFoundException in MyLibUtils::deleteMyLibRMS()->"+rsnfex.getMessage());
       }
      catch (RecordStoreException rsex) {
            //#debug
            Main.LOGGER.logError("RecordStoreException in MyLibUtils::deleteMyLibRMS()->"+rsex.getMessage());
       }
    }
     
     
    public void updateBookRecordInMyLibRMS(BookInMyLibRMS bookInMyLibRMS) {

        try {
            //open MyLibRMS
            RecordStore rs=openMyLibRMS();
            byte[] b;

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            bookInMyLibRMS.writeTo(dout);

            b = bout.toByteArray();

            rs.setRecord(bookInMyLibRMS.getBookRecordId(),b,0,b.length);

            dout.close();
            bout.close();

            //close MyLibRMS
            closeMyLibRMS(rs);
           }
           catch(Exception ex) {
               Main.LOGGER.logError(ex.getMessage());
           } 
    }
    

    /**
     * Set selected book for read as BookInMyLibRMS object
     * 
     * @return 
     */
    
     public void setSelectedBookForRead(BookInMyLibRMS selectedBook) {
        this.selectedBookForRead=selectedBook;
     } 
    
    
    
    /**
     * Get selected book for read as BookInMyLibRMS object
     * 
     * @return selectedBook
     */
    public BookInMyLibRMS getSelectedBookForRead() {
        return selectedBookForRead;
    }
    
    
    /**
     * Get myLibDataIsChecked value
     * 
     * @return myLibDataIsChecked
     */
    public boolean getMyLibDataIsChecked() {
        return myLibDataIsChecked;
    }
    
    
    /**
     * Set myLibDataIsChecked value
     * 
    */
    public void setMyLibDataIsChecked(boolean myLibDataIsChecked) {
        this.myLibDataIsChecked=myLibDataIsChecked;
    }
     
    
    /**
     * Get booksFolderStateCacheKey  value
     * 
     * @return BOOKS_FOLDER_STATE_CACHE_KEY
     */

    public String getBooksFolderStateCacheKey() {
        return BOOKS_FOLDER_STATE_CACHE_KEY;
    }
    
    
    public void commandAction(Command command, Displayable displayable) {
        
        if (command == viewMaster.getDismissCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            
            Main.getInstance().switchDisplayable(null,viewMaster.getMyLibViewList());          
        }          
        
        if(command == viewMaster.getCancelCmd()) {
                //Interrupts the thread that moves the progress bar
                //thread.interrupt();
                Main.getInstance().switchDisplayable(null,viewMaster.getMyLibViewList()); 
        }
            
    }
    
    
    public void showMemoryCardNotFoundAlert() {
        
        alertMemoryCardNotFound.setTimeout(Alert.FOREVER); 
        alertMemoryCardNotFound.addCommand(viewMaster.getDismissCmd());
        alertMemoryCardNotFound.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertMemoryCardNotFound,viewMaster.getMyLibViewList());
    }
    
    
    public void showBooksFolderIsEmptyAlert() {
        alertBooksFolderIsEmpty = new Alert(L10n.getMessage("BOOKS_FOLDER_IS_EMPTY_ALERT_TITLE"), 
            L10n.getMessage("BOOKS_FOLDER_IS_EMPTY_ALERT_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("BOOKS_FOLDER_IS_EMPTY_ALERT_CONTENT_2"), null, null);
        alertBooksFolderIsEmpty.setTimeout(Alert.FOREVER); 
        alertBooksFolderIsEmpty.addCommand(viewMaster.getDismissCmd());
        alertBooksFolderIsEmpty.setCommandListener(this);

        Main.getInstance().switchDisplayable(alertBooksFolderIsEmpty,viewMaster.getMyLibViewList());        
    }  
    
    
    public void showInvalidBooksFolderNameAlert() {
        
        alertInvalidBooksFolderName = new Alert(L10n.getMessage("INVALID_BOOKS_FOLDER_NAME_ALERT_TITLE"), 
            L10n.getMessage("INVALID_BOOKS_FOLDER_NAME_ALERT_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("INVALID_BOOKS_FOLDER_NAME_ALERT_CONTENT_2"), null, null);
        alertInvalidBooksFolderName.setTimeout(Alert.FOREVER); 
        alertInvalidBooksFolderName.addCommand(viewMaster.getDismissCmd());
        alertInvalidBooksFolderName.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertInvalidBooksFolderName,viewMaster.getMyLibViewList());        
    }
    
    
    public void showBooksFolderNotFoundAlert() {

        alertBooksFolderNotFound = new Alert(L10n.getMessage("BOOKS_FOLDER_NOT_FOUND_ALERT_TITLE"), 
            L10n.getMessage("BOOKS_FOLDER_NOT_FOUND_ALERT_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("BOOKS_FOLDER_NOT_FOUND_ALERT_CONTENT_2"), null, null);
        alertBooksFolderNotFound.setTimeout(Alert.FOREVER); 
        alertBooksFolderNotFound.addCommand(viewMaster.getDismissCmd());
        alertBooksFolderNotFound.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertBooksFolderNotFound,viewMaster.getMyLibViewList());        
    }
    
    
    public void showFB2BookParseErrorAlert() {

        alertBooksFolderNotFound = new Alert(L10n.getMessage("FB2_BOOK_PARSE_ERROR_ALERT_TITLE"), 
            L10n.getMessage("FB2_BOOK_PARSE_ERROR_ALERT_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("FB2_BOOK_PARSE_ERROR_ALERT_CONTENT_2"), null, null);
        alertBooksFolderNotFound.setTimeout(Alert.FOREVER); 
        alertBooksFolderNotFound.addCommand(viewMaster.getDismissCmd());
        alertBooksFolderNotFound.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertBooksFolderNotFound,viewMaster.getMyLibViewList());        
    }
    
    public void showBooksFolderAccessDeniedAlert() {

        alertBooksFolderAccessDenied = new Alert(L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_TITLE"), 
            L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_CONTENT_1")
            +"\""+booksFolderName+"\""
            + L10n.getMessage("BOOKS_FOLDER_ACCESS_DENIED_ALERT_CONTENT_2"), null, null);
        alertBooksFolderAccessDenied.setTimeout(Alert.FOREVER); 
        alertBooksFolderAccessDenied.addCommand(viewMaster.getDismissCmd());
        alertBooksFolderAccessDenied.setCommandListener(this);
        
        Main.getInstance().switchDisplayable(alertBooksFolderAccessDenied,viewMaster.getMyLibViewList());        
    }    
}
