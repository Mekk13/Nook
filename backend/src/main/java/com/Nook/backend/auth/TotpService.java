package com.Nook.backend.auth;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Wraps the java-totp library for TOTP secret generation, QR code creation,
 * and 6-digit code verification.
 *
 * Maven dependency to add to pom.xml:
 *   <dependency>
 *     <groupId>dev.samstevens.totp</groupId>
 *     <artifactId>totp-spring-boot-starter</artifactId>
 *     <version>1.7.1</version>
 *   </dependency>
 */
@Slf4j
@Service
public class TotpService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();

    public TotpService() {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        // Allow a 1-window drift on each side (30 s each) to account for clock skew
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        ((DefaultCodeVerifier) this.codeVerifier).setAllowedTimePeriodDiscrepancy(1);
    }

    /** Generates a new random Base32 TOTP secret. */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Returns a data-URI (PNG) for a QR code the user can scan with Google/
     * Microsoft Authenticator.
     *
     * @param email  shown as the account label inside the authenticator app
     * @param secret the Base32 TOTP secret
     */
    public String generateQrCodeDataUri(String email, String secret) {
        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("Nook")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            byte[] imageData = qrGenerator.generate(data);
            String mimeType = qrGenerator.getImageMimeType();
            return getDataUriForImage(imageData, mimeType);
        } catch (QrGenerationException e) {
            log.error("Failed to generate TOTP QR code", e);
            throw new RuntimeException("Could not generate QR code");
        }
    }

    /**
     * Verifies a 6-digit TOTP code against the stored secret.
     *
     * @param secret the user's stored Base32 secret
     * @param code   the 6-digit code submitted by the user
     */
    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}