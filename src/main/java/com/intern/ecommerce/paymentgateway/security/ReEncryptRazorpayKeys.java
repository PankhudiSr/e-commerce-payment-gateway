package com.intern.ecommerce.paymentgateway.security;

public class ReEncryptRazorpayKeys {

    public static void main(String[] args) {

        //  Put your REAL Razorpay keys here temporarily
        String razorpayKeyId = "rzp_test_RtVxX2GgS4LcYd";
        String razorpaySecretKey = "44aYMRRThrD6QrBYg6XpMobH";

        String encryptedKeyId = AESUtil.encrypt(razorpayKeyId);
        String encryptedSecretKey = AESUtil.encrypt(razorpaySecretKey);

        System.out.println("Encrypted Razorpay Key ID:");
        System.out.println(encryptedKeyId);

        System.out.println("\nEncrypted Razorpay Secret Key:");
        System.out.println(encryptedSecretKey);
    }
}
