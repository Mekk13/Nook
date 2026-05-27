import React, { useState, useEffect } from "react";
import "./Register.css";
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useAuthStore } from "../../stores/useAuthStore";
import { useNavigate } from "react-router-dom";

// The Regex we built
const PWD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).{10,}$/;

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setSidebarOpen] = useState<boolean>(false);
  const setAuth = useAuthStore((state) => state.setAuth);

  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // New state for validation
  const [validPassword, setValidPassword] = useState(false);

  useEffect(() => {
    setValidPassword(PWD_REGEX.test(password));
  }, [password]);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validPassword) return; // Guard clause

    setIsLoading(true);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, username, email, password }),
      });

      if (response.ok) {
        const data = await response.json();
        setAuth(data);
        navigate("/home");
      } else {
        const errorData = await response.json();
        alert(errorData.message || "Registration failed.");
      }
    } catch (error) {
      alert("Could not connect to the server.");
    } finally {
      setIsLoading(false);
    }
  };

  // Helper to check individual parts for the UI
  const check = {
    length: password.length >= 10,
    upper: /[A-Z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[@$!%*?&]/.test(password)
  };

  return (
    <div className="register-page">
      <Sidebar isOpen={isSidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="circle circle--small"></div>
      <div className="bottom-decoration"><img src={deco1} alt="decoration1" /></div>

      <div className="register-container">
        <div className="register-logo"><img src={starLogo} alt="Nook logo" /></div>

        <div className="register-card">
          <h1 className="register-text">Register</h1>
          <h2 className="register-subtitle">Enter your details below:</h2>

          <form className="register-form" onSubmit={handleRegister}>
            <div className="register-group1">
              <label>Full Name:</label>
              <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
            </div>

            <div className="register-group1">
              <label>Username:</label>
              <input type="text" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="new-username" required />
            </div>

            <div className="register-group1">
              <label>Email:</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>

            <div className="register-group1">
              <label>Password:</label>
              <input 
                type="password" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                autoComplete="new-password" 
                required 
              />
            </div>

            {/* PRETTIER CHECKLIST */}
            <div className="password-checklist">
              <p style={{ color: check.length ? '#4CAF50' : '#f44336', margin: '2px 0', fontSize: '0.8rem' }}>
                {check.length ? "✓" : "✕"} 10+ Characters
              </p>
              <p style={{ color: check.upper ? '#4CAF50' : '#f44336', margin: '2px 0', fontSize: '0.8rem' }}>
                {check.upper ? "✓" : "✕"} Uppercase Letter
              </p>
              <p style={{ color: check.number ? '#4CAF50' : '#f44336', margin: '2px 0', fontSize: '0.8rem' }}>
                {check.number ? "✓" : "✕"} Number
              </p>
              <p style={{ color: check.special ? '#4CAF50' : '#f44336', margin: '2px 0', fontSize: '0.8rem' }}>
                {check.special ? "✓" : "✕"} Special Symbol (@$!%*?&)
              </p>
            </div>

            <button 
              type="submit" 
              className="register-button" 
              disabled={isLoading || !validPassword}
              style={{ opacity: (isLoading || !validPassword) ? 0.6 : 1 }}
            >
              {isLoading ? "Creating account..." : "Register!"}
            </button>
          </form>

          <button className="create-account-link" onClick={() => navigate("/login")}>
            Already have an account? Login here
          </button>
        </div>
      </div>
    </div>
  );
};

export default Register;