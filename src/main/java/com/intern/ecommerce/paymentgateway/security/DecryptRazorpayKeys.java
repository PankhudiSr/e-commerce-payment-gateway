package com.intern.ecommerce.paymentgateway.security;

public class DecryptRazorpayKeys {

    public static void main(String[] args) {

        // Step 1: Copy your encrypted keys from console / properties
        String encryptedKeyId = "Gk/nwB/z0xDqUgpD/D3KI3Ttzw9j4DpYIFw01uTEW9Q=";   // Replace with your actual encrypted value
        String encryptedSecret = "I59qpESRDBvLfGhqb2MpY1T2KvCunF8MG+gW8hD+gWM=";  // Replace with your actual encrypted value

        // Step 2: Decrypt using AESUtil
        String keyId = AESUtil.decrypt(encryptedKeyId);
        String secretKey = AESUtil.decrypt(encryptedSecret);

        // Step 3: Print decrypted values
        System.out.println("Decrypted Razorpay Key ID: " + keyId);
        System.out.println("Decrypted Razorpay Secret: " + secretKey);
    }
}
