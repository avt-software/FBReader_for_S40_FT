/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

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

* Changes and additions to the author's code:
* --------------------------------------------
*    1.The module FB2SaxParser.java designed for search and pre-processing
*      structure and length of <binary> and <empty-line> elements in FB2 files.
*    
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.albite.book.model.parser;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


/**
 * FB2SaxParser abstract class to be extended by specific parsers.
 * Based on the default SAX parser event handler.
 */
public abstract class FB2SaxParser
    extends DefaultHandler {

    private StringBuffer chars = new StringBuffer();
    private int startElementPosInBytes=0;
    private int endElementPosInBytes=0;


    /**
     * Event handler for the start of an XML document. 
    */ 
    public void startDocument(){
            
    }
    
    
    /**
     * Event handler for the the of an XML document. 
    */ 
    public void endDocument(){
        
    }    
    
    
    
    /**
     * Event handler for the start of an XML element. 
     * Clears the content string buffer so that a new string can be stored.
     * @see DefaultHandler#startElement(java.lang.String, java.lang.String, 
     * java.lang.String, org.xml.sax.Attributes) 
     */
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) {
        chars = new StringBuffer();

        //Pre-Processing Structure and Length of 
        //the <image> and <empty-line> elements
        
        startElementPosInBytes=endElementPosInBytes;
        
        String startElementStr="<"+qName;
        
        for (int i = 0; i < attributes.getLength(); i++) {
            // Get names and values for each attribute
            String name = attributes.getQName(i);
            String value = attributes.getValue(i);
                startElementStr+=" "+name+"="+"\""+value+"\"";
            }
        
        //If the element is not paired
        if (qName.equalsIgnoreCase("image") || 
            qName.equalsIgnoreCase("empty-line") 
           ) {
            startElementStr+="/>";
        }
        else {
            startElementStr+=">";
        }
        
        byte[] startElementAsByteArray = startElementStr.getBytes();
        
        endElementPosInBytes+=startElementAsByteArray.length;
    }          
        
   

    /**
     * Character callback for content characters. Data is appended to the
     * string buffer.
     * @see DefaultHandler#characters(char[], int, int) 
     */
    public void characters(char[] ch, int start, int length) {
        chars.append(ch, start, length);
        byte[] cAsByteArray = chars.toString().getBytes();
        endElementPosInBytes+=cAsByteArray.length;
    }
    
    
    /**
     * @see FB2SaxParser#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) {

        String endElementStr;
        
        if (!qName.equalsIgnoreCase("image") && 
            !qName.equalsIgnoreCase("empty-line")
           ) {
            endElementStr="</"+qName+">";
            byte[] endElementAsByteArray = endElementStr.getBytes();
            endElementPosInBytes+=endElementAsByteArray.length;
        }
    }
    
    

    /**
     * Retrieves the last content string and clears the string buffer.
     * @return content string
     */
    protected final String getChars() {
  
        // For some reason there is some extra whitespace around 
        // the characters sometimes. Trim those out.
        String c = chars.toString().trim();
        chars = new StringBuffer();
        return c;
    }
    
    
    public final int getEndElementPosInBytes() {
        return endElementPosInBytes;
    }
    
    
    public final void setEndElementPosInBytes(int endElementPosInBytes) {
        this.endElementPosInBytes=endElementPosInBytes;
    }
    
    
    public final int getStartElementPosInBytes() {
        return startElementPosInBytes;
    }
    
    
    public final void setStartElementPosInBytes(int startElementPosInBytes) {
        this.startElementPosInBytes=startElementPosInBytes;
    }    
  
}
