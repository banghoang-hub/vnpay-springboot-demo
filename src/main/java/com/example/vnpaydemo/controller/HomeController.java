package com.example.vnpaydemo.controller;

import com.example.vnpaydemo.dto.PaymentRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(10000L);
        paymentRequest.setOrderInfo("Thanh toan demo VNPAY Spring Boot");
        model.addAttribute("paymentRequest", paymentRequest);
        return "index";
    }
}
