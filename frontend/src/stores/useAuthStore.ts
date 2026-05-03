import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  user: {
    userId: string;
    username: string;
    fullName: string;
    avatar: string;
  } | null;
  setAuth: (data: any) => void; 
  logout: () => void;
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      setAuth: (data) => set({ 
        token: data.token, 
        user: { 
          userId: data.userId, 
          username: data.username, 
          fullName: data.fullName, 
          avatar: data.avatar 
        } 
      }),
      logout: () => set({ token: null, user: null }),
      isAuthenticated: () => !!get().token,
    }),
    { name: 'nook-auth' } 
  )
);