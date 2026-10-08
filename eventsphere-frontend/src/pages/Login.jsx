import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/useAuth";

function Login() {
    const [step, setStep] = useState(1);

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [otp, setOtp] = useState("");

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const { login } = useAuth();

    const handleLogin = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setError("");
        setMessage("");
        setLoading(true);

        try {
            const response = await api.post(
                "/auth/login",
                {
                    identifier: email,
                    password: password,
                }
            );

            setMessage(
                response.data?.message ||
                "OTP sent to your email."
            );

            setStep(2);

        } catch (error) {
            console.error(
                "Login failed:",
                error
            );

            if (error.response?.data?.message) {
                setError(
                    error.response.data.message
                );
            } else {
                setError(
                    "Invalid email or password."
                );
            }
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyOtp = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setError("");
        setMessage("");
        setLoading(true);

        try {
            const response = await api.post(
                "/auth/login/verify-otp",
                {
                    target: email,
                    otp: otp,
                }
            );

            const token = response.data.token;

            login(token);

            navigate("/", { replace: true });

        } catch (error) {
            console.error(
                "OTP verification failed:",
                error
            );

            if (error.response?.data?.message) {
                setError(
                    error.response.data.message
                );
            } else {
                setError(
                    "Invalid OTP. Please try again."
                );
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="login-page">

            <div className="login-container">

                {/* =====================================================
                    LOGIN INTRO
                   ===================================================== */}

                <div className="login-intro">

                    <span className="login-badge">
                        ✨ Welcome back
                    </span>

                    <h1>
                        Welcome to
                        <span> EventSphere.</span>
                    </h1>

                    <p>
                        Sign in to discover events, manage your
                        bookings, save your favorite experiences,
                        and stay updated.
                    </p>

                    <div className="login-feature-list">

                        <div className="login-feature">
                            <span>🎟️</span>
                            <div>
                                <strong>
                                    Easy Event Booking
                                </strong>
                                <p>
                                    Find and book events in just a few steps.
                                </p>
                            </div>
                        </div>

                        <div className="login-feature">
                            <span>❤️</span>
                            <div>
                                <strong>
                                    Save Your Favorites
                                </strong>
                                <p>
                                    Keep track of events you want to attend.
                                </p>
                            </div>
                        </div>

                        <div className="login-feature">
                            <span>🔔</span>
                            <div>
                                <strong>
                                    Stay Updated
                                </strong>
                                <p>
                                    Receive notifications about your activity.
                                </p>
                            </div>
                        </div>

                    </div>

                </div>

                {/* =====================================================
                    LOGIN CARD
                   ===================================================== */}

                <div className="login-card">

                    <div className="login-card-header">

                        <div className="login-icon">
                            🔐
                        </div>

                        <h2>
                            {step === 1
                                ? "Sign In"
                                : "Verify OTP"}
                        </h2>

                        <p>
                            {step === 1
                                ? "Enter your account details to continue."
                                : "Enter the 6-digit OTP sent to your email."}
                        </p>

                    </div>

                    {step === 1 && (
                        <form
                            onSubmit={handleLogin}
                            className="login-form"
                        >

                            <div className="login-input-group">

                                <label htmlFor="email">
                                    Email Address
                                </label>

                                <input
                                    id="email"
                                    type="email"
                                    placeholder="Enter your email"
                                    value={email}
                                    onChange={(event) =>
                                        setEmail(event.target.value)
                                    }
                                    disabled={loading}
                                    autoComplete="email"
                                    required
                                />

                            </div>

                            <div className="login-input-group">

                                <label htmlFor="password">
                                    Password
                                </label>

                                <input
                                    id="password"
                                    type="password"
                                    placeholder="Enter your password"
                                    value={password}
                                    onChange={(event) =>
                                        setPassword(event.target.value)
                                    }
                                    disabled={loading}
                                    autoComplete="current-password"
                                    required
                                />

                            </div>

                            {error && (
                                <p className="login-error">
                                    {error}
                                </p>
                            )}

                            {message && (
                                <p className="success-message">
                                    {message}
                                </p>
                            )}

                            <button
                                type="submit"
                                className="login-button"
                                disabled={loading}
                            >
                                {loading
                                    ? "Sending OTP..."
                                    : "Continue"}
                            </button>

                        </form>
                    )}

                    {step === 2 && (
                        <form
                            onSubmit={handleVerifyOtp}
                            className="login-form"
                        >

                            <div className="login-input-group">

                                <label htmlFor="otp">
                                    Email OTP
                                </label>

                                <input
                                    id="otp"
                                    type="text"
                                    placeholder="Enter 6-digit OTP"
                                    value={otp}
                                    onChange={(event) =>
                                        setOtp(event.target.value)
                                    }
                                    maxLength={6}
                                    pattern="[0-9]{6}"
                                    inputMode="numeric"
                                    disabled={loading}
                                    required
                                />

                            </div>

                            {message && (
                                <p className="success-message">
                                    {message}
                                </p>
                            )}

                            {error && (
                                <p className="login-error">
                                    {error}
                                </p>
                            )}

                            <button
                                type="submit"
                                className="login-button"
                                disabled={loading}
                            >
                                {loading
                                    ? "Verifying..."
                                    : "Verify OTP"}
                            </button>

                        </form>
                    )}

                    <div className="login-register">

                        <span>
                            Don't have an account?
                        </span>

                        <Link to="/register">
                            Create an account
                        </Link>

                    </div>

                </div>

            </div>

        </main>
    );
}

export default Login;