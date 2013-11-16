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
*    1.The module FB2ChaptersPrepareParser.java designed for search and processing
*    <binary> elements in FB2 files.
*    
*    2. For decode binary image from Base64 using Bouncy Castle Crypto APIs for Java, 
*       licensed under the MIT license. 
*       Copyright (c) 2000 - 2013 The Legion Of The Bouncy Castle (http://www.bouncycastle.org)
* 
* 
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.albite.book.model.parser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.fbreader.Main;
import org.bouncycastle.util.encoders.Base64;
import org.xml.sax.Attributes;



/**
 * Parses the FB2 .
 */
public class FB2ChaptersPrepareParser
    extends FB2SaxParser {

    private final Vector chaptersPosition = new Vector();
    
    String idAttr=null;
    private FileConnection fcFB2BookFile;
    
    private int startBinaryPosInBytes;
    
    
    public FB2ChaptersPrepareParser(FileConnection fcFB2BookFile) {
        this.fcFB2BookFile=fcFB2BookFile;
    }

    /**
     * Event handler for the start of an XML document. 
    */ 
    public void startDocument(){
        super.startDocument();
        if (!chaptersPosition.isEmpty()) {
            chaptersPosition.removeAllElements();
        }
        
        //Setting the initial value for  startBinaryPosInBytes
        startBinaryPosInBytes=-1;
        
    }    

    /**
     * @see FB2SaxParser#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     */
    public final void startElement(String uri, String localName, String qName,
        Attributes attributes) {
        super.startElement(uri, localName, qName, attributes);

        
        //processing start <binary> element
        if (qName.equalsIgnoreCase("binary")) {
            if (startBinaryPosInBytes == -1) {
                startBinaryPosInBytes=getStartElementPosInBytes();
            }   
            
            for (int i = 0; i < attributes.getLength(); i++) {
                // Get names and values for each attribute
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);
                if (name.equals("id")) {
                    idAttr=value;
                }
            }    
                
        }
        
    }

    
    
    /**
     * @see FB2SaxParser#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public final void endElement(String uri, String localName, String qName) {
        
        super.endElement(uri, localName, qName);
        
        String chars = getChars();
        
        
        //Decode binary image from Base64
        //and save it in .jpeg or .png file
        if (qName.equals("binary")) {
            
            String binaryImageData=chars;
            
            //#debug
            //L.i("Start Base64.decode(chars)","qname=" + qName);                
            byte[] base64DecodedImage = Base64.decode(binaryImageData);
            //#debug
            //L.i("End Base64.decode(chars)","qname=" + qName);
            
            //Save byte array byte[] base64DecodedImage as Image file
            String booksFolderURL = fcFB2BookFile.getURL();
            int booksFolderURLSplitPos=booksFolderURL.lastIndexOf('/');
            String booksFolderPath=booksFolderURL.substring(0,booksFolderURLSplitPos+1);
            String imageFileName = idAttr;
            String imageFileNameURL=booksFolderPath+imageFileName;
            FileConnection fcImageFileNameURL=null;

            try { 
                fcImageFileNameURL = (FileConnection)Connector.open(imageFileNameURL);

                //if Image file already exists in bookFolder, delete it
                if (fcImageFileNameURL.exists()){
                  fcImageFileNameURL.delete();
                }

                //create new Image file
                fcImageFileNameURL.create();  

                //open DataOutputStream for write Image file
                DataOutputStream dos = fcImageFileNameURL.openDataOutputStream();
                dos.write(base64DecodedImage);

                dos.close();
                fcImageFileNameURL.close();
            } catch (IOException ioe) {
               System.out.println("IOException: "+ioe.getMessage());            
            }                                 
        }

    }
    
   public  Vector getChaptersPosition() {
       return chaptersPosition;
   } 
   
   public  int getStartBinaryPosInBytes() {
       return startBinaryPosInBytes;
   }    
    
}


