package com.test.sii.model;

import com.test.sii.util.PromoCodePattern;
import jakarta.persistence.*;
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
@Table(name = "promo_codes_monetary")
public class PromoCodeMonetary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            nullable = false,
            unique = true,
            length = 24
    )
    @PromoCodePattern
    private String code;

    private Date expirationDate;

    private int maxUsages;

    private int usages;

    private BigDecimal amount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            name = "currency_id",
            referencedColumnName = "id"
    )
    private Currency currency;

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
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
