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
import org.fbreader.localization.L10n;

public class LicenseViewForm extends Form
                      implements View, CommandListener {

    private ViewMaster viewMaster;
    
    public LicenseViewForm() {
        super(L10n.getMessage("LICENSE_VIEW_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);     
        showLicense();           
    }
    
    
    /**
     * Activate LicenseViewForm
     */
    public void activate() {
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
    }
    
    
    /**
     * Deactivate LicenseViewForm
     */
    public void deactivate() {
    }
    
    
    //license information for users
    private void showLicense() {
        
        StringItem stringItemLicense = 
                   new StringItem(L10n.getMessage("LICENSE_TEXT_HEADER"),
                                  L10n.getMessage("LICENSE_TEXT"));
        this.append(stringItemLicense); 
    }
    
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
        
          if (command == viewMaster.getBackCmd()) {
            //show CategoryBar
            viewMaster.setCategoryBarVisible(true);
            viewMaster.backView();
        }
         
      }    
    
    
    
}
