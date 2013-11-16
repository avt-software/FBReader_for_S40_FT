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

package org.fbreader.settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import org.fbreader.Main;


/**
 * Class containing application settings.
 */
public final class Settings {

//--- 1 --- SettingsBooksFolderViewForm
    private String booksFolderLocation;
    private  String booksFolderName = "books";
    private  String booksFolderURL;
    
//--- 2 --- SettingsFontSizeViewForm
    //0 - small
    //1 - medium
    //2 - large
    private int bookBaseTextFontSize = 1; 
    
    
//--- 3 --- marginsSettingsViewForm
    private  int bookMarginWidth = 1;
    
//--- 4 --- statusBarSettingsViewForm
    //0 - hide
    //1 - show
    private int showChapterNumber = 1; 
    
//--- 5 --- scrollingSettingsView
   
    //true - horizontal scrolling
    //false - vertical scrolling
    private boolean horizontalScrolling=true;
    
    //true - tap can be used for scroll
    //false - tap can't be used for scroll
    private  boolean useTapForScroll = true;
    
    //true - flick and swipe can be used for scroll pages
    //false - flick and swipe can't be used for scroll pages
    private  boolean useFlickAndSwipeForScroll = true;    
    
    
    private static Settings self = null;
    
    
    public void Settings() {
        
    }

    /**
     * Returns and/or instantiates Settings object
     * @return
     */
    public static Settings getInstance() {
        if (self == null) {
            self = new Settings();
        }
        return self;
    }    
    

    public void saveSettings() {
       
        try {
            //Clear data 
            RecordStore.deleteRecordStore("fbreader_settings"); 
         }
         catch (Exception e) { 
            // Nothing to delete  
         }        


         try {
             RecordStore rs = RecordStore.openRecordStore("fbreader_settings", true);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos);

             dos.writeUTF(booksFolderLocation);
             dos.writeUTF(booksFolderName);
             dos.writeUTF(booksFolderURL);
             dos.writeInt(bookBaseTextFontSize);
             dos.writeInt(bookMarginWidth);
             dos.writeInt(showChapterNumber);
             dos.writeBoolean(horizontalScrolling);
             dos.writeBoolean(useTapForScroll);
             dos.writeBoolean(useFlickAndSwipeForScroll);
             byte[] b = baos.toByteArray();
             // Add it to the record store
             rs.addRecord(b, 0, b.length);
             rs.closeRecordStore();
         }
         catch (IOException ioe) {
            //#debug
            Main.LOGGER.logError("IOException in Settings::saveSettings()->"+ioe.getMessage());
         } 
         catch (RecordStoreNotFoundException rsnfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreNotFoundException in Settings::saveSettings()->"+rsnfex.getMessage());
        }
        catch (RecordStoreFullException rsfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreFullException in Settings::saveSettings()->"+rsfex.getMessage());
        }
        catch (RecordStoreException rsex) {
            //ex.printStackTrace();
            //#debug
            Main.LOGGER.logError("RecordStoreException in Settings::saveSettings()->"+rsex.getMessage());
        } 
    
    }

    
    public void loadSettings() {
        try {
            RecordStore rs = RecordStore.openRecordStore("fbreader_settings", false);
            RecordEnumeration re = rs.enumerateRecords(null, null, true);

            int id = re.nextRecordId();
            ByteArrayInputStream bais = new ByteArrayInputStream(rs.getRecord(id));
            DataInputStream dis = new DataInputStream(bais);
            try {
                  booksFolderLocation=dis.readUTF();
                  booksFolderName=dis.readUTF();
                  booksFolderURL=dis.readUTF();
                  bookBaseTextFontSize=dis.readInt();
                  bookMarginWidth=dis.readInt();
                  showChapterNumber=dis.readInt();
                  horizontalScrolling=dis.readBoolean();
                  useTapForScroll=dis.readBoolean();
                  useFlickAndSwipeForScroll=dis.readBoolean();
            }
            catch (EOFException eofe) {
                //#debug
                Main.LOGGER.logError("EOFException in Settings::loadSettings()->"+eofe.getMessage());
            }
            catch (IOException ioe) {
                //#debug
                Main.LOGGER.logError("IOException in Settings::loadSettings()->"+ioe.getMessage());
            }                    
            rs.closeRecordStore();
        }
        catch (RecordStoreNotFoundException rsnfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreNotFoundException in Settings::loadSettings()->"+rsnfex.getMessage());
        }
        catch (RecordStoreFullException rsfex) {
            //#debug
            Main.LOGGER.logError("RecordStoreFullException in Settings::loadSettings()->"+rsfex.getMessage());
        }
        catch (RecordStoreException rsex) {
            //ex.printStackTrace();
            //#debug
            Main.LOGGER.logError("RecordStoreException in Settings::loadSettings()->"+rsex.getMessage());
        }  
    }      
    
    
    
    //get() and set() methods for settings fields
    
//--- 1 ---  booksFolderLocation field 
    
    /**
     * Get the booksFolderLocation field
     * 
     * @return 
     */
     public String getBooksFolderLocation() {
        return booksFolderLocation;
    }
     
    /**
     * Set the booksFolderLocation field
     * 
     * @param booksFolderLocation
     */
    public void setBooksFolderLocation(String booksFolderLocation) {
        this.booksFolderLocation = booksFolderLocation;
    }    

    
//--- 2 ---  booksFolderName field 
    /**
     * Get the booksFolderName field
     * 
     * @return 
     */
     public String getBooksFolderName() {
        return booksFolderName;
    }
     
    /**
     * Set the booksFolderLocation field
     * 
     * @param booksFolderName
     */
    public void setBooksFolderName(String booksFolderName) {
        this.booksFolderName = booksFolderName;
    }  
    
//--- 3 ---  booksFolderURL field 
    /**
     * Get the booksFolderURL field
     * 
     * @return 
     */
     public String getBooksFolderURL() {
        return booksFolderURL;
    }
     
    /**
     * Set the booksFolderURL field
     * 
     * @param booksFolderURL 
     */
    public void setBooksFolderURL(String booksFolderURL) {
        this.booksFolderURL = booksFolderURL;
    }
    
//--- 4 ---  bookBaseTextFontSize field 
    /**
     * Get the bookBaseTextFontSize field
     * 
     * @return 
     */
     public int getBookBaseTextFontSize() {
        return bookBaseTextFontSize;
    }
     
    /**
     * Set the bookBaseTextFontSize field
     *  baseTextFontFace 
     * @param bookBaseTextFontSize
     */
    public void setBookBaseTextFontSize(int bookBaseTextFontSize) {
        this.bookBaseTextFontSize = bookBaseTextFontSize;
    }
    
    

//--- 5 ---  bookMarginWidth field 
    /**
     * Get the bookMarginWidth field
     * 
     * @return 
     */
     public int getBookMarginWidth() {
        return bookMarginWidth;
    }
     
    /**
     * Set the bookMarginWidth field
     * 
     * @param bookMarginWidth
     */
    public void setBookMarginWidth(int bookMarginWidth) {
        this.bookMarginWidth = bookMarginWidth;
    }     
    

//--- 6 ---  showChapterNumber  field 
    /**
     * Get the showChapterNumber field
     * 
     * @return 
     */
     public int getShowChapterNumber() {
        return showChapterNumber;
    }
     
    /**
     * Set the showChapterNumber field
     * 
     * @param showChapterNumber
     */
    public void setShowChapterNumber(int showChapterNumber) {
        this.showChapterNumber = showChapterNumber;
    } 
    
    
//--- 7 --- useTapForScroll  field 
    /**
     * Get the useTapForScroll field
     * 
     * @return 
     */
     public boolean getUseTapForScroll() {
        return useTapForScroll;
    }
     
    /**
     * Set the gestureTapOnLeftZone field
     * 
     * @param useTapForScroll
     */
    public void setUseTapForScroll(boolean useTapForScroll) {
        this.useTapForScroll = useTapForScroll;
    }
    
    
    
//--- 8 --- useFlickAndSwipeForScroll  field 
    /**
     * Get the useFlickAndSwipeForScroll field
     * 
     * @return 
     */
     public boolean getUseFlickAndSwipeForScroll() {
        return useFlickAndSwipeForScroll;
    }
     
    /**
     * Set the useFlickAndSwipeForScroll field
     * 
     * @param useFlickAndSwipeForScroll
     */
    public void setUseFlickAndSwipeForScroll(boolean useFlickAndSwipeForScroll) {
        this.useFlickAndSwipeForScroll = useFlickAndSwipeForScroll;
    }    
    
    
//--- 9 ---  scrollingDirection  field 
    /**
     * Get the scrollingDirection field
     * 
     * @return 
     */
     public  boolean getScrollingDirection() {
        return horizontalScrolling;
    }
     
    /**
     * Set the horizontalScrolling field
     * 
     * @param horizontalScrolling 
     */
    public  void setScrollingDirection (boolean horizontalScrolling) {
        this.horizontalScrolling  = horizontalScrolling;
    }    

}
