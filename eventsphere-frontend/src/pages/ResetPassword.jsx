import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import api from "../services/api";

function ResetPassword() {
    const navigate = useNavigate();
    const location = useLocation();

    const [identifier] = useState(
        location.state?.identifier || ""
    );

    const [otp, setOtp] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        setMessage("");
        setError("");

        if (!identifier) {
            setError("Email or mobile number is missing.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (newPassword.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        setLoading(true);

        try {
            await api.post("/auth/forgot-password/reset", {
                identifier,
                otp,
                newPassword,
                confirmPassword
            });

            setMessage("Password reset successfully.");

            setTimeout(() => {
                navigate("/login");
            }, 1000);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to reset password. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <form className="auth-form" onSubmit={handleSubmit}>

                <h2>Reset Password</h2>

                <p>
                    Enter the OTP sent to your registered contact and create
                    a new password.
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
                    value={identifier}
                    readOnly
                    placeholder="Email or mobile number"
                />

                <input
                    type="text"
                    placeholder="6-digit OTP"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    maxLength={6}
                    inputMode="numeric"
                    required
                />

                <input
                    type="password"
                    placeholder="New password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                />

                <input
                    type="password"
                    placeholder="Confirm new password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />

                <button
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Resetting..." : "Reset Password"}
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

export default ResetPassword;
