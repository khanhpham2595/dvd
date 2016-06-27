/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.controller;

import dal.Entity.Categories;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dal.Entity.Product;
import dal.controller.exceptions.IllegalOrphanException;
import dal.controller.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author KhanhPham
 */
public class CategoriesJpaController implements Serializable {

    public CategoriesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Categories categories) {
        if (categories.getProductCollection() == null) {
            categories.setProductCollection(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Product> attachedProductCollection = new ArrayList<Product>();
            for (Product productCollectionProductToAttach : categories.getProductCollection()) {
                productCollectionProductToAttach = em.getReference(productCollectionProductToAttach.getClass(), productCollectionProductToAttach.getId());
                attachedProductCollection.add(productCollectionProductToAttach);
            }
            categories.setProductCollection(attachedProductCollection);
            em.persist(categories);
            for (Product productCollectionProduct : categories.getProductCollection()) {
                Categories oldCategoryidOfProductCollectionProduct = productCollectionProduct.getCategoryid();
                productCollectionProduct.setCategoryid(categories);
                productCollectionProduct = em.merge(productCollectionProduct);
                if (oldCategoryidOfProductCollectionProduct != null) {
                    oldCategoryidOfProductCollectionProduct.getProductCollection().remove(productCollectionProduct);
                    oldCategoryidOfProductCollectionProduct = em.merge(oldCategoryidOfProductCollectionProduct);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Categories categories) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categories persistentCategories = em.find(Categories.class, categories.getId());
            Collection<Product> productCollectionOld = persistentCategories.getProductCollection();
            Collection<Product> productCollectionNew = categories.getProductCollection();
            List<String> illegalOrphanMessages = null;
            for (Product productCollectionOldProduct : productCollectionOld) {
                if (!productCollectionNew.contains(productCollectionOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productCollectionOldProduct + " since its categoryid field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Product> attachedProductCollectionNew = new ArrayList<Product>();
            for (Product productCollectionNewProductToAttach : productCollectionNew) {
                productCollectionNewProductToAttach = em.getReference(productCollectionNewProductToAttach.getClass(), productCollectionNewProductToAttach.getId());
                attachedProductCollectionNew.add(productCollectionNewProductToAttach);
            }
            productCollectionNew = attachedProductCollectionNew;
            categories.setProductCollection(productCollectionNew);
            categories = em.merge(categories);
            for (Product productCollectionNewProduct : productCollectionNew) {
                if (!productCollectionOld.contains(productCollectionNewProduct)) {
                    Categories oldCategoryidOfProductCollectionNewProduct = productCollectionNewProduct.getCategoryid();
                    productCollectionNewProduct.setCategoryid(categories);
                    productCollectionNewProduct = em.merge(productCollectionNewProduct);
                    if (oldCategoryidOfProductCollectionNewProduct != null && !oldCategoryidOfProductCollectionNewProduct.equals(categories)) {
                        oldCategoryidOfProductCollectionNewProduct.getProductCollection().remove(productCollectionNewProduct);
                        oldCategoryidOfProductCollectionNewProduct = em.merge(oldCategoryidOfProductCollectionNewProduct);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = categories.getId();
                if (findCategories(id) == null) {
                    throw new NonexistentEntityException("The categories with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categories categories;
            try {
                categories = em.getReference(Categories.class, id);
                categories.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The categories with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Product> productCollectionOrphanCheck = categories.getProductCollection();
            for (Product productCollectionOrphanCheckProduct : productCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Categories (" + categories + ") cannot be destroyed since the Product " + productCollectionOrphanCheckProduct + " in its productCollection field has a non-nullable categoryid field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(categories);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Categories> findCategoriesEntities() {
        return findCategoriesEntities(true, -1, -1);
    }

    public List<Categories> findCategoriesEntities(int maxResults, int firstResult) {
        return findCategoriesEntities(false, maxResults, firstResult);
    }

    private List<Categories> findCategoriesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Categories.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Categories findCategories(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Categories.class, id);
        } finally {
            em.close();
        }
    }

    public int getCategoriesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Categories> rt = cq.from(Categories.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
