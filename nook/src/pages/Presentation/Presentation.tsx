import "./Presentation.css";
import logo from "../../assets/logo.svg";
import deco1 from "../../assets/Decoration1.svg";
import { useNavigation } from "../../services/NavigationContext";
function Presentation() {
  const { navigateTo } = useNavigation(); 

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
            onClick={() => navigateTo("rooms")}
          >
            Start studying!
          </button>
        </div>
      </div>
    </div>
  );
}

export default Presentation;