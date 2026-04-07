import React, { useState } from "react";
import "./Register.css"; // Using your existing CSS file
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import { useNavigation } from "../../services/NavigationContext";
import Sidebar from "../../components/Sidebar/Sidebar";

const Register: React.FC = () => {
  const { navigateTo } = useNavigation();
  const [isSidebarOpen, setSidebarOpen] = useState<boolean>(false);

  return (
    <div className="register-page">
      <Sidebar 
        isOpen={isSidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
        isLoggedIn={false} 
      />

      <div className="circle circle--small"></div>
      <div className="bottom-decoration">
        <img src={deco1} alt="decoration1" />
      </div>

      <div className="register-container">
        <div className="register-logo">
          <img src={starLogo} alt="Nook logo" />
        </div>

        {/* Added 'register-card' class for the height adjustment */}
        <div className="register-card">
          <h1 className="register-text">Register</h1>
          <h2 className="register-subtitle">Enter your details below:</h2>

          <div className="register-form">
            <div className="register-group1">
              <label>Full Name:</label>
              <input type="text" />
            </div>

            <div className="register-group1">
              <label>Username:</label>
              <input type="text" />
            </div>

            <div className="register-group1">
              <label>Email:</label>
              <input type="email" />
            </div>

            <div className="register-group1">
              <label>Password:</label>
              <div className="register-column1">
                <input type="password" />
              </div>
            </div>

            <div className="password-hint">
                <label>(must be at least 8 character long)</label>
            </div>

            <button className="register-button" onClick={() => navigateTo("rooms")}>
              Register!
            </button>
          </div>

          <button className="create-account-link" onClick={() => navigateTo("login")}>
            Or click here if you already have an account
          </button>
        </div>
      </div>
    </div>
  );
};

export default Register;