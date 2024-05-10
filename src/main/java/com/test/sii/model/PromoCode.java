package com.test.sii.model;

import com.test.sii.dto.DiscountMethod;
import com.test.sii.util.PromoCodePattern;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "promo_codes")
public abstract class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(
            nullable = false,
            unique = true,
            length = 24
    )
    @PromoCodePattern
    protected String code;

    protected Date expirationDate;

    protected int maxUsages;

    protected int usages;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(
            name = "currency_id",
            referencedColumnName = "id"
    )
    protected Currency currency;

    public void use() throws ResponseStatusException {
        if (this.usages >= this.maxUsages) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot use this code", new Exception("Cannot use this code - reached maximum usages"));
        }
        if (this.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot use this code", new Exception("Cannot use this code - the promo code has expired"));
        }
        this.usages++;
    }

    public boolean isExpired() {
        return this.expirationDate.before(Date.from(Instant.now()));
    }

    public abstract BigDecimal calculateDiscountPrice(Product product);

    public abstract BigDecimal getAmount();

    public abstract DiscountMethod getDiscountMethod();
}
