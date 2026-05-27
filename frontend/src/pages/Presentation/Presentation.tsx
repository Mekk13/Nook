import "./Presentation.css";
import logo from "../../assets/logo.svg";
import deco1 from "../../assets/Decoration1.svg";
import { useAuthStore } from "../../stores/useAuthStore";
import { useNavigate } from "react-router-dom";


function Presentation() {
  const navigate = useNavigate(); 
  const { isAuthenticated } = useAuthStore();

  const handleStart = () => {
  if (isAuthenticated()) {
    navigate("/home");
  } else {
    navigate("/login"); 
  }
};

  return (
    <div className="presentation">
      <div className="circle circle--big"></div>
      <div className="bottom-decoration">
        <img src={deco1} alt="decoration1" />
      </div>

      <div className="presentation__content">
        <div className="presentation__logo">
          <img src={logo} alt="Nook logo" />
        </div>

        <div className="presentation__text">
          <h1 className="presentation__title">Nook</h1>
          <h5 className="presentation__tagline">~ Your cozy study lounge ~</h5>
          <p className="presentation__descr">
            A collaborative study platform where users join virtual
            lounges, study sessions and stay motivated together.
          </p>
          
          <button 
            className="presentation__button" 
            onClick={handleStart}
          >
            Start studying!
          </button>
        </div>
      </div>
    </div>
  );
}

export default Presentation;