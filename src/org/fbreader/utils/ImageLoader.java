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

* Changes and additions to the original code:
* --------------------------------------------
*    1. Removed method loadImage(), which is used for
*       loads an image from resources or network.
* 
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.fbreader.uihelpers.Visual;

/**
 * Utility for loading image resources.
 */
public final class ImageLoader {

    private static ImageLoader self;
    private static final Hashtable imageCache = new Hashtable();

    /**
     * An interface for notifying that an image has been loaded.
     */
    public interface Listener {

        void imageLoaded(Image image);
    }

    private ImageLoader() {
    }

    /**
     * @return ImageLoader singleton
     */
    public static ImageLoader getInstance() {
        if (self == null) {
            self = new ImageLoader();
        }
        return self;
    }

    /**
     * Loads an image from resources and returns it.
     * Caches all loaded images in hopes of saving some memory.
     * @param imagePath
     * @return loaded image
     * @throws IOException
     */
    public final Image loadImage(final String imagePath, final Hashtable cache)
        throws IOException {
        Image image = getImageFromCache(imagePath, cache);
        if (image == null) {
            InputStream in = this.getClass().getResourceAsStream(imagePath);
            if (in == null) {
                throw new IOException("Image not found.");
            }
            image = Image.createImage(in);
            cacheImage(imagePath, image, cache, false);
        }
        return image;
    }

    private void cacheImage(String key, Image image, Hashtable cache,
        boolean toWeakCache) {
        if (cache != null) {
            cache.put(key, image);
        }
        if (toWeakCache) {
            imageCache.put(key, new WeakReference(image));
        }
    }

    private Image getImageFromCache(String key, Hashtable cache) {
        Image image = null;
        if (cache != null) {
            image = (Image) cache.get(key);
        }
        if (image == null) {
            WeakReference imageRef = (WeakReference) imageCache.get(key);
            image = imageRef == null ? null : (Image) imageRef.get();
            if (image != null && cache != null) {
                cache.put(key, image);
            }
        }
        if (image == null) {
            imageCache.remove(key);
        }

        return image;
    }

    /**
     * Creates a map marker.
     * @param id text which is shown in the marker
     * @param imagePath
     * @return map marker image
     * @throws IOException 
     */
    public final Image loadMapMarker(final String id, final String imagePath,
        final Hashtable cache)
        throws IOException {
        Image background = loadImage(imagePath, cache);
        String url = "" + id + imagePath;
        Image image = null;
        if (cache != null) {
            image = (Image) cache.get(url);
        }
        if (image == null) {
            int w = background.getWidth();
            int h = background.getHeight();
            image = Image.createImage(w, h);
            Graphics g = image.getGraphics();
            g.setColor(0xffffff);
            g.fillRect(0, 0, w, h);
            g.setColor(Visual.MAP_MARKER_COLOR);
            g.setFont(Visual.MAP_MARKER_FONT);
            /*
            g.drawString(id, w / 2, h / 2 + g.getFont().getHeight() / 2 - (Main.
                isS60Phone() ? 3 : 5),
                Graphics.BOTTOM | Graphics.HCENTER);
            */ 
            g.drawString(id, w / 2, h / 2 + g.getFont().getHeight() / 2 - 5,
                Graphics.BOTTOM | Graphics.HCENTER);

            image = multiplyImages(background, image);
            if (cache != null) {
                cache.put(url, image);
            }
        }
        return image;
    }

    private static Image multiplyImages(Image img1, Image img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight()
            != img2.getHeight()) {
            throw new IllegalArgumentException(
                "Sizes of the images must be same");
        }
        int[] rawImg1 = new int[img1.getHeight() * img1.getWidth()];
        img1.getRGB(rawImg1, 0, img1.getWidth(), 0, 0,
            img1.getWidth(), img1.getHeight());
        int[] rawImg2 = new int[img2.getHeight() * img2.getWidth()];
        img2.getRGB(rawImg2, 0, img2.getWidth(), 0, 0, img2.getWidth(), img2.
            getHeight());

        int mrgb, mr, mg, mb, a, r, g, b;
        for (int i = 0, l = rawImg1.length; i < l; i++) {
            mrgb = rawImg2[i] & 0xffffff;
            if (mrgb < 0xffffff) {
                mr = mrgb >>> 16;
                mg = (mrgb & 0xff00) >>> 8;
                mb = mrgb & 0xff;
                a = rawImg1[i] & 0xff000000;
                r = (((rawImg1[i] & 0xff0000) * mr) / 0xff) & 0xff0000;
                g = (((rawImg1[i] & 0xff00) * mg) / 0xff) & 0xff00;
                b = (((rawImg1[i] & 0xff) * mb) / 0xff) & 0xff;
                rawImg1[i] = a | r | g | b;
            }
        }

        return Image.createRGBImage(rawImg1, img1.getWidth(),
            img1.getHeight(), true);
    }

    /**
     * Scales an image.
     * @param original
     * @param newWidth
     * @param newHeight
     * @return new scaled image
     */
    public static Image scaleImage(Image original, int newWidth,
        int newHeight) {
        int[] rawInput = new int[original.getHeight() * original.getWidth()];
        original.getRGB(rawInput, 0, original.getWidth(), 0, 0, original.
            getWidth(), original.getHeight());

        int[] rawOutput = new int[newWidth * newHeight];

        // YD compensates for the x loop by subtracting the width back out
        int YD = (original.getHeight() / newHeight) * original.getWidth()
            - original.getWidth();
        int YR = original.getHeight() % newHeight;
        int XD = original.getWidth() / newWidth;
        int XR = original.getWidth() % newWidth;
        int outOffset = 0;
        int inOffset = 0;

        for (int y = newHeight, YE = 0; y > 0; y--) {
            for (int x = newWidth, XE = 0; x > 0; x--) {
                rawOutput[outOffset++] = rawInput[inOffset];
                inOffset += XD;
                XE += XR;
                if (XE >= newWidth) {
                    XE -= newWidth;
                    inOffset++;
                }
            }
            inOffset += YD;
            YE += YR;
            if (YE >= newHeight) {
                YE -= newHeight;
                inOffset += original.getWidth();
            }
        }
        return Image.createRGBImage(rawOutput, newWidth, newHeight, true);
    }
}
