import { useState } from "react";
import { useNavigate } from "react-router-dom";
import React from "react";

function MagicRequestPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/api/auth/magic-link`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      if (!res.ok) throw new Error();
      setSent(true);
    } catch {
      alert("Something went wrong. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const pageStyle: React.CSSProperties = {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "100vh",
    backgroundColor: "#8e9dcc",
    fontFamily: '"Baloo", cursive',
  };

  const cardStyle: React.CSSProperties = {
    backgroundColor: "#faf9f0",
    borderRadius: "30px",
    boxShadow: "0px 10px 0px rgba(0,0,0,0.1)",
    padding: "50px 60px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    gap: "24px",
    minWidth: "420px",
  };

  const titleStyle: React.CSSProperties = {
    fontSize: "60px",
    color: "white",
    WebkitTextStroke: "2px #8e9dcc",
    textShadow: "4px 4px 0px rgba(142,157,204,0.3)",
    margin: 0,
  };

  const inputRowStyle: React.CSSProperties = {
    display: "flex",
    flexDirection: "row",
    alignItems: "center",
    gap: "12px",
    width: "100%",
  };

  const labelStyle: React.CSSProperties = {
    color: "#8e9dcc",
    fontSize: "20px",
    whiteSpace: "nowrap",
    minWidth: "50px",
  };

  const inputStyle: React.CSSProperties = {
    borderRadius: "40px",
    border: "2px solid #8e9dcc",
    padding: "12px 18px",
    flex: 1,
    backgroundColor: "white",
    fontSize: "16px",
    fontFamily: '"Baloo", cursive',
    boxShadow: "inset 2px 4px 6px rgba(0,0,0,0.1)",
    outline: "none",
    boxSizing: "border-box",
  };

  const sendButtonStyle: React.CSSProperties = {
    marginTop: "8px",
    padding: "14px 40px",
    borderRadius: "40px",
    border: "2px solid #8e9dcc",
    backgroundColor: "#dbf4a7",
    color: "#6475a6",
    fontSize: "22px",
    fontFamily: '"Baloo", cursive',
    cursor: isLoading ? "not-allowed" : "pointer",
    boxShadow: "0px 4px 0px rgba(142,157,204,0.4)",
    transition: "transform 0.1s, opacity 0.1s",
    opacity: isLoading ? 0.6 : 1,
  };

  const backButtonStyle: React.CSSProperties = {
    background: "none",
    border: "none",
    color: "#8e9dcc",
    textDecoration: "underline",
    cursor: "pointer",
    fontSize: "15px",
    fontFamily: '"Baloo", cursive',
    marginTop: "4px",
  };

  if (sent) {
    return (
      <div style={pageStyle}>
        <div style={cardStyle}>
          <h2 style={titleStyle}>CHECK EMAIL</h2>
          <p style={{ color: "#8e9dcc", fontSize: "18px", textAlign: "center", margin: 0 }}>
            ✉ We sent a login link to <strong>{email}</strong>.<br />
            Check your inbox!
          </p>
          <button style={backButtonStyle} onClick={() => navigate("/login")}>
            ← back to login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={pageStyle}>
      <div style={cardStyle}>
        <h2 style={titleStyle}>MAGIC LINK</h2>
        <p style={{ color: "#a0a8c0", fontSize: "15px", margin: 0, textAlign: "center" }}>
          Enter your email and we'll send you a login link — no password needed.
        </p>

        <form
          onSubmit={handleSubmit}
          style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "16px", width: "100%" }}
        >
          <div style={inputRowStyle}>
            <label style={labelStyle}>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoFocus
              style={inputStyle}
              placeholder="you@example.com"
            />
          </div>

          <button type="submit" style={sendButtonStyle} disabled={isLoading}>
            {isLoading ? "sending..." : "✉ send link!"}
          </button>
        </form>

        <button style={backButtonStyle} onClick={() => navigate("/login")}>
          ← back to login
        </button>
      </div>
    </div>
  );
}

export default MagicRequestPage;