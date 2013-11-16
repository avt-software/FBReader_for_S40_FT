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

* 1.The module TitleInfo.java is the data model for the 
*   <title-info> section of the FB2 document
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/
package org.fbreader.formats.fb2;


/**
 * 1.1.1 Element <title-info>
 * 
 * This class is thread safe. You can and should externally synchronized on this
 * object to access multiple fields without risk that they will be changed by
 * background updates.
 *
 */

public final class TitleInfo {
        
        private int id;
        //1.1.1.1 Element <genre>  
        private String genre="";
        //1.1.1.2 <first-name>+<middle-name>+<last-name>
        private String author="";
        //1.1.1.3 <book-title>
        private String bookTitle="";
        //1.1.1.4 <annotation>
        private String annotation="";
        //1.1.1.5 <date>
        private String date="";
        //1.1.1.6 <coverpage><image>
        private String coverPageImagePath="";
        //1.1.1.7 <lang>
        private String bookLang="";
        //1.1.1.8 <src-lang>
        private String srcLang="";
        //1.1.1.9 <first-name>+<middle-name>+<last-name>
        private String translator="";
        
        //detection flag opening tag <author>
        private boolean flagAuthor=false;
        //detection flag opening tag <translator>
        private boolean flagTranslator=false;

        
        /**
         * Get the id for TitleInfo object in fb2BookDataVector
         * id equals current fb2BookDataVector.size()
         * 
         * @return id
         */
        public synchronized int getId() {
            return id;
        }
        
        /**
         * Set the id for TitleInfo object in fb2BookDataVector
         * id equals current fb2BookDataVector.size()
         * 
         * @param id
         */
        public synchronized void setId(int id) {
            this.id = id;
        } 
        
        
        /**
         * Get the genre
         * 
         * @return genre 
         */
        public synchronized String getGenre() {
            return genre;
        }
        
        /**
         * Set the genre
         * 
         * @param genre
         */
        public synchronized void setGenre(String genre) {
            this.genre += genre+" ";
        }
        
        /**
         * Set the "Unknown genre" value
         * @param genre
         */
        public synchronized void setUnknownGenre(String genre) {
            this.genre = genre;
        }
        
        
        /**
         * Get the author
         * 
         * @return author
         */
        public synchronized String getAuthor() {
            return author;
        }
        
        /**
         * Set the Author Name
         * 
         * @param author
         */
        public synchronized void setAuthor(String author) {
            this.author += author+" ";
        }
        
        
         /** Get value of the the flagAuthor field
         * 
         * @return flagAuthor
         */
        public synchronized boolean getFlagAuthor() {
            return flagAuthor;
        }         
        
        /** Check value of the the flagAuthor field
         * 
         * @return 
         */
        public synchronized boolean isAuthor() {
            if (!flagAuthor) {
                return false;    
            }
            else {
                return true;
            }
        }
        
        /**
         * Set the flagAuthor field
         * 
         * @param flagAuthor
         */
        public synchronized void setFlagAuthor(boolean flagAuthor) {
            this.flagAuthor = flagAuthor;
        }        
        
        
        
        
        
        
        /**
         * Set Unknown Author
         * 
         * @param author
         */
        public synchronized void setUnknownAuthor(String author) {
            this.author = author;
        } 
        
        
        /**
         * Get the Book Title
         * 
         * @return author
         */
        public synchronized String getBookTitle() {
            return bookTitle;
        }
        
        /**
         * Set the Book Title
         * 
         * @param author
         */
        public synchronized void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        } 
        
        /**
         * Get the Book language
         * 
         * @return author
         */
        public synchronized String getBookLang() {
            return bookLang;
        }
        
        /**
         * Set the Book language
         * 
         * @param author
         */
        public synchronized void setBookLang(String bookLang) {
            this.bookLang = bookLang;
        }         
        
        
        
        
        
        /**
         * Debug use
         * @return String
         */
        public synchronized String toStringTitleInfo() {
            return "TitleInfo:"+ 
                   "genre=" + genre + 
                   "author=" + author + 
                   "bookTitle=" + bookTitle;
        }          
        
}
