/*
 * Copyright © 2012 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners.
 * See LICENSE.TXT for license information.
 */

package org.fbreader.utils;

import java.util.Stack;
import org.fbreader.views.ViewMaster;

public class ViewStack {

    private Stack stack = new Stack();

    /**
     * Add a view to the stack.
     * @param viewId
     */
    public void pushView(int id) {
        // Remove the view from viewstack to prevent stack size growing too big
        stack.removeElement(new Integer(id));
        // Push the view id into viewstack.
        stack.push(new Integer(id));
    }

    /**
     * Returns the top most view from the stack.
     */
    public int popView() {
        if (!stack.isEmpty()) {
            int viewId = ((Integer) stack.pop()).intValue();
            return viewId;
        }
        return ViewMaster.VIEW_NOVIEW;
    }
}
