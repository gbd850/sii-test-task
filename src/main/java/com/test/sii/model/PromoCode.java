package com.test.sii.model;

import com.test.sii.util.PromoCodePattern;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

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
}
