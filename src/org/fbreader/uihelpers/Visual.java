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
package org.fbreader.uihelpers;

import com.nokia.mid.ui.DirectUtils;
import javax.microedition.lcdui.Font;

/**
 * Visual properties.
 */
public class Visual {

    //Colors
    public static final int BACKGROUND_COLOR = 0xffffff;
    public static final int DARK_BACKGROUND_COLOR = 0x101010;
    public static final int SPLASH_BACKGROUND_COLOR = 0x107EDA;
    public static final int HEADER_FONT_COLOR = BACKGROUND_COLOR;
    public static final int SPLASH_TEXT_COLOR = DARK_BACKGROUND_COLOR;
    public static final int LIST_PRIMARY_COLOR = 0x0;
    public static final int LIST_SECONDARY_COLOR = 0x888888;
    public static final int LIST_SCROLLBAR_COLOR = 0x333333;
    public static final int LOGO_TEXT_COLOR = 0xffffff;
    public static final int LIST_FOCUS_COLOR = 0xaaaaaa;
    public static final int MAP_MARKER_COLOR = 0x333333;
    //Fonts
    public static final Font SMALL_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static final Font SMALL_BOLD_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_BOLD, Font.SIZE_SMALL);
    public static final Font MEDIUM_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    public static final Font MEDIUM_BOLD_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    public static final Font LARGE_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_PLAIN, Font.SIZE_LARGE);
    public static final Font TITLE_FONT = Font.getFont(Font.FACE_SYSTEM,
        Font.STYLE_BOLD, Font.SIZE_LARGE);
    public static final Font MAP_MARKER_FONT;

    static {
        if ("true".equals(System.getProperty("com.nokia.mid.ui.customfontsize"))) {
            MAP_MARKER_FONT = DirectUtils.getFont(Font.FACE_SYSTEM,
                Font.STYLE_BOLD, 11);
        }
        else {
            MAP_MARKER_FONT = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
                Font.SIZE_SMALL);
        }
    }
    //Other
    public static final int SOFTKEY_MARGIN = 4;
}
