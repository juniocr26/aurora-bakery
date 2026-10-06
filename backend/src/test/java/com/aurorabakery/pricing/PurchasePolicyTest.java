package com.aurorabakery.pricing;
import com.aurorabakery.pricing.domain.PurchasePolicy;
import com.aurorabakery.identity.domain.Role;
import com.aurorabakery.catalog.domain.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class PurchasePolicyTest {
 private final PurchasePolicy policy = new PurchasePolicy(new BigDecimal("15"));
 @Test void adminDiscountExcludesAllCustomerBenefits(){
  assertThat(policy.total(Role.ADMIN,new BigDecimal("100"),new BigDecimal("50"),new BigDecimal("90"))).isEqualByComparingTo("85.00");
  assertThat(policy.loyaltyEligible(Role.ADMIN)).isFalse();
  assertThat(policy.marketingEligible(Role.ADMIN,true)).isFalse();
 }
 @Test void customerBenefitsDoNotStackAndConsentIsRequired(){
  assertThat(policy.total(Role.CUSTOMER,new BigDecimal("100"),new BigDecimal("20"),new BigDecimal("10"))).isEqualByComparingTo("80.00");
  assertThat(policy.marketingEligible(Role.CUSTOMER,false)).isFalse();
  assertThat(policy.marketingEligible(Role.CUSTOMER,true)).isTrue();
 }
 @Test void discountIsConfigurableAndMoneyRounded(){
  assertThat(new PurchasePolicy(new BigDecimal("10")).total(Role.ADMIN,new BigDecimal("12.55"),BigDecimal.ZERO,BigDecimal.ZERO)).isEqualByComparingTo("11.30");
  assertThatThrownBy(()->new PurchasePolicy(new BigDecimal("101"))).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->policy.total(Role.CUSTOMER,new BigDecimal("-1"),BigDecimal.ZERO,BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void onlyAvailableProductsArePurchasable(){
  for(var state:Availability.values())
   assertThat(new Product(UUID.randomUUID(),"bread","Bread",BigDecimal.TEN,state).isPurchasable()).isEqualTo(state==Availability.AVAILABLE);
 }
}
