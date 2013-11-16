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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.AlertType;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import org.fbreader.localization.L10n;
import org.fbreader.models.BookInMyLibRMS;
import org.fbreader.views.ViewMaster;


/**
 * Classes for sort MyLibRMS
 *  
 */
public class MyLibSort {
    
    private static MyLibSort self = null;
    
    private MyLibUtils myLibUtils;
    private ViewMaster viewMaster;
    private Vector myLibRMSByBookLastOpenSortDataVector=null;
    
    
    public MyLibSort() {
        myLibUtils=MyLibUtils.getInstance();
        viewMaster=ViewMaster.getInstance();
        
    }
    
    /**
     * @return MyLibSort singleton
     */
    public static MyLibSort getInstance() {
        if (self == null) {
            self = new MyLibSort();
        }
        return self;
    }    
    
    
    
    /**
     * Sort myLibRMS by OpenBookTime
     * 
     */
    public void sortMyLibRMSByBookLastOpen() {
        try {
//--- 1 --- open MyLibRMS
            RecordStore rs=myLibUtils.openMyLibRMS();
            
            
            if(rs.getNumRecords()>0) {
                
                //prepare myLibRMSDataVector for writing new BookInMyLibRMS objects
                if (myLibRMSByBookLastOpenSortDataVector!=null) {
                    myLibRMSByBookLastOpenSortDataVector.removeAllElements();
                }
                else {
                     myLibRMSByBookLastOpenSortDataVector=new Vector();
                }                 
                
//--- 2 --- define MyLibRMSComparatorByBookLastOpen comparator
                MyLibRMSComparatorByBookLastOpen comparator = new MyLibRMSComparatorByBookLastOpen();
                
                RecordEnumeration re = rs.enumerateRecords(null, comparator, false);
                if( re.numRecords()>0 )
                {
                    while(re.hasNextElement()) {
                        try {
                            byte[] filterRecordData = re.nextRecord();
                            ByteArrayInputStream bis = new ByteArrayInputStream(filterRecordData);
                            DataInputStream dis = new DataInputStream(bis);
                            
                            BookInMyLibRMS bookInMyLibRMS=BookInMyLibRMS.readFrom(dis);

                            myLibRMSByBookLastOpenSortDataVector.addElement(bookInMyLibRMS); 

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InvalidRecordIDException ex) {
                            ex.printStackTrace();
                        } catch (RecordStoreNotOpenException ex) {
                            ex.printStackTrace();
                        } catch (RecordStoreException ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                    //close MyLibRMS
                    myLibUtils.closeMyLibRMS(rs);
                }
                else{
                    viewMaster.showAlert(L10n.getMessage("BOOKS_LAST_OPEN_NOT_FOUND_IN_MY_LIB_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_LAST_OPEN_NOT_FOUND_IN_MY_LIB_MSG_CONTENT"),
                                      AlertType.WARNING);                 
                 
                }
            }
            else {
                    viewMaster.showAlert(L10n.getMessage("BOOKS_LAST_OPEN_NOT_FOUND_IN_MY_LIB_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_LAST_OPEN_NOT_FOUND_IN_MY_LIB_MSG_CONTENT"),
                                      AlertType.WARNING); 
            }
        }
        catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        }
    } 
    
    /**
     * Nested Class MyLibRMSComparatorByOpenBookTime define sorting rules 
     * for sortingMyLibRMSByOpenBookTime()
     */
    public class MyLibRMSComparatorByBookLastOpen implements RecordComparator{
       
        
        public int compare(byte[] rec1, byte[] rec2) {
            long bookLastOpenTime1 = getBookLastOpenTime(rec1);
            long bookLastOpenTime2 = getBookLastOpenTime(rec2);
            
            if (bookLastOpenTime2 < bookLastOpenTime1){
                
                return PRECEDES;
            }    
            else if (bookLastOpenTime1 == bookLastOpenTime2){
                return EQUIVALENT;
            }    
            else {
                return FOLLOWS;
            }

        }
        
        private long getBookLastOpenTime(byte[] currentRecord){
           
            BookInMyLibRMS bookInMyLibRMS=null;
            
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(currentRecord);
                DataInputStream dis = new DataInputStream(bis);
                bookInMyLibRMS = BookInMyLibRMS.readFrom(dis);
            }
             catch (Exception e) {
                  e.printStackTrace();
            }
            finally {
                return bookInMyLibRMS.getBookLastOpen();
            }
            
            
        }
    }        
        
        
    
    /**
    * Get myLibRMSByBookLastOpenSortDataVector
    */     
    public Vector getMyLibRMSByBookLastOpenSortDataVector() {
        return myLibRMSByBookLastOpenSortDataVector;
    }


}
