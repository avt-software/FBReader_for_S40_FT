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
 * 
 */

public final class FeedBooksCatalog {
        
       /*
        * nested elements for  <entry> element  
        */
        private String title="";
        private String id="";
        private String content="";

        
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
         * Get the id
         * 
         * @return id
         */
        public synchronized String getId() {
            return id;
        }
        
        /**
         * Set the id
         * 
         * @param id
         */
        public synchronized void setId(String id) {
            this.id = id;
        }        
        


        /** 
         * Get the content
         * 
         * @return content
         */
        public synchronized String getContent() {
            return content;
        }
        
        /**
         * Set the content
         * 
         * @param content
         */
        public synchronized void setContent(String content) {
            this.content = content;
        }             
        
        /**
         * Debug use
         * @return String
         */
        public synchronized String toStringManyBooksCategory() {
            return "<entry> element for FeedBooksCatalog(catalog.atom):"+ 
                   "title=" + title + 
                   "id=" + id +
                   "content="+content;
        }          
        
}
