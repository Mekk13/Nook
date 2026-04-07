import './MenuButton.css';

interface MenuButtonProps {
  onClick: () => void;
}

const MenuButton : React.FC<MenuButtonProps> = ({ onClick }) =>{
    return(
        <button className="menu-button" onClick={onClick}>
            <div className="menu-line"></div>
            <div className="menu-line"></div>
            <div className="menu-line"></div>
        </button>
    );
};

export default MenuButton;