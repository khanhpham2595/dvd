/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.controller;

import dal.Entity.Producer;
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
public class ProducerJpaController implements Serializable {

    public ProducerJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Producer producer) {
        if (producer.getProductCollection() == null) {
            producer.setProductCollection(new ArrayList<Product>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Product> attachedProductCollection = new ArrayList<Product>();
            for (Product productCollectionProductToAttach : producer.getProductCollection()) {
                productCollectionProductToAttach = em.getReference(productCollectionProductToAttach.getClass(), productCollectionProductToAttach.getId());
                attachedProductCollection.add(productCollectionProductToAttach);
            }
            producer.setProductCollection(attachedProductCollection);
            em.persist(producer);
            for (Product productCollectionProduct : producer.getProductCollection()) {
                Producer oldProducerIdOfProductCollectionProduct = productCollectionProduct.getProducerId();
                productCollectionProduct.setProducerId(producer);
                productCollectionProduct = em.merge(productCollectionProduct);
                if (oldProducerIdOfProductCollectionProduct != null) {
                    oldProducerIdOfProductCollectionProduct.getProductCollection().remove(productCollectionProduct);
                    oldProducerIdOfProductCollectionProduct = em.merge(oldProducerIdOfProductCollectionProduct);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Producer producer) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Producer persistentProducer = em.find(Producer.class, producer.getId());
            Collection<Product> productCollectionOld = persistentProducer.getProductCollection();
            Collection<Product> productCollectionNew = producer.getProductCollection();
            List<String> illegalOrphanMessages = null;
            for (Product productCollectionOldProduct : productCollectionOld) {
                if (!productCollectionNew.contains(productCollectionOldProduct)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Product " + productCollectionOldProduct + " since its producerId field is not nullable.");
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
            producer.setProductCollection(productCollectionNew);
            producer = em.merge(producer);
            for (Product productCollectionNewProduct : productCollectionNew) {
                if (!productCollectionOld.contains(productCollectionNewProduct)) {
                    Producer oldProducerIdOfProductCollectionNewProduct = productCollectionNewProduct.getProducerId();
                    productCollectionNewProduct.setProducerId(producer);
                    productCollectionNewProduct = em.merge(productCollectionNewProduct);
                    if (oldProducerIdOfProductCollectionNewProduct != null && !oldProducerIdOfProductCollectionNewProduct.equals(producer)) {
                        oldProducerIdOfProductCollectionNewProduct.getProductCollection().remove(productCollectionNewProduct);
                        oldProducerIdOfProductCollectionNewProduct = em.merge(oldProducerIdOfProductCollectionNewProduct);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = producer.getId();
                if (findProducer(id) == null) {
                    throw new NonexistentEntityException("The producer with id " + id + " no longer exists.");
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
            Producer producer;
            try {
                producer = em.getReference(Producer.class, id);
                producer.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The producer with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Product> productCollectionOrphanCheck = producer.getProductCollection();
            for (Product productCollectionOrphanCheckProduct : productCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Producer (" + producer + ") cannot be destroyed since the Product " + productCollectionOrphanCheckProduct + " in its productCollection field has a non-nullable producerId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(producer);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Producer> findProducerEntities() {
        return findProducerEntities(true, -1, -1);
    }

    public List<Producer> findProducerEntities(int maxResults, int firstResult) {
        return findProducerEntities(false, maxResults, firstResult);
    }

    private List<Producer> findProducerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Producer.class));
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

    public Producer findProducer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Producer.class, id);
        } finally {
            em.close();
        }
    }

    public int getProducerCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Producer> rt = cq.from(Producer.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
