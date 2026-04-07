import { createContext, useState, useContext, useEffect } from 'react';
import type { ReactNode } from 'react';
import { logLastPage } from '../hooks/useUserActivity';
import { getCookie } from '../hooks/useCookies'; 

interface NavContextType {
  view: string;
  navigateTo: (page: string) => void;
}

const NavigationContext = createContext<NavContextType | undefined>(undefined);

export function NavigationProvider({ children }: { children: ReactNode }) {
  const [view, setView] = useState("presentation");

  useEffect(() => {
    
    const savedPage = getCookie('lastPage');
    if (savedPage) {
      setView(savedPage);
    }
  }, []); 

  const navigateTo = (page: string) => {
    setView(page);
    logLastPage(page); 
  };

  return (
    <NavigationContext.Provider value={{ view, navigateTo }}>
      {children}
    </NavigationContext.Provider>
  );
}

export const useNavigation = () => {
  const context = useContext(NavigationContext);
  if (!context) throw new Error("useNavigation must be used within NavigationProvider");
  return context;
};