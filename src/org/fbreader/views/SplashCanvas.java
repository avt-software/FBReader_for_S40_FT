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
* 
* 1. Minor changes and adoptation to the FBReader for S40FT project

* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/


package org.fbreader.views;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.Sprite;
import org.fbreader.localization.L10n;
import org.fbreader.uihelpers.Visual;
import org.fbreader.utils.ImageLoader;


/**
 * Splash view. Shows application name and loading animation while 
 * initializing resources.
 */
public final class SplashCanvas
    extends GameCanvas {

    private static SplashCanvas self = null;
    private Image image;
    private static final String UPPER_TITLE = "FBReader";
    private static final int LOADER_HEIGHT = 28;
    private Sprite loaderSprite;
    private Timer animationTimer;
    private int width;
    private int height;
    private Graphics g;
    private final Font upperTitleFont = Visual.TITLE_FONT;
    private final Font lowerTitleFont = Visual.MEDIUM_BOLD_FONT;
    private final int upperTitleHeight = upperTitleFont.getHeight();
    private final int lowerTitleHeight = lowerTitleFont.getHeight();
    private int textAreaPadding;
    private int textAreaHeight;
    private int backgroundHeight;
    private volatile boolean hidden = true;

    private SplashCanvas() {
        super(false);
        setFullScreenMode(true);
    }

    public boolean isHidden() {
        return hidden;
    }

    /**
     * @return SplashCanvas singleton
     */
    public static SplashCanvas getInstance() {
        if (self == null) {
            self = new SplashCanvas();
        }
        return self;
    }

    /**
     * @see GameCanvas#showNotify() 
     */
    protected final void showNotify() {
        hidden = false;
        g = getGraphics();

        // S60 doesn't call sizeChanged on start-up so it has to be called here.
        sizeChanged(getWidth(), getHeight());
            
        draw();
        animationTimer = new Timer();
        animationTimer.schedule(new TimerTask() {

            public void run() {
                draw();
            }
        }, 50, 50);
    }

    /**
     * @see GameCanvas#hideNotify() 
     */
    protected final void hideNotify() {
        hidden = true;
        animationTimer.cancel();
        animationTimer = null;
        image = null;
        loaderSprite = null;
        System.gc();
    }

    /**
     * @see GameCanvas#sizeChanged(int, int) 
     */
    protected final void sizeChanged(int w, int h) {
        width = w;
        height = h;
        textAreaPadding = 8;
        textAreaHeight = textAreaPadding + upperTitleHeight + 2
            + lowerTitleHeight + textAreaPadding + 4 + LOADER_HEIGHT;
        if (textAreaHeight < height / 3) {
            textAreaPadding += (height / 3 - textAreaHeight) / 2;
            textAreaHeight = height / 3;
        }
        backgroundHeight = (height - textAreaHeight) / 2;
    }

    private void draw() {
        //Draw background
        g.setColor(Visual.BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);

        //Draw text background
        int y = backgroundHeight;
        g.setColor(Visual.BACKGROUND_COLOR);
        g.fillRect(0, y, width, textAreaHeight);

        //Draw text
        g.setColor(Visual.SPLASH_TEXT_COLOR);
        y += textAreaPadding;
        g.setFont(upperTitleFont);
        g.drawString(UPPER_TITLE, width / 2, y, Graphics.TOP | Graphics.HCENTER);
        y += upperTitleHeight + 2;
        g.setFont(lowerTitleFont);
        g.drawString(L10n.getMessage("SPLASH_CANVAS_LOWER_TITLE"), width / 2, y, Graphics.TOP | Graphics.HCENTER);
        y += lowerTitleHeight + 4;

        if (loaderSprite == null) {
            try {
                Image i =
                    ImageLoader.getInstance().loadImage("/open_book_sprite_136x35.png", null);
                loaderSprite = new Sprite(i, i.getWidth() / 4, i.getHeight());
            }
            catch (IOException e) {
            }
        }

        if (loaderSprite != null) {
            loaderSprite.nextFrame();
            loaderSprite.setPosition((width - loaderSprite.getWidth()) / 2, y);
            loaderSprite.paint(g);
        }

        flushGraphics();
    }

    /**
     * @see GameCanvas#pointerPressed(int, int) 
     */
    protected final void pointerPressed(int x, int y) {
    }
}
