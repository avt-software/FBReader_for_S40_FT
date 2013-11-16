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

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.*;
import org.fbreader.localization.L10n;
import org.fbreader.Main;

public class AboutViewForm extends Form
                      implements View, CommandListener,ItemCommandListener {

    private ViewMaster viewMaster;
    private StringItem stringItemSupportWebSite;
    private StringItem stringItemSupportEmailAddress;
    
    
    private final Alert alertNetworkError = new Alert(L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_NET_ERROR_TITLE"), 
            L10n.getMessage("NET_LIB_BOOK_INFO_FORM_ALERT_NET_ERROR_CONTENT"), null, null);
    
    
    public AboutViewForm() {
        super(L10n.getMessage("ABOUT_VIEW_FORM_TITLE"));
        viewMaster = ViewMaster.getInstance();
        this.addCommand(viewMaster.getBackCmd());
        setCommandListener(this);     
        showAbout();          
    }
    
    
    /**
     * Activate AboutViewForm and view form data
     */
    public void activate() {
        //hide CategoryBar
        viewMaster.setCategoryBarVisible(false);
    }
    
    
    /**
     * Empties the list items
     */
    public void deactivate() {
        //deleteAll();
    }
    
    
    //information for users
    private void showAbout() {
               
//--- 1 --- App name topic 
        StringItem stringItemMidletName = 
                   new StringItem(L10n.getMessage("MIDLET_NAME_TITLE"),
                                  Main.getInstance().getName() + " for S40FT");

        
        stringItemMidletName.setPreferredSize(stringItemMidletName.getPreferredWidth()-28, 
                                          stringItemMidletName.getPreferredHeight());
        stringItemMidletName.setLayout(Item.LAYOUT_CENTER);
        this.append(stringItemMidletName); 
        
//--- 2 --- Version topic 
        StringItem stringItemMidletVersion = 
                   new StringItem(L10n.getMessage("MIDLET_VERSION_TITLE"),
                                  Main.getInstance().getVersion());

        
        stringItemMidletVersion.setPreferredSize(stringItemMidletVersion.getPreferredWidth()-28, 
                                          stringItemMidletVersion.getPreferredHeight());
        stringItemMidletVersion.setLayout(Item.LAYOUT_CENTER);
        this.append(stringItemMidletVersion);         
        
//--- 3 --- Publisher
        StringItem stringItemMidletVendor = 
                   new StringItem(L10n.getMessage("MIDLET_VENDOR_TITLE"),
                                    Main.getInstance().getVendor());
       
        stringItemMidletVendor.setPreferredSize(stringItemMidletVendor.getPreferredWidth()-28, 
                                          stringItemMidletVendor.getPreferredHeight());
        stringItemMidletVendor.setLayout(Item.LAYOUT_CENTER);
        this.append(stringItemMidletVendor);
        
//--- 4 --- Support website 
        stringItemSupportWebSite = 
                   new StringItem(L10n.getMessage("SUPPORT_WEB_SITE_TITLE"),
                                    "http://software.avt.dn.ua/", Item.HYPERLINK);
        
        stringItemSupportWebSite.addCommand(viewMaster.getOkCmd());
        stringItemSupportWebSite.setDefaultCommand(viewMaster.getOkCmd());
        stringItemSupportWebSite.setItemCommandListener(this);
        
       
        stringItemSupportWebSite.setPreferredSize(stringItemSupportWebSite.getPreferredWidth()-28, 
                                          stringItemSupportWebSite.getPreferredHeight());
        stringItemSupportWebSite.setLayout(Item.LAYOUT_CENTER);
        this.append(stringItemSupportWebSite);
        
        
//--- 5 --- Support Email Address
        stringItemSupportEmailAddress = 
                   new StringItem(L10n.getMessage("SUPPORT_EMAIL_ADDRESS_TITLE"),
                                    "support@software.avt.dn.ua");
       
        stringItemSupportEmailAddress.setPreferredSize(stringItemSupportEmailAddress.getPreferredWidth()-28, 
                                          stringItemSupportEmailAddress.getPreferredHeight());
        stringItemSupportEmailAddress.setLayout(Item.LAYOUT_CENTER);
        this.append(stringItemSupportEmailAddress);
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
        else if (displayable==alertNetworkError && command==viewMaster.getDismissCmd()) {
            Main.getInstance().switchDisplayable(null,this); 
        }          
         
      }
      
      
      
      
       /**
         * Handles the select command related to a list item
         * @param c
         * @param item
       */
       public void commandAction(Command c, Item item) {
           if (item==stringItemSupportWebSite) {
               
               try {
                   Main.getInstance().platformRequest(stringItemSupportWebSite.getText());
               }
               catch (ConnectionNotFoundException cnfe) {
                   System.out.println("AboutViewForm::commandAction()->ConnectionNotFoundException: "+cnfe.getMessage()); 
                   showNetworkErrorAlert();
               }
           }
       }
       
       
    public void showNetworkErrorAlert() {
        alertNetworkError.setTimeout(Alert.FOREVER); 
        alertNetworkError.addCommand(viewMaster.getDismissCmd());
        alertNetworkError.setCommandListener(this);
        Main.getInstance().switchDisplayable(alertNetworkError,this);
    }        
    
    
    
}
