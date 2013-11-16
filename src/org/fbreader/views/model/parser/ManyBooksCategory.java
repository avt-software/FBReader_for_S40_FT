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
 * Element <category>
 * 
 * This class is thread safe. You can and should externally synchronized on this
 * object to access multiple fields without risk that they will be changed by
 * background updates.
 * 
 */

public final class ManyBooksCategory {
        
        /*
         * attributes for  <manybooksCategory> element  
         */
    
        //attribute categoryName 
        private String categoryName="";
        //attribute fileUrl
        private String fileUrl="";        

        
        /**
         * Get the categoryName
         * 
         * @return categoryName
         */
        public synchronized String getCategoryName() {
            return categoryName;
        }
        
        /**
         * Set the categoryName
         * 
         * @param categoryName
         */
        public synchronized void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
        
        
        /**
         * Get the fileUrl
         * 
         * @return fileUrl
         */
        public synchronized String getFileUrl() {
            return fileUrl;
        }
        
        /**
         * Set the fileUrl
         * 
         * @param fileUrl
         */
        public synchronized void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }        
        
        
        
        /**
         * Debug use
         * @return String
         */
        public synchronized String toStringManyBooksCategory() {
            return "ManyBooksCategory:"+ 
                   "nameCategory=" + categoryName + 
                   "fileUrl=" + fileUrl;
        }          
        
}
