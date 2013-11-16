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

/*
 * Help and Information view. 
 * Class to display a variety of textual information.
 */
public class UniversalHelpViewForm extends Form
                      implements View, CommandListener {

    private ViewMaster viewMaster;
    private String helpTextHeader;
    private String helpTextContent;
    private boolean showCategoryBar;
    
    public UniversalHelpViewForm(String formTitle, String helpTextHeader, String helpTextContent, boolean showCategoryBar) {
        super(formTitle);
        
        this.helpTextHeader=helpTextHeader;
        this.helpTextContent=helpTextContent;
        this.showCategoryBar=showCategoryBar;
        
        viewMaster = ViewMaster.getInstance();
    }
    
    
    /**
     * Activate UniversalHelpViewForm and view form data
     */
    public void activate() {
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
        
        //Runs the garbage collector
        System.gc();           
        
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);     

         showHelp();      
    }
    
    
    /**
     * Deactivate UniversalHelpViewForm
     */
    public void deactivate() {
        deleteAll();
        //show CategoryBar
        //viewMaster.setCategoryBarVisible(true);
    }
    
    
    //help information for users
    public void showHelp() {
        
        StringItem stringItemHelp = 
                   new StringItem(this.helpTextHeader,this.helpTextContent);
        this.append(stringItemHelp); 
       
    }
    
    
    /**
     * Handles the select command related to a Displayable
     * @param command
     * @param displayable The Displayable on which this event has occurred
     */
      public void commandAction(Command command, Displayable displayable) {
        
          if (command == viewMaster.getBackCmd()) {
            //show or hide CategoryBar when returning to the previous view
            viewMaster.setCategoryBarVisible(this.showCategoryBar);
            viewMaster.backView();
        }
         
      }    
    
    
    
}
