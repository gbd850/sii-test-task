package com.test.sii.model;

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
        this.code = code;
        this.expirationDate = expirationDate;
        this.maxUsages = maxUsages;
        this.usages = 0;
        this.amount = amount;
        this.currency = currency;
    }

    public void use() throws Exception {
        if (this.usages >= this.maxUsages) {
            throw new Exception("Cannot use this code - reached maximum usages");
        }
        this.usages++;
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
}
