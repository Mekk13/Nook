import React, { useState } from "react";
import "./Register.css"; // Using your existing CSS file
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import { useNavigation } from "../../services/NavigationContext";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useAuthStore } from "../../stores/useAuthStore";

const Register: React.FC = () => {
  const { navigateTo } = useNavigation();
  const [isSidebarOpen, setSidebarOpen] = useState<boolean>(false);
  const setAuth = useAuthStore((state) => state.setAuth);

  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ fullName, username, email, password }),
      });

      if (response.ok) {
        const data = await response.json(); // This is the AuthResponse with the token
        setAuth(data); // Save the session immediately
        navigateTo("home"); // Go to study rooms
      } else {
        const errorData = await response.json();
        alert(errorData.message || "Registration failed. Try a different username/email.");
      }
    } catch (error) {
      alert("Could not connect to the server.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="register-page">
      <Sidebar 
        isOpen={isSidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
      />

      <div className="circle circle--small"></div>
      <div className="bottom-decoration">
        <img src={deco1} alt="decoration1" />
      </div>

      <div className="register-container">
        <div className="register-logo">
          <img src={starLogo} alt="Nook logo" />
        </div>

        <div className="register-card">
          <h1 className="register-text">Register</h1>
          <h2 className="register-subtitle">Enter your details below:</h2>

          <form className="register-form" onSubmit={handleRegister}>
            <div className="register-group1">
              <label>Full Name:</label>
              <input 
                type="text" 
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
              />
            </div>

            <div className="register-group1">
              <label>Username:</label>
              <input 
                type="text" 
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="new-username"
                required
              />
            </div>

            <div className="register-group1">
              <label>Email:</label>
              <input 
                type="email" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="register-group1">
              <label>Password:</label>
              <div className="register-column1">
                <input 
                type="password" 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
                required
                minLength={8}
              />
              </div>
            </div>

            <div className="password-hint">
                <label>(must be at least 8 character long)</label>
            </div>

            <button type="submit" className="register-button" disabled={isLoading}>
              {isLoading ? "creating account..." : "Register!"}
            </button>
          </form>

          <button className="create-account-link" onClick={() => navigateTo("login")}>
            Or click here if you already have an account
          </button>
        </div>
      </div>
    </div>
  );
};

export default Register;