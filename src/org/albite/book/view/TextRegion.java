package org.albite.book.view;

import javax.microedition.lcdui.Graphics;
import org.albite.font.AlbiteFont;
import org.fbreader.views.ColorScheme;

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
* 1. Minor changes and simplification in the source code
* 
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

public class TextRegion extends Region {

    protected final short length;

    protected final byte  color;
    protected final byte  style;

    public TextRegion (
            final short x,
            final short y,
            final short width,
            final short height,
            final int position,
            final int length,
            final byte style,
            final byte color) {

        super(x, y, width, height, position);
        this.length = (short) length;
        this.style = style;
        this.color = color;
    }

    public void draw(
            final Graphics g,
            final ColorScheme cp,
            final AlbiteFont fontPlain,
            final AlbiteFont fontItalic,
            final char[] chapterBuffer) {

        AlbiteFont font =
                TextPage.chooseFont(fontPlain, fontItalic, style);

        font.drawChars(g, cp.colors[color], chapterBuffer,
                x, y, position, length);
    }

    public void drawSelected(
            final Graphics g,
            final ColorScheme cp,
            final AlbiteFont fontPlain,
            final AlbiteFont fontItalic,
            final char[] chapterBuffer) {
        
        AlbiteFont font =
                TextPage.chooseFont(fontPlain, fontItalic, style);

        g.setColor(cp.colors[color]);
        g.fillRect(x, y, width, height);
        font.drawChars(g, cp.colors[ColorScheme.COLOR_BACKGROUND], chapterBuffer,
                x, y, position, length);
    }

    public final String getText(final char[] chapterBuffer) {
        return new String(chapterBuffer, position, length);
    }

    public void addTextChunk(
            final char[] chapterBuffer,
            final StringBuffer buf) {
        buf.append(chapterBuffer, position, length);
        buf.append(' ');
    }
}