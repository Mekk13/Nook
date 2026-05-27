import { useState } from "react";
import Sidebar from "../Sidebar/Sidebar";
import UpperBar from "../UpperBar/UpperBar";
import MenuButton from "../MenuButton/MenuButton"; 
import "./MainLayout.css";
import { useSyncManager } from "../../hooks/useSyncManager";
import { useNetworkStore } from "../../stores/useNetworkStore";
import { useRoomStore } from "../../stores/roomStore";
import { useAuthStore } from "../../stores/useAuthStore";
import { useLocation } from "react-router-dom";

function MainLayout({ children }: { children: React.ReactNode }) {
  useSyncManager();
  const location = useLocation();

  const isOnline = useNetworkStore((state) => state.isOnline);
  const queueLength = useRoomStore((state) => state.offlineQueue.length);

  const [isSidebarOpen, setSidebarOpen] = useState(false);
  const isLobbyOrHome = location.pathname === "/home" || location.pathname.includes("/lobby");

  const handleToggleSidebar = () => setSidebarOpen(!isSidebarOpen);


  const username = useAuthStore((state) => state.user?.username ?? "user");

  return (
    <div className="main-layout">
      {/* 1. Lobby/Home get the UpperBar (which now contains the MenuButton) */}
      {isLobbyOrHome && (
        <UpperBar userName={username} onMenuClick={handleToggleSidebar} />
      )}

      {/* Network status banner */}
      {!isOnline && (
        <div className="offline-banner">
          ⚠ You are offline — changes will sync when reconnected
          {queueLength > 0 && ` (${queueLength} pending)`}
        </div>
      )}


      {/* 2. Show a floating MenuButton ONLY if it's NOT an Auth page AND NOT a page with an UpperBar */}
      {!isLobbyOrHome && (
        <div className="floating-menu-container">
          <MenuButton onClick={handleToggleSidebar} />
        </div>
      )}

      <Sidebar 
        isOpen={isSidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
      />

      <main className="page-content">
        {children}
      </main>
    </div>
  );
}

export default MainLayout;