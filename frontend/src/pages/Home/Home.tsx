import { useNavigation } from "../../services/NavigationContext";
import "./Home.css";

function Home() {
  const handleToggleStats = () => {
    console.log("This will slide up the stats section later!");
  };
  const { navigateTo } = useNavigation();

  return (
    <div className="home-container">
      <div className="home-content">
        <h2 className="home-greeting">Good day, Alex!</h2>
        <p className="home-subtext">How would you like to study today?</p>
        <div className="card-group">
          <button className="nook-card" onClick={() => navigateTo("lobby")}>
            <span className="card-label">Enter the</span>
            <span className="card-title">Quiet Nook</span>
          </button>
          <button className="nook-card" onClick={() => navigateTo("lobby")}>
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