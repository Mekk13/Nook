import { Routes, Route, Navigate } from "react-router-dom";
import MainLayout from "./components/MainLayout/MainLayout";
import Presentation from "./pages/Presentation/Presentation";
import RoomMaster from "./pages/RoomMaster/RoomMaster";
import CreateRoom from "./pages/RoomCreate/RoomCreate";
import RoomDetail from "./pages/RoomDetail/RoomDetail";
import RoomEdit from "./pages/RoomEdit/RoomEdit";
import Login from "./pages/Login/Login";
import Register from "./pages/Register/Register";
import Lobby from "./pages/Lobby/Lobby";
import Home from "./pages/Home/Home";
import { useAuthStore } from "./stores/useAuthStore";
import type { JSX } from "react";
import AdminPanel from "./pages/AdminPanel/AdminPanel";
import { useInactivityLogout } from "./hooks/useInactivityLogout";
import ResetPasswordPage from "./pages/ResetPasswordPage/ResetPasswordPage";
import MagicLoginPage from "./pages/MagicLoginPage/MagicLoginPage";
import OAuthCallback from "./pages/OAuthCallback/OAuthcallback";
import Settings from "./pages/Settings/Settings";
import MagicRequestPage from "./pages/MagicRequestPage/MagicRequestPage";
import MfaSetup from "./pages/MfaSetup/MfaSetup";

function ProtectedRoute({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((state) => !!state.token);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

function GuestRoute({ children }: { children: JSX.Element }) {
  const isAuthenticated = useAuthStore((state) => !!state.token);
  if (isAuthenticated) return <Navigate to="/home" replace />;
  return children;
}

function AdminRoute({ children }: { children: JSX.Element }) {
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => !!state.token);
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "ADMIN") return <Navigate to="/home" replace />;
  return children;
}

function App() {
  console.log("My API URL is:", import.meta.env.VITE_API_URL);
  useInactivityLogout(600000);
  return (
    <Routes>
      <Route path="/magic-request" element={<MagicRequestPage />} />
      {/* Guest only */}
      <Route
        path="/"
        element={
          <GuestRoute>
            <Presentation />
          </GuestRoute>
        }
      />
      <Route
        path="/login"
        element={
          <GuestRoute>
            <Login />
          </GuestRoute>
        }
      />
      <Route
        path="/register"
        element={
          <GuestRoute>
            <Register />
          </GuestRoute>
        }
      />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/oauth/callback/:provider" element={<OAuthCallback />} />
      <Route path="/magic-login" element={<MagicLoginPage />} />
      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Settings />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings/mfa-setup"
        element={
          <ProtectedRoute>
            <MfaSetup />
          </ProtectedRoute>
        }
      />
      {/* Protected */}
      <Route
        path="/home"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Home />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/rooms"
        element={
          <ProtectedRoute>
            <MainLayout>
              <RoomMaster />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/rooms/create"
        element={
          <ProtectedRoute>
            <MainLayout>
              <CreateRoom />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/rooms/:id"
        element={
          <ProtectedRoute>
            <MainLayout>
              <RoomDetail />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/rooms/:id/edit"
        element={
          <ProtectedRoute>
            <MainLayout>
              <RoomEdit />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/rooms/:id/lobby"
        element={
          <ProtectedRoute>
            <MainLayout>
              <Lobby />
            </MainLayout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <AdminRoute>
            <AdminPanel />
          </AdminRoute>
        }
      />
      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
