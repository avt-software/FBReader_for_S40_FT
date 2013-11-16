/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.albite.lang;

import java.util.Vector;

/**
 * Basic text processing tools
 *
 * @author albus
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

public final class TextTools {
    private TextTools() {}

    public static Vector split(final String string, final char[] separators) {
        int len = string.length();
        int lastpos = 0;
        int pos = 0;
        final Vector res = new Vector();

        while ((pos = indexOf(string, separators, lastpos)) != -1 && pos < len) {
            res.addElement(string.substring(lastpos, pos));
            lastpos = pos + 1;
        }
        res.addElement(string.substring(lastpos));

        return res;
    }

    public static int indexOf(
            final String string, final char[] needles, final int start) {

        char ch;
        for (int i = start; i < string.length(); i++) {
            ch = string.charAt(i);
            for (int j = 0; j < needles.length; j++) {
                if (needles[j] == ch) {
                    return i;
                }
            }
        }

        return -1;
    }
}