package com.example.projectlibary.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class VietQRGeneratorUtil {
    /**
     * Tạo ra một chuỗi VietQR theo chuẩn EMVCo để render thành ảnh QR.
     *
     * @param bankBin           Mã BIN của ngân hàng (ví dụ: "970436" cho Vietcombank).
     * @param bankAccountNumber Số tài khoản ngân hàng của người nhận.
     * @param accountName       Tên chủ tài khoản (thường là tên thư viện).
     * @param amount            Số tiền cần thanh toán.
     * @param description       Nội dung chuyển khoản (rất quan trọng, chứa transactionId).
     * @return Chuỗi dữ liệu để tạo thành ảnh QR.
     */
    public String generate(String bankBin, String bankAccountNumber, String accountName,
                           BigDecimal amount, String description) {

        // --- Phần 1: Xây dựng các trường dữ liệu (payload) ---

        // Field 00: Payload Format Indicator
        String f00 = formatTLV("00", "01");

        // Field 01: Point of Initiation Method (12 for dynamic QR)
        String f01 = formatTLV("01", "12");

        // Field 38: Merchant Account Information (Thông tin tài khoản người nhận)
        // Đây là trường phức tạp nhất, nó chứa các trường con
        String merchantAccountInfo = "";
        //   - Field 00 (con): GUID (Bắt buộc là A000000727 cho VietQR)
        merchantAccountInfo += formatTLV("00", "A000000727");
        //   - Field 01 (con): Thông tin Ngân hàng và Số tài khoản
        String bankInfo = "";
        bankInfo += formatTLV("00", bankBin);
        bankInfo += formatTLV("01", bankAccountNumber);
        merchantAccountInfo += formatTLV("01", bankInfo);
        //   - Field 02 (con): Tên người nhận
        merchantAccountInfo += formatTLV("02", accountName);

        String f38 = formatTLV("38", merchantAccountInfo);

        // Field 53: Transaction Currency (704 for VND)
        String f53 = formatTLV("53", "704");

        // Field 54: Transaction Amount (Số tiền)
        String f54 = formatTLV("54", String.valueOf(amount.intValue())); // Lấy phần nguyên của số tiền

        // Field 58: Country Code (VN for Vietnam)
        String f58 = formatTLV("58", "VN");

        // Field 62: Additional Data Field Template (Thông tin thêm)
        String additionalData = "";
        //   - Field 08 (con): Nội dung chuyển khoản
        additionalData += formatTLV("08", description);

        String f62 = formatTLV("62", additionalData);

        // --- Phần 2: Ghép nối và tính toán CRC16 ---

        // Ghép tất cả các trường lại (trừ CRC)
        String payloadToCalculateCRC = f00 + f01 + f38 + f53 + f54 + f58 + f62;

        // Thêm trường CRC (Field 63) vào cuối
        // Field 63 luôn có Length là 04
        String f63Header = "6304";

        // Tính toán CRC16
        String crc = calculateCRC16_CCITT(payloadToCalculateCRC + f63Header);

        // --- Phần 3: Trả về chuỗi hoàn chỉnh ---
        return payloadToCalculateCRC + f63Header + crc;
    }

    /**
     * Định dạng một cặp Tag-Length-Value (TLV).
     * @param tag Mã tag (ví dụ: "00", "01").
     * @param value Giá trị của tag.
     * @return Chuỗi đã được định dạng, ví dụ: "000201".
     */
    private String formatTLV(String tag, String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String length = String.format("%02d", value.length());
        return tag + length + value;
    }

    /**
     * Tính toán mã CRC16-CCITT (với giá trị khởi tạo là 0xFFFF).
     * Đây là thuật toán chuẩn được VietQR sử dụng.
     * @param data Dữ liệu cần tính CRC.
     * @return Chuỗi Hex 4 ký tự đại diện cho mã CRC.
     */
    private String calculateCRC16_CCITT(String data) {
        int crc = 0xFFFF; // Giá trị khởi tạo
        int polynomial = 0x1021; // Đa thức 0x1021

        byte[] bytes = data.getBytes();

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF; // Đảm bảo kết quả là 16-bit
        return String.format("%04X", crc).toUpperCase(); // Định dạng thành chuỗi Hex 4 ký tự
    }
}
