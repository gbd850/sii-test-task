package com.test.sii.model;

import com.test.sii.dto.DiscountMethod;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeMonetary extends PromoCode {

    private BigDecimal amount;


    public PromoCodeMonetary(String code, Date expirationDate, int maxUsages, BigDecimal amount, Currency currency) {
        super(null, code, expirationDate, maxUsages, 0, currency);
        this.amount = amount;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PromoCodeMonetary that = (PromoCodeMonetary) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public BigDecimal calculateDiscountPrice(Product product) {
        return product.getPrice().subtract(this.amount).max(BigDecimal.ZERO);
    }

    @Override
    public DiscountMethod getDiscountMethod() {
        return DiscountMethod.MONETARY;
    }
}
