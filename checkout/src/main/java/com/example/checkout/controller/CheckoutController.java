package com.example.checkout.controller;

import com.example.checkout.model.Checkout;
import com.example.checkout.service.ICheckoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/checkout")
public class CheckoutController {
    private ICheckoutService checkoutService;


    public CheckoutController(ICheckoutService checkoutService) {
        super();
        this.checkoutService = checkoutService;
    }

    @GetMapping("/{id}")
    public Checkout getById(@PathVariable String id) {
        return new Checkout(id);
    }


    @GetMapping()
    public Checkout getCheckout(@RequestParam List<String> productIds,@RequestHeader("X-Request-from") String requestFrom,@RequestHeader() Map<String,String> headers) {
        System.out.println("Enviado desde: "+ requestFrom);
        if(!requestFrom.equals("gateway")) {
            return null;
        }
        return checkoutService.buildCheckout(productIds);

    }
}
