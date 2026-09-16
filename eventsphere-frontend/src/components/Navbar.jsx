import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function Navbar() {
    const { isAuthenticated, role, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const normalizedRoles = Array.isArray(role)
        ? role
        : [role];

    const canAccessOrganizer = normalizedRoles.some(
        (userRole) => {
            const normalizedRole = String(userRole)
                .replace("ROLE_", "")
                .toUpperCase();

            return (
                normalizedRole === "ADMIN" ||
                normalizedRole === "ORGANIZER"
            );
        }
    );

    return (
        <nav>
            <h2>
                <Link to="/">EventSphere</Link>
            </h2>

            <div>
                <Link to="/">Home</Link>
                <Link to="/events">Events</Link>

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

                        {canAccessOrganizer && (
                            <Link to="/organizer">
                                Organizer
                            </Link>
                        )}

                        <button onClick={handleLogout}>
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
