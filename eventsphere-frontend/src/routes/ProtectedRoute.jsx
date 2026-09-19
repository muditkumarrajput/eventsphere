import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth";

function ProtectedRoute({ allowedRoles }) {
    const { isAuthenticated, role } = useAuth();
    const location = useLocation();

    if (!isAuthenticated) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: location }}
            />
        );
    }

    if (allowedRoles) {
        const userRoles = Array.isArray(role)
            ? role
            : [role];

        const hasAllowedRole = userRoles.some(
            (userRole) => {
                const normalizedRole =
                    String(userRole)
                        .replace("ROLE_", "")
                        .toUpperCase();

                return allowedRoles.includes(
                    normalizedRole
                );
            }
        );

        if (!hasAllowedRole) {
            return (
                <Navigate
                    to="/events"
                    replace
                />
            );
        }
    }

    return <Outlet />;
}

export default ProtectedRoute;
