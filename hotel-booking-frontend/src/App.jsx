import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import RoleRoute from "./components/RoleRoute";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import RoomsPage from "./pages/RoomsPage";
import BookingPage from "./pages/BookingPage";
import MyBookingsPage from "./pages/MyBookingsPage";
import ProfilePage from "./pages/ProfilePage";
import AdminRoomsPage from "./pages/admin/AdminRoomsPage";
import AdminBookingsPage from "./pages/admin/AdminBookingsPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";

const STAFF_ROLES = ["ADMIN", "MANAGER"];

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app-shell">
          <Navbar />
          <main>
            <Routes>
              <Route path="/" element={<Navigate to="/rooms" replace />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/rooms" element={<RoomsPage />} />
              <Route
                path="/rooms/:roomId/book"
                element={
                  <ProtectedRoute>
                    <BookingPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-bookings"
                element={
                  <ProtectedRoute>
                    <MyBookingsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <ProfilePage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/admin/rooms"
                element={
                  <RoleRoute roles={STAFF_ROLES}>
                    <AdminRoomsPage />
                  </RoleRoute>
                }
              />
              <Route
                path="/admin/bookings"
                element={
                  <RoleRoute roles={STAFF_ROLES}>
                    <AdminBookingsPage />
                  </RoleRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <RoleRoute roles={["ADMIN"]}>
                    <AdminUsersPage />
                  </RoleRoute>
                }
              />

              <Route path="*" element={<Navigate to="/rooms" replace />} />
            </Routes>
          </main>
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
}
