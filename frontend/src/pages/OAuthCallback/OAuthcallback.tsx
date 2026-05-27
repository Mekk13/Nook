import { useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuthStore } from "../../stores/useAuthStore";
import './OAuthCallback.css'

function OAuthCallback() {
  const { provider } = useParams<{ provider: string }>();
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);
  const called = useRef(false);

  useEffect(() => {
    if (called.current) return;
    called.current = true;

    const code = new URLSearchParams(window.location.search).get("code");
    if (!code) {
      navigate("/login");
      return;
    }

    fetch(`${import.meta.env.VITE_API_URL}/api/auth/oauth/${provider}/callback?code=${code}`)
      .then((res) => {
        if (!res.ok) throw new Error();
        return res.json();
      })
      .then((data) => {
        setAuth(data);
        navigate("/home");
      })
      .catch(() => {
        alert("OAuth login failed. Please try again.");
        navigate("/login");
      });
  }, []);

  return (
  <div className="oauth-callback-page">
    <p className="oauth-callback-text">Logging you in...</p>
  </div>
);
}

export default OAuthCallback;