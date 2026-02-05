package com.intern.ecommerce.paymentgateway.common;

public class Constants {

    // AES related constants
    public static final String AES_ALGORITHM = "AES";
    public static final String HASH_ALGORITHM = "SHA-1";

    // This is the BASE secret used to derive AES key
    // (Should ideally come from env variable in real projects)
    public static final String AES_SECRET_KEY = "MyAESSecretKey12";

    // Order status constants
    public static final String PAYMENT_DONE = "PAYMENT DONE";

    // Currency constants
    public static final String CURRENCY_INR = "INR";

}
