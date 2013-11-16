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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Describes a Book in My Library RMS
 */
public final class BookInMyLibRMS {
    
    private  int    bookRecordId=0;
    private  String bookFileName = "";
    private  String bookFileExt = "";
    private  long   bookFileSize;
    private  long   bookFileLastModified;
    private  String bookGenre;
    private  String bookAuthor = "";
    private  String bookTitle = "";
    private  String bookLang = "";
    private  long   bookLastOpen=0;
    
    

    /**
     * Write bookInMyLibData to a data stream.
     * @param dout data stream
     * @throws IOException 
     */
    public final void writeTo(DataOutputStream dout)
        throws IOException {
        dout.writeInt(bookRecordId);
        dout.writeUTF(bookFileName);
        dout.writeUTF(bookFileExt);
        dout.writeLong(bookFileSize);
        dout.writeLong(bookFileLastModified);
        dout.writeUTF(bookGenre);        
        dout.writeUTF(bookAuthor);
        dout.writeUTF(bookTitle);
        dout.writeUTF(bookLang);
        dout.writeLong(bookLastOpen);
    }

    /**
     * Read a bookInMyLibData from a data stream
     * @param din data stream
     * @return new bookInMyLib read from din
     * @throws IOException 
     */
    public static BookInMyLibRMS readFrom(DataInputStream din)
        throws IOException {
        BookInMyLibRMS bookInMyLibRMS = new BookInMyLibRMS();
        bookInMyLibRMS.bookRecordId = din.readInt();
        bookInMyLibRMS.bookFileName = din.readUTF();
        bookInMyLibRMS.bookFileExt = din.readUTF();
        bookInMyLibRMS.bookFileSize = din.readLong();
        bookInMyLibRMS.bookFileLastModified = din.readLong();
        bookInMyLibRMS.bookGenre = din.readUTF();
        bookInMyLibRMS.bookAuthor = din.readUTF();
        bookInMyLibRMS.bookTitle = din.readUTF();
        bookInMyLibRMS.bookLang = din.readUTF();
        bookInMyLibRMS.bookLastOpen = din.readLong();
        return bookInMyLibRMS;
    }

    
    
    public int getBookRecordId() {
        return bookRecordId;
    }
    
    public void setBookRecordId(int bookRecordId) {
        this.bookRecordId = bookRecordId;
    } 
    
    
    
    public String getBookFileName() {
        return bookFileName;
    }
    
    public void setBookFileName(String bookFileName) {
        this.bookFileName = bookFileName;
    }
    
    
    public String getBookFileExt() {
        return bookFileExt;
    }
    
    public void setBookFileExt(String bookFileExt) {
        this.bookFileExt = bookFileExt;
    }
    
    public long getBookFileSize() {
        return bookFileSize;
    }
    
    public void setBookFileSize(long bookFileSize) {
        this.bookFileSize = bookFileSize;
    }
    
    
    public long getBookFileLastModified() {
        return bookFileLastModified;
    }
    
    public void setBookFileLastModified(long bookFileLastModified) {
        this.bookFileLastModified = bookFileLastModified;
    }    
    
    
 

    public String getBookAuthor() {
        return bookAuthor;
    }
    
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }    
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
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
    
    
    public long getBookLastOpen() {
        return bookLastOpen;
    }
    
    public void setBookLastOpen(long bookLastOpen) {
        this.bookLastOpen = bookLastOpen;
    }     

}
