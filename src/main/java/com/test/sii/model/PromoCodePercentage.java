package com.test.sii.model;

import com.test.sii.dto.DiscountMethod;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodePercentage extends PromoCode {

    private BigDecimal amount;

    public PromoCodePercentage(String code, Date expirationDate, int maxUsages, BigDecimal amount, Currency currency) {
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
        PromoCodePercentage that = (PromoCodePercentage) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public BigDecimal calculateDiscountPrice(Product product) {
        return product.getPrice().subtract(product.getPrice().multiply(this.amount.divide(BigDecimal.valueOf(100)))).setScale(this.amount.scale(), RoundingMode.HALF_UP);
    }

    @Override
    public DiscountMethod getDiscountMethod() {
        return DiscountMethod.PERCENTAGE;
    }
}
