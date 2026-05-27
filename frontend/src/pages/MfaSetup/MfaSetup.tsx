import "./MfaSetup.css";
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useApi } from "../../hooks/useApi";

type SetupStep = "loading" | "scan" | "verify" | "done" | "error";

function MfaSetup() {
  const navigate = useNavigate();
  const { apiFetch } = useApi();

  const [step, setStep] = useState<SetupStep>("loading");
  const [qrCodeUrl, setQrCodeUrl] = useState("");
  const [secret, setSecret] = useState("");
  const [code, setCode] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [isVerifying, setIsVerifying] = useState(false);

  useEffect(() => {
    apiFetch("/api/auth/mfa/setup", { method: "POST" })
      .then(async (res) => {
        if (!res.ok) throw new Error("Failed to initialize setup");
        const data = await res.json();
        setQrCodeUrl(data.qrCodeDataUri);
        setSecret(data.secret);
        setStep("scan");
      })
      .catch(() => {
        setErrorMsg("Could not start MFA setup. Please try again.");
        setStep("error");
      });
  }, []);

  const handleVerify = async () => {
    if (code.length !== 6) return;
    setIsVerifying(true);
    setErrorMsg("");

    try {
      const res = await apiFetch("/api/auth/mfa/setup/confirm", {
        method: "POST",
        body: JSON.stringify({ code, secret }),
      });

      if (!res.ok) {
        const text = await res.text();
        setErrorMsg(text || "Incorrect code. Try again.");
        setIsVerifying(false);
        return;
      }

      setStep("done");
    } catch {
      setErrorMsg("Something went wrong.");
      setIsVerifying(false);
    }
  };

  const handleCodeChange = (val: string) => {
    const digits = val.replace(/\D/g, "").slice(0, 6);
    setCode(digits);
    setErrorMsg("");
  };

  return (
    <div className="mfa-setup-page">
      <div className="mfa-setup-container">
        <button className="mfa-back-btn" onClick={() => navigate("/settings")}>
          ← Back to settings
        </button>

        <h1 className="mfa-setup-title">Set up 3FA</h1>

        {/* ── LOADING ─────────────────────────────────────── */}
        {step === "loading" && (
          <div className="mfa-card mfa-card--center">
            <div className="mfa-spinner" />
            <p className="mfa-hint">Generating your setup code…</p>
          </div>
        )}

        {/* ── ERROR ───────────────────────────────────────── */}
        {step === "error" && (
          <div className="mfa-card mfa-card--center">
            <p className="mfa-error">{errorMsg}</p>
            <button
              className="mfa-btn mfa-btn--primary"
              onClick={() => navigate("/settings")}
            >
              Go back
            </button>
          </div>
        )}

        {/* ── SCAN QR ─────────────────────────────────────── */}
        {step === "scan" && (
          <div className="mfa-card">
            <div className="mfa-step-badge">Step 1 of 2</div>
            <h2 className="mfa-section-title">Scan this QR code</h2>
            <p className="mfa-hint">
              Open <strong>Google Authenticator</strong>, <strong>Microsoft Authenticator</strong>, or any TOTP app and scan the code below.
            </p>

            <div className="mfa-qr-wrapper">
              {qrCodeUrl ? (
                <img
                  src={qrCodeUrl}
                  alt="TOTP QR code"
                  className="mfa-qr-image"
                />
              ) : (
                <div className="mfa-qr-placeholder">
                  <div className="mfa-spinner" />
                </div>
              )}
            </div>

            <div className="mfa-secret-wrapper">
              <p className="mfa-hint mfa-hint--small">
                Can't scan? Enter this key manually:
              </p>
              <code className="mfa-secret">{secret}</code>
            </div>

            <button
              className="mfa-btn mfa-btn--primary"
              onClick={() => setStep("verify")}
            >
              I've scanned it →
            </button>
          </div>
        )}

        {/* ── VERIFY CODE ─────────────────────────────────── */}
        {step === "verify" && (
          <div className="mfa-card">
            <div className="mfa-step-badge">Step 2 of 2</div>
            <h2 className="mfa-section-title">Enter the 6-digit code</h2>
            <p className="mfa-hint">
              Open your authenticator app and enter the current code for <strong>Nook</strong> to confirm setup.
            </p>

            <div className="mfa-code-input-wrapper">
              <input
                className="mfa-code-input"
                type="text"
                inputMode="numeric"
                pattern="\d{6}"
                maxLength={6}
                placeholder="000000"
                value={code}
                onChange={(e) => handleCodeChange(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleVerify()}
                autoFocus
              />
            </div>

            {errorMsg && <p className="mfa-error">{errorMsg}</p>}

            <div className="mfa-actions">
              <button
                className="mfa-btn mfa-btn--secondary"
                onClick={() => setStep("scan")}
              >
                ← Back
              </button>
              <button
                className="mfa-btn mfa-btn--primary"
                onClick={handleVerify}
                disabled={isVerifying || code.length !== 6}
              >
                {isVerifying ? "Verifying…" : "Confirm & enable"}
              </button>
            </div>
          </div>
        )}

        {/* ── DONE ────────────────────────────────────────── */}
        {step === "done" && (
          <div className="mfa-card mfa-card--center">
            <div className="mfa-success-icon">✓</div>
            <h2 className="mfa-section-title">3FA is enabled!</h2>
            <p className="mfa-hint">
              From now on, each login will ask for your password, an email code, and your authenticator code.
            </p>
            <button
              className="mfa-btn mfa-btn--primary"
              onClick={() => navigate("/settings")}
            >
              Back to settings
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default MfaSetup;