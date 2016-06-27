/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import dal.Entity.User;
import dal.Entity.Orderdetail;
import dal.Entity.Orders;
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
public class OrdersJpaController implements Serializable {

    public OrdersJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Orders orders) {
        if (orders.getOrderdetailCollection() == null) {
            orders.setOrderdetailCollection(new ArrayList<Orderdetail>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            User userId = orders.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                orders.setUserId(userId);
            }
            Collection<Orderdetail> attachedOrderdetailCollection = new ArrayList<Orderdetail>();
            for (Orderdetail orderdetailCollectionOrderdetailToAttach : orders.getOrderdetailCollection()) {
                orderdetailCollectionOrderdetailToAttach = em.getReference(orderdetailCollectionOrderdetailToAttach.getClass(), orderdetailCollectionOrderdetailToAttach.getId());
                attachedOrderdetailCollection.add(orderdetailCollectionOrderdetailToAttach);
            }
            orders.setOrderdetailCollection(attachedOrderdetailCollection);
            em.persist(orders);
            if (userId != null) {
                userId.getOrdersCollection().add(orders);
                userId = em.merge(userId);
            }
            for (Orderdetail orderdetailCollectionOrderdetail : orders.getOrderdetailCollection()) {
                Orders oldOrderIdOfOrderdetailCollectionOrderdetail = orderdetailCollectionOrderdetail.getOrderId();
                orderdetailCollectionOrderdetail.setOrderId(orders);
                orderdetailCollectionOrderdetail = em.merge(orderdetailCollectionOrderdetail);
                if (oldOrderIdOfOrderdetailCollectionOrderdetail != null) {
                    oldOrderIdOfOrderdetailCollectionOrderdetail.getOrderdetailCollection().remove(orderdetailCollectionOrderdetail);
                    oldOrderIdOfOrderdetailCollectionOrderdetail = em.merge(oldOrderIdOfOrderdetailCollectionOrderdetail);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Orders orders) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Orders persistentOrders = em.find(Orders.class, orders.getId());
            User userIdOld = persistentOrders.getUserId();
            User userIdNew = orders.getUserId();
            Collection<Orderdetail> orderdetailCollectionOld = persistentOrders.getOrderdetailCollection();
            Collection<Orderdetail> orderdetailCollectionNew = orders.getOrderdetailCollection();
            List<String> illegalOrphanMessages = null;
            for (Orderdetail orderdetailCollectionOldOrderdetail : orderdetailCollectionOld) {
                if (!orderdetailCollectionNew.contains(orderdetailCollectionOldOrderdetail)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Orderdetail " + orderdetailCollectionOldOrderdetail + " since its orderId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                orders.setUserId(userIdNew);
            }
            Collection<Orderdetail> attachedOrderdetailCollectionNew = new ArrayList<Orderdetail>();
            for (Orderdetail orderdetailCollectionNewOrderdetailToAttach : orderdetailCollectionNew) {
                orderdetailCollectionNewOrderdetailToAttach = em.getReference(orderdetailCollectionNewOrderdetailToAttach.getClass(), orderdetailCollectionNewOrderdetailToAttach.getId());
                attachedOrderdetailCollectionNew.add(orderdetailCollectionNewOrderdetailToAttach);
            }
            orderdetailCollectionNew = attachedOrderdetailCollectionNew;
            orders.setOrderdetailCollection(orderdetailCollectionNew);
            orders = em.merge(orders);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getOrdersCollection().remove(orders);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getOrdersCollection().add(orders);
                userIdNew = em.merge(userIdNew);
            }
            for (Orderdetail orderdetailCollectionNewOrderdetail : orderdetailCollectionNew) {
                if (!orderdetailCollectionOld.contains(orderdetailCollectionNewOrderdetail)) {
                    Orders oldOrderIdOfOrderdetailCollectionNewOrderdetail = orderdetailCollectionNewOrderdetail.getOrderId();
                    orderdetailCollectionNewOrderdetail.setOrderId(orders);
                    orderdetailCollectionNewOrderdetail = em.merge(orderdetailCollectionNewOrderdetail);
                    if (oldOrderIdOfOrderdetailCollectionNewOrderdetail != null && !oldOrderIdOfOrderdetailCollectionNewOrderdetail.equals(orders)) {
                        oldOrderIdOfOrderdetailCollectionNewOrderdetail.getOrderdetailCollection().remove(orderdetailCollectionNewOrderdetail);
                        oldOrderIdOfOrderdetailCollectionNewOrderdetail = em.merge(oldOrderIdOfOrderdetailCollectionNewOrderdetail);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = orders.getId();
                if (findOrders(id) == null) {
                    throw new NonexistentEntityException("The orders with id " + id + " no longer exists.");
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
            Orders orders;
            try {
                orders = em.getReference(Orders.class, id);
                orders.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The orders with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Orderdetail> orderdetailCollectionOrphanCheck = orders.getOrderdetailCollection();
            for (Orderdetail orderdetailCollectionOrphanCheckOrderdetail : orderdetailCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Orders (" + orders + ") cannot be destroyed since the Orderdetail " + orderdetailCollectionOrphanCheckOrderdetail + " in its orderdetailCollection field has a non-nullable orderId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            User userId = orders.getUserId();
            if (userId != null) {
                userId.getOrdersCollection().remove(orders);
                userId = em.merge(userId);
            }
            em.remove(orders);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Orders> findOrdersEntities() {
        return findOrdersEntities(true, -1, -1);
    }

    public List<Orders> findOrdersEntities(int maxResults, int firstResult) {
        return findOrdersEntities(false, maxResults, firstResult);
    }

    private List<Orders> findOrdersEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Orders.class));
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

    public Orders findOrders(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Orders.class, id);
        } finally {
            em.close();
        }
    }

    public int getOrdersCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Orders> rt = cq.from(Orders.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
