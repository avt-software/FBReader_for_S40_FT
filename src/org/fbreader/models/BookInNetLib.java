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
package org.fbreader.models;

/**
 * Describes a Book in Network Library
 */
public final class BookInNetLib {
    
    private  String bookTitle = "";
    private  String bookAuthor = "";
    private  String bookGenre = "";
    private  String bookLang = "";
    private  String downloadBookURL = "";
    private  String downloadBookFileName = "";
    
    

    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }      
    

    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }    
    

    public String getBookTags() {
        return bookGenre;
    }     
    
    public void setBookTags(String bookTags) {
        this.bookGenre = bookTags;
    }      

    
    public String getBookLang() {
        return bookLang;
    }
    
    public void setBookLang(String bookLang) {
        this.bookLang = bookLang;
    }
    
    
    public String getDownloadBookURL() {
        return downloadBookURL;
    }
    
    public void setDownloadBookURL(String downloadBookURL) {
        this.downloadBookURL = downloadBookURL;
    }
    
    public String getDownloadBookFileName() {
        return downloadBookFileName;
    }
    
    public void setDownloadBookFileName(String downloadBookFileName) {
        this.downloadBookFileName = downloadBookFileName;
    }      
    
    
   

}
