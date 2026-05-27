import React, { useRef, useEffect } from "react";
import "./Sidebar.css";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../stores/useAuthStore";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const sidebarRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { isAuthenticated, logout, user } = useAuthStore();
  const loggedIn = isAuthenticated();

  const handleNav = (path: string) => {
  onClose();
  setTimeout(() => navigate(path), 10);
};

  const handleLogout = () => {
    logout();
    navigate("/login");
    onClose();
  };

  useEffect(() => {
  function handleClickOutside(event: MouseEvent) {
    if (
      sidebarRef.current &&
      !sidebarRef.current.contains(event.target as Node)
    ) {
      onClose();
    }
  }
  document.addEventListener("mouseup", handleClickOutside);
  return () => document.removeEventListener("mouseup", handleClickOutside);
}, [onClose]);

  return (
    <aside ref={sidebarRef} className={`sidebar ${isOpen ? "open" : ""}`}>
      <div className="sidebar-header">
        <button className="close-btn" onClick={onClose}>
          ✕
        </button>
      </div>

      {loggedIn && user && (
        <div className="sidebar-profile">
          <div className="sidebar-avatar">
            {user.username?.charAt(0).toUpperCase()}
          </div>
          <p className="sidebar-username">{user.username}</p>
          <span className="sidebar-role">{user.role}</span>
        </div>
      )}

      <nav className="sidebar-nav">
        {loggedIn ? (
          <>
            <button className="sidebar-btn" onClick={() => handleNav("/home")}>
              <i className="ti ti-home"></i> Home
            </button>
            <button className="sidebar-btn" onClick={() => handleNav("/rooms")}>
              <i className="ti ti-door-enter"></i> My Rooms
            </button>
            <button
              className="sidebar-btn"
              onClick={() => handleNav("/profile")}
            >
              <i className="ti ti-user"></i> Profile
            </button>
            <button
              className="sidebar-btn"
              onClick={() => handleNav("/settings")}
            >
              <i className="ti ti-settings"></i> Settings
            </button>
            <div className="sidebar-divider" />
            <button
              className="sidebar-btn sidebar-btn--logout"
              onClick={handleLogout}
            >
              <i className="ti ti-logout"></i> Logout
            </button>
          </>
        ) : (
          <>
            <button className="sidebar-btn" onClick={() => handleNav("/login")}>
              Login
            </button>
            <button
              className="sidebar-btn"
              onClick={() => handleNav("/register")}
            >
              Sign Up
            </button>
            <button className="sidebar-btn" onClick={() => handleNav("/")}>
              About
            </button>
          </>
        )}
      </nav>
    </aside>
  );
};

export default Sidebar;
