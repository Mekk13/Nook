import { create } from 'zustand';

interface NetworkStore {
  isOnline: boolean;
  setOnline: (status: boolean) => void;
}

export const useNetworkStore = create<NetworkStore>((set) => ({
  isOnline: navigator.onLine,
  setOnline: (status) => set({ isOnline: status }),
}));

const PING_URL = `${import.meta.env.VITE_API_URL}/api/rooms?page=0&size=1`;
const PING_INTERVAL = 1000;

async function checkServerReachable(): Promise<boolean> {
  try {
    const res = await fetch(`${PING_URL}&_=${Date.now()}`, { 
      method: 'GET',
      cache: 'no-store'
    });
    return res.ok;
  } catch (e) {
    return false;
  }
}


export function startNetworkMonitor() {
  console.log("startNetworkMonitor called");
  const { setOnline } = useNetworkStore.getState();

  const update = (status: boolean) => {
  (window as any).__networkOnline = status;
  setOnline(status);
};

  window.addEventListener('online', async () => {
    const reachable = await checkServerReachable();
    update(reachable);
  });

  window.addEventListener('offline', () => update(false));

  setInterval(async () => {
    const reachable = await checkServerReachable();
    update(reachable);
  }, PING_INTERVAL);

  checkServerReachable().then(update);
}