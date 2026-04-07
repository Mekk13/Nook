import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import RoomCreate from "./RoomCreate";

const mockNavigate = vi.fn();
const mockSubmit = vi.fn();
const mockIncrement = vi.fn();
const mockDecrement = vi.fn();
let mockErrors: any = {};

vi.mock("../../services/NavigationContext", () => ({
  useNavigation: () => ({
    navigateTo: mockNavigate,
  }),
}));

vi.mock("../../hooks/useRoomCreate", () => ({
  useRoomCreate: () => ({
    maxParticipants: 5,
    increment: mockIncrement,
    decrement: mockDecrement,
    submit: mockSubmit,
    errors: mockErrors,
  }),
}));

describe("RoomCreate", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockErrors = {};
  });

  it("handles input changes and form interactions", () => {
    render(<RoomCreate />);

    const nameInput = screen.getByPlaceholderText("Enter room name...") as HTMLInputElement;
    fireEvent.change(nameInput, { target: { value: "New Study Room" } });
    expect(nameInput.value).toBe("New Study Room");

    const descInput = screen.getByPlaceholderText("What are we studying?") as HTMLTextAreaElement;
    fireEvent.change(descInput, { target: { value: "Exam Prep" } });
    expect(descInput.value).toBe("Exam Prep");

    const select = screen.getByRole("combobox") as HTMLSelectElement;
    fireEvent.change(select, { target: { value: "Private" } });
    expect(select.value).toBe("Private");

    fireEvent.click(screen.getByText("+"));
    expect(mockIncrement).toHaveBeenCalled();

    fireEvent.click(screen.getByText("-"));
    expect(mockDecrement).toHaveBeenCalled();
  });

  it("shows success window and navigates back when submit is successful", () => {
    mockSubmit.mockReturnValue(true);
    render(<RoomCreate />);

    fireEvent.click(screen.getByText("Create Room"));
    expect(mockSubmit).toHaveBeenCalled();

    expect(screen.getByText("Your cozy study lounge is ready!")).toBeDefined();

    const okBtn = screen.getByText("Awesome!");
    fireEvent.click(okBtn);
    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

  it("handles navigation cancel button", () => {
    render(<RoomCreate />);
    fireEvent.click(screen.getByText("Cancel"));
    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

  it("displays validation errors", () => {
    mockErrors = { name: "Name is required", desc: "Description is too short" };
    render(<RoomCreate />);

    expect(screen.getByText("Name is required")).toBeDefined();
    expect(screen.getByText("Description is too short")).toBeDefined();
  });

  it("does not show success window if submit fails", () => {
    mockSubmit.mockReturnValue(false);
    render(<RoomCreate />);

    fireEvent.click(screen.getByText("Create Room"));
    expect(screen.queryByText("Your cozy study lounge is ready!")).toBeNull();
  });
});