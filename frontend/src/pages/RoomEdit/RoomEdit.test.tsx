import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import RoomEdit from "./RoomEdit";

const mockNavigate = vi.fn();
const mockSave = vi.fn();
const mockSetName = vi.fn();
const mockSetDescription = vi.fn();
const mockSetStatus = vi.fn();
const mockIncrement = vi.fn();
const mockDecrement = vi.fn();

let mockRoomData: any = null;
let mockErrors: any = {};

vi.mock("../../services/NavigationContext", () => ({
  useNavigation: () => ({
    navigateTo: mockNavigate,
  }),
}));

vi.mock("../../hooks/useRoomEdit", () => ({
  useRoomEdit: () => ({
    roomToEdit: mockRoomData,
    name: "Study Room",
    setName: mockSetName,
    description: "Original Description",
    setDescription: mockSetDescription,
    maxParticipants: 5,
    increment: mockIncrement,
    decrement: mockDecrement,
    status: "Public",
    setStatus: mockSetStatus,
    save: mockSave,
    errors: mockErrors,
  }),
}));

describe("RoomEdit Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockRoomData = { id: "1", name: "Study Room" };
    mockErrors = {};
  });

  it("renders 'Room Not Found' when roomToEdit is null", () => {
    mockRoomData = null;
    render(<RoomEdit />);
    expect(screen.getByText("Room Not Found")).toBeDefined();
  });

  it("renders full form and handles all interactions", () => {
    mockSave.mockReturnValue(true);
    render(<RoomEdit />);

    expect(screen.getByText("EDIT ROOM")).toBeDefined();

    const nameInput = screen.getByDisplayValue("Study Room");
    fireEvent.change(nameInput, { target: { value: "New Name" } });
    expect(mockSetName).toHaveBeenCalled();

    fireEvent.click(screen.getByText("+"));
    expect(mockIncrement).toHaveBeenCalled();

    fireEvent.click(screen.getByText("-"));
    expect(mockDecrement).toHaveBeenCalled();

    const descInput = screen.getByDisplayValue("Original Description");
    fireEvent.change(descInput, { target: { value: "New Desc" } });
    expect(mockSetDescription).toHaveBeenCalled();

    const select = screen.getByRole("combobox");
    fireEvent.change(select, { target: { value: "Private" } });
    expect(mockSetStatus).toHaveBeenCalledWith("Private");

    fireEvent.click(screen.getByText("Save Changes"));
    expect(mockSave).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith("rooms");

    fireEvent.click(screen.getByText("Cancel"));
    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

  it("shows error messages", () => {
    mockErrors = { name: "Name is required", desc: "Desc is too short" };
    render(<RoomEdit />);
    
    expect(screen.getByText("Name is required")).toBeDefined();
    expect(screen.getByText("Desc is too short")).toBeDefined();
  });

  it("does not navigate if save fails", () => {
    mockSave.mockReturnValue(false);
    render(<RoomEdit />);
    
    fireEvent.click(screen.getByText("Save Changes"));
    expect(mockSave).toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalledWith("rooms");
  });
});