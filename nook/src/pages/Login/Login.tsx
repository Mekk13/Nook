import "./Login.css";
import deco1 from "../../assets/Decoration1.svg";
import starLogo from "../../assets/SoftLogo.png";
import { useNavigation } from "../../services/NavigationContext";
import Sidebar from "../../components/Sidebar/Sidebar";
import { useState } from "react";

function Login() {
  const { navigateTo } = useNavigation();
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="login-page">
      <Sidebar 
        isOpen={isSidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
        isLoggedIn={false} 
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

          <div className="login-form">
            <div className="input-group">
              <label>Username</label>
              <input type="text" />
            </div>
            <div className="input-group">
              <label>Password</label>
              <input type="password" />
            </div>
            <button className="go-button" onClick={() => navigateTo("rooms")}>
              go!
            </button>
          </div>

          <button className="create-account-link" onClick={() => navigateTo("register")}>
            Or create an account
          </button>
        </div>
      </div>
    </div>
  );
}

export default Login;