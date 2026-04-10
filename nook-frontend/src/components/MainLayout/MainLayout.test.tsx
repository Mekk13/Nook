import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import MainLayout from "./MainLayout";

vi.mock("../Sidebar/Sidebar", () => ({
  default: ({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) => (
    <aside role="complementary" className={isOpen ? "open" : "closed"}>
      <button onClick={onClose}>Close Sidebar</button>
    </aside>
  ),
}));

vi.mock("../MenuButton/MenuButton", () => ({
  default: ({ onClick }: { onClick: () => void }) => (
    <button onClick={onClick}>Menu Toggle</button>
  ),
}));

describe("MainLayout", () => {
  it("renders children correctly", () => {
    render(
      <MainLayout>
        <div data-testid="child">Hello World</div>
      </MainLayout>
    );
    expect(screen.getByTestId("child")).toBeDefined();
  });

  it("toggles sidebar state when menu button is clicked", () => {
    render(
      <MainLayout>
        <div>Content</div>
      </MainLayout>
    );

    const toggleBtn = screen.getByText("Menu Toggle");
    const sidebar = screen.getByRole("complementary", { hidden: true }); 

    expect(sidebar.className).toBe("closed");

    fireEvent.click(toggleBtn);
    expect(sidebar.className).toBe("open");

    fireEvent.click(toggleBtn);
    expect(sidebar.className).toBe("closed");
  });

  it("closes sidebar when onClose is called", () => {
    render(
      <MainLayout>
        <div>Content</div>
      </MainLayout>
    );

    const toggleBtn = screen.getByText("Menu Toggle");
    fireEvent.click(toggleBtn); 
    
    const sidebar = screen.getByRole("complementary");
    expect(sidebar.className).toBe("open");

    // Click the close button inside our mock
    const closeBtn = screen.getByText("Close Sidebar");
    fireEvent.click(closeBtn);
    
    expect(sidebar.className).toBe("closed");
  });
});