
package org.albite.book.view;

import javax.microedition.lcdui.Graphics;
import org.fbreader.views.ColorScheme;
import org.albite.font.AlbiteFont;

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
* 1. Added localization for titles of the dummy pages
* 
* 
* 
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

public class DummyPage extends Page {
    final public static byte    TYPE_CHAPTER_PREV       = 0;
    final public static byte    TYPE_CHAPTER_NEXT       = 1;
    final public static byte    TYPE_BOOK_START         = 2;
    final public static byte    TYPE_BOOK_END           = 3;
    final public static byte    TYPE_EMPTY_CHAPTER      = 4;
    final public static byte    TYPE_CHAPTER_TOO_BIG    = 5;
    final public static byte    TYPE_CHAPTER_ERROR      = 6;
    
    final public static int     TYPE_COUNT              = 7;

    private byte type;

    
    
    final public static char[]  LABEL_CHAPTER_PREV =
            L10n.getMessage("LABEL_CHAPTER_PREV").toCharArray();
    
    final public static char[]  LABEL_CHAPTER_NEXT =
            L10n.getMessage("LABEL_CHAPTER_NEXT").toCharArray();    
    
    final public static char[]  LABEL_BOOK_START =
            L10n.getMessage("LABEL_BOOK_START").toCharArray();    
    
    final public static char[]  LABEL_BOOK_END =
             L10n.getMessage("LABEL_BOOK_END").toCharArray();
    
    final public static char[]  LABEL_EMPTY_CHAPTER =
            L10n.getMessage("LABEL_EMPTY_CHAPTER").toCharArray();    
    
    final public static char[]  LABEL_CHAPTER_TOO_BIG =
            L10n.getMessage("LABEL_CHAPTER_TOO_BIG").toCharArray();    
    
    final public static char[] LABEL_CHAPTER_ERROR =
            L10n.getMessage("LABEL_CHAPTER_ERROR").toCharArray();    
    

    public DummyPage(final Booklet booklet, final byte pageType) {
        if (pageType < 0 || pageType >= TYPE_COUNT) {
            throw new IllegalArgumentException();
        }

        this.type = pageType;
        this.booklet = booklet;
    }

    public final Region getRegionAt(final int x, final int y) {
        return null;
    }

    public final int getRegionIndexAt(final int x, final int y) {
        return -1;
    }

    public final boolean contains(int position) {
        return false;
    }

    public final void draw(
            final Graphics g,
            final ColorScheme cp,
            final AlbiteFont fontPlain,
            final AlbiteFont fontItalic,
            final char[] chapterBuffer) {

        final int colorDummy = cp.colors[ColorScheme.COLOR_TEXT_DUMMY];
        final int width = booklet.width;
        final int hcentered = booklet.height / 2 - 20;

        char[] label = LABEL_EMPTY_CHAPTER;

        switch (type) {

            case TYPE_CHAPTER_PREV:
                label = LABEL_CHAPTER_PREV;
                break;

            case TYPE_CHAPTER_NEXT:
                label = LABEL_CHAPTER_NEXT;
                break;

            case TYPE_BOOK_START:
                label = LABEL_BOOK_START;
                break;

            case TYPE_BOOK_END:
                label = LABEL_BOOK_END;
                break;

            case TYPE_EMPTY_CHAPTER:
                label = LABEL_EMPTY_CHAPTER;
                break;

            case TYPE_CHAPTER_TOO_BIG:
                label = LABEL_CHAPTER_TOO_BIG;
                break;

            case TYPE_CHAPTER_ERROR:
                label = LABEL_CHAPTER_ERROR;
                break;
        }

        int w = fontItalic.charsWidth(label);

        fontItalic.drawChars(g, colorDummy, label,
                (booklet.width - w) / 2, booklet.height / 2 - 20);
    }

    public final byte getType() {
        return type;
    }
}