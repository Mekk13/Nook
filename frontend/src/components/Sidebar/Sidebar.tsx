import React, { useRef, useEffect } from "react";
import "./Sidebar.css";
import { useNavigation } from "../../services/NavigationContext";
import { useAuthStore } from "../../stores/useAuthStore";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose}) => {
  const sidebarRef = useRef<HTMLDivElement>(null);
  const { navigateTo } = useNavigation();
  const { isAuthenticated } = useAuthStore();
  const loggedIn = isAuthenticated();

  const handleNav = (page: string) => {
    navigateTo(page);
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
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [onClose]);

  return (
    <aside ref={sidebarRef} className={`sidebar ${isOpen ? "open" : ""}`}>
      <button className="close-btn" onClick={onClose}>
        x
      </button>
      <nav>
        {loggedIn ? (
      <div className="sidebar-buttons">
        <button onClick={() => handleNav("home")}>Home</button>
        <button onClick={() => handleNav("rooms")}>My Rooms</button>
      </div>
    ) : (
      <div className="sidebar-buttons">
        <button onClick={() => handleNav("login")}>Login</button>
        <button onClick={() => handleNav("register")}>Sign Up</button>
        <button onClick={() => handleNav("presentation")}>About</button>
      </div>
    )}
      </nav>
    </aside>
  );
};

export default Sidebar;
