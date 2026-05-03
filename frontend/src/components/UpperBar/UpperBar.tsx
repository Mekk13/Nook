import "./UpperBar.css";
import logo from "../../assets/CuteLogo.png";
import defaultPfp from "../../assets/default-pfp.svg";
import MenuButton from "../MenuButton/MenuButton"; // Assuming it's in the same folder

interface UpperBarProps {
    userName: string;
    profilePic?: string;
    onMenuClick: () => void; // Added to handle the burger click
}

const UpperBar: React.FC<UpperBarProps> = ({ userName, profilePic, onMenuClick }) => {
    return (
        <nav className="top-bar">
            {/* Left Side: Logo and Title */}
            <div className="upper-bar__logo-group">
                <img src={logo} alt="Nook Logo" className="upper-bar__logo-img" />
                <div className="upper-bar__logo-text">
                    <h1>Nook</h1>
                    <h3>Your cozy study lounge</h3>
                </div>
            </div>

            {/* Right Side: User Profile + Menu */}
            <div className="upper-bar__actions">
                <div className="upper-bar__user-profile">
                    <img src={profilePic || defaultPfp} alt="pfp" className="upper-bar__pfp-img" />
                    <span>{userName}</span>
                </div>
                <MenuButton onClick={onMenuClick} />
            </div>
        </nav>
    );
};

export default UpperBar;