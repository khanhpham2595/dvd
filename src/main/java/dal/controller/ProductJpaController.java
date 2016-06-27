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
import dal.Entity.Categories;
import dal.Entity.Supplier;
import dal.Entity.Producer;
import dal.Entity.Items;
import java.util.ArrayList;
import java.util.Collection;
import dal.Entity.Orderdetail;
import dal.Entity.Product;
import dal.controller.exceptions.IllegalOrphanException;
import dal.controller.exceptions.NonexistentEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author KhanhPham
 */
public class ProductJpaController implements Serializable {

    public ProductJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Product product) {
        if (product.getItemsCollection() == null) {
            product.setItemsCollection(new ArrayList<Items>());
        }
        if (product.getOrderdetailCollection() == null) {
            product.setOrderdetailCollection(new ArrayList<Orderdetail>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Categories categoryid = product.getCategoryid();
            if (categoryid != null) {
                categoryid = em.getReference(categoryid.getClass(), categoryid.getId());
                product.setCategoryid(categoryid);
            }
            Supplier supplierId = product.getSupplierId();
            if (supplierId != null) {
                supplierId = em.getReference(supplierId.getClass(), supplierId.getId());
                product.setSupplierId(supplierId);
            }
            Producer producerId = product.getProducerId();
            if (producerId != null) {
                producerId = em.getReference(producerId.getClass(), producerId.getId());
                product.setProducerId(producerId);
            }
            Collection<Items> attachedItemsCollection = new ArrayList<Items>();
            for (Items itemsCollectionItemsToAttach : product.getItemsCollection()) {
                itemsCollectionItemsToAttach = em.getReference(itemsCollectionItemsToAttach.getClass(), itemsCollectionItemsToAttach.getId());
                attachedItemsCollection.add(itemsCollectionItemsToAttach);
            }
            product.setItemsCollection(attachedItemsCollection);
            Collection<Orderdetail> attachedOrderdetailCollection = new ArrayList<Orderdetail>();
            for (Orderdetail orderdetailCollectionOrderdetailToAttach : product.getOrderdetailCollection()) {
                orderdetailCollectionOrderdetailToAttach = em.getReference(orderdetailCollectionOrderdetailToAttach.getClass(), orderdetailCollectionOrderdetailToAttach.getId());
                attachedOrderdetailCollection.add(orderdetailCollectionOrderdetailToAttach);
            }
            product.setOrderdetailCollection(attachedOrderdetailCollection);
            em.persist(product);
            if (categoryid != null) {
                categoryid.getProductCollection().add(product);
                categoryid = em.merge(categoryid);
            }
            if (supplierId != null) {
                supplierId.getProductCollection().add(product);
                supplierId = em.merge(supplierId);
            }
            if (producerId != null) {
                producerId.getProductCollection().add(product);
                producerId = em.merge(producerId);
            }
            for (Items itemsCollectionItems : product.getItemsCollection()) {
                Product oldProductIdOfItemsCollectionItems = itemsCollectionItems.getProductId();
                itemsCollectionItems.setProductId(product);
                itemsCollectionItems = em.merge(itemsCollectionItems);
                if (oldProductIdOfItemsCollectionItems != null) {
                    oldProductIdOfItemsCollectionItems.getItemsCollection().remove(itemsCollectionItems);
                    oldProductIdOfItemsCollectionItems = em.merge(oldProductIdOfItemsCollectionItems);
                }
            }
            for (Orderdetail orderdetailCollectionOrderdetail : product.getOrderdetailCollection()) {
                Product oldProductIdOfOrderdetailCollectionOrderdetail = orderdetailCollectionOrderdetail.getProductId();
                orderdetailCollectionOrderdetail.setProductId(product);
                orderdetailCollectionOrderdetail = em.merge(orderdetailCollectionOrderdetail);
                if (oldProductIdOfOrderdetailCollectionOrderdetail != null) {
                    oldProductIdOfOrderdetailCollectionOrderdetail.getOrderdetailCollection().remove(orderdetailCollectionOrderdetail);
                    oldProductIdOfOrderdetailCollectionOrderdetail = em.merge(oldProductIdOfOrderdetailCollectionOrderdetail);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Product product) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Product persistentProduct = em.find(Product.class, product.getId());
            Categories categoryidOld = persistentProduct.getCategoryid();
            Categories categoryidNew = product.getCategoryid();
            Supplier supplierIdOld = persistentProduct.getSupplierId();
            Supplier supplierIdNew = product.getSupplierId();
            Producer producerIdOld = persistentProduct.getProducerId();
            Producer producerIdNew = product.getProducerId();
            Collection<Items> itemsCollectionOld = persistentProduct.getItemsCollection();
            Collection<Items> itemsCollectionNew = product.getItemsCollection();
            Collection<Orderdetail> orderdetailCollectionOld = persistentProduct.getOrderdetailCollection();
            Collection<Orderdetail> orderdetailCollectionNew = product.getOrderdetailCollection();
            List<String> illegalOrphanMessages = null;
            for (Items itemsCollectionOldItems : itemsCollectionOld) {
                if (!itemsCollectionNew.contains(itemsCollectionOldItems)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Items " + itemsCollectionOldItems + " since its productId field is not nullable.");
                }
            }
            for (Orderdetail orderdetailCollectionOldOrderdetail : orderdetailCollectionOld) {
                if (!orderdetailCollectionNew.contains(orderdetailCollectionOldOrderdetail)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Orderdetail " + orderdetailCollectionOldOrderdetail + " since its productId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (categoryidNew != null) {
                categoryidNew = em.getReference(categoryidNew.getClass(), categoryidNew.getId());
                product.setCategoryid(categoryidNew);
            }
            if (supplierIdNew != null) {
                supplierIdNew = em.getReference(supplierIdNew.getClass(), supplierIdNew.getId());
                product.setSupplierId(supplierIdNew);
            }
            if (producerIdNew != null) {
                producerIdNew = em.getReference(producerIdNew.getClass(), producerIdNew.getId());
                product.setProducerId(producerIdNew);
            }
            Collection<Items> attachedItemsCollectionNew = new ArrayList<Items>();
            for (Items itemsCollectionNewItemsToAttach : itemsCollectionNew) {
                itemsCollectionNewItemsToAttach = em.getReference(itemsCollectionNewItemsToAttach.getClass(), itemsCollectionNewItemsToAttach.getId());
                attachedItemsCollectionNew.add(itemsCollectionNewItemsToAttach);
            }
            itemsCollectionNew = attachedItemsCollectionNew;
            product.setItemsCollection(itemsCollectionNew);
            Collection<Orderdetail> attachedOrderdetailCollectionNew = new ArrayList<Orderdetail>();
            for (Orderdetail orderdetailCollectionNewOrderdetailToAttach : orderdetailCollectionNew) {
                orderdetailCollectionNewOrderdetailToAttach = em.getReference(orderdetailCollectionNewOrderdetailToAttach.getClass(), orderdetailCollectionNewOrderdetailToAttach.getId());
                attachedOrderdetailCollectionNew.add(orderdetailCollectionNewOrderdetailToAttach);
            }
            orderdetailCollectionNew = attachedOrderdetailCollectionNew;
            product.setOrderdetailCollection(orderdetailCollectionNew);
            product = em.merge(product);
            if (categoryidOld != null && !categoryidOld.equals(categoryidNew)) {
                categoryidOld.getProductCollection().remove(product);
                categoryidOld = em.merge(categoryidOld);
            }
            if (categoryidNew != null && !categoryidNew.equals(categoryidOld)) {
                categoryidNew.getProductCollection().add(product);
                categoryidNew = em.merge(categoryidNew);
            }
            if (supplierIdOld != null && !supplierIdOld.equals(supplierIdNew)) {
                supplierIdOld.getProductCollection().remove(product);
                supplierIdOld = em.merge(supplierIdOld);
            }
            if (supplierIdNew != null && !supplierIdNew.equals(supplierIdOld)) {
                supplierIdNew.getProductCollection().add(product);
                supplierIdNew = em.merge(supplierIdNew);
            }
            if (producerIdOld != null && !producerIdOld.equals(producerIdNew)) {
                producerIdOld.getProductCollection().remove(product);
                producerIdOld = em.merge(producerIdOld);
            }
            if (producerIdNew != null && !producerIdNew.equals(producerIdOld)) {
                producerIdNew.getProductCollection().add(product);
                producerIdNew = em.merge(producerIdNew);
            }
            for (Items itemsCollectionNewItems : itemsCollectionNew) {
                if (!itemsCollectionOld.contains(itemsCollectionNewItems)) {
                    Product oldProductIdOfItemsCollectionNewItems = itemsCollectionNewItems.getProductId();
                    itemsCollectionNewItems.setProductId(product);
                    itemsCollectionNewItems = em.merge(itemsCollectionNewItems);
                    if (oldProductIdOfItemsCollectionNewItems != null && !oldProductIdOfItemsCollectionNewItems.equals(product)) {
                        oldProductIdOfItemsCollectionNewItems.getItemsCollection().remove(itemsCollectionNewItems);
                        oldProductIdOfItemsCollectionNewItems = em.merge(oldProductIdOfItemsCollectionNewItems);
                    }
                }
            }
            for (Orderdetail orderdetailCollectionNewOrderdetail : orderdetailCollectionNew) {
                if (!orderdetailCollectionOld.contains(orderdetailCollectionNewOrderdetail)) {
                    Product oldProductIdOfOrderdetailCollectionNewOrderdetail = orderdetailCollectionNewOrderdetail.getProductId();
                    orderdetailCollectionNewOrderdetail.setProductId(product);
                    orderdetailCollectionNewOrderdetail = em.merge(orderdetailCollectionNewOrderdetail);
                    if (oldProductIdOfOrderdetailCollectionNewOrderdetail != null && !oldProductIdOfOrderdetailCollectionNewOrderdetail.equals(product)) {
                        oldProductIdOfOrderdetailCollectionNewOrderdetail.getOrderdetailCollection().remove(orderdetailCollectionNewOrderdetail);
                        oldProductIdOfOrderdetailCollectionNewOrderdetail = em.merge(oldProductIdOfOrderdetailCollectionNewOrderdetail);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = product.getId();
                if (findProduct(id) == null) {
                    throw new NonexistentEntityException("The product with id " + id + " no longer exists.");
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
            Product product;
            try {
                product = em.getReference(Product.class, id);
                product.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The product with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Items> itemsCollectionOrphanCheck = product.getItemsCollection();
            for (Items itemsCollectionOrphanCheckItems : itemsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the Items " + itemsCollectionOrphanCheckItems + " in its itemsCollection field has a non-nullable productId field.");
            }
            Collection<Orderdetail> orderdetailCollectionOrphanCheck = product.getOrderdetailCollection();
            for (Orderdetail orderdetailCollectionOrphanCheckOrderdetail : orderdetailCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Product (" + product + ") cannot be destroyed since the Orderdetail " + orderdetailCollectionOrphanCheckOrderdetail + " in its orderdetailCollection field has a non-nullable productId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Categories categoryid = product.getCategoryid();
            if (categoryid != null) {
                categoryid.getProductCollection().remove(product);
                categoryid = em.merge(categoryid);
            }
            Supplier supplierId = product.getSupplierId();
            if (supplierId != null) {
                supplierId.getProductCollection().remove(product);
                supplierId = em.merge(supplierId);
            }
            Producer producerId = product.getProducerId();
            if (producerId != null) {
                producerId.getProductCollection().remove(product);
                producerId = em.merge(producerId);
            }
            em.remove(product);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Product> findProductEntities() {
        return findProductEntities(true, -1, -1);
    }

    public List<Product> findProductEntities(int maxResults, int firstResult) {
        return findProductEntities(false, maxResults, firstResult);
    }

    private List<Product> findProductEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Product.class));
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

    public Product findProduct(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public int getProductCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Product> rt = cq.from(Product.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
