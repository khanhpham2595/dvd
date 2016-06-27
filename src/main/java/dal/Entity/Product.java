/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dal.Entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author KhanhPham
 */
@Entity
@Table(name = "product")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"),
    @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.id = :id"),
    @NamedQuery(name = "Product.findByName", query = "SELECT p FROM Product p WHERE p.name = :name"),
    @NamedQuery(name = "Product.findByPrice", query = "SELECT p FROM Product p WHERE p.price = :price"),
    @NamedQuery(name = "Product.findByPublishDate", query = "SELECT p FROM Product p WHERE p.publishDate = :publishDate"),
    @NamedQuery(name = "Product.findByArtistId", query = "SELECT p FROM Product p WHERE p.artistId = :artistId"),
    @NamedQuery(name = "Product.findByImage", query = "SELECT p FROM Product p WHERE p.image = :image")})
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "price")
    private float price;
    @Basic(optional = false)
    @NotNull
    @Column(name = "publishDate")
    @Temporal(TemporalType.DATE)
    private Date publishDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "artistId")
    private int artistId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "image")
    private String image;
    @JoinColumn(name = "categoryid", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Categories categoryid;
    @JoinColumn(name = "supplierId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Supplier supplierId;
    @JoinColumn(name = "producerId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Producer producerId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private Collection<Items> itemsCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productId")
    private Collection<Orderdetail> orderdetailCollection;

    public Product() {
    }

    public Product(Integer id) {
        this.id = id;
    }

    public Product(Integer id, String name, float price, Date publishDate, int artistId, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.publishDate = publishDate;
        this.artistId = artistId;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Categories getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(Categories categoryid) {
        this.categoryid = categoryid;
    }

    public Supplier getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Supplier supplierId) {
        this.supplierId = supplierId;
    }

    public Producer getProducerId() {
        return producerId;
    }

    public void setProducerId(Producer producerId) {
        this.producerId = producerId;
    }

    @XmlTransient
    public Collection<Items> getItemsCollection() {
        return itemsCollection;
    }

    public void setItemsCollection(Collection<Items> itemsCollection) {
        this.itemsCollection = itemsCollection;
    }

    @XmlTransient
    public Collection<Orderdetail> getOrderdetailCollection() {
        return orderdetailCollection;
    }

    public void setOrderdetailCollection(Collection<Orderdetail> orderdetailCollection) {
        this.orderdetailCollection = orderdetailCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Product)) {
            return false;
        }
        Product other = (Product) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entity.Product[ id=" + id + " ]";
    }
    
}
