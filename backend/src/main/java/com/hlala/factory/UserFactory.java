package com.hlala.factory;

import com.hlala.domain.User;
import com.hlala.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class UserFactory {

    // South African mobile format, e.g. +27821234567
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+27\\d{9}$");

    public User create(String phone, String name) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Phone number must be a valid South African mobile number (+27...)");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is required");
        }
        return new User(UUID.randomUUID(), phone, name.trim(), false, Instant.now());
    }
}
