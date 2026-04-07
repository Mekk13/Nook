import { useState } from "react";
import { useRoomStore } from "../stores/roomStore";

export function useRoomMaster(roomsPerPage: number = 5) {
  const rooms = useRoomStore((state) => state.rooms) || [];
  const deleteRoom = useRoomStore((state) => state.remove);
  const setSelectedRoom = useRoomStore((state) => state.setSelectedRoom);
  
  const [currentPage, setCurrentPage] = useState(1);

  // Math logic
  const totalPages = Math.max(1, Math.ceil(rooms.length / roomsPerPage));
  const indexOfLastRoom = currentPage * roomsPerPage;
  const indexOfFirstRoom = indexOfLastRoom - roomsPerPage;
  const currentRooms = Array.isArray(rooms) 
    ? rooms.slice(indexOfFirstRoom, indexOfLastRoom) 
    : [];

  // Helpers
  const goToNextPage = () => {
    if (currentPage < totalPages) setCurrentPage(prev => prev + 1);
  };

  const goToPrevPage = () => {
    if (currentPage > 1) setCurrentPage(prev => prev - 1);
  };

  const isPrevDisabled = currentPage === 1;
  const isNextDisabled = currentPage >= totalPages || totalPages === 0;

  return {
    currentRooms,
    currentPage,
    totalPages,
    isPrevDisabled,
    isNextDisabled,
    goToNextPage,
    goToPrevPage,
    deleteRoom,
    setSelectedRoom,
  };
}