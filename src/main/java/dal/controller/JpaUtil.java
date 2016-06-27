/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dal.controller;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author tien.hd
 */
public class JpaUtil {
    private static CategoriesJpaController categoriesJpaController;

    public static CategoriesJpaController getCategoriesJpaController() {
        return categoriesJpaController;
    }

    public static void setCategoriesJpaController(CategoriesJpaController categoriesJpaController) {
        JpaUtil.categoriesJpaController = categoriesJpaController;
    }

    static {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_dvdProject_war_1.0-SNAPSHOTPU");
        categoriesJpaController = new CategoriesJpaController(emf);
    }
}
