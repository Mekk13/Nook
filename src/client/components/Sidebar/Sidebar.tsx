import React, { useRef, useEffect } from "react";
import "./Sidebar.css";
import { useNavigation } from "../../services/NavigationContext";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
  isLoggedIn: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose, isLoggedIn }) => {
  const sidebarRef = useRef<HTMLDivElement>(null);
  const { navigateTo } = useNavigation();

  const handleNav = (page: string) => {
    navigateTo(page);
    onClose(); // Close sidebar after clicking
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
        {isLoggedIn ? (
          <div className="sidebar-buttons">
            <button onClick={() => handleNav("dashboard")}>My Dashboard</button>
            <button onClick={() => handleNav("rooms")}>Study Lounge</button>
            {/* ... other buttons */}
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
