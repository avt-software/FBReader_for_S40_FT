package org.albite.book.model.book;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;
import javax.microedition.io.file.FileConnection;
import javax.xml.parsers.*;
import org.albite.book.model.parser.FB2ChaptersPrepareParser;
import org.albite.book.model.parser.FB2TextParser;
import org.albite.book.model.parser.HTMLTextParser;
import org.albite.book.model.parser.PlainTextParser;
import org.albite.book.model.parser.TextParser;
import org.albite.io.PartitionedConnection;
import org.albite.io.RandomReadingFile;
import org.albite.io.decoders.AlbiteStreamReader;
import org.albite.io.decoders.Encodings;
import org.albite.util.archive.Archive;
import org.albite.util.archive.File;
import org.albite.util.archive.folder.ArchiveFolder;
import org.fbreader.localization.L10n;


/**
 *
 * @author Svetlin Ankov <galileostudios@gmail.com>
 * 
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
* 1. Added logic to process the FB2 files using the following key methods:
*    1.1 Method parseFB2ForChaptersPrepareSAX()
*    1.2 Method checkFB2DocDeclaration()
*    1.3 Method splitFB2ChapterIntoPieces()
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/



public abstract class Book
        implements Connection {

    public static final String EPUB_EXTENSION = ".epub";
    public static final String PLAIN_TEXT_EXTENSION = ".txt";
    public static final String HTM_EXTENSION = ".htm";
    public static final String HTML_EXTENSION = ".html";
    public static final String XHTML_EXTENSION = ".xhtml";
    public static final String FB2_EXTENSION = ".fb2";
    

    public static final String[] SUPPORTED_BOOK_EXTENSIONS = new String[] {
        EPUB_EXTENSION,
        PLAIN_TEXT_EXTENSION,
        HTM_EXTENSION, HTML_EXTENSION, XHTML_EXTENSION,
        FB2_EXTENSION
    };

    protected static final String   FB2_BINARY_TAG    = "binary";
    


    
    
    /*
     * Main info
     */
    protected String                title                    = "Untitled";
    protected String                author                   = "Unknown Author";
    protected String                language            = "";
    protected String                currentLanguage     = "";    
    

   
    protected String                bookURL                  = null;

    /*
     * Chapters
     */
    protected Chapter[]             chapters;
    protected Chapter               currentChapter;
    
    protected TextParser            parser;
    
    protected int                   startBinaryPosInBytes;

    
    public abstract void close() throws IOException;




    /**
     * unload all chapters from memory
     */
    public final void unloadChaptersBuffers() {
        Chapter chap = chapters[0];
        while (chap != null) {
            chap.unload();
            chap = chap.getNextChapter();
        }
    }

    public boolean setEncoding(final String encoding) {
        boolean reflowNeeded = currentChapter.setEncoding(encoding);

        //Set it for all chapters. Epubs would override this behaviour
        for (int i = 0; i < chapters.length; i++) {
            chapters[i].setEncoding(encoding);
        }

        return reflowNeeded;
    }


    public final int getChaptersCount() {
        return chapters.length;
    }

    public final Chapter getChapter(final int number) {

        if (number < 0) {
            return chapters[0];
        }

        if (number > chapters.length - 1) {
            return chapters[chapters.length - 1];
        }

        return chapters[number];
    }

    public final Chapter getCurrentChapter() {
        return currentChapter;
    }

    public final void setCurrentChapter(final Chapter bc) {
        currentChapter = bc;
    }

    public final int getCurrentChapterPosition() {
        return currentChapter.getCurrentPosition();
    }

    public final void setCurrentChapterPos(final int pos) {
        if (pos < 0 || pos >= currentChapter.getTextBuffer().length) {
            throw new IllegalArgumentException("Position is wrong");
        }

        currentChapter.setCurrentPosition(pos);
    }

    public final TextParser getParser() {
        return parser;
    }

    public static Book open(String filename)
            throws IOException, BookException {

        String filenameLowerCase = filename.toLowerCase();
   
        if (filenameLowerCase.endsWith(FB2_EXTENSION)) {
            
            return new FB2Book(
                    filename,
                    new ArchiveFolder(
                    RandomReadingFile.getPathFromURL(filename)),
                    new FB2TextParser(),
                    true);            
            
        }
        
        if (filenameLowerCase.endsWith(EPUB_EXTENSION)) {
            return new EPubBook(filename);
        }

        if (filenameLowerCase.endsWith(PLAIN_TEXT_EXTENSION)) {
            return new FileBook(filename, null, new PlainTextParser(), false);
        }

        if (filenameLowerCase.endsWith(HTM_EXTENSION)
                || filenameLowerCase.endsWith(HTML_EXTENSION)
                || filenameLowerCase.endsWith(XHTML_EXTENSION)) {
            return new FileBook(
                    filename,
                    new ArchiveFolder(
                            RandomReadingFile.getPathFromURL(filename)),
                    new HTMLTextParser(),
                    true);
         }

        throw new BookException("Unsupported file format.");
    }
   
   /**
    * Parse FB2 with SAX parser and calculate position in bytes for 
    * start <binary> sections in FB2 and uses this value in 
    * splitFB2ChapterIntoPieces() method
    * 
    * 
    * @param fcFB2BookFile
    * @throws IOException
    * @throws BookException 
    */     
   protected final void parseFB2ForChaptersPrepareSAX(FileConnection fcFB2BookFile) throws IOException, BookException {
      
       InputStream in;
       
       try {
            // this will handle our XML
            FB2ChaptersPrepareParser handler = new FB2ChaptersPrepareParser(fcFB2BookFile);
            
            // get a parser object
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();            
            
            //get an InputStream from FB2 book file
            in = fcFB2BookFile.openInputStream();
        
            //Analyze structure and size in bytes FB2 Document Declaration
            int sizeFB2DocDecl=checkFB2DocDeclaration(in);
            handler.setEndElementPosInBytes(sizeFB2DocDecl);
            
            //Close Input Stream
            in.close();
            
            //get an InputStream from FB2 book file
            in = fcFB2BookFile.openInputStream();
            
            //parse the XML data stream
            saxParser.parse(in, handler);
            
            //Get start positions in bytes for binary section(s)
            startBinaryPosInBytes=handler.getStartBinaryPosInBytes();
            
            //close InputStream
            in.close();

       }
       catch (Exception e) {
                //throw new BookException("Error  <binary> tags in FB2 Book");           
       }

   }
   
   
   /**
    * Analyze structure and length in bytes Document Declaration 
    * as AlbiteStreamReader from InputStream
    * 
    * @param in
    * @throws IOException
    * @throws BookException 
    */ 
   protected final int checkFB2DocDeclaration(InputStream in) throws IOException, BookException {
         
       int sizeDocDecl=0; 
       
        
       AlbiteStreamReader isr = new AlbiteStreamReader(in, Encodings.DEFAULT);
        
        char[] docDeclAsCharArray=new char[1024]; 
        
               
        isr.read(docDeclAsCharArray, 0, 5);

         if (docDeclAsCharArray[0]=='<' && 
             docDeclAsCharArray[1]=='?' && 
             docDeclAsCharArray[2]=='x' &&     
             docDeclAsCharArray[3]=='m' &&         
             docDeclAsCharArray[4]=='l') {
             
             //read next char from DataInputStream
             int i=5;
             isr.read(docDeclAsCharArray, i, 1);
             
             while (docDeclAsCharArray[i] != '>') {
                 i++;
                 isr.read(docDeclAsCharArray, i, 1);
             }
             
             String docDeclStr=new String(docDeclAsCharArray,0,i+1);
             
             
             byte[] docDeclAsByteArray = docDeclStr.getBytes();
             
             sizeDocDecl=docDeclAsByteArray.length;
             
         }         
         

         isr.close();
         
         return sizeDocDecl;
         

    }    
   
   
   
    
    protected final void linkChapters() {
        Chapter prev;
        Chapter cur;

        for (int i = 1; i < chapters.length; i++) {
            prev = chapters[i - 1];
            cur  = chapters[i];

            prev.setNextChapter(cur);
            cur.setPrevChapter(prev);
        }
    }
    
    
   /**
    * Split FB2 book files into pieces (chapters)
    *    
    * @param chapterFile
    * @param chapterFilesize
    * @param pathReference
    * @param maxChapterSize
    * @param maxChapterSize
    * @param chapterNumber
    * @param processHtmlEntities
    * @param chapters
    * @throws IOException
    * @throws BookException 
    */     
    protected final void splitFB2ChapterIntoPieces(
            final InputConnection chapterFile,
            final int chapterFilesize,
            final File pathReference,
            final int maxChapterSize,
            final int chapterNumber,
            final boolean processHtmlEntities,
            final Vector chapters
            
            
            ) throws IOException, BookException {

        int trimChapterFilesize=chapterFilesize;
            
        if (startBinaryPosInBytes != -1) {
                trimChapterFilesize-=startBinaryPosInBytes;    
       }        
        
        
        
        if (trimChapterFilesize <= maxChapterSize) {
            chapters.addElement(new Chapter(
                        chapterFile, trimChapterFilesize, pathReference,
                        L10n.getMessage("CHAPTER_NUM") + (chapterNumber + 1),
                        processHtmlEntities, chapterNumber)
            );

        } else {
                    
            int kMax = trimChapterFilesize / maxChapterSize;
            if (trimChapterFilesize % maxChapterSize > 0) {
                kMax++;
            }

            int left = trimChapterFilesize;
            int chapSize;

            for (int k = 0; k < kMax; k++) {
                chapSize = (left > maxChapterSize ? maxChapterSize : left);
                
                chapters.addElement(new Chapter(
                        new PartitionedConnection(
                            chapterFile, k * maxChapterSize, chapSize),
                        chapSize,
                        pathReference,
                        L10n.getMessage("CHAPTER_NUM") + (chapterNumber + k + 1),
                        processHtmlEntities,
                        chapterNumber + k
                        ));
                left -= maxChapterSize;
            }
           
        }
    }    
    

    protected final void splitChapterIntoPieces(
            final InputConnection chapterFile,
            final int chapterFilesize,
            final File pathReference,
            final int maxChapterSize,
            final int chapterNumber,
            final boolean processHtmlEntities,
            final Vector chapters
            ) throws IOException, BookException {

        if (chapterFilesize <= maxChapterSize) {
            chapters.addElement(new Chapter(
                        chapterFile, chapterFilesize, pathReference,
                        
                        L10n.getMessage("CHAPTER_NUM") + (chapterNumber + 1),
                        processHtmlEntities, chapterNumber)
            );

        } else {

            int kMax = chapterFilesize / maxChapterSize;
            if (chapterFilesize % maxChapterSize > 0) {
                kMax++;
            }

            int left = chapterFilesize;
            int chapSize;

            for (int k = 0; k < kMax; k++) {
                chapSize = (left > maxChapterSize ? maxChapterSize : left);
                chapters.addElement(new Chapter(
                        new PartitionedConnection(
                            chapterFile, k * maxChapterSize, chapSize),
                        chapSize,
                        pathReference,
                        L10n.getMessage("CHAPTER_NUM") + (chapterNumber + k + 1),
                        processHtmlEntities,
                        chapterNumber + k
                        ));
                left -= maxChapterSize;
            }
        }
    }

    /*
     * The maximum file size after which the Filebook is split
     * forcefully into chapters. The split is a dumb one, for it splits
     * on bytes, not characters or tags, i.e. it may split a utf-8 character
     * in two halves, making it unreadable (so that it would be visible as a
     * question mark) or it may split an HTML tag (so that it would become
     * useless and be shown in the text of the chapter)
     */
    
    protected final int getMaximumTxtFilesize(final boolean lightMode) {
        return (lightMode ? 64 * 1024 : 48 * 1024);
    }
    
    public static final int MAXIMUM_TXT_FILESIZE = 64 * 1024;
    public static final int MAXIMUM_HTML_FILESIZE = 4 * 1024; 
    

    protected final int getMaximumHtmlFilesize(final boolean lightMode) {
        return (lightMode ? 128 * 1024 : 64 * 1024);
    }

     

    public final String getURL() {
        return bookURL;
    }

    public abstract Archive getArchive();
}