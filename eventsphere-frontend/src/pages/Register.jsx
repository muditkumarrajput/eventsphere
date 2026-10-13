
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
        phoneNumber: "",
    });

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleRegister = async (event) => {
        event.preventDefault();

        if (loading) return;

        setMessage("");
        setError("");

        const name = formData.name.trim();
        const email = formData.email.trim().toLowerCase();
        const phoneNumber = formData.phoneNumber.trim();

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            setError("Enter a valid email address.");
            return;
        }

        if (!/^[6-9]\d{9}$/.test(phoneNumber)) {
            setError("Enter a valid 10-digit Indian mobile number.");
            return;
        }

        if (formData.password.length < 8) {
            setError("Password must contain at least 8 characters.");
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await api.post("/auth/register", {
                name,
                email,
                phoneNumber,
                password: formData.password,
                confirmPassword: formData.confirmPassword,
            });

            setMessage("Registration successful! Redirecting to login...");

            setTimeout(() => {
                navigate("/login");
            }, 1200);
        } catch (error) {
            console.error("Registration failed:", error);

            setError(
                error.response?.data?.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h1>Create Account</h1>

            <form onSubmit={handleRegister} className="auth-form">
                <input
                    type="text"
                    name="name"
                    placeholder="Name"
                    value={formData.name}
                    onChange={handleChange}
                    autoComplete="name"
                    disabled={loading}
                    required
                />

                <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={formData.email}
                    onChange={handleChange}
                    autoComplete="email"
                    disabled={loading}
                    required
                />

                <div style={{ position: "relative", width: "100%" }}>
                    <input
                        type={showPassword ? "text" : "password"}
                        name="password"
                        placeholder="Password (minimum 8 characters)"
                        value={formData.password}
                        onChange={handleChange}
                        autoComplete="new-password"
                        minLength={8}
                        style={{ width: "100%", boxSizing: "border-box", paddingRight: "48px" }}
                        disabled={loading}
                        required
                    />

                    <button
                        type="button"
                        onClick={() => setShowPassword((previous) => !previous)}
                        aria-label={showPassword ? "Hide password" : "Show password"}
                        title={showPassword ? "Hide password" : "Show password"}
                        style={{
                            position: "absolute",
                            right: "8px",
                            top: "50%",
                            transform: "translateY(-50%)",
                            border: "none",
                            background: "transparent",
                            cursor: "pointer",
                            padding: "6px",
                        }}
                    >
                        {showPassword ? "🙈" : "👁️"}
                    </button>
                </div>

                <div style={{ position: "relative", width: "100%" }}>
                    <input
                        type={showConfirmPassword ? "text" : "password"}
                        name="confirmPassword"
                        placeholder="Confirm Password"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        autoComplete="new-password"
                        minLength={8}
                        style={{ width: "100%", boxSizing: "border-box", paddingRight: "48px" }}
                        disabled={loading}
                        required
                    />

                    <button
                        type="button"
                        onClick={() => setShowConfirmPassword((previous) => !previous)}
                        aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                        title={showConfirmPassword ? "Hide password" : "Show password"}
                        style={{
                            position: "absolute",
                            right: "8px",
                            top: "50%",
                            transform: "translateY(-50%)",
                            border: "none",
                            background: "transparent",
                            cursor: "pointer",
                            padding: "6px",
                        }}
                    >
                        {showConfirmPassword ? "🙈" : "👁️"}
                    </button>
                </div>

                <input
                    type="tel"
                    name="phoneNumber"
                    placeholder="10-digit Indian mobile number"
                    value={formData.phoneNumber}
                    onChange={(event) => {
                        const value = event.target.value
                            .replace(/\D/g, "")
                            .slice(0, 10);

                        setFormData((previous) => ({
                            ...previous,
                            phoneNumber: value,
                        }));
                    }}
                    pattern="[6-9][0-9]{9}"
                    maxLength={10}
                    inputMode="numeric"
                    autoComplete="tel-national"
                    disabled={loading}
                    required
                />

                <button type="submit" disabled={loading}>
                    {loading ? "Creating Account..." : "Register"}
                </button>
            </form>

            {message && (
                <p className="success-message">{message}</p>
            )}

            {error && (
                <p className="error-message">{error}</p>
            )}
        </div>
    );
}

export default Register;