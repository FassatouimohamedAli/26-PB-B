package com.example.pfebtk.auth.dto;

public record Authresp(String Role , String token, String refreshToken,boolean passwordMustChange ) {
}
