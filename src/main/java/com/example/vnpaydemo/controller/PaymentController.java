package com.example.vnpaydemo.controller;

import com.example.vnpaydemo.dto.PaymentRequest;
import com.example.vnpaydemo.service.VnpayService;
import com.example.vnpaydemo.util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    private final VnpayService vnpayService;

    public PaymentController(VnpayService vnpayService) {
        this.vnpayService = vnpayService;
    }

    @PostMapping("/create")
    public Object createPayment(@Valid @ModelAttribute PaymentRequest paymentRequest,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("paymentRequest", paymentRequest);
            return "index";
        }
        String paymentUrl = vnpayService.createPaymentUrl(
                paymentRequest.getAmount(),
                paymentRequest.getOrderInfo(),
                request
        );
        return new RedirectView(paymentUrl);
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        Map<String, String> fields = VnpayUtil.requestParamsToMap(request.getParameterMap());
        boolean validSignature = vnpayService.isValidSecureHash(fields);
        boolean success = validSignature && "00".equals(fields.get("vnp_ResponseCode"));

        Map<String, String> display = new LinkedHashMap<>();
        display.put("Mã đơn hàng", fields.get("vnp_TxnRef"));
        display.put("Số tiền", formatMoney(fields.get("vnp_Amount")));
        display.put("Ngân hàng thanh toán", fields.get("vnp_BankCode"));
        display.put("Mã giao dịch VNPAY", fields.get("vnp_TransactionNo"));
        display.put("Thời gian thanh toán", fields.get("vnp_PayDate"));
        display.put("Mã phản hồi", fields.get("vnp_ResponseCode"));
        display.put("Mã giao dịch ngân hàng", fields.get("vnp_BankTranNo"));
        display.put("Loại thẻ", fields.get("vnp_CardType"));
        display.put("Chữ ký hợp lệ", validSignature ? "Có" : "Không");

        model.addAttribute("success", success);
        model.addAttribute("message", success ? "Thanh toán thành công" : "Giao dịch thất bại hoặc chữ ký không hợp lệ");
        model.addAttribute("paymentData", display);
        model.addAttribute("rawData", fields);
        return "result";
    }

    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> vnpayIpn(HttpServletRequest request) {
        Map<String, String> fields = VnpayUtil.requestParamsToMap(request.getParameterMap());
        boolean validSignature = vnpayService.isValidSecureHash(fields);

        Map<String, String> response = new LinkedHashMap<>();
        if (!validSignature) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return ResponseEntity.ok(response);
        }

        // Demo không dùng DB nên chỉ mô phỏng cập nhật đơn hàng.
        // Khi làm thật: kiểm tra order tồn tại, amount đúng, trạng thái chưa thanh toán, sau đó UPDATE DB.
        if ("00".equals(fields.get("vnp_ResponseCode"))) {
            System.out.println("[IPN] Don hang " + fields.get("vnp_TxnRef") + " thanh toan thanh cong.");
        } else {
            System.out.println("[IPN] Don hang " + fields.get("vnp_TxnRef") + " that bai. ResponseCode=" + fields.get("vnp_ResponseCode"));
        }

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return ResponseEntity.ok(response);
    }

    private String formatMoney(String vnpAmount) {
        try {
            long amount = vnpayService.parseAmountVnd(vnpAmount);
            return String.format("%,d VND", amount).replace(',', '.');
        } catch (Exception e) {
            return vnpAmount;
        }
    }
}
