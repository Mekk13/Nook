package com.Nook.backend.auth.dto;

public record TotpSetupResponse(
        String qrCodeDataUri,
        String secret
) {}