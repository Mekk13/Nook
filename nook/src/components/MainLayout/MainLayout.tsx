import { useState } from "react";
import Sidebar from "../Sidebar/Sidebar";
import MenuButton from "../MenuButton/MenuButton";

function MainLayout({ children }: { children: React.ReactNode }) {
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  const handleToggleSidebar = () => setSidebarOpen(!isSidebarOpen);

  return (
    <div className="main-layout">
      <MenuButton onClick={handleToggleSidebar} />

      <Sidebar 
        isOpen={isSidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
        isLoggedIn={false} 
      />

      <main className="page-content">
        {children}
      </main>
    </div>
  );
}

export default MainLayout;