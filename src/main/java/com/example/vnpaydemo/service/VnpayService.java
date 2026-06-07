package com.example.vnpaydemo.service;

import com.example.vnpaydemo.config.VnpayProperties;
import com.example.vnpaydemo.util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class VnpayService {
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpayProperties props;

    public VnpayService(VnpayProperties props) {
        this.props = props;
    }

    public String createPaymentUrl(long amountVnd, String orderInfo, HttpServletRequest request) {
        String txnRef = VnpayUtil.randomTxnRef();
        LocalDateTime now = LocalDateTime.now();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", props.getCommand());
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", props.getCurrencyCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo == null || orderInfo.isBlank() ? "Thanh toan don hang " + txnRef : orderInfo);
        params.put("vnp_OrderType", props.getOrderType());
        params.put("vnp_Locale", props.getLocale());
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        // VNPAY bản sandbox mới có thể không bắt buộc vnp_IpnUrl trong URL, nhưng thêm vào để phục vụ bài demo webhook.
        params.put("vnp_IpnUrl", props.getIpnUrl());
        params.put("vnp_IpAddr", VnpayUtil.getClientIp(request));
        params.put("vnp_CreateDate", now.format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(VNPAY_DATE_FORMAT));

        String hashData = VnpayUtil.buildHashData(params);
//        System.out.println("TMN = [" + props.getTmnCode() + "]");
//        System.out.println("HASH SECRET LENGTH = " + props.getHashSecret().length());
        System.out.println("HASH DATA = " + hashData);
        String secureHash = VnpayUtil.hmacSHA512(props.getHashSecret(), hashData);
        params.put("vnp_SecureHash", secureHash);

        return props.getPayUrl() + "?" + VnpayUtil.buildQueryString(params);
    }

    public boolean isValidSecureHash(Map<String, String> fields) {
        String receivedHash = fields.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) return false;

        Map<String, String> cloned = new LinkedHashMap<>(fields);
        cloned.remove("vnp_SecureHash");
        cloned.remove("vnp_SecureHashType");

        String hashData = VnpayUtil.buildHashData(cloned);
        String calculatedHash = VnpayUtil.hmacSHA512(props.getHashSecret(), hashData);
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    public long parseAmountVnd(String vnpAmount) {
        if (vnpAmount == null || vnpAmount.isBlank()) return 0;
        return Long.parseLong(vnpAmount) / 100;
    }
}
