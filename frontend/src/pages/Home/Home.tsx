import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../stores/useAuthStore";
import "./Home.css";

function Home() {
  const navigate = useNavigate();
  const username = useAuthStore((state) => state.user?.username ?? "there");
  const role = useAuthStore((state) => state.user?.role);
  const handleToggleStats = () => {
    console.log("This will slide up the stats section later!");
  };

  return (
    <div className="home-container">
      <div className="home-content">
        <h2 className="home-greeting">Good day, {username}</h2>
        <p className="home-subtext">How would you like to study today?</p>
        <div className="card-group">
          {role === "ADMIN" && (
            <button
              className="nook-card admin-card"
              onClick={() => navigate("/admin")}
            >
              <span className="card-label">Admin</span>
              <span className="card-title">Panel</span>
            </button>
          )}
          <button className="nook-card" onClick={() => navigate("/rooms")}>
            <span className="card-label">Enter the</span>
            <span className="card-title">Quiet Nook</span>
          </button>
          <button className="nook-card" onClick={() => navigate("/rooms")}>
            <span className="card-label">Join the</span>
            <span className="card-title">Common Room</span>
          </button>
        </div>
      </div>
      <button className="stats-toggle-btn" onClick={handleToggleStats}>
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#9b9fc4"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M6 9l6 6 6-6" />
        </svg>
      </button>
    </div>
  );
}

export default Home;
