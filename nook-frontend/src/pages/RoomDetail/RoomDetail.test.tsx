import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import RoomDetail from "./RoomDetail";

const mockNavigate = vi.fn();
let mockRoom: any = null;

vi.mock("../../services/NavigationContext", () => ({
  useNavigation: () => ({
    navigateTo: mockNavigate,
  }),
}));

vi.mock("../../hooks/useRoomDetail", () => ({
  useRoomDetail: () => ({
    room: mockRoom,
    displayDate: "01/01/2024",
  }),
}));

describe("RoomDetail", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders 'Room not found' when room is null", () => {
    mockRoom = null;
    render(<RoomDetail />);
    
    expect(screen.getByText("Room not found!")).toBeDefined();
    
    const backBtn = screen.getByText("Go Back");
    fireEvent.click(backBtn);
    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

  it("renders room details and handles back navigation", () => {
    mockRoom = {
      name: "Test Room",
      creator: "Tester",
      participants: "1/5",
      status: "Public",
      description: "Testing",
    };
    
    render(<RoomDetail />);

    expect(screen.getByText("Test Room")).toBeDefined();
    expect(screen.getByText("Tester")).toBeDefined();
    expect(screen.getByText("Testing")).toBeDefined();
    expect(screen.getByText("01/01/2024")).toBeDefined();

    const backBtn = screen.getByText("Back");
    fireEvent.click(backBtn);
    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

  it("renders default description if empty", () => {
    mockRoom = {
      name: "Empty Room",
      creator: "Tester",
      participants: "1/5",
      status: "Public",
      description: "",
    };
    
    render(<RoomDetail />);
    expect(screen.getByText("No description provided.")).toBeDefined();
  });
});