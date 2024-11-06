package com.example.checkout.repository;


import com.example.checkout.model.dto.Product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
@Repository
@FeignClient(name="products-service")
//@LoadBalancerClient(value = "product-service", configuration = LoadBalancerConfiguration.class)
public interface FeignProductRepository {
    @RequestMapping(method = RequestMethod.GET,value="/products")
    Product getProductById(@RequestParam String id,@RequestParam Boolean throwError );
}
