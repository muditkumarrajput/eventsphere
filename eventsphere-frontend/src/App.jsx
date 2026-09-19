import { BrowserRouter, Routes, Route } from "react-router-dom";

import Navbar from "./components/Navbar";
import AuthProvider from "./context/AuthContext.jsx";
import ProtectedRoute from "./routes/ProtectedRoute";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Events from "./pages/Events";
import EventDetails from "./pages/EventDetails";
import Booking from "./pages/Booking";
import Payment from "./pages/Payment";
import MyBookings from "./pages/MyBookings";
import BookingDetails from "./pages/BookingDetails";
import AdminBookingDetails from "./pages/AdminBookingDetails";
import Favorites from "./pages/Favorites";
import Notifications from "./pages/Notifications";
import OrganizerDashboard from "./pages/OrganizerDashboard";
import OrganizerRequest from "./pages/OrganizerRequest";
import AdminBookings from "./pages/AdminBookings";
import AdminOrganizerRequests from "./pages/AdminOrganizerRequests";

function App() {
    return (
        <AuthProvider>

            <BrowserRouter>

                <Navbar />

                <Routes>

                    {/* =====================================================
                        PUBLIC ROUTES
                    ===================================================== */}

                    <Route
                        path="/"
                        element={<Home />}
                    />

                    <Route
                        path="/login"
                        element={<Login />}
                    />

                    <Route
                        path="/register"
                        element={<Register />}
                    />

                    <Route
                        path="/events"
                        element={<Events />}
                    />

                    <Route
                        path="/events/:id"
                        element={<EventDetails />}
                    />


                    {/* =====================================================
                        USER-ONLY BOOKING ROUTE
                    ===================================================== */}

                    <Route
                        element={
                            <ProtectedRoute
                                allowedRoles={[
                                    "USER"
                                ]}
                            />
                        }
                    >

                        <Route
                            path="/events/:id/book"
                            element={<Booking />}
                        />

                    </Route>


                    {/* =====================================================
                        AUTHENTICATED ROUTES
                    ===================================================== */}

                    <Route
                        element={
                            <ProtectedRoute />
                        }
                    >

                        <Route
                            path="/payments/:id"
                            element={<Payment />}
                        />

                        <Route
                            path="/bookings"
                            element={<MyBookings />}
                        />

                        <Route
                            path="/bookings/:id"
                            element={<BookingDetails />}
                        />

                        <Route
                            path="/favorites"
                            element={<Favorites />}
                        />

                        <Route
                            path="/notifications"
                            element={<Notifications />}
                        />

                        <Route
                            path="/organizer-request"
                            element={<OrganizerRequest />}
                        />

                    </Route>


                    {/* =====================================================
                        ORGANIZER / ADMIN ROUTES
                    ===================================================== */}

                    <Route
                        element={
                            <ProtectedRoute
                                allowedRoles={[
                                    "ADMIN",
                                    "ORGANIZER"
                                ]}
                            />
                        }
                    >

                        <Route
                            path="/organizer"
                            element={
                                <OrganizerDashboard />
                            }
                        />

                    </Route>


                    {/* =====================================================
                        ADMIN-ONLY ROUTES
                    ===================================================== */}

                    <Route
                        element={
                            <ProtectedRoute
                                allowedRoles={[
                                    "ADMIN"
                                ]}
                            />
                        }
                    >

                        <Route
                            path="/admin/bookings"
                            element={
                                <AdminBookings />
                            }
                        />

                        <Route
                            path="/admin/bookings/:id"
                            element={
                                <AdminBookingDetails />
                            }
                        />

                        <Route
                            path="/admin/organizer-requests"
                            element={
                                <AdminOrganizerRequests />
                            }
                        />

                    </Route>

                </Routes>

            </BrowserRouter>

        </AuthProvider>
    );
}

export default App;
