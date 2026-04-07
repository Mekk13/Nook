import { NavigationProvider, useNavigation } from "./services/NavigationContext";
import MainLayout from "./components/MainLayout/MainLayout";
import Presentation from "./pages/Presentation/Presentation";
import RoomMaster from "./pages/RoomMaster/RoomMaster";
import CreateRoom from "./pages/RoomCreate/RoomCreate"; 
import type { JSX } from "react";
import RoomDetail from "./pages/RoomDetail/RoomDetail";
import RoomEdit from "./pages/RoomEdit/RoomEdit";
import Login from "./pages/Login/Login";
import Register from "./pages/Register/Register";
import Lobby from "./pages/Lobby/Lobby"

const pages: Record<string, JSX.Element> = {
  presentation: <Presentation />,
  login: <Login />,
  register: <Register />,
  rooms: <RoomMaster />,
  create: <CreateRoom />, 
  detail: <RoomDetail />,
  edit: <RoomEdit />,
  lobby: <Lobby/>
};

const RouterOutlet = () => {
  const { view } = useNavigation();
  // This will now find 'create' in the object above
  return pages[view] || <div>Page not found</div>;
};

function App() {
  return (
    <NavigationProvider>
      <MainLayout>
        <RouterOutlet />
      </MainLayout>
    </NavigationProvider>
  );
}

export default App;