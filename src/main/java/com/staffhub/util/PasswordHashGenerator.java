package com.staffhub.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        PasswordEncoder encoder =
                PasswordEncoderFactories
                        .createDelegatingPasswordEncoder();

        String password = "0123456789";

        String encoded =
                encoder.encode(password);

        System.out.println(encoded);
    }
}