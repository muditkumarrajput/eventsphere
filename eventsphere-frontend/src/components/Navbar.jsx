import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.jsx";

function Navbar() {
    const {
        isAuthenticated,
        role,
        logout
    } = useAuth();

    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const normalizedRoles =
        Array.isArray(role)
            ? role
            : [role];

    const canAccessOrganizer =
        normalizedRoles.some((userRole) => {

            const normalizedRole =
                String(userRole)
                    .replace("ROLE_", "")
                    .toUpperCase();

            return (
                normalizedRole === "ADMIN" ||
                normalizedRole === "ORGANIZER"
            );
        });

    const isUser =
        normalizedRoles.some((userRole) => {

            const normalizedRole =
                String(userRole)
                    .replace("ROLE_", "")
                    .toUpperCase();

            return normalizedRole === "USER";
        });

    const isAdmin =
        normalizedRoles.some((userRole) => {

            const normalizedRole =
                String(userRole)
                    .replace("ROLE_", "")
                    .toUpperCase();

            return normalizedRole === "ADMIN";
        });

    return (
        <nav>

            <h2>
                <Link to="/">
                    EventSphere
                </Link>
            </h2>

            <div>

                <Link to="/">
                    Home
                </Link>

                <Link to="/events">
                    Events
                </Link>

                {isAuthenticated && (
                    <>

                        <Link to="/bookings">
                            My Bookings
                        </Link>

                        <Link to="/favorites">
                            Favorites
                        </Link>

                        <Link to="/notifications">
                            Notifications
                        </Link>


                        {/* =================================================
                            USER
                        ================================================= */}

                        {isUser && (
                            <Link to="/organizer-request">
                                Become an Organizer
                            </Link>
                        )}


                        {/* =================================================
                            ORGANIZER / ADMIN
                        ================================================= */}

                        {canAccessOrganizer && (
                            <Link to="/organizer">
                                Organizer
                            </Link>
                        )}


                        {/* =================================================
                            ADMIN
                        ================================================= */}

                        {isAdmin && (
                            <>
                                <Link to="/admin/bookings">
                                    Bookings
                                </Link>

                                <Link to="/admin/organizer-requests">
                                    Organizer Requests
                                </Link>
                            </>
                        )}


                        <button
                            onClick={handleLogout}
                        >
                            Logout
                        </button>

                    </>
                )}

                {!isAuthenticated && (
                    <>

                        <Link to="/login">
                            Login
                        </Link>

                        <Link to="/register">
                            Register
                        </Link>

                    </>
                )}

            </div>

        </nav>
    );
}

export default Navbar;
