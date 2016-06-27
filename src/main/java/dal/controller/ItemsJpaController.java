/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.controller;

import dal.Entity.Items;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dal.Entity.Product;
import dal.controller.exceptions.NonexistentEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author KhanhPham
 */
public class ItemsJpaController implements Serializable {

    public ItemsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Items items) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Product productId = items.getProductId();
            if (productId != null) {
                productId = em.getReference(productId.getClass(), productId.getId());
                items.setProductId(productId);
            }
            em.persist(items);
            if (productId != null) {
                productId.getItemsCollection().add(items);
                productId = em.merge(productId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Items items) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Items persistentItems = em.find(Items.class, items.getId());
            Product productIdOld = persistentItems.getProductId();
            Product productIdNew = items.getProductId();
            if (productIdNew != null) {
                productIdNew = em.getReference(productIdNew.getClass(), productIdNew.getId());
                items.setProductId(productIdNew);
            }
            items = em.merge(items);
            if (productIdOld != null && !productIdOld.equals(productIdNew)) {
                productIdOld.getItemsCollection().remove(items);
                productIdOld = em.merge(productIdOld);
            }
            if (productIdNew != null && !productIdNew.equals(productIdOld)) {
                productIdNew.getItemsCollection().add(items);
                productIdNew = em.merge(productIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = items.getId();
                if (findItems(id) == null) {
                    throw new NonexistentEntityException("The items with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Items items;
            try {
                items = em.getReference(Items.class, id);
                items.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The items with id " + id + " no longer exists.", enfe);
            }
            Product productId = items.getProductId();
            if (productId != null) {
                productId.getItemsCollection().remove(items);
                productId = em.merge(productId);
            }
            em.remove(items);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Items> findItemsEntities() {
        return findItemsEntities(true, -1, -1);
    }

    public List<Items> findItemsEntities(int maxResults, int firstResult) {
        return findItemsEntities(false, maxResults, firstResult);
    }

    private List<Items> findItemsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Items.class));
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

    public Items findItems(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Items.class, id);
        } finally {
            em.close();
        }
    }

    public int getItemsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Items> rt = cq.from(Items.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
