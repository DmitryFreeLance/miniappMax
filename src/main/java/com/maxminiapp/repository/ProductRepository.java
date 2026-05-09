package com.maxminiapp.repository;

import com.maxminiapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.active = true and (p.fixPrice = false or p.fixPrice is null) order by p.createdAt desc")
    List<Product> findActiveCatalogOrderByCreatedAtDesc();

    @Query("select p from Product p where p.active = true and p.fixPrice = true order by p.createdAt desc")
    List<Product> findActiveFixPriceOrderByCreatedAtDesc();

    List<Product> findAllByOrderByCreatedAtDesc();

    List<Product> findByActiveTrueOrderByCreatedAtDesc();
}
