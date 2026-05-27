import { useEffect, useRef } from 'react';
import { useAuthStore } from '../stores/useAuthStore';

export const useInactivityLogout = (timeoutMs: number = 600000) => { // Default 10 mins
  const logout = useAuthStore((state) => state.logout);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const resetTimer = () => {
    if (timerRef.current) clearTimeout(timerRef.current);
    
    timerRef.current = setTimeout(() => {
      console.log("User inactive. Logging out...");
      logout();
      window.location.href = '/login'; // Force redirect to login
    }, timeoutMs);
  };

  useEffect(() => {
    // List of events that count as "activity"
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];

    // Start the initial timer
    resetTimer();

    // Add listeners to reset the timer on any activity
    events.forEach((event) => {
      window.addEventListener(event, resetTimer);
    });

    // Cleanup: remove listeners and clear timer when component unmounts
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      events.forEach((event) => {
        window.removeEventListener(event, resetTimer);
      });
    };
  }, [logout, timeoutMs]);
};