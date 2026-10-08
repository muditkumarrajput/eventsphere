import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function ForgotPassword() {
    const navigate = useNavigate();

    const [identifier, setIdentifier] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");
        setLoading(true);

        try {
            await api.post("/auth/forgot-password", {
                identifier
            });

            navigate("/reset-password", {
                state: {
                    identifier
                }
            });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to send OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <form className="auth-form" onSubmit={handleSubmit}>

                <h2>Forgot Password</h2>

                <p>
                    Enter your email or mobile number to receive an OTP.
                </p>

                {message && (
                    <div className="success-message">
                        {message}
                    </div>
                )}

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <input
                    type="text"
                    placeholder="Email or mobile number"
                    value={identifier}
                    onChange={(e) => setIdentifier(e.target.value)}
                    required
                />

                <button
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Sending OTP..." : "Send OTP"}
                </button>

                <button
                    type="button"
                    onClick={() => navigate("/login")}
                >
                    Back to Login
                </button>

            </form>
        </div>
    );
}

export default ForgotPassword;
