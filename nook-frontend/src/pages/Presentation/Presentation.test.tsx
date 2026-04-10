import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import Presentation from "./Presentation";

const mockNavigate = vi.fn();

vi.mock("../../services/NavigationContext", () => ({
  useNavigation: () => ({
    navigateTo: mockNavigate,
  }),
}));

describe("Presentation", () => {

  it("renders title and button", () => {
    render(<Presentation />);

    expect(screen.getByText("Nook")).toBeInTheDocument();
    expect(screen.getByText("Start studying!")).toBeInTheDocument();
  });

  it("navigates to rooms when button clicked", () => {
    render(<Presentation />);

    fireEvent.click(screen.getByText("Start studying!"));

    expect(mockNavigate).toHaveBeenCalledWith("rooms");
  });

});