import "./Login.css";
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import { useNavigation } from "../../services/NavigationContext";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useState } from "react";
import { useAuthStore } from "../../stores/useAuthStore";
import React from "react";

function Login() {
  const { navigateTo } = useNavigation();
  const [isSidebarOpen, setSidebarOpen] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);

  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault(); // Prevents page reload
    setIsLoading(true);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ identifier, password }), // Matches backend LoginRequest
      });

      if (response.ok) {
        const data = await response.json(); // Matches backend AuthResponse
        setAuth(data); // Save token and user info to Zustand
        navigateTo("home");
      } else {
        alert("Invalid credentials. Please try again.");
      }
    } catch (error) {
      alert("Something went wrong. Is the server running?");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-page">
      <Sidebar
        isOpen={isSidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      <div className="circle circle--small"></div>
      <div className="bottom-decoration">
        <img src={deco1} alt="decoration1" />
      </div>

      <div className="login-container">
        <div className="login-logo">
          <img src={starLogo} alt="Nook logo" />
        </div>

        <div className="login-card">
          <h1 className="welcome-text">Welcome!</h1>
          <h2 className="login-title">LOGIN</h2>

          <form className="login-form" onSubmit={handleLogin}>
            <div className="input-group">
              <label>Username</label>
              <input
                type="text"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                required
              />
            </div>
            <div className="input-group">
              <label>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="go-button" disabled={isLoading}>
              {isLoading ? "..." : "go!"}
            </button>
          </form>

          <button
            className="create-account-link"
            onClick={() => navigateTo("register")}
          >
            Or create an account
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;
