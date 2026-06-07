# Demo tích hợp VNPAY Sandbox bằng Spring Boot

Project đáp ứng bài thực hành:

- Trang chủ nhập số tiền và bấm **Thanh toán qua VNPAY**.
- Backend tạo Payment URL và redirect sang VNPAY sandbox.
- Trang kết quả nhận dữ liệu từ `ReturnUrl`, kiểm tra `vnp_SecureHash`, hiển thị mã đơn hàng, số tiền, ngân hàng, mã giao dịch VNPAY, thời gian thanh toán.
- Có endpoint IPN/Webhook: `/payment/vnpay-ipn`.
- Cấu hình bằng biến môi trường, không hard-code secret thật.

## Công nghệ

- Java 17
- Spring Boot 3.3.5
- Thymeleaf
- Maven

## Cấu hình

Mở `src/main/resources/application.properties` và thay bằng dữ liệu sandbox của nhóm:

```properties
vnpay.tmn-code=${VNPAY_TMN_CODE:DEMO_TMN_CODE}
vnpay.hash-secret=${VNPAY_HASH_SECRET:DEMO_HASH_SECRET}
vnpay.return-url=${VNPAY_RETURN_URL:http://localhost:8080/payment/vnpay-return}
vnpay.ipn-url=${VNPAY_IPN_URL:http://localhost:8080/payment/vnpay-ipn}
```

Khuyến nghị chạy bằng biến môi trường:

```bash
set VNPAY_TMN_CODE=YOUR_TMN_CODE
set VNPAY_HASH_SECRET=YOUR_HASH_SECRET
set VNPAY_RETURN_URL=https://your-ngrok-url.ngrok-free.app/payment/vnpay-return
set VNPAY_IPN_URL=https://your-ngrok-url.ngrok-free.app/payment/vnpay-ipn
mvn spring-boot:run
```

Trên PowerShell:

```powershell
$env:VNPAY_TMN_CODE="YOUR_TMN_CODE"
$env:VNPAY_HASH_SECRET="YOUR_HASH_SECRET"
$env:VNPAY_RETURN_URL="https://your-ngrok-url.ngrok-free.app/payment/vnpay-return"
$env:VNPAY_IPN_URL="https://your-ngrok-url.ngrok-free.app/payment/vnpay-ipn"
mvn spring-boot:run
```

## Chạy local

```bash
mvn spring-boot:run
```

Mở trình duyệt:

```text
http://localhost:8080
```

## Dùng ngrok

```bash
ngrok http 8080
```

Lấy URL HTTPS của ngrok, ví dụ:

```text
https://abc123.ngrok-free.app
```

Sau đó cấu hình:

```text
VNPAY_RETURN_URL=https://abc123.ngrok-free.app/payment/vnpay-return
VNPAY_IPN_URL=https://abc123.ngrok-free.app/payment/vnpay-ipn
```

## Lưu ý khi push GitHub

Không đưa `TmnCode` thật và `HashSecret` thật lên GitHub. Chỉ để biến môi trường hoặc file mẫu.
