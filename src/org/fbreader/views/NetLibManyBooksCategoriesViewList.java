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

import java.io.IOException;
import javax.microedition.lcdui.*;
import org.fbreader.Main;
import org.fbreader.utils.ImageLoader;
import org.fbreader.utils.mylib.MyLibUtils;
import org.fbreader.views.model.parser.ManyBooksCategory;
import org.fbreader.views.model.parser.ManyBooksByCategoryModel;
import org.tantalum.Worker;
import org.tantalum.util.StringUtils;

public final class NetLibManyBooksCategoriesViewList 
                    extends List 
                    implements View, CommandListener {

    private Image imageListIcon;
    
    private final String MANYBOOKS_ICON = 
            "/icons/netlib_manybooks_38x38.png";
    private ViewMaster viewMaster;
    private MyLibUtils myLibUtils;
    public static String MANYBOOKS_CATEGORIES_XML_URL = "/manybooks/manybooks_categories.xml";
    private static Command selectManyBooksCategoryItemCmd;
    private static ManyBooksCategory selectedManyBooksCategory;
    private static String manyBooksCategoryListTitle;
    private static int manyBooksCategoryListType;
    private final ManyBooksByCategoryModel manyBooksByCategoryModel = new ManyBooksByCategoryModel(100);
    

    public NetLibManyBooksCategoriesViewList(String manyBooksCategoryListTitle, int manyBooksCategoryListType) {
        super(manyBooksCategoryListTitle, manyBooksCategoryListType); 
        
        this.manyBooksCategoryListTitle=manyBooksCategoryListTitle;
        this.manyBooksCategoryListType=manyBooksCategoryListType;
        this.setTitle(this.manyBooksCategoryListTitle);
         viewMaster = ViewMaster.getInstance();
         myLibUtils=MyLibUtils.getInstance();
         selectManyBooksCategoryItemCmd=viewMaster.getSelectListItemCmd();
        try  {
               imageListIcon=ImageLoader.getInstance().loadImage(MANYBOOKS_ICON, null);
        } 
        catch(IOException e) {
        }
        this.setFitPolicy(List.TEXT_WRAP_DEFAULT);
        this.addCommand(viewMaster.getBackCmd());
        this.setSelectCommand(selectManyBooksCategoryItemCmd);
        this.setCommandListener(this);
        parseManyBooksByCategory();
        for (int i = 0; i < manyBooksByCategoryModel.size(); i++) {
            ManyBooksCategory currentManyBooksCategory=manyBooksByCategoryModel.getManyBooksCategoryByIdAt(i);
            this.append(currentManyBooksCategory.getCategoryName(),imageListIcon);
        }
    }
    
    /**
     * parse parseManyBooksByCategory on the following steps: 
     * 1. load the manybooks_categories.xml  from /res Folder 
     *    to the byte[] array (manyBooksByCategoryAsByteArray) 
     * 2. parsing XML Data from manyBooksByCategoryAsByteArray 
     *    and return result to manyBooksByCategoryModel
     * 
     * @return Object (manyBooksByCategoryModel or null)
     */   
    private Object parseManyBooksByCategory(){
          
        try {
            byte[] manyBooksByCategoryAsByteArray;
            manyBooksByCategoryAsByteArray = StringUtils.readBytesFromJAR(MANYBOOKS_CATEGORIES_XML_URL);
            manyBooksByCategoryModel.removeAllElements();
            synchronized (Worker.LARGE_MEMORY_MUTEX) {
                manyBooksByCategoryModel.setXML(manyBooksByCategoryAsByteArray);
            }
            return manyBooksByCategoryModel;
        } catch (Exception e) {
            //#debug
            Main.LOGGER.logError("Error in parsing XML"+manyBooksByCategoryModel.toString());
            //#debug
            Main.LOGGER.logError("Exception: "+e.getMessage());
            return null;
        }          
   }      

    
     /**
     * Activate NetLibManyBooksCategoriesViewList
     */
    public void activate() {
        //hide CategoryBar 
        viewMaster.setCategoryBarVisible(false);
    }
    

    /**
     * Deactivate NetLibManyBooksCategoriesViewList
     * 
     */
    public void deactivate() {
    }
    

    public void commandAction(final Command command, final Displayable displayable) {
        
        if (command == viewMaster.getBackCmd()){
            viewMaster.backView();
        }
        else if (command == selectManyBooksCategoryItemCmd) {
//--- 1 --- choice selectManyBooksCategoryItemCmd
            selectedManyBooksCategory = manyBooksByCategoryModel.getManyBooksCategoryByIdAt(this.getSelectedIndex());
            viewMaster.setSelectedManyBooksCategoryListItem(selectedManyBooksCategory);
            viewMaster.setFirsLoadManyBooksNewTitlesByCategoryList(true);
            viewMaster.getNetLibManyBooksNewTitlesByCategoryViewList();
            viewMaster.openView(ViewMaster.VIEW_MANYBOOKS_NEW_TITLES_BY_CATEGORY_LIST);
        }        
    }
}
