
package org.albite.book.view;

import java.util.Vector;
import org.albite.book.model.book.Chapter;
import org.albite.book.model.parser.TextParser;
import org.albite.font.AlbiteFont;
import org.albite.util.archive.Archive;
import org.fbreader.Main;

/**
 * This class is non-mutable with the exception that the ColorProfile can be
 * changed if desired for no reflow is required when changing colours.
 * 
 * @author Svetlin Ankov <galileostudios@gmail.com>
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
* 1. Minor changes and simplification in the source code
* 
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

public class Booklet {

    final Archive               bookArchive;
    final Chapter               chapter;

    final int                   width;
    final int                   height;

    final AlbiteFont            fontPlain;
    final AlbiteFont            fontItalic;

    final boolean               renderImages;

    final int                   fontHeight;
    final int                   fontIndent;

    final byte                  defaultAlign = StylingConstants.LEFT;

    private final Vector        pages; //Page elements

    private Page                currentPage;
    private int                 currentPageIndex;
    private Page                prevPage;
    private Page                nextPage;

    /* this inverts the direction of pages */
    private boolean             inverted;

    public Booklet(
            final int width,
            final int height,
            final boolean inverted,
            final Chapter chapter,
            final Archive bookArchive,
            final AlbiteFont fontPlain,
            final AlbiteFont fontItalic,
            final int lineSpacing,
            final boolean renderImages,
            final TextParser parser) {

        this.width = width;
        this.height = height;
        this.inverted = inverted;
        this.chapter = chapter;
        this.bookArchive = bookArchive;
        this.fontPlain = fontPlain;
        this.fontItalic = fontItalic;
        this.renderImages = renderImages;

        fontHeight = fontPlain.getLineHeight() + lineSpacing;
        fontIndent = fontPlain.charWidth(' ') * 3;

        /*
         * Typically ~60-100 pages per chapter, so 200 is quite enough
         */
        Vector pagesTemp = new Vector(200);
        
        /*
         * Reserve the position of the first dummy page
         */
        pagesTemp.addElement(null);

        PageState ps = new PageState(parser);
        //#debug
        int i = 0;

        try {
            /*
             * Real pages
             */
            TextPage current;

            while (!ps.finishedReading()) {
                //#debug
                Main.LOGGER.log("New page #" + (i++));

                current = new TextPage(this, ps);

                if (!current.isEmpty()) {
                    /*
                     * page with content to render
                     */

                    pagesTemp.addElement(current);
                }
            }

            //#debug
            Main.LOGGER.log("pages done!");
        } catch (OutOfMemoryError e) {
            //#debug
            Main.LOGGER.log(e);
            pagesTemp = new Vector(3);
            
            /*
             * Reserve the position of the first dummy page
             */
            pagesTemp.addElement(null);
            
            /*
             * Add a warning message
             */
            pagesTemp.addElement(
                    new DummyPage(this, DummyPage.TYPE_CHAPTER_TOO_BIG));
        } catch (Exception e) {
            //#debug
            Main.LOGGER.log(e);
            pagesTemp = new Vector(3);

            /*
             * Reserve the position of the first dummy page
             */
            pagesTemp.addElement(null);

            /*
             * Add a warning message
             */
            pagesTemp.addElement(
                    new DummyPage(this, DummyPage.TYPE_CHAPTER_ERROR));
        }

        /*
         * Don't forget to set the final field
         */
        pages = pagesTemp;

        if (pages.size() == 1) {
            /*
             * No TextPages have been added; == 1 because of the reserved
             * position of the first dummy page
             */

            pages.addElement(new DummyPage(this, DummyPage.TYPE_EMPTY_CHAPTER));
        }

        /*
         * First dummy page (transition to prev chapter or opening of book)
         */
        if (chapter.getPrevChapter() == null) {
            pages.setElementAt(
                    new DummyPage(this, DummyPage.TYPE_BOOK_START), 0);
        } else {
            pages.setElementAt(
                    new DummyPage(this, DummyPage.TYPE_CHAPTER_PREV), 0);
        }

        /*
         * Last dummy page (transition to next chapter or end of book)
         */
        if (chapter.getNextChapter() == null) {
            pages.addElement(new DummyPage(this, DummyPage.TYPE_BOOK_END));
        } else {
            pages.addElement(new DummyPage(this, DummyPage.TYPE_CHAPTER_NEXT));
        }

        goToFirstPage();
    }

    public final Page getCurrentPage() {
        return currentPage;
    }

    public final Page getNextPage() {
        return nextPage;
    }

    public final Page getPrevPage() {
        return prevPage;
    }

    public final boolean goToPrevPage() {
        if (inverted) {
            return incrementPage();
        } else {
            return decrementPage();
        }
    }

    private boolean decrementPage() {
        int index = currentPageIndex - 1;
        if (index < 0) {
            return false;
        }
        currentPageIndex = index;
        setPages();
        return true;
    }

    public final boolean goToNextPage() {
        if (inverted) {
            return decrementPage();
        } else {
            return incrementPage();
        }
    }

    private boolean incrementPage() {
        int index = currentPageIndex + 1;
        if (index == pages.size()) {
            return false;
        }
        currentPageIndex = index;
        setPages();
        return true;
    }

    public final void goToFirstPage() {
        currentPageIndex = 1;
        setPages();
    }

    public final void goToLastPage() {
        currentPageIndex = pages.size() - 2;
        setPages();
    }

    public final void goToPosition(final int position) {
        if (position <= 0) {
            goToFirstPage();
            return;
        }

        if (position >= chapter.getTextBuffer().length) {
            goToLastPage();
            return;
        }

        Page foundPage;
        final int pagesSize = pages.size();
        for (int i = 0; i < pagesSize; i++) {
            foundPage = (Page) pages.elementAt(i);
            if (foundPage.contains(position)) {
                goToPage(i);
                return;
            }
        }
        goToFirstPage();
    }

    public void goToPage(final int page) {
        if (page <= 0) {
            goToFirstPage();
            return;
        }

        if (page >= pages.size() - 1) {
            goToLastPage();
            return;
        }

        currentPageIndex = page;
        setPages();
    }

    private void setPages() {

        /* there are always at least three Pages in a booklet! */
        currentPage = (Page)(pages.elementAt(currentPageIndex));

        if (inverted) {
            prevPage = chooseNextPage();
            nextPage = choosePrevPage();
        } else {
            prevPage = choosePrevPage();
            nextPage = chooseNextPage();
        }

        chapter.setCurrentPosition(currentPage.getStart());
    }

    public void setInverted(final boolean inverted) {
        //#debug
        Main.LOGGER.log("setting inverted: " + inverted);

        this.inverted = inverted;
        setPages();
    }

    private Page choosePrevPage() {
        int index = currentPageIndex -1;
        if (index < 0) {
            return null;
        } else {
            return (Page)(pages.elementAt(index));
        }
    }

    private Page chooseNextPage() {
        int index = currentPageIndex +1;
        if (index == pages.size()) {
            return null;
        } else {
            return (Page)(pages.elementAt(index));
        }
    }

    public final int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public final int getPagesCount() {
        return pages.size();
    }

    public final char[] getTextBuffer() {
        return chapter.getTextBuffer();
    }

    public final Chapter getChapter() {
        return chapter;
    }
}