package com.test.sii.model;

import com.test.sii.dto.ProductUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            nullable = false,
            unique = true
    )
    private String name;

    private String description;

    private BigDecimal price;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            name = "currency_id",
            referencedColumnName = "id"
    )
    private Currency currency;

    public final void updateFieldsByRequest(ProductUpdateRequest productRequest) {

        this.name = Objects.requireNonNullElse(productRequest.name(), this.name);
        this.description = Objects.requireNonNullElse(productRequest.description(), this.description);
        this.price = Objects.requireNonNullElse(productRequest.price(), this.price);
        this.currency = Objects.requireNonNullElse(productRequest.currency(), this.currency);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product product = (Product) o;
        return getId() != null && Objects.equals(getId(), product.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}