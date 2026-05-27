import "./Login.css";
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useState } from "react";
import { useAuthStore } from "../../stores/useAuthStore";
import React from "react";
import { useNavigate } from "react-router-dom";
import ForgotPasswordModal from "../../components/ForgotPasswordModal/ForgotPasswordModal";

type LoginStep = "credentials" | "email-otp" | "totp";

function Login() {
  const navigate = useNavigate();
  const [isSidebarOpen, setSidebarOpen] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");

  const [step, setStep] = useState<LoginStep>("credentials");
  const [mfaSessionToken, setMfaSessionToken] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [totpCode, setTotpCode] = useState("");

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);

  const apiUrl = import.meta.env.VITE_API_URL;

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch(`${apiUrl}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }),
      });
      if (!response.ok) {
        setError("Invalid credentials. Please try again.");
        return;
      }
      const data = await response.json();
      if (data.mfaSessionToken) {
        setMfaSessionToken(data.mfaSessionToken);
        setStep("email-otp");
      } else {
        setAuth(data);
        navigate("/home");
      }
    } catch {
      setError("Something went wrong. Is the server running?");
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch(`${apiUrl}/api/auth/mfa/verify-email-otp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mfaSessionToken, code: otpCode }),
      });
      if (!response.ok) {
        setError("Incorrect code. Please try again or check if it expired.");
        return;
      }
      setStep("totp");
      setOtpCode("");
    } catch {
      setError("Something went wrong.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyTotp = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch(`${apiUrl}/api/auth/mfa/verify-totp`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mfaSessionToken, code: totpCode }),
      });
      if (!response.ok) {
        const text = await response.text();
        setError(text || "Incorrect authenticator code.");
        return;
      }
      const data = await response.json();
      setAuth(data);
      navigate("/home");
    } catch {
      setError("Something went wrong.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuth = (provider: "google" | "github") => {
    const clientId =
      provider === "google"
        ? import.meta.env.VITE_GOOGLE_CLIENT_ID
        : import.meta.env.VITE_GITHUB_CLIENT_ID;
    const redirectUri = encodeURIComponent(
      `https://localhost:5173/oauth/callback/${provider}`
    );
    const url =
      provider === "google"
        ? `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=email%20profile&prompt=select_account`
        : `https://github.com/login/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email&login=`;
    window.location.href = url;
  };

  const renderCredentialsStep = () => (
    <>
      <form className="login-form" onSubmit={handleLogin}>
        <div className="input-group">
          <label>Username</label>
          <input
            type="text"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            required
            autoComplete="username"
          />
        </div>
        <div className="input-group">
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            autoComplete="current-password"
          />
        </div>

        <button
          type="button"
          className="forgot-password-link"
          onClick={() => setIsResetModalOpen(true)}
        >
          Forgot password?
        </button>

        {error && <p className="login-error">{error}</p>}

        <button type="submit" className="go-button" disabled={isLoading}>
          {isLoading ? "..." : "go!"}
        </button>
      </form>

      <div className="oauth-divider">
        <div className="oauth-divider-line" />
        <span className="oauth-divider-text">or continue with</span>
        <div className="oauth-divider-line" />
      </div>

      <div className="oauth-buttons">
        <button type="button" className="oauth-btn" onClick={() => handleOAuth("google")}>
          <span style={{ fontWeight: 900, color: "#4285F4", fontFamily: "Arial" }}>G</span>
          Google
        </button>
        <button type="button" className="oauth-btn" onClick={() => handleOAuth("github")}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#6475a6">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.3 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0 1 12 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.63-5.37-12-12-12z" />
          </svg>
          GitHub
        </button>
      </div>

      <button
        type="button"
        className="magic-link-btn"
        onClick={() => navigate("/magic-request")}
      >
        ✉ email me a login link
      </button>

      <button className="create-account-link" onClick={() => navigate("/register")}>
        Or create an account
      </button>
    </>
  );

  const renderEmailOtpStep = () => (
    <form className="mfa-form" onSubmit={handleVerifyOtp}>
      <p className="mfa-hint">
        We sent a 6-digit code to your email address. Enter it below.
      </p>
      <div className="mfa-input-group">
        <label>Email verification code</label>
        <input
          type="text"
          inputMode="numeric"
          pattern="\d{6}"
          maxLength={6}
          value={otpCode}
          onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ""))}
          placeholder="000000"
          required
          autoFocus
        />
      </div>

      {error && <p className="login-error">{error}</p>}

      <button
        type="submit"
        className="verify-button"
        disabled={isLoading || otpCode.length !== 6}
      >
        {isLoading ? "..." : "verify"}
      </button>

      <button
        type="button"
        className="forgot-password-link"
        onClick={() => { setStep("credentials"); setError(null); }}
      >
        ← back
      </button>
    </form>
  );

  const renderTotpStep = () => (
    <form className="mfa-form" onSubmit={handleVerifyTotp}>
      <p className="mfa-hint">
        Open Google Authenticator or Microsoft Authenticator and enter the 6-digit
        code for <strong>Nook</strong>.
      </p>
      <div className="mfa-input-group">
        <label>Authenticator code</label>
        <input
          type="text"
          inputMode="numeric"
          pattern="\d{6}"
          maxLength={6}
          value={totpCode}
          onChange={(e) => setTotpCode(e.target.value.replace(/\D/g, ""))}
          placeholder="000000"
          required
          autoFocus
        />
      </div>

      {error && <p className="login-error">{error}</p>}

      <button
        type="submit"
        className="verify-button"
        disabled={isLoading || totpCode.length !== 6}
      >
        {isLoading ? "..." : "verify"}
      </button>

      <button
        type="button"
        className="forgot-password-link"
        onClick={() => { setStep("credentials"); setError(null); }}
      >
        ← start over
      </button>
    </form>
  );

  const stepTitle: Record<LoginStep, string> = {
    "credentials": "LOGIN",
    "email-otp": "CHECK YOUR EMAIL",
    "totp": "AUTHENTICATOR APP",
  };

  return (
    <div className="login-page">
      <Sidebar isOpen={isSidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="circle circle--small" />
      <div className="bottom-decoration">
        <img src={deco1} alt="" />
      </div>

      <div className="login-container">
        <div className="login-logo">
          <img src={starLogo} alt="Nook logo" />
        </div>

        <div className="login-card">
          <h1 className="welcome-text">Welcome!</h1>
          <h2 className={step === "credentials" ? "login-title" : "login-title--mfa"}>
            {stepTitle[step]}
          </h2>

          {step === "credentials" && renderCredentialsStep()}
          {step === "email-otp"   && renderEmailOtpStep()}
          {step === "totp"        && renderTotpStep()}
        </div>
      </div>

      <ForgotPasswordModal
        isOpen={isResetModalOpen}
        onClose={() => setIsResetModalOpen(false)}
      />
    </div>
  );
}

export default Login;