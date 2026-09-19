import { useState } from "react";
import { AuthContext } from "./AuthContext.js";

function getRoleFromToken(token) {
    if (!token) {
        return null;
    }

    try {
        const payload = JSON.parse(
            atob(token.split(".")[1])
        );

        return payload.role || payload.roles || null;
    } catch (error) {
        console.error(
            "Failed to decode JWT:",
            error
        );

        return null;
    }
}

function AuthProvider({ children }) {
    const [token, setToken] = useState(
        localStorage.getItem("token")
    );

    const [role, setRole] = useState(
        getRoleFromToken(
            localStorage.getItem("token")
        )
    );

    const login = (jwtToken) => {
        localStorage.setItem("token", jwtToken);

        setToken(jwtToken);
        setRole(getRoleFromToken(jwtToken));
    };

    const logout = () => {
        localStorage.removeItem("token");

        setToken(null);
        setRole(null);
    };

    const isAuthenticated = !!token;

    return (
        <AuthContext.Provider
            value={{
                token,
                role,
                isAuthenticated,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export default AuthProvider;
