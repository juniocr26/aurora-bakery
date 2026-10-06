package com.aurorabakery.pricing.domain;

import com.aurorabakery.identity.domain.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Server-side policy prepared for checkout. Customer benefits never stack. */
@Component
public class PurchasePolicy {
    private final BigDecimal adminPercent;
    public PurchasePolicy(@Value("${app.pricing.admin-discount-percent:15}") BigDecimal adminPercent) {
        this.adminPercent = percentage(adminPercent);
    }
    public BigDecimal total(Role role, BigDecimal subtotal, BigDecimal eligiblePromotionPercent,
                            BigDecimal eligibleLoyaltyPercent) {
        Objects.requireNonNull(role);
        if (subtotal == null || subtotal.signum() < 0) throw new IllegalArgumentException("Invalid subtotal");
        // Caller must obtain role from authenticated identity and determine eligible benefits server-side.
        BigDecimal discount = role == Role.ADMIN ? adminPercent
            : percentage(eligiblePromotionPercent).max(percentage(eligibleLoyaltyPercent));
        return subtotal.multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2))).setScale(2, RoundingMode.HALF_UP);
    }
    public boolean loyaltyEligible(Role role) { return Objects.requireNonNull(role) == Role.CUSTOMER; }
    public boolean marketingEligible(Role role, boolean emailOptIn) { return loyaltyEligible(role) && emailOptIn; }
    private static BigDecimal percentage(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        return value;
    }
}
