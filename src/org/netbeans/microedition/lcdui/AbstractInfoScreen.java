/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * AbstractInfoScreen.java
 *
 * Created on August 26, 2005, 1:53 PM
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
* 1. Added waiting indicator as animated book 
* 2. Minor changes in the source code
* 
*  
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.netbeans.microedition.lcdui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import org.albite.font.AlbiteFont;
import org.fbreader.views.BookCanvas;
import org.fbreader.views.ColorScheme;
import org.fbreader.utils.ImageLoader;
import org.tantalum.Workable;
import org.tantalum.Worker;

/**
 * 
 * An abstract class serving as a parent for SplashScreen and WaitScreen. This class provides
 * the basic visualization of the screen. 
 * 
 * When this screen is displayed, it can display either supplied text or image. The current implementation
 * shows both, text and image, centered in the middle of the display. 
 * 
 * > Please note, in previous version this component automatically switched to displayables
 * specified by setNextDisplayable() method, but this approach has been deprecated in favor
 * of using static command and calling CommandListener's commandAction() method when an action
 * happens. This gives the developer much higher flexibility for processing the action - it
 * is no longer limited to switching to another displayable, but it can do whatever developer wants.
 *
 * @author breh
 */


public abstract class AbstractInfoScreen extends Canvas {
	
    private Display display;
    
    private Image image;
    private char[] text;
    
    private Displayable nextDisplayable;
    private Alert nextAlert;
    
    private Displayable previousDisplayable;
    
    private CommandListener commandListener;

    private BookCanvas bookCanvas;
    private Font textFont;
    
    private Sprite loaderSprite;
    private Timer animationTimer;
    
    private boolean wasLoadedSprite;
   
    /**
     * Creates a new instance of AbstractInfoScreen
     * @param display display parameter. Cannot be null
     * @param colorSchema color schema to be used for this component. If null, SystemColorSchema is used.
     * @throws java.lang.IllegalArgumentException if the display parameter is null
     */
    public AbstractInfoScreen(Display display) {
        if (display == null) {
            throw new IllegalArgumentException(
                    "Display parameter cannot be null.");
        }
        this.display = display;
    }
    
    
    // properties
    
    /**
     * Sets ColorSchema
     */    
    public void setBookCanvas(final BookCanvas bookCanvas) {
        this.bookCanvas = bookCanvas;
        repaint();
    }
    
    /**
     * Sets the text to be painted on the screen.
     * @param text text to be painter, or null if no text should be shown
     */
    public void setText(String text) {
        this.text = text.toCharArray();
        repaint();
    }
    
    /**
     * Gets the text to be painted on the screen.
     * @return text
     */
    public String getText() {
        return new String(this.text);
    }
    
    
    /**
     * Gets the image to be painted on the screen.
     * @return image
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * Sets image to be painted on the screen. If set to null, no image
     * will be painted
     * @param image image to be painted. Can be null.
     */
    public void setImage(Image image) {
        this.image = image;
        repaint();
    }
    
    /**
     * Sets the font to be used to paint the specified text. If set
     * to null, the default font (Font.STATIC_TEXT_FONT) will be used.
     * @param font font to be used to paint the text. May be null.
     */
    public void setTextFont(Font font) {
        if (font != null) {
            this.textFont = font;
        } else {
            this.textFont = Font.getFont(Font.FONT_STATIC_TEXT);
        }
    }
    
    /**
     * Gets the current font used to paint the text.
     * @return text font
     */
    public Font getTextFont() {
        return null;
    }
    
    /**
     * Gets command listener assigned to this displayable
     * @return command listener assigned to this component or null if there is no command listener assigned
     */
    protected final CommandListener getCommandListener() {
        return this.commandListener;
    }
    
    /**
     * Sets command listener to this component
     * @param commandListener - command listener to be used
     */
    public void setCommandListener(CommandListener commandListener) {
        super.setCommandListener(commandListener);
        this.commandListener = commandListener;
    }      
    
    /**
     * 
     * Sets the displayable to be used to switch when the screen is being dismissed.
     *
     * @param nextDisplayable displayable, or null if the component should switch back
     * to the screen from which was displayed prior showing this component.
     * 
     * @deprecated - use static Commands and CommandListener from the actual implementation 
     */
    public void setNextDisplayable(Displayable nextDisplayable) {
        this.nextDisplayable = nextDisplayable;
    }
    
    
    /**
     * Requests that the specified Alert is going to be shown in the case of
     * screen dismiss, and nextDisplayable be made current after the Alert is dismissed.
     *  <p/>
     * The nextDisplayable parameter cannot be Alert and in the case
     * nextAlert is not null, it also cannot be null.
     * @param nextAlert alert to be shown, or null if the component should return back to the original screen
     * @param nextDisplayable a displayable to be shown after the alert is being dismissed. This displayable
     * cannot be null if the <code>nextAlert</code> is not null and it also cannot be
     * Alert.
     * @throws java.lang.IllegalArgumentException If the nextAlert is not null and nextDisplayable is null at the same time, or
     * if the nextDisplayable is instance of <code>Alert</code>
     *
     * @deprecated - use static Commands and CommandListener pattern from the actual implementation class
     */
    public void setNextDisplayable(Alert nextAlert, Displayable nextDisplayable) throws IllegalArgumentException {
        if ((nextAlert != null) && (nextDisplayable == null))
            throw new IllegalArgumentException("A nextDisplayable parameter cannot be null if the nextAlert parameter is not null.");
        if (nextDisplayable instanceof Alert)
            throw new IllegalArgumentException("nextDisplayable paremter cannot be Alert.");
        this.nextAlert = nextAlert;
        this.nextDisplayable  = nextDisplayable;
    }
    
    // protected methods
    
    /**
     * implementation of abstract method
     * @param g
     */
    protected void paint(Graphics g) {

        final int w = g.getClipWidth();
        final int h = g.getClipHeight();
        int x = g.getClipX();
        int y = g.getClipY();
        int centerX = w / 2 + x;
        int centerY = h / 2 + y;
        
        final int spriteX;
        final int spriteY;
        final Graphics g1;         

        final int backgroundColor, textColor;
        
        if (!wasLoadedSprite) {
            if (bookCanvas != null) {
                final ColorScheme cs = bookCanvas.getColorScheme();
                backgroundColor = cs.colors[ColorScheme.COLOR_BACKGROUND];
                textColor = cs.colors[ColorScheme.COLOR_TEXT_ITALIC];
            } else {
                backgroundColor = 0xFFFFFF;
                textColor = 0x0;
            }

            g.setColor(backgroundColor);
            g.fillRect(x, y, w, h);

            if (image != null) {
                g.drawImage(image, centerX, centerY, Graphics.HCENTER | Graphics.VCENTER);
                centerY += (image.getHeight() / 2) + 20;
            }

            if (text != null) {
                if (bookCanvas != null) {
                    final AlbiteFont font = bookCanvas.getFontItalic();

                    font.drawChars(
                            g,
                            textColor,
                            text,
                            centerX - (font.charsWidth(text) / 2), centerY);
                } else {
                    g.setColor(textColor);
                    if (textFont != null) {
                        g.setFont(textFont);
                    }

                    g.drawString(
                            getText(),
                            centerX, centerY,
                            Graphics.HCENTER | Graphics.TOP);
                }
            }

            if (bookCanvas != null){ 
                if (loaderSprite == null) {
                    try {
                        Image i =
                            ImageLoader.getInstance().loadImage("/open_book_sprite_136x35.png", null);
                        loaderSprite = new Sprite(i, i.getWidth() / 4, i.getHeight());
                        wasLoadedSprite=true;



                    }
                    catch (IOException e) {
                    }
                }            
            
            }

         }
        else {

           spriteX=(w - loaderSprite.getWidth()) / 2;
           spriteY=h / 2 + y - loaderSprite.getHeight();
           g1=g;            
           repaintLoadedSprite(g1, spriteX, spriteY);  
 
        }

    }
    
    
    /**
     * repaints the screen after loaderSprite.nextFrame(); 
    */
    protected void repaintLoadedSprite(final Graphics g, final int spriteX, final int spriteY) {
            Worker.fork(new Workable() {
                public Object exec(final Object in) {
                    
                    
                animationTimer.schedule(new TimerTask() {
                    public void run() {
                            
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                            
                            if (loaderSprite != null) {
                                loaderSprite.nextFrame();
                                loaderSprite.setPosition(spriteX, spriteY);
                                //loaderSprite.paint(g);
                                loaderSprite.paint(g);
                                repaint();
                            }
                        }
                    }, 0, 500);                    
                    return in;
                }
            });        
         
    }
    
    
    /**
     * repaints the screen when a size has changed.
     */
    protected void sizeChanged(int w, int h) {
        repaint();
    }
    
    
    /**
     * Gets the used display object
     * @return display object
     */
    protected Display getDisplay() {
        return display;
    }
    
    
    /**
     * Gets the next displayable
     * @return next displayable
     * @deprecated - use static Commands and CommandListener pattern from the actual implementation class
     */
    protected Displayable getNextDisplayable() {
        return nextDisplayable;
    }
    
    
    /**
     * gets the next alert
     * @return next alert
     * @deprecated - use static Commands and CommandListener pattern from the actual implementation class
     */
    protected Alert getNextAlert() {
        return nextAlert;
    }
    
    
    /**
     * switch to the next displayable (or alert)
     * @deprecated - use static Commands and CommandListener pattern from the actual implementation class
     */
    protected void switchToNextDisplayable() {
        if (nextDisplayable != null) {
            switchToDisplayable(display, nextAlert, nextDisplayable);
        } else if (previousDisplayable != null) {
            display.setCurrent(previousDisplayable);
        }
    }
    
    /**
     * Switch to the given displayable and alert
     * @param display
     * @param alert
     * @param displayable
     *
     * @deprecated - use SplashScreen.DISMISS_COMMAND or WaitScreen.SUCCESS_COMMAND in CommandListener.commandAction()
     * to handle this event for specific implementation of the info screen.
     */
    protected static void switchToDisplayable(Display display, Alert alert, Displayable displayable) {
        if (displayable != null) {
            if (alert != null) {
                display.setCurrent(alert,displayable);
            } else {
                display.setCurrent(displayable);
            }
        }
    }
    
    /**
     * sets value of previous displayable. Implementation should always
     * call this super implementation when overriding this method
     *
     */
    protected void showNotify() {
        previousDisplayable = getDisplay().getCurrent();
        super.showNotify();
        
        animationTimer = new Timer();
        wasLoadedSprite=false;
        
        
    }
    

    /**
     * @see Canvas#hideNotify() 
     */
    protected void hideNotify() {
        loaderSprite = null;
        animationTimer.cancel();
        animationTimer = null;
        wasLoadedSprite=false;
       
    }    
    
    
}
