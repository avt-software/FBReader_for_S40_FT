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
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import javax.microedition.rms.RecordFilter;
import org.fbreader.models.BookInMyLibRMS;
import org.fbreader.views.ViewMaster;
import org.fbreader.localization.L10n;



/**
 * Classes for filter MyLibRMS
 * 
 */
public class MyLibFilter {
    
    private static MyLibFilter self = null;
    
    private MyLibUtils myLibUtils;
    private ViewMaster viewMaster;
    
    private Vector myLibRMSByAuthorFilterDataVector=null;
    private String filterStringByAuthor;
    
    private Vector myLibRMSByTagFilterDataVector=null;
    private String filterStringByTag;
    
        
    public MyLibFilter() {
        myLibUtils=MyLibUtils.getInstance();
        viewMaster=ViewMaster.getInstance();
        
    }
    
    /**
     * @return MyLibSort singleton
     */
    public static MyLibFilter getInstance() {
        if (self == null) {
            self = new MyLibFilter();
        }
        return self;
    }    
    
    
    
    /**
     * Filter myLibRMS by Author
     * 
     */
    public void filterMyLibRMSByAuthor() {
        
        try {
//--- 1 --- open MyLibRMS
            RecordStore rs=myLibUtils.openMyLibRMS();
            
            
            if(rs.getNumRecords()>0) {
                
                //prepare myLibRMSDataVector for writing new BookInMyLibRMS objects
                if (myLibRMSByAuthorFilterDataVector!=null) {
                    myLibRMSByAuthorFilterDataVector.removeAllElements();
                }
                else {
                     myLibRMSByAuthorFilterDataVector=new Vector();
                }                 
                
//--- 2 --- define MyLibRMSFilterByAuthor filter
                MyLibRMSFilterByAuthor filter = new MyLibRMSFilterByAuthor(filterStringByAuthor);
                
                RecordEnumeration re = rs.enumerateRecords(filter, null, false);
                if( re.numRecords()>0 )
                {
                    while(re.hasNextElement()) {
                        try {
                            
                           
                            int id = re.nextRecordId();
                            
                            ByteArrayInputStream bis = new ByteArrayInputStream(rs.getRecord(id));
                            
                            DataInputStream dis = new DataInputStream(bis);
                            
                            
                            //set for the bookInMyLibRMS bookRecordId field value
                            //and add element in myLibRMSByAuthorFilterDataVector
                            BookInMyLibRMS bookInMyLibRMS=BookInMyLibRMS.readFrom(dis);

                            bookInMyLibRMS.setBookRecordId(id);
                           
                            myLibRMSByAuthorFilterDataVector.addElement(bookInMyLibRMS); 

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
                 viewMaster.showAlert(L10n.getMessage("BOOKS_BY_AUTHOR_NOT_FOUND_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_BY_AUTHOR_NOT_FOUND_MSG_CONTENT"),
                                      AlertType.WARNING);
                }
            }
            else {
                 viewMaster.showAlert(L10n.getMessage("BOOKS_BY_AUTHOR_NOT_FOUND_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_BY_AUTHOR_NOT_FOUND_MSG_CONTENT"),
                                      AlertType.WARNING);
            }
        }
        catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        }
    } 
    
    /**
     * Nested Class MyLibRMSFilterByAuthor define filter rule
     * for  filterMyLibRMSByAuthor()
     */
    public class MyLibRMSFilterByAuthor implements RecordFilter {
       
        private String filterString;
        
        public MyLibRMSFilterByAuthor(String filterString){  
            this.filterString = filterString; 
        }
        
       
        public boolean matches(byte[] candidate) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(candidate);
                DataInputStream dis = new DataInputStream(bis);
                
                BookInMyLibRMS bookInMyLibRMS = BookInMyLibRMS.readFrom(dis);
                
                String bookAuthor = bookInMyLibRMS.getBookAuthor();
                                
                if (filterString.equals(bookAuthor)) {
                    return true;
                }
                return false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }        

    }        
        
    /**
     * Get FilterStringByAuthor
     * 
     * @return 
     */    
    
    public String getFilterStringByAuthor(){
        return filterStringByAuthor;
    }
    
    /**
     * Set FilterStringByAuthor
     * 
     * @param filterStringByAuthor
     */    
    
    public void setFilterStringByAuthor(String filterStringByAuthor){
        this.filterStringByAuthor=filterStringByAuthor;
    }
    
    
    /**
    * Get myLibRMSByAuthorFilterDataVector
    */     
    public Vector getMyLibRMSByAuthorFilterDataVector() {
        return myLibRMSByAuthorFilterDataVector;
    }
    
    

    /**
     * Filter myLibRMS by Tag
     * 
     */
    public void filterMyLibRMSByTag() {
        
        try {
//--- 1 --- open MyLibRMS
            RecordStore rs=myLibUtils.openMyLibRMS();
            
            
            if(rs.getNumRecords()>0) {
                
                //prepare myLibRMSDataVector for writing new BookInMyLibRMS objects
                if (myLibRMSByTagFilterDataVector!=null) {
                    myLibRMSByTagFilterDataVector.removeAllElements();
                }
                else {
                     myLibRMSByTagFilterDataVector=new Vector();
                }                 
                
//--- 2 --- define MyLibRMSFilterByAuthor filter
                
                MyLibRMSFilterByTag filter = new MyLibRMSFilterByTag(filterStringByTag);
                
                RecordEnumeration re = rs.enumerateRecords(filter, null, false);
                
                
                if( re.numRecords()>0 )
                {
                    while(re.hasNextElement()) {
                        try {
                            
                            int id = re.nextRecordId();
                            
                            ByteArrayInputStream bis = new ByteArrayInputStream(rs.getRecord(id));
                            
                            DataInputStream dis = new DataInputStream(bis);
                            
                            
                            //set for the bookInMyLibRMS bookRecordId field value
                            //and add element in myLibRMSByTagFilterDataVector
                            BookInMyLibRMS bookInMyLibRMS=BookInMyLibRMS.readFrom(dis);
                            
                            bookInMyLibRMS.setBookRecordId(id);

                            myLibRMSByTagFilterDataVector.addElement(bookInMyLibRMS); 

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
                 viewMaster.showAlert(L10n.getMessage("BOOKS_BY_TAG_NOT_FOUND_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_BY_TAG_NOT_FOUND_MSG_CONTENT"),
                                      AlertType.WARNING);
                }
            }
            else {
                 viewMaster.showAlert(L10n.getMessage("BOOKS_BY_TAG_NOT_FOUND_MSG_TITLE"), 
                                      L10n.getMessage("BOOKS_BY_TAG_NOT_FOUND_MSG_CONTENT"),
                                      AlertType.WARNING);
            }
        }
        catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        }
    } 
    
    /**
     * Nested Class MyLibRMSFilterByTag define filter rule
     * for  filterMyLibRMSByTag()
     */
    public class MyLibRMSFilterByTag implements RecordFilter {
       
        private String filterString;
        
        public MyLibRMSFilterByTag(String filterString){  
            this.filterString = filterString; 
        }
        
       
        public boolean matches(byte[] candidate) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(candidate);
                DataInputStream dis = new DataInputStream(bis);
                
                BookInMyLibRMS bookInMyLibRMS = BookInMyLibRMS.readFrom(dis);
                
                String bookTag = bookInMyLibRMS.getBookTags();
                                
                if (filterString.equals(bookTag)) {
                    return true;
                }
                return false;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }        

    }        
        
    /**
     * Get FilterStringByTag
     * 
     * @return 
     */    
    
    public String getFilterStringByTag(){
        return filterStringByTag;
    }
    
    /**
     * Set FilterStringByTag
     * 
     * @param filterStringByTag
     */    
    
    public void setFilterStringByTag(String filterStringByTag){
        this.filterStringByTag=filterStringByTag;
    }
    
    
    /**
    * Get myLibRMSByTagFilterDataVector
    */     
    public Vector getMyLibRMSByTagFilterDataVector() {
        return myLibRMSByTagFilterDataVector;
    }

}
