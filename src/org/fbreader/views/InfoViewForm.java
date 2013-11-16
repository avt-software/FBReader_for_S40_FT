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

public class InfoViewForm extends Form
                      implements View, CommandListener {

    private ViewMaster viewMaster;
    
    //commands for InfoViewForm screen
    private final Command aboutCmd = new Command(L10n.getMessage("ABOUT_COMMAND"), 
                                                Command.SCREEN, 1);
    private final Command licenseCmd = new Command(L10n.getMessage("LICENSE_COMMAND"), 
                                                Command.SCREEN, 1);

    
    public InfoViewForm() {
        super(L10n.getMessage("INFO_VIEW_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        addCommand(aboutCmd);
        addCommand(licenseCmd);
        setCommandListener(this);
        showInfo();         
    }
    
    
    /**
     * Activate InfoViewForm
     */
    public void activate() {
    }
    
    
    /**
     * Deactivate InfoViewForm
     */
    public void deactivate() {
    }
    
    
    //information for users
    private void showInfo() {
        final StringItem stringItemInfoStr = 
                new StringItem(L10n.getMessage("FBREADER_INFO_TITLE"), L10n.getMessage("FBREADER_INFO")); 
        append(stringItemInfoStr);
    }
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
          if (command == aboutCmd) {
              viewMaster.getAboutViewForm();
              viewMaster.openView(ViewMaster.VIEW_ABOUT_FORM);
          }
          else if (command == licenseCmd) {
              viewMaster.getLicenseViewForm();
              viewMaster.openView(ViewMaster.VIEW_LICENSE_FORM);
          }
      }    
    
    
    
}
