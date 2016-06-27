/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package action;

import dal.Entity.Categories;
import dal.controller.CategoriesJpaController;
import dal.controller.JpaUtil;
import java.util.List;

/**
 *
 * @author tien.hd
 */
public class CategoriesAction {

    private List<Categories> categoriesList;

    public CategoriesAction() {
        CategoriesJpaController categoriesJpaController = JpaUtil.getCategoriesJpaController();
        categoriesList = categoriesJpaController.findCategoriesEntities();
    }

    public String home() {
        return "success";
    }

    public List<Categories> getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(List<Categories> categoriesList) {
        this.categoriesList = categoriesList;
    }

}
