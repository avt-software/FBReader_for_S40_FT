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

package org.fbreader.views.model.parser;

/**
 * Element <entry>
 * 
 * This class is thread safe. You can and should externally synchronized on this
 * object to access multiple fields without risk that they will be changed by
 * background updates.
 *  
 */

public final class FeedBooksEbook {
        
        /*
         * needed nested elements for  <entry> element  
         */
        private String title="";
        private String name="";
        private String language="";
        private String category="";
        private String link="";

        
        /**
         * Get the title
         * 
         * @return title
         */
        public synchronized String getTitle() {
            return title;
        }
        
        /**
         * Set the title
         * 
         * @param title
         */
        public synchronized void setTitle(String title) {
            this.title = title;
        }
        
        
        /**
         * Get the name
         * 
         * @return name
         */
        public synchronized String getName() {
            return name;
        }
        
        /**
         * Set the name
         * 
         * @param name
         */
        public synchronized void setName(String name) {
            this.name = name;
        }        
        


        /** 
         * Get the language
         * 
         * @return language
         */
        public synchronized String getLanguage() {
            return language;
        }
        
        /**
         * Set the language
         * 
         * @param language
         */
        public synchronized void setLanguage(String language) {
            this.language = language;
        } 
        
        /* Get the category
         * 
         * @return category
         */
        public synchronized String getCategory() {
            return category;
        }
        
        /**
         * Set the category
         * 
         * @param category
         */
        public synchronized void setCategory(String category) {
            this.category += category+" ";
        }
        
        
         /**
         * Get the link
         * 
         * @return link
         */
        public synchronized String getLink() {
            return link;
        }
        
        /**
         * Set the link
         * 
         * @param link
         */
        public synchronized void setLink(String link) {
            this.link = link;
        }        
 
        
        /**
         * Debug use
         * @return String
         */
        public synchronized String toStringManyBooksCategory() {
            return "<entry> element for FeedBooksEbooks(top.atom etc.):"+ 
                   "title=" + title + 
                   "name=" + name +
                   "language="+language+
                   "category="+category+
                   "link="+link;
        }          
        
}
