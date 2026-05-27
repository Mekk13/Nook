import { useState } from "react";

export const useForgotPassword = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error', text: string } | null>(null);

  const sendResetEmail = async (email: string) => {
    if (!email) return;
    setIsLoading(true);
    setMessage(null);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/auth/forgot-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        setMessage({ type: 'success', text: "If an account exists, a reset link has been sent! 📩" });
      } else {
        setMessage({ type: 'error', text: "Something went wrong. Please try again." });
      }
    } catch (error) {
      setMessage({ type: 'error', text: "Connection failed. Check your server." });
    } finally {
      setIsLoading(false);
    }
  };

  return { sendResetEmail, isLoading, message, setMessage };
};