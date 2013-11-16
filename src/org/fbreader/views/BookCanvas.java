
package org.fbreader.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import org.albite.image.ImageButton;
import org.albite.book.model.book.Book;
import org.albite.book.model.book.BookException;
import org.albite.book.model.book.Chapter;
import org.albite.book.view.Booklet;
import org.albite.book.view.Page;
import org.albite.font.AlbiteFont;
import org.albite.book.view.DummyPage;
import org.albite.book.view.TextPage;
import org.albite.font.AlbiteNativeFont;
import org.albite.util.RMSHelper;
import org.fbreader.Main;
import org.fbreader.localization.L10n;
import org.fbreader.settings.Settings;
import org.tantalum.util.L;

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
* 1.Numerous changes in the source code 
* 2.Adopted for FBReader for S40FT project
*    
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/


public class BookCanvas extends Canvas 
                        implements View, 
                                   CommandListener {
    
    /*
     * timestamp to measure gesture length (time)
     */
    //private long lastTimestamp = 0;
    
    
    int startX=0;
    int startY=0;
    
    //the number of pixels moved horizontally (x-axis) 
    //and vertically (y-axis) since the previous GESTURE_DRAG event
    int dragDistanceX=0;
    int dragDistanceY=0;
    
    private static final int DRAG_DISTANCE_MIN=1;
    
    boolean gestureDragStart=false;
    

    private static final int    TASK_NONE               = 0;
    

    private static final int    MENU_HEIGHT             = 7;     
    private static final int    STATUS_BAR_SPACING      = 3;

    //BACK_BUTTON_HEIGHT for Series 40 FT
    private static final int    BACK_BUTTON_HEIGHT      = 42;
    
    /*
     * Some page settings
     */

    private              int    currentMarginWidth;
    private static final int    LINE_SPACING            = 2;
    private              int    currentLineSpacing      = LINE_SPACING;
    private boolean             renderImages;

    private static final int    DRAG_TRESHOLD           = 20;
    
    private static final int    MARGIN_CLICK_TRESHOLD   = 60;
        
    private static final int    HOLDING_TIME_MIN        = 250;

    private int                 currentHoldingTime      = HOLDING_TIME_MIN * 3;

    private long                startPointerHoldingTime;
    private long                stopPointerHoldingTime;
    private long                durationPointerHoldingTime;
    
    private boolean             holdingValid            = false;

    private int                 prevWidth               = 0;
    private int                 prevHeight              = 0;

    private int                 fullScreenWidth;
    private int                 fullScreenHeight;

    /*
     * Targeting at 60 FPS
     */
    private final int           frameTime;

    private static final float  MAXIMUM_SPEED           = 4F;

    private float               speedMultiplier         = 0.3F;
    private boolean             scrollingOnX            = true;

    private int                 scrollNextPagePixels    = 55;
    private int                 scrollSamePagePixels    = 5;
    private int                 scrollStartBookPixels   = 30;
    private boolean             smoothScrolling;
    private boolean             horizontalScrolling;

    /**
     * If true, the pages will be in reversed order
     */
    private boolean             inverted                = false;

    private int                 pageCanvasPositionMin   = 0;
    private int                 pageCanvasPositionMax   = 0;

    /**
     * Rendering is disabled
     */
    private static final int    MODE_DONT_RENDER        = 0;

    /**
     * Rendering is enabled, but user input is not being processed
     */
    private static final int    MODE_PAGE_LOCKED        = 1;

    /**
     * Same as MODE_PAGE_LOCKED, but displays a hour-glass icon on top
     */
    private static final int    MODE_PAGE_LOADING       = 2;

    /**
     * Rendering is enabled, ready to process user input
     */
    private static final int    MODE_PAGE_READING       = 3;

    /**
     * Rendering is enabled, user input is not processed,
     * scrolling animation is in progress
     */
    private static final int    MODE_PAGE_SCROLLING     = 4;

    /**
     * Rendering is enabled, only pointer dragging is being processed
     */
    private static final int    MODE_PAGE_DRAGGING      = 5;

    private int                 mode                    = MODE_DONT_RENDER;

    /*
     * 180-degree rotation will not be supported as it introduces code
     * complexity, that is not quite necessary
     */
    public static final int     ORIENTATION_0           = Sprite.TRANS_NONE;
    public static final int     ORIENTATION_90          = Sprite.TRANS_ROT90;
    public static final int     ORIENTATION_180         = Sprite.TRANS_ROT180;
    public static final int     ORIENTATION_270         = Sprite.TRANS_ROT270;

    private int                 orientation             = ORIENTATION_0;
    private boolean             fullscreen;
    private boolean             seenFullscreenAlert     = false;

    public static final int     SCROLL_PREV             = 0;
    public static final int     SCROLL_NEXT             = 1;
    public static final int     SCROLL_SAME_PREV        = 2;
    public static final int     SCROLL_SAME_NEXT        = 3;
    public static final int     SCROLL_BOOK_START       = 4;
    public static final int     SCROLL_BOOK_END         = 5;

    private volatile boolean    repaintStatusBar        = true;

    private volatile boolean    repaintChapterNum       = false;
    private volatile boolean    repaintProgressBar      = false;
        
    private char[]              chapterNoChars          = {'#', '0', '0', '0'};
    private int                 pagesCount;
    
    private int                 statusBarHeight;
    private int                 chapterNoWidth;
    private int                 progressBarHeight;
    private int                 progressBarWidth;
    private int                 progressBarX;
    private int                 clockWidth;

    private int                 centerBoxSide;
    
    private ImageButton         waitCursor;

    //input events
    private int                 xx                      = 0;
    private int                 yy                      = 0;
    
    private ColorScheme         currentScheme;

    private AlbiteFont          fontPlain;
    private AlbiteFont          fontItalic;

    public final byte[]         fontSizes;

    private boolean             fontGrowing             = true;
    private byte                currentFontSizeIndex;

    private boolean             useNativeFonts          = true;
    
    
    private final int[]         nativeFontSizes         =
            {Font.SIZE_SMALL, Font.SIZE_MEDIUM, Font.SIZE_LARGE};

    private AlbiteFont          fontStatus;
    private int                 fontStatusMaxWidth;

    private Book                currentBook;

    private Booklet             chapterBooklet;
    private PageCanvas          prevPageCanvas;
    private PageCanvas          currentPageCanvas;
    private PageCanvas          nextPageCanvas;
    private int                 currentPageCanvasPosition;

    private Timer               timer;
    private TimerTask           scrollingTimerTask;
    private TimerTask           pointerPressedTimerTask;
    private TimerTask           pointerReleasedTimerTask;
    private boolean             pointerPressedReady     = true;
    private boolean             pointerReleasedReady    = true;
    private Main                  app;

    private RecordStore         rs;

    private boolean             initialized             = false;
    
    private ViewMaster viewMaster;
    private Settings settings;
    
    //options list commands for BookCanvas screen
    private final Command tocCmd = new Command(L10n.getMessage("TABLE_OF_CONTENT_COMMAND"), 
                                                Command.SCREEN, 1);    

    private final Command dayModeCmd = new Command(L10n.getMessage("DAY_MODE_COMMAND"), 
                                                Command.SCREEN, 1);
    
    private final Command nightModeCmd = new Command(L10n.getMessage("NIGHT_MODE_COMMAND"), 
                                                Command.SCREEN, 1);

    
    private final Command bookInfoCmd = new Command(L10n.getMessage("BOOK_INFO_COMMAND"), 
                                                Command.SCREEN, 1); 

    public BookCanvas(final Main app) {
        super();
        this.app = app;
        viewMaster = ViewMaster.getInstance();
        settings=Settings.getInstance();
        settings.loadSettings();
        this.addCommand(viewMaster.getHelpCmd());
        this.addCommand(tocCmd);
        this.addCommand(bookInfoCmd);
        this.addCommand(nightModeCmd);
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);
        
        /*
         * Initialize default values
        */
        currentMarginWidth=settings.getBookMarginWidth();
        renderImages = true;
        frameTime = 1000 / 40;
        fontSizes = new byte[] {12, 14, 16};
        currentFontSizeIndex = (byte) settings.getBookBaseTextFontSize();
        fullscreen = false;
        smoothScrolling = true;        
        
        /*
         * Load custom data from RMS
         */
        openRMSAndLoadData();
    }
    
    
    /**
     * Activate BookCanvas
     * 
     */
    public void activate() {
        this.repaint();
    }
    
    
    /**
     * Deactivate BookCanvas
     */
    public void deactivate() {
        
    }    
    
    
    

    public final synchronized void initialize() {

        //prevent re-initialization
        if(initialized) {
            return;
        }

        fullScreenWidth = getWidth();
        fullScreenHeight = getHeight();

        loadFont();
        loadStatusFont();

        final int w = getWidth();

        /*
         * Take the min value; take into account that some screens
         * are in landscape mode by default.
         */
        centerBoxSide = Math.min(w, getHeight()) / 8;

        statusBarHeight = fontStatus.getLineHeight() + (STATUS_BAR_SPACING * 2);
        
        
        
        /*
         * If the height of the status bar(statusBarHeight) 
         * is smaller than the height of the "Back" button, 
         * then set the height equal to the height of the status bar "Back" button
         */
        if (statusBarHeight < BACK_BUTTON_HEIGHT) {
            
            statusBarHeight = BACK_BUTTON_HEIGHT;
        }

        /*
         * We assume that there would be no more than 999 chapters
         */
        chapterNoWidth = (fontStatusMaxWidth * chapterNoChars.length) + (STATUS_BAR_SPACING * 2);

        updateProgressBarSize(w);

        progressBarHeight = (statusBarHeight - (STATUS_BAR_SPACING * 2)) / 3;

        waitCursor = new ImageButton("/gfx/hourglass.ali", TASK_NONE);

        /* set default profiles if none selected */
        if (currentScheme == null) {
            ColorScheme day = ColorScheme.DEFAULT_DAY;
            ColorScheme night = ColorScheme.DEFAULT_NIGHT;
            day.link(night);
            currentScheme = day;
        }

        applyColorProfile();

        initializePageCanvases();

        /*
         * Update the inverted mode and the max scrolling values
         * Can't be done at any place before, for the canvases must have been
         * already initialized
         */
        applyScrollingSpeed();
        applyAbsScrPgOrd();
        applyScrollingLimits();

        timer = new Timer();

        initialized = true;
    }

    private void initializePageCanvases() {
        int w = getWidth() - (2 * currentMarginWidth);
        int h = getHeight();

        if (orientation == ORIENTATION_0) {
            h -= statusBarHeight;
        }

        if (!fullscreen) {
            h -= MENU_HEIGHT;
        } else {
            h -= currentMarginWidth;
            if (orientation != ORIENTATION_0) {
                h -= currentMarginWidth;
            }
        }

        /*
         * Free memory before claiming it!
         */

        currentPageCanvas = null;
        nextPageCanvas = null;
        prevPageCanvas = null;

        currentPageCanvas   = new PageCanvas(w, h, orientation);
        nextPageCanvas      = new PageCanvas(w, h, orientation);
        prevPageCanvas      = new PageCanvas(w, h, orientation);

        currentPageCanvasPosition = 0;
    }

    protected final void paint(final Graphics g) {
        if (mode != MODE_DONT_RENDER) {
            final int w = getWidth();
            final int h = getHeight();

            if (orientation == ORIENTATION_0) {

                if (repaintStatusBar) {
                    repaintStatusBar = false;

                    g.setColor(currentScheme.colors[
                            ColorScheme.COLOR_BACKGROUND]);

                    g.fillRect(0, h - statusBarHeight, w, statusBarHeight);

                    drawChapterNum(w, h, g);
                    drawProgressBar(w, h, g);

                } else {
                    /* If not the whole status bar is to be updated,
                     check if parts of it are
                     */

                    if (repaintChapterNum) {
                        drawChapterNum(w, h, g);
                    }

                    if (repaintProgressBar) {
                        drawProgressBar(w, h, g);
                    }
                }
            }

            final int anchor = Graphics.TOP | Graphics.LEFT;

            final Image imageC = currentPageCanvas.getImage();
            final Image imageP = prevPageCanvas.getImage();
            final Image imageN = nextPageCanvas.getImage();

            final int imageWidth = imageC.getWidth();
            final int imageHeight = imageC.getHeight();

            int x = 0;
            int y = 0;

            switch(orientation) {
                case ORIENTATION_0:

                    if (fullscreen) {
                        g.setClip(0, 0, w, imageHeight-currentMarginWidth*2);
                    } else {
                        g.setClip(0, MENU_HEIGHT, w, imageHeight);
                    }

                    x = currentMarginWidth +
                            (scrollingOnX ? currentPageCanvasPosition : 0);

                    y = (fullscreen ? currentMarginWidth : MENU_HEIGHT) +
                            (scrollingOnX ? 0 : currentPageCanvasPosition);
                break;

                case ORIENTATION_90:
                case ORIENTATION_180:
                case ORIENTATION_270:
                    g.setClip(0, 0, w, h);
                    
                    x = currentMarginWidth +
                            (scrollingOnX ? currentPageCanvasPosition : 0);

                    y = currentMarginWidth +
                            (scrollingOnX ? 0 : currentPageCanvasPosition);
                    break;
            }

            g.setColor(
                    currentScheme.colors[
                    ColorScheme.COLOR_BACKGROUND]);

            g.fillRect(0, 0, w, h);

            g.drawImage(imageP,
                    (scrollingOnX ? x - imageWidth - currentMarginWidth : x),
                    (scrollingOnX ? y : y - imageHeight - currentMarginWidth),
                    anchor);
            g.drawImage(imageC, x, y, anchor);

            g.drawImage(imageN,
                    (scrollingOnX ? x + imageWidth + currentMarginWidth : x),
                    (scrollingOnX ? y : y + imageHeight + currentMarginWidth),
                    anchor);

            if (mode == MODE_PAGE_LOADING) {
                waitCursor.drawRotated(
                        g,
                        (w - waitCursor.getWidth()) / 2,
                        (h - waitCursor.getHeight()) / 2,
                        orientation);
            }
        }
    }


    private void drawChapterNum(final int w, final int h, final Graphics g) {

        repaintChapterNum = false;

        /*
         * Clearing background
         */
        g.setColor(
                currentScheme.colors[ColorScheme.COLOR_BACKGROUND]);

        g.fillRect(0, h - statusBarHeight, chapterNoWidth, statusBarHeight);

        
        if (settings.getShowChapterNumber()==1) {
        /* drawing current chapter area */
        fontStatus.drawChars(g, currentScheme.colors[
                ColorScheme.COLOR_TEXT_STATUS], chapterNoChars,
                STATUS_BAR_SPACING, h - statusBarHeight + STATUS_BAR_SPACING,
                0, chapterNoChars.length);
        }
    }

    private void drawProgressBar(final int w, final int h, final Graphics g) {

	/* Update the state */
        repaintProgressBar = false;

	/* setup some temp vars */
        final int fillHeight = h - (statusBarHeight + progressBarHeight) / 2;
        final int progressBarHeight_2 = progressBarHeight / 2;

        /*
         * Clearing background
         */
        g.setColor(currentScheme.colors[ColorScheme.COLOR_BACKGROUND]);

        g.fillRect(progressBarX, h - statusBarHeight,
                progressBarWidth, statusBarHeight);

        /* drawing progress bar */
        g.setColor(currentScheme.colors[ColorScheme.COLOR_TEXT_STATUS_2]);
        g.drawLine(progressBarX, fillHeight + progressBarHeight_2,
                        progressBarX + progressBarWidth - 1, fillHeight + progressBarHeight_2);

        g.setColor(currentScheme.colors[ColorScheme.COLOR_TEXT_STATUS]);

        g.drawRect(
                progressBarX, h - ((statusBarHeight + progressBarHeight) / 2),
                progressBarWidth - 1, progressBarHeight);

        final int pagesBarWidth;

        if (pagesCount > 0) {
            pagesBarWidth =
                    (int) (progressBarWidth
                    * (((float) chapterBooklet.getCurrentPageIndex() - 1)
                    / pagesCount));
        } else {
            /*
             * This should never happen, how could there be 0 pages?!
             */
            pagesBarWidth = progressBarWidth;
        }

        final int chaptersCount = currentBook.getChaptersCount() - 1;
        final int chaptersBarWidth;
        
        if (chaptersCount > 0) {
            chaptersBarWidth =
                    (int) (progressBarWidth
                    * (((float) (currentBook.getCurrentChapter().getNumber()))
                    / chaptersCount));
        } else {
            /*
             * This should never happen, how could there be 0 chapters?!
             */
            chaptersBarWidth = progressBarWidth;
        }
        

        /* Fill the pages bar */
        g.fillRect(progressBarX, fillHeight,
                pagesBarWidth, progressBarHeight_2 + 1);

	/* Fill the chapters bar */
        g.fillRect(progressBarX, fillHeight + progressBarHeight_2,
                chaptersBarWidth, progressBarHeight_2);
    }

    
    protected final void pointerPressed(final int x, final int y) {

        if (pointerPressedReady) {
             pointerPressedReady = false;
             pointerPressedTimerTask =
                 new TimerTask() {
                     public void run() {
                         try {
                               processPointerPressed(x, y);
                         } catch (Throwable t) {
                             //#debug
                             Main.LOGGER.log(t);
                         }

                         pointerPressedReady = true;
                     }
                 };
             timer.schedule(pointerPressedTimerTask, 0);
         }

    }
   
    protected final void pointerReleased(final int x, final int y) {
            if (pointerReleasedReady) {
                pointerReleasedReady = false;
                pointerReleasedTimerTask =
                    new TimerTask() {
                        public void run() {
                            try {
                                    processPointerReleased(x, y);
                            } catch (Throwable t) {
                                //#debug
                                Main.LOGGER.log(t);
                            }

                            pointerReleasedReady = true;
                        }
                    };
                timer.schedule(pointerReleasedTimerTask, 0);
            }

    }



    protected final void pointerDragged(final int x, final int y) {
           dragDistanceX=Math.abs(x-startX);
           dragDistanceY=Math.abs(y-startY);
           if (dragDistanceX > DRAG_DISTANCE_MIN*5 || dragDistanceY > DRAG_DISTANCE_MIN*5) {
             try {
                  processPointerDragged(x, y);
              } catch (Throwable t) {
                  //#debug
                  Main.LOGGER.log(t);
              }
           }           
    
    }


    private void processPointerPressed(final int x, final int y) {

        xx = x;
        yy = y;
        
        startX=x;
        startY=y;
        holdingValid = true;
        startPointerHoldingTime = System.currentTimeMillis();
    }

    private void processPointerReleased(final int x, final int y) {
        xx = x;
        yy = y;
        final int w = getWidth();
        final int h = getHeight();
        stopPointerHoldingTime=System.currentTimeMillis();
        durationPointerHoldingTime=stopPointerHoldingTime-startPointerHoldingTime;
        boolean holding = (holdingValid && durationPointerHoldingTime > currentHoldingTime);

        //scroll page with tap 
        if (mode==MODE_PAGE_READING && settings.getUseTapForScroll()) {
                if ((scrollingOnX ? x - w : y - h) >
                    (orientation == ORIENTATION_0 && !scrollingOnX
                    ? -MARGIN_CLICK_TRESHOLD - statusBarHeight
                    : -MARGIN_CLICK_TRESHOLD)) {
                        /* Right Page position */
                        mode = MODE_PAGE_SCROLLING;
                        scheduleScrolling(SCROLL_NEXT);
                    } else if ((scrollingOnX ? x : y) <
                            (orientation == ORIENTATION_0 && !scrollingOnX
                                ? MARGIN_CLICK_TRESHOLD + MENU_HEIGHT
                                : MARGIN_CLICK_TRESHOLD)) {
                        /* Left Page position */
                        mode = MODE_PAGE_SCROLLING;
                        scheduleScrolling(SCROLL_PREV);
            }
            
        }
        //scroll with drag
        else if (mode==MODE_PAGE_DRAGGING && settings.getUseFlickAndSwipeForScroll()) {
                final int px = currentPageCanvasPosition;
                if (px == 0) {
                    stopScrolling();
                    mode = MODE_PAGE_READING;
                }
                else {
                    mode = MODE_PAGE_SCROLLING;

                    if (px < -DRAG_TRESHOLD) {
                        scheduleScrolling(SCROLL_NEXT);
                        //break;
                    }
                    else if (px > DRAG_TRESHOLD) {
                        scheduleScrolling(SCROLL_PREV);
                        //break;
                    }
                    else if (px > 0) {
                        scheduleScrolling(SCROLL_SAME_PREV);
                        //break;
                    }
                    else if (px <= 0) {
                        scheduleScrolling(SCROLL_SAME_NEXT);
                        //break;
                    }
                    else {
                      mode = MODE_PAGE_READING;                      
                    }
                    
                }

          
        }
      
    }        



    private void processPointerDragged(final int x, final int y) {
        switch(mode) {
            case MODE_PAGE_READING:
                mode = MODE_PAGE_DRAGGING;
                /* FALLING THROUGH */
                
            case MODE_PAGE_DRAGGING:
               
                if (settings.getUseFlickAndSwipeForScroll()) {
                    currentPageCanvasPosition += (scrollingOnX ? x - xx: y - yy);
                    repaint();
                }
                else {
                 mode = MODE_PAGE_READING;     
                }
                
                break;
        }

        /* It's essential that these values are updated
         * AFTER the switch statement! */
        xx = x;
        yy = y;
    }

  
    public final boolean isBookOpen(final String bookURL) {

        if (isBookOpen() && currentBook.getURL().equals(bookURL)) {
            return true;
        }
        return false;
    }

    public final Book openBook(final String bookURL)
            throws IOException, BookException {

         /*
         * If the book is already open, no need to load it again
         */
        
        if (isBookOpen(bookURL)) {
            mode = MODE_PAGE_READING;
            return currentBook;
        }
        
        
        /*
         * try to open the book
         */
        Book newBook = null;
            newBook = Book.open(bookURL);
            /*
             * All was OK, let's close current book
             */
            closeBook();

            currentBook = newBook;
            /*
             * Reset the Toc
             */
            app.resetToc();
            
            //Open the current book from beginning
            currentBook.setCurrentChapter(currentBook.getChapter(0));
            currentBook.setCurrentChapterPos(0);
            goToPosition(currentBook.getCurrentChapter(),
                    currentBook.getCurrentChapterPosition());
            

            
        mode = MODE_PAGE_READING;
        return currentBook;            
    }

    private void closeBook() {
        if (isBookOpen()) {
            saveAllOptions();
            try {
                currentBook.close();
            } catch (IOException e) {}
            currentBook = null;
            chapterBooklet = null;
        }
    }

    private synchronized void saveAllOptions() {
        saveOptionsToRMS();
    }

    public final boolean isBookOpen() {
        return currentBook != null;
    }

    private void scheduleScrolling(final int scrollMode) {
        /*
         * Invalidate holding time. It's important for the situtations when 
         * the canvas can't respond to pointerReleased eventes (e.g. when
         * loading pictures) and it may interpret the time wrongly.
         */
        holdingValid = false;

        if (scrollingTimerTask == null) {
            scrollingTimerTask = new TimerTask() {
                private int dx;
                private boolean fullPage;

                public void run() {
                    switch(scrollMode) {
                        case SCROLL_PREV:
                            dx = scrollNextPagePixels;
                            fullPage = true;
                            break;
                        case SCROLL_NEXT:
                            dx = -scrollNextPagePixels;
                            fullPage = true;
                            break;
                        case SCROLL_SAME_NEXT:
                            dx = scrollSamePagePixels;
                            fullPage = false;
                            break;
                        case SCROLL_SAME_PREV:
                            dx = -scrollSamePagePixels;
                            fullPage = false;
                            break;
                        case SCROLL_BOOK_END:
                            dx = scrollStartBookPixels;
                            fullPage = false;
                            break;
                        case SCROLL_BOOK_START:
                            dx = -scrollStartBookPixels;
                            fullPage = false;
                            break;
                        default:
                            dx = 0;
                            fullPage = false;
                            break;
                    }

                    scrollPages(dx, fullPage);
                }
            };

            timer.schedule(scrollingTimerTask, frameTime, frameTime);
        }
    }

    private synchronized void stopScrolling() {
        if (scrollingTimerTask != null) {
            scrollingTimerTask.cancel();
            scrollingTimerTask = null;
        }
    }

    private int nonLineaScroll(final int dx) {
        final float part;

        if (dx < 0) {
            part = ((float) (-currentPageCanvasPosition)) / ((float) pageCanvasPositionMax);
        } else {
            part = ((float) currentPageCanvasPosition) / ((float) pageCanvasPositionMax);
        }

        /*
         * Calculate the speed
         */
        int dxNew = dx;

        if (part > 0.3) {
            /*
             * Activate non-linear mode
             */
            if (dxNew < 0) {
                dxNew = -dxNew;
            }

            dxNew = (int) (((-1.8 * part) + 1.9) * dxNew);

            if (dxNew == 0) {
                dxNew = 1;
            }
            
            if (dx < 0) {
                dxNew = -dxNew;
            }
        }
        
        return dxNew;
    }

    /**
     * Scrolls the three PageCanvases across the screen.
     *
     * @param dx Relative amount to scroll
     * @param fullPage If true, the tree scroll to the next/previous page.
     * If false, scrolls back to the current page
     *
     */
    protected final void scrollPages(int dx, final boolean fullPage) {

        if (smoothScrolling) {
            dx = nonLineaScroll(dx);
        }
        
        currentPageCanvasPosition += dx;

        if (fullPage) {

                if (currentPageCanvasPosition >= pageCanvasPositionMax) {

                /*
                 * loading prev page
                 */
                currentPageCanvasPosition = pageCanvasPositionMax;
                mode = MODE_PAGE_LOCKED;

                final Page page = chapterBooklet.getPrevPage();

                if (page instanceof DummyPage) {
                    DummyPage pd = (DummyPage)page;
                    handleDummyPage(pd.getType(), SCROLL_BOOK_START);
                }

                if (page instanceof TextPage) {
                    stopScrolling();
                    loadPrevPage();
                    return;
                }
            }

            if (currentPageCanvasPosition <= pageCanvasPositionMin) {
                
                /*
                 * loading next page
                 */
                currentPageCanvasPosition = pageCanvasPositionMin;
                mode = MODE_PAGE_LOCKED;

                final Page page = chapterBooklet.getNextPage();

                if (page instanceof DummyPage) {
                    DummyPage pd = (DummyPage)page;
                    handleDummyPage(pd.getType(), SCROLL_BOOK_END);
                }

                if (page instanceof TextPage) {
                    stopScrolling();
                    loadNextPage();
                    return;
                }
            }

        repaint();
        serviceRepaints();

        } else {

            /* scrolling to the same page */
            if ((dx < 0 && currentPageCanvasPosition <= 0)
                    || (dx >= 0 && currentPageCanvasPosition >= 0)) {
                currentPageCanvasPosition = 0;
                stopScrolling();
                mode = MODE_PAGE_READING;
            }
            repaint();
            serviceRepaints();
        }
    }

    private void handleDummyPage(
            final byte type, final int bookScrollingDirection) {

        stopScrolling();

        switch (type) {
            case DummyPage.TYPE_CHAPTER_PREV:
                renderWaitCursor();
                goToLastPage(currentBook.getCurrentChapter().getPrevChapter());
                break;

            case DummyPage.TYPE_CHAPTER_NEXT:
                renderWaitCursor();
                goToFirstPage(currentBook.getCurrentChapter().getNextChapter());
                break;

            case DummyPage.TYPE_BOOK_START:
            case DummyPage.TYPE_BOOK_END:
                mode = MODE_PAGE_SCROLLING;
                scheduleScrolling(bookScrollingDirection);
                break;
        }

    }

    private void loadPrevPage() {
        chapterBooklet.goToPrevPage();

        Page prev = chapterBooklet.getPrevPage();

        if (prev.hasImage()) {
            mode = MODE_PAGE_LOADING;
        }
        
        repaint();
        serviceRepaints();

        currentPageCanvasPosition = 0;

        PageCanvas p = nextPageCanvas;
        nextPageCanvas = currentPageCanvas;
        currentPageCanvas = prevPageCanvas;
        prevPageCanvas = p;

        p.setPage(prev);
        p.renderPage(currentScheme);

        repaintProgressBar = true;
        mode = MODE_PAGE_READING;

        repaint();
        serviceRepaints();
    }

    private void loadNextPage() {
        chapterBooklet.goToNextPage();

        Page next = chapterBooklet.getNextPage();

        if (next.hasImage()) {
            mode = MODE_PAGE_LOADING;
        }

        repaint();
        serviceRepaints();
        
        currentPageCanvasPosition = 0;

        PageCanvas p = prevPageCanvas;
        prevPageCanvas = currentPageCanvas;
        currentPageCanvas = nextPageCanvas;
        nextPageCanvas = p;


        p.setPage(next);

        p.renderPage(currentScheme);

        repaintProgressBar = true;
        mode = MODE_PAGE_READING;

        repaint();
        serviceRepaints();
    }

    private void loadChapter(final Chapter chapter) {

        if (chapter != currentBook.getCurrentChapter()
                || chapterBooklet == null) {

            /* chapter changed or book not loaded at all */
            currentBook.unloadChaptersBuffers();
            currentBook.setCurrentChapter(chapter);
            updateChapterNum(chapter.getNumber() + 1);
            renderWaitCursor();
            reflowPages();
            mode = MODE_PAGE_READING;
        }
    }

    public final void goToFirstPage(final int chapterNumber) {
        final Chapter c = currentBook.getChapter(chapterNumber);
        goToFirstPage(c);
    }

    private void goToFirstPage(final Chapter chapter) {
        loadChapter(chapter);
        chapterBooklet.goToFirstPage();
        renderPages();
    }

    public final void goToLastPage(final int chapterNumber) {
        final Chapter c = currentBook.getChapter(chapterNumber);
        goToLastPage(c);
    }

    private void goToLastPage(final Chapter chapter) {
        loadChapter(chapter);
        chapterBooklet.goToLastPage();
        renderPages();
    }

    public final void goToPosition(final Chapter chapter, final int position) {
        loadChapter(chapter);
        chapterBooklet.goToPosition(position);
        renderPages();
    }

    public final void goToPosition(
            final int chapterNumber, final float percent) {

        final Chapter c = currentBook.getChapter(chapterNumber);
        goToPosition(c, percent);
    }

    private void goToPosition(
            final Chapter chapter, final float percent) {

        loadChapter(chapter);

        /*
         * Calculate position, using percent representation
         */
        final float f = (float) (percent - Math.floor(percent));
        final int page =
                (int) (percent * chapterBooklet.getPagesCount());
        chapterBooklet.goToPage(page);
        renderPages();
    }


    private void renderPages() {

        currentPageCanvas.setPage(chapterBooklet.getCurrentPage());
        prevPageCanvas.setPage(chapterBooklet.getPrevPage());
        nextPageCanvas.setPage(chapterBooklet.getNextPage());

        prevPageCanvas.renderPage(currentScheme);
        currentPageCanvas.renderPage(currentScheme);
        nextPageCanvas.renderPage(currentScheme);

        currentPageCanvasPosition = 0;

        repaintProgressBar = true;

        mode = MODE_PAGE_READING;

        repaint();
        serviceRepaints();
    }

    private void renderWaitCursor() {
        holdingValid = false;
        mode = MODE_PAGE_LOADING;
        repaint();
        serviceRepaints();
    }

    private void reflowPages() {
        /*
         * Free memory before claiming it!
         */
        chapterBooklet = null;
        chapterBooklet = new Booklet(
                currentPageCanvas.getPageWidth(),
                currentPageCanvas.getPageHeight(),
                inverted,
                currentBook.getCurrentChapter(),
                currentBook.getArchive(),
                fontPlain,
                fontItalic,
                currentLineSpacing,
                renderImages,
                currentBook.getParser());
        pagesCount = chapterBooklet.getPagesCount() - 3;
    }

    public final void cycleColorSchemes() {
        currentScheme = currentScheme.getOther();
        applyColorProfile();
    }

    public final void setScheme(final byte type, final float hue) {

        ColorScheme sc =
                ColorScheme.getScheme(type, currentScheme.isDay(), hue);

        final ColorScheme other = currentScheme.getOther();
        sc.link(other);
        currentScheme = sc;
        applyColorProfile();
    }

    private void applyColorProfile() {

        /*
         * apply to cursor
         */
        waitCursor.setColor(
                currentScheme.colors[ColorScheme.COLOR_CURSOR_WAIT]);

        /*
         * apply to status bar
         */
        repaintStatusBar = true;

        /*
         * apply to pages
         */
        if (currentPageCanvas != null) {
            renderPages();
        }
    }

    private void loadFont() {
        final int currentFontSize;

        if (useNativeFonts) {
            if (currentFontSizeIndex < 0 || currentFontSizeIndex >= nativeFontSizes.length) {
                currentFontSizeIndex = (byte) ((nativeFontSizes.length - 1) / 2);
                fontGrowing = true;
            }
            currentFontSize = nativeFontSizes[currentFontSizeIndex];

            fontPlain = new AlbiteNativeFont(Font.STYLE_PLAIN, currentFontSize);
            fontItalic = new AlbiteNativeFont(Font.STYLE_ITALIC, currentFontSize);
            
        } else {
            if (currentFontSizeIndex < 0 || currentFontSizeIndex >= fontSizes.length) {
                currentFontSizeIndex = (byte) ((fontSizes.length - 1) / 2);
                fontGrowing = true;
            }
            currentFontSize = fontSizes[currentFontSizeIndex];
        }
    }

    private void cycleFontSizes() {
        if (
                (!useNativeFonts && fontSizes.length > 1) ||
                (useNativeFonts && nativeFontSizes.length > 1)) {

            if (currentFontSizeIndex == 0) {
                fontGrowing = true;
            }

            if (currentFontSizeIndex == (useNativeFonts ? nativeFontSizes.length : fontSizes.length) - 1) {
                fontGrowing = false;
            }
            
            if (fontGrowing) {
                currentFontSizeIndex++;
            } else {
                currentFontSizeIndex--;
            }

            loadFont();

            /*
             * Reflow the chapter
             */
            reflowChapter();
        }
    }

    public final void setFontSize(final byte fontSizeIndex) {

        if (fontSizes.length > 1) {
            if (currentFontSizeIndex > fontSizeIndex) {
                fontGrowing = false;
            } else if (currentFontSizeIndex < fontSizeIndex) {
                fontGrowing = true;
            }

            currentFontSizeIndex = fontSizeIndex;

            loadFont();
        }
    }

    public final void switchNativeFonts() {
        useNativeFonts = !useNativeFonts;
        fontGrowing = true;

        loadFont();
        reflowChapter();
    }

    private void loadStatusFont() {
        fontStatus = fontPlain;

        int max = 0;
        int w;
        char[] chars =
            {'#', ':', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        for (int i = 0; i < chars.length; i++) {
            w = fontStatus.charWidth(chars[i]);
            if (w > max) {
                max = w;
            }
        }

        fontStatusMaxWidth = max;        
    }

    /**
     * @see Canvas#hideNotify() 
    */    
    public final void hideNotify() {
       
    }

    
    /**
     * @see Canvas#showNotify() 
    */
    public final void showNotify() {
        repaintStatusBar = true;
    }

    private void openRMSAndLoadData() {
        try {
            rs = RecordStore.openRecordStore("bookcanvas", true);

            if (rs.getNumRecords() > 0) {

                /*
                 * Deserialize first record
                 */
                byte[] data = rs.getRecord(1);
                DataInputStream din = new DataInputStream(
                        new ByteArrayInputStream(data));
                try {
                    RMSHelper.checkValidity(app, din);

                    /*
                     * Loading color schemes
                     */
                    ColorScheme sc1 = ColorScheme.load(din);
                    ColorScheme sc2 = ColorScheme.load(din);
                    sc1.link(sc2);
                    //operator-stub so as not to disrupt the logic of reading RMS
                    currentScheme = sc1;
                    //Operating value used in the application FBReader
                    ColorScheme day = ColorScheme.DEFAULT_DAY;
                    ColorScheme night = ColorScheme.DEFAULT_NIGHT;
                    day.link(night);
                    currentScheme = day;
                    /*
                     * Loading font options
                     */
                    useNativeFonts = din.readBoolean();
                    //operator-stub so as not to disrupt the logic of reading RMS
                    currentFontSizeIndex = din.readByte();
                    //Operating value used in the application FBReader
                    currentFontSizeIndex = (byte) settings.getBookBaseTextFontSize();
                    /*
                     * Loading scrolling options
                     */
                    speedMultiplier = din.readFloat();
                    smoothScrolling = din.readBoolean();
                    //operator-stub so as not to disrupt the logic of reading RMS
                    horizontalScrolling = din.readBoolean();
                    //Operating value used in the application FBReader
                    horizontalScrolling=settings.getScrollingDirection();
                    currentHoldingTime = din.readInt();
                    /*
                     * Loading screen mode options
                     */
                    //operator-stub so as not to disrupt the logic of reading RMS
                    orientation = din.readInt();
                    //Operating value used in the application FBReader
                    orientation = ORIENTATION_0;
                    //operator-stub so as not to disrupt the logic of reading RMS
                    fullscreen = din.readBoolean();
                    //Operating value used in the application FBReader
                    fullscreen = false;
                    seenFullscreenAlert = din.readBoolean();
                    /*
                     * Write page options
                     */
                    //operator-stub so as not to disrupt the logic of reading RMS
                    currentMarginWidth = din.readInt();
                    //Operating value used in the application FBReader
                    currentMarginWidth=settings.getBookMarginWidth();
                    //operator-stub so as not to disrupt the logic of reading RMS
                    currentLineSpacing = din.readInt();
                    //Operating value used in the application FBReader
                    currentLineSpacing = LINE_SPACING;
                    //operator-stub so as not to disrupt the logic of reading RMS
                    renderImages = din.readBoolean();
                    //Operating value used in the application FBReader
                    renderImages = true;
                } catch (IOException ioe) {
                    //#debug
                    Main.LOGGER.log(ioe);
                }
            }

        } catch (RecordStoreException rse) {
            //#debug
           Main.LOGGER.log(rse);
        }
    }

    protected final void saveOptionsToRMS() {

        /*
         * Has the bookcanvas been opened AT ALL?
         */
        if (isBookOpen()) {
            try {
                //serialize first record
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(boas);
                try {
                    RMSHelper.writeVersionNumber(app, dout);
                    
                    /*
                     * Save color schemes
                     */
                    currentScheme.save(dout);
                    currentScheme.getOther().save(dout);

                    /*
                     * Save font options
                     */
                    dout.writeBoolean(useNativeFonts);
                    dout.writeByte(currentFontSizeIndex);

                    /*
                     * Save scrolling options
                     */
                    dout.writeFloat(speedMultiplier);
                    dout.writeBoolean(smoothScrolling);
                    dout.writeBoolean(horizontalScrolling);
                    dout.writeInt(currentHoldingTime);

                    /*
                     * Save screen mode options
                     */
                    dout.writeInt(orientation);
                    dout.writeBoolean(fullscreen);
                    dout.writeBoolean(seenFullscreenAlert);

                    /*
                     * Write page options
                     */
                    dout.writeInt(currentMarginWidth);
                    dout.writeInt(currentLineSpacing);
                    dout.writeBoolean(renderImages);
                    
                   

                } catch (IOException ioe) {
                    //#debug
                    Main.LOGGER.log(ioe);
                }

                byte[] data = boas.toByteArray();

                if (rs.getNumRecords() > 0) {
                    rs.setRecord(1, data, 0, data.length);
                } else {
                    rs.addRecord(data, 0, data.length);
                }
            } catch (RecordStoreException rse) {
                //#debug
                Main.LOGGER.log(rse);
            }
        }
    }

    private void closeRMS() {
        try {
            rs.closeRecordStore();
        } catch (RecordStoreException rse) {}
    }

    public final void close() {
        timer.cancel();
        closeBook();
        closeRMS();
    }

    public final void setScrollingOptions(
            final float speedMultiplier,
            final boolean smoothScrolling,
            final boolean horizontalScrolling) {

        this.speedMultiplier = speedMultiplier;
        this.smoothScrolling = smoothScrolling;
        this.horizontalScrolling = horizontalScrolling;

        applyScrollingSpeed();
        applyAbsScrPgOrd();
        chapterBooklet.setInverted(inverted);
        if (inverted) {
            PageCanvas pc = nextPageCanvas;
            nextPageCanvas = prevPageCanvas;
            prevPageCanvas = pc;
        }
        applyScrollingLimits();
    }

    /**
     * Sets up the following:
     *
     * Scrolling speed:
     * - scrollNextPagePixels
     * - scrollSamePagePixels
     * - scrollStartBookPixels
     *
     * Depends on:
     * - speedMultiplier
     *
     * Therefore, needs to be called after:
     * - Page interaction screen
     */
    private void applyScrollingSpeed() {

        scrollNextPagePixels = (int)
                (MAXIMUM_SPEED * speedMultiplier * frameTime);

        /*
         * These values are calculated as a fraction
         * of the normal page scrolling
         */
        scrollSamePagePixels = (int) (scrollNextPagePixels / 8);
        scrollStartBookPixels = (int) (scrollNextPagePixels / 2);

        /*
         * If something by any chane is zero, one wouldn't be
         * able to scroll at all
         */
        if (scrollNextPagePixels == 0) {
            scrollNextPagePixels = 1;
        }

        if (scrollSamePagePixels == 0) {
            scrollSamePagePixels = 1;
        }

        if (scrollStartBookPixels == 0) {
            scrollStartBookPixels = 1;
        }

    }

    /**
     * Sets up absolute scrolling direction and page ordering, i.e.:
     * - scrollinOnX
     * - inverted
     *
     * Depends on:
     * - horizontalScrolling
     * - orientation
     *
     * Therefore, needs to be called after:
     * - Page interaction screen
     * - Screen mode screen
     */
    private void applyAbsScrPgOrd() {
        /*
         * Set which direction on should scroll around
         */
        if (orientation == ORIENTATION_0 || orientation == ORIENTATION_180) {
            scrollingOnX = horizontalScrolling;
        } else {
            scrollingOnX = !horizontalScrolling;
        }

        /*
         * Do we need to inverted the ordering of pages?
         */
        switch (orientation) {
            case ORIENTATION_0:
                inverted = false;
                break;

            case ORIENTATION_90:
                inverted = (horizontalScrolling ? false : true);
                break;

            case ORIENTATION_180:
                inverted = true;
                break;

            case ORIENTATION_270:
                inverted = (horizontalScrolling ? true : false);
                break;
        }
    }

    /**
     * Sets up scrolling limits according to current page size, i.e.
     * it sets up the following:
     *
     * - pageCanvasPositionMax
     * - pageCanvasPositionMin
     *
     * Depends on:
     * - width and height of PageCanvases
     * - scrollingOnX
     *
     * Therefore, it must be called after:
     * - Screen mode screen
     * - Page layout screen
     *
     * Also, it should not be called before:
     * - applyScrolling
     */
    private void applyScrollingLimits() {
        /*
         * Set min/max where prev/next page must be loaded
         */
        if (scrollingOnX) {
            pageCanvasPositionMax =
                    currentPageCanvas.getWidth() + currentMarginWidth;

        } else {
            pageCanvasPositionMax =
                    currentPageCanvas.getHeight() + currentMarginWidth;
 
        }

        pageCanvasPositionMin = -pageCanvasPositionMax;
        
    }

    public final void setOrientation(
            final int orientation, boolean fullscreen) {

        if (orientation != ORIENTATION_0) {
            /*
             * When not in 0-degree view, always use fullscreen.
             */
            fullscreen = true;
        }

        if (this.orientation != orientation
                || this.fullscreen != fullscreen) {

            renderWaitCursor();

            this.orientation = orientation;
            this.fullscreen = fullscreen;

            applyAbsScrPgOrd();
            //loadButtons();
            reloadPages();
            applyScrollingLimits();
        }
    }

    public final void sizeChanged(final int width, final int height) {

        if (prevWidth == width && prevHeight == height) {
            /* Nothing has changed */
            return;
        }

        if ((width == fullScreenWidth && height == fullScreenHeight)
                || (width == fullScreenHeight && height == fullScreenWidth)) {

            /*
             * It's a correct change of size, not because of going into
             * non-fullscreen for some obscure reason of the j2me implementation
             */
            
            prevWidth = width;
            prevHeight = height;

            renderWaitCursor();
            updateProgressBarSize(width);
            reloadPages();
            applyScrollingLimits();
        }
    }

    private void reloadPages() {
        final int currentPos = chapterBooklet.getCurrentPage().getStart();

        initializePageCanvases();
        reflowPages();
        goToPosition(currentBook.getCurrentChapter(), currentPos);

        repaintStatusBar = true;
        mode = MODE_PAGE_READING;
        repaint();
        serviceRepaints();
    }

    private void updateChapterNum(final int chapterNo) {

        /*
         * update chapter's number
         */
        final char[] chapterNoCharsF = chapterNoChars;
        final int currentChapterNo = chapterNo;
        int i = 1;

        if (currentChapterNo > 99) {
            chapterNoCharsF[i] = (char) ('0' + (currentChapterNo / 100));
            i++;
        }

        if (currentChapterNo > 9) {
            chapterNoCharsF[i] = (char) ('0' + ((currentChapterNo % 100) / 10));
            i++;
        }

        chapterNoCharsF[i] = (char) ('0' + ((currentChapterNo % 100) % 10));
        i++;

        for (; i < chapterNoCharsF.length; i++) {
            chapterNoCharsF[i] = ' ';
        }

        chapterNoChars = chapterNoCharsF;
        repaintChapterNum = true;
    }


    public final boolean getHorizontalScalling() {
        return horizontalScrolling;
    }

    public final boolean getSmoothScrolling() {
        return smoothScrolling;
    }

    public final int getScrollingSpeed() {
        return (int) (speedMultiplier * 100);
    }

    public final void updatePageSettings(
            final int margin,
            final int lineSpacing,
            final boolean images) {

        if (margin == currentMarginWidth
                && lineSpacing == currentLineSpacing
                && images == renderImages) {

            /*
             * Nothing has changed
             */
            return;
        }

        this.currentMarginWidth = margin;
        this.currentLineSpacing = lineSpacing;
        this.renderImages = images;

        renderWaitCursor();

        reloadPages();
        applyScrollingLimits();
    }

    public final int getCurrentMargin() {
        return currentMarginWidth;
    }

    public final int getCurrentLineSpacing() {
        return currentLineSpacing;
    }

    public final boolean rendersImages() {
        return renderImages;
    }

    public final Book getCurrentBook() {
        return currentBook;
    }

    public final void setHoldingTimeByMultiplier(final int multiplier) {
        currentHoldingTime = HOLDING_TIME_MIN * (multiplier + 1);
    }

    public final int getHoldingTimeMultiplier() {
        return currentHoldingTime / HOLDING_TIME_MIN;
    }

    public final int getFontSizeIndex() {
        return currentFontSizeIndex;
    }
    
    public final void setFontSizeIndex (final byte fontSizeIndex) {
        this.currentFontSizeIndex=fontSizeIndex;
    }    
    
    

    /*
     * This one is pretty a bit dirty, but there's no time for it.
     */
    public final int getScreenMode() {

        switch (orientation) {
            case ORIENTATION_0:
                if (!fullscreen) {
                    return 0;
                } else {
                    return 1;
                }

            case ORIENTATION_90:
                return 2;

            case ORIENTATION_180:
                return 3;

            case ORIENTATION_270:
                return 4;

            default:
                return 0;
        }
    }

    private void reflowChapter() {
        int start = chapterBooklet.getCurrentPage().getStart();
        renderWaitCursor();
        reflowPages();
        goToPosition(currentBook.getCurrentChapter(), start);
        mode = MODE_PAGE_READING;
    }

    public final void setChapterEncoding(final String encoding) {
        if (currentBook.setEncoding(encoding)) {
            /*
             * reflow the chapter
             */
            reflowChapter();
        }
    }

    public final void setAutoChapterEncoding() {
        setChapterEncoding(Chapter.AUTO_ENCODING);
    }

    
    public final ColorScheme getColorScheme() {
        return currentScheme;
    }

    public final AlbiteFont getFontPlain() {
        return fontPlain;
    }

    public final AlbiteFont getFontItalic() {
        return fontItalic;
    }

    public final boolean useNativeFonts() {
        return useNativeFonts;
    }

    private void updateProgressBarSize(final int w) {
        progressBarWidth = w - (STATUS_BAR_SPACING * 4);

        if (chapterNoWidth > clockWidth) {
            progressBarWidth -= chapterNoWidth * 2;
        } else {
            progressBarWidth -= clockWidth * 2;
        }
        
        progressBarX = (w - progressBarWidth) / 2;
    }
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
        
          if (command == viewMaster.getBackCmd()) {
            closeBook();
            //back to previous Form
            Main.getInstance().switchDisplayable(null, viewMaster.getMyLibBookInfoViewForm()); 
          }
          else if (command == tocCmd) {
            Main.getInstance().showToc();
          }
          else if (command == nightModeCmd) {
            this.removeCommand(this.nightModeCmd);
            this.addCommand(this.dayModeCmd);
            cycleColorSchemes();  
          }
          
          else if (command == dayModeCmd) {
            this.removeCommand(this.dayModeCmd);
            this.addCommand(this.nightModeCmd);
            cycleColorSchemes();  
          }
          else if (command == bookInfoCmd) {
            Main.getInstance().switchDisplayable(null, viewMaster.getBookCanvasBookInfoViewForm());
            
          }
          else if (command == viewMaster.getHelpCmd()){
            Main.getInstance().switchDisplayable(null, viewMaster.getBookCanvasHelpForm());
          }          
   
      }
}
