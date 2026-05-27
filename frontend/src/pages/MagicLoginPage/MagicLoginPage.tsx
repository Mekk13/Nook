import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../stores/useAuthStore";

function MagicLoginPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("token");
    if (!token) {
      navigate("/login");
      return;
    }

    fetch(`${import.meta.env.VITE_API_URL}/api/auth/magic-link/verify?token=${token}`)
      .then((res) => {
        if (!res.ok) throw new Error();
        return res.json();
      })
      .then((data) => {
        setAuth(data);
        navigate("/home");
      })
      .catch(() => {
        alert("This link is invalid or has expired.");
        navigate("/login");
      });
  }, []);

  return <p style={{ textAlign: "center", marginTop: "40vh", fontFamily: "Baloo, cursive" }}>Logging you in...</p>;
}

export default MagicLoginPage;