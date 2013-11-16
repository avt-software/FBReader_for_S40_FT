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
* support website: http://software.avt.dn.ua
*
* support email address: support@software.avt.dn.ua
*  
*/

package org.fbreader.views;

import javax.microedition.lcdui.*;
import org.fbreader.Main;
import org.fbreader.localization.L10n;


public class BookCanvasHelpForm extends Form
                      implements View, CommandListener {

    private ViewMaster viewMaster;
    
    public BookCanvasHelpForm() {
        super(L10n.getMessage("HELP_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);     
        showHelp();          
    }
    
    
    /**
     * Activate BookCanvasHelpForm
     */
    public void activate() {
    }
    
    
    /**
     * Deactivate BookCanvasHelpForm
     */
    public void deactivate() {
    }
    
    //license information for users
    private void showHelp() {
        
        StringItem stringItemHelp = 
                   new StringItem(L10n.getMessage("BOOK_CANVAS_HELP_FORM_TEXT_HEADER"),
                                  L10n.getMessage("BOOK_CANVAS_HELP_FORM_TEXT"));
        this.append(stringItemHelp); 
    }
    
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
        
          if (command == viewMaster.getBackCmd()) {
            Main.getInstance().switchDisplayable(null, Main.getInstance().getbookCanvas());
        }
         
      }    
    
    
    
}
