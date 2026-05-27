import { useEffect } from "react";
import { useRoomStore } from "../stores/roomStore";
import { useNetworkStore } from "../stores/useNetworkStore";

export function useSyncManager() {
  const isOnline = useNetworkStore((state) => state.isOnline);
  const flushQueue = useRoomStore((state) => state.flushQueue);
  const fetchRooms = useRoomStore((state) => state.fetchRooms);

  useEffect(() => {
    if (isOnline) {
      console.log("Back online — syncing...");
      flushQueue().then(() => fetchRooms(0, 5));
    }
  }, [isOnline]);
}