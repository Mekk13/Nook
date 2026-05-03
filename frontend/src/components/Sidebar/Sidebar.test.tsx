import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import Sidebar from "./Sidebar";

describe("Sidebar", () => {
  const mockOnClose = vi.fn();

  it("renders and handles all logged-out interactions", () => {
    const { unmount } = render(
      <Sidebar isOpen={true} onClose={mockOnClose} isLoggedIn={false} />
    );

    fireEvent.click(screen.getByText("Login"));
    fireEvent.click(screen.getByText("Sign Up"));
    fireEvent.click(screen.getByText("About"));

    fireEvent.mouseDown(document.body);
    expect(mockOnClose).toHaveBeenCalled();

    fireEvent.click(screen.getByText("x"));
    expect(mockOnClose).toHaveBeenCalledTimes(2);
    unmount();
  });

  it("renders and handles all logged-in interactions", () => {
    render(
      <Sidebar isOpen={true} onClose={mockOnClose} isLoggedIn={true} />
    );

    fireEvent.click(screen.getByText("My Dashboard"));
    fireEvent.click(screen.getByText("Study Lounge"));
    fireEvent.click(screen.getByText("Folders/Subjects"));
    fireEvent.click(screen.getByText("Settings"));
    fireEvent.click(screen.getByText("Logout"));

    expect(screen.getByText("Logout")).toBeDefined();
  });

  it("does not call onClose when clicking inside", () => {
    mockOnClose.mockClear();
    render(<Sidebar isOpen={true} onClose={mockOnClose} isLoggedIn={true} />);
    
    fireEvent.mouseDown(screen.getByRole("complementary"));
    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it("handles the closed state class", () => {
    const { container } = render(
      <Sidebar isOpen={false} onClose={mockOnClose} isLoggedIn={false} />
    );
    const aside = container.querySelector("aside");
    expect(aside?.classList.contains("open")).toBe(false);
  });
});