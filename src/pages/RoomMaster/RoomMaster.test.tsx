import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import type {Mock} from 'vitest';
import RoomMaster from "./RoomMaster";
import * as RoomMasterHook from "../../hooks/useRoomMaster";

const mockNavigate = vi.fn();
const mockDelete = vi.fn();
const mockSetSelected = vi.fn();
const mockNext = vi.fn();
const mockPrev = vi.fn();

vi.mock("../../services/NavigationContext", () => ({
  useNavigation: () => ({
    navigateTo: mockNavigate,
  }),
}));

vi.mock("../../hooks/useRoomMaster", () => ({
  useRoomMaster: vi.fn(),
}));

describe("RoomMaster", () => {
  const useRoomMasterMock = RoomMasterHook.useRoomMaster as Mock;

  it("renders room list and header buttons", () => {
    useRoomMasterMock.mockReturnValue({
      currentRooms: [
        { id: "1", name: "Test Room", creator: "Tester", participants: "1/5" },
      ],
      currentPage: 1,
      totalPages: 2,
      isPrevDisabled: false,
      isNextDisabled: false,
      goToNextPage: mockNext,
      goToPrevPage: mockPrev,
      deleteRoom: mockDelete,
      setSelectedRoom: mockSetSelected,
    });

    render(<RoomMaster />);

    fireEvent.click(screen.getByText("+ Create Room"));
    expect(mockNavigate).toHaveBeenCalledWith("create");

    fireEvent.click(screen.getByText("← Back"));
    expect(mockNavigate).toHaveBeenCalledWith("presentation");
  });

  it("handles room actions and pagination", () => {
    useRoomMasterMock.mockReturnValue({
      currentRooms: [
        { id: "1", name: "Test Room", creator: "Tester", participants: "1/5" },
      ],
      currentPage: 1,
      totalPages: 2,
      isPrevDisabled: false,
      isNextDisabled: false,
      goToNextPage: mockNext,
      goToPrevPage: mockPrev,
      deleteRoom: mockDelete,
      setSelectedRoom: mockSetSelected,
    });

    render(<RoomMaster />);

    fireEvent.click(screen.getByText("View"));
    expect(mockSetSelected).toHaveBeenCalledWith("1");
    expect(mockNavigate).toHaveBeenCalledWith("detail");

    fireEvent.click(screen.getByText("Edit"));
    expect(mockSetSelected).toHaveBeenCalledWith("1");
    expect(mockNavigate).toHaveBeenCalledWith("edit");

    fireEvent.click(screen.getByText("Delete"));
    expect(mockDelete).toHaveBeenCalledWith("1");

    fireEvent.click(screen.getByText("< Prev"));
    expect(mockPrev).toHaveBeenCalled();

    fireEvent.click(screen.getByText("Next >"));
    expect(mockNext).toHaveBeenCalled();
  });

  it("renders disabled pagination and fallback strings correctly", () => {
    useRoomMasterMock.mockReturnValue({
      currentRooms: [
        { id: "2", name: "Empty Room", creator: "", participants: "" },
      ],
      currentPage: 1,
      totalPages: 1,
      isPrevDisabled: true,
      isNextDisabled: true,
      goToNextPage: mockNext,
      goToPrevPage: mockPrev,
      deleteRoom: mockDelete,
      setSelectedRoom: mockSetSelected,
    });

    render(<RoomMaster />);
    
    // Checks "N/A" and "0/0" fallbacks (Lines 63-64)
    expect(screen.getByText("N/A")).toBeDefined();
    expect(screen.getByText("0/0")).toBeDefined();

    // Checks disabled classes (Lines 78, 82)
    const prev = screen.getByText("< Prev");
    const next = screen.getByText("Next >");
    expect(prev.className).toContain("disabled");
    expect(next.className).toContain("disabled");
  });

  it("covers the totalPages fallback logic (Line 79)", () => {
    useRoomMasterMock.mockReturnValue({
      currentRooms: [],
      currentPage: 1,
      // Setting totalPages to 0 triggers the "|| 1" branch
      totalPages: 0,
      isPrevDisabled: true,
      isNextDisabled: true,
      goToNextPage: mockNext,
      goToPrevPage: mockPrev,
      deleteRoom: mockDelete,
      setSelectedRoom: mockSetSelected,
    });

    render(<RoomMaster />);
    
    // This confirms the "|| 1" logic rendered "Page 1 of 1" instead of "Page 1 of 0"
    expect(screen.getByText("Page 1 of 1")).toBeDefined();
  });
});