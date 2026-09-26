import { createContext, useContext, useState, useCallback } from "react";
import { api } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const applySession = useCallback((jwtResponse) => {
    localStorage.setItem("token", jwtResponse.token);
    localStorage.setItem("user", JSON.stringify(jwtResponse.user));
    setUser(jwtResponse.user);
  }, []);

  const login = useCallback(
    async (username, password) => {
      const response = await api.post("/auth/login", { username, password }, { auth: false });
      applySession(response);
      return response;
    },
    [applySession]
  );

  const register = useCallback(
    async (payload) => {
      const response = await api.post("/auth/register", payload, { auth: false });
      applySession(response);
      return response;
    },
    [applySession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
