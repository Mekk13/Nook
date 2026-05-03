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
import Home from "./pages/Home/Home";
import { useAuthStore } from "./stores/useAuthStore";

const pages: Record<string, JSX.Element> = {
  presentation: <Presentation />,
  login: <Login />,
  register: <Register />,
  rooms: <RoomMaster />,
  create: <CreateRoom />, 
  detail: <RoomDetail />,
  edit: <RoomEdit />,
  lobby: <Lobby/>,
  home: <Home/>,
};

const RouterOutlet = () => {
  const { view } = useNavigation();
  const isAuthenticated = useAuthStore((state) => !!state.token);

  const authPages = ["login", "register"];
  const publicPages = ["presentation", ...authPages];
  const isPublic = publicPages.includes(view);

  if(!isPublic && !isAuthenticated){
    return <Login />
  }
  return pages[view] || <div>Page not found</div>;
};

const AppContent = () => {
  const {view} = useNavigation();
  const isAuthenticated = useAuthStore((state) => !!state.token);
  const showLayout = isAuthenticated && view !== "presentation";
  return showLayout ? (
    <MainLayout>
      <RouterOutlet />
    </MainLayout>
  ) : (
    <RouterOutlet />
  );
}

function App() {
    return (
    <NavigationProvider>
      <AppContent />
    </NavigationProvider>
  );
}

export default App;