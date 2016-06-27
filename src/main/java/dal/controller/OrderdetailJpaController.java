/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.controller;

import dal.Entity.Orderdetail;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dal.Entity.Orders;
import dal.Entity.Product;
import dal.controller.exceptions.NonexistentEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author KhanhPham
 */
public class OrderdetailJpaController implements Serializable {

    public OrderdetailJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Orderdetail orderdetail) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Orders orderId = orderdetail.getOrderId();
            if (orderId != null) {
                orderId = em.getReference(orderId.getClass(), orderId.getId());
                orderdetail.setOrderId(orderId);
            }
            Product productId = orderdetail.getProductId();
            if (productId != null) {
                productId = em.getReference(productId.getClass(), productId.getId());
                orderdetail.setProductId(productId);
            }
            em.persist(orderdetail);
            if (orderId != null) {
                orderId.getOrderdetailCollection().add(orderdetail);
                orderId = em.merge(orderId);
            }
            if (productId != null) {
                productId.getOrderdetailCollection().add(orderdetail);
                productId = em.merge(productId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Orderdetail orderdetail) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Orderdetail persistentOrderdetail = em.find(Orderdetail.class, orderdetail.getId());
            Orders orderIdOld = persistentOrderdetail.getOrderId();
            Orders orderIdNew = orderdetail.getOrderId();
            Product productIdOld = persistentOrderdetail.getProductId();
            Product productIdNew = orderdetail.getProductId();
            if (orderIdNew != null) {
                orderIdNew = em.getReference(orderIdNew.getClass(), orderIdNew.getId());
                orderdetail.setOrderId(orderIdNew);
            }
            if (productIdNew != null) {
                productIdNew = em.getReference(productIdNew.getClass(), productIdNew.getId());
                orderdetail.setProductId(productIdNew);
            }
            orderdetail = em.merge(orderdetail);
            if (orderIdOld != null && !orderIdOld.equals(orderIdNew)) {
                orderIdOld.getOrderdetailCollection().remove(orderdetail);
                orderIdOld = em.merge(orderIdOld);
            }
            if (orderIdNew != null && !orderIdNew.equals(orderIdOld)) {
                orderIdNew.getOrderdetailCollection().add(orderdetail);
                orderIdNew = em.merge(orderIdNew);
            }
            if (productIdOld != null && !productIdOld.equals(productIdNew)) {
                productIdOld.getOrderdetailCollection().remove(orderdetail);
                productIdOld = em.merge(productIdOld);
            }
            if (productIdNew != null && !productIdNew.equals(productIdOld)) {
                productIdNew.getOrderdetailCollection().add(orderdetail);
                productIdNew = em.merge(productIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = orderdetail.getId();
                if (findOrderdetail(id) == null) {
                    throw new NonexistentEntityException("The orderdetail with id " + id + " no longer exists.");
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
            Orderdetail orderdetail;
            try {
                orderdetail = em.getReference(Orderdetail.class, id);
                orderdetail.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orderdetail with id " + id + " no longer exists.", enfe);
            }
            Orders orderId = orderdetail.getOrderId();
            if (orderId != null) {
                orderId.getOrderdetailCollection().remove(orderdetail);
                orderId = em.merge(orderId);
            }
            Product productId = orderdetail.getProductId();
            if (productId != null) {
                productId.getOrderdetailCollection().remove(orderdetail);
                productId = em.merge(productId);
            }
            em.remove(orderdetail);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Orderdetail> findOrderdetailEntities() {
        return findOrderdetailEntities(true, -1, -1);
    }

    public List<Orderdetail> findOrderdetailEntities(int maxResults, int firstResult) {
        return findOrderdetailEntities(false, maxResults, firstResult);
    }

    private List<Orderdetail> findOrderdetailEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Orderdetail.class));
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

    public Orderdetail findOrderdetail(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Orderdetail.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrderdetailCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Orderdetail> rt = cq.from(Orderdetail.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
