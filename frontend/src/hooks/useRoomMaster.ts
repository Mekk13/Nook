import { useState, useEffect } from "react";
import { useRoomStore } from "../stores/roomStore";

export function useRoomMaster(roomsPerPage: number = 5) {
  const rooms = useRoomStore((state) => state.rooms) || [];
  const totalPages = useRoomStore((state) => state.totalPages);
  const deleteRoom = useRoomStore((state) => state.remove);
  const setSelectedRoom = useRoomStore((state) => state.setSelectedRoom);
  const fetchRooms = useRoomStore((state) => state.fetchRooms);
  useEffect(() => {
    fetchRooms(0, 5);
  }, [fetchRooms]);

  const [currentPage, setCurrentPage] = useState(0); // backend is 0-indexed

  const goToNextPage = () => {
    const next = currentPage + 1;
    setCurrentPage(next);
    fetchRooms(next, roomsPerPage);
  };

  const goToPrevPage = () => {
    const prev = currentPage - 1;
    setCurrentPage(prev);
    fetchRooms(prev, roomsPerPage);
  };

  const isPrevDisabled = currentPage === 0;
  const isNextDisabled = currentPage >= totalPages - 1;

  return {
    currentRooms: rooms,
    currentPage: currentPage + 1, // display as 1-indexed
    totalPages,
    isPrevDisabled,
    isNextDisabled,
    goToNextPage,
    goToPrevPage,
    deleteRoom,
    setSelectedRoom,
  };
}
