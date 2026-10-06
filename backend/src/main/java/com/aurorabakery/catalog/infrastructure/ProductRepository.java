package com.aurorabakery.catalog.infrastructure;
import com.aurorabakery.catalog.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductRepository extends JpaRepository<Product,UUID> {
 List<Product> findByAvailabilityInOrderByFeaturedDescNameAsc(Collection<Availability> statuses);
}
