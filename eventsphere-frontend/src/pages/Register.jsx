import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Register() {
    const navigate = useNavigate();

    const [step, setStep] = useState(1);

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        confirmPassword: "",
        phoneNumber: "",
    });

    const [emailOtp, setEmailOtp] = useState("");
    const [resendSeconds, setResendSeconds] = useState(0);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (step !== 2 || resendSeconds <= 0) {
            return;
        }

        const timer = setTimeout(() => {
            setResendSeconds((previous) =>
                Math.max(previous - 1, 0)
            );
        }, 1000);

        return () => clearTimeout(timer);
    }, [step, resendSeconds]);

    const handleChange = (event) => {
        const { name, value } = event.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));
    };

    const handleRegister = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setMessage("");
        setError("");

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (!/^[6-9]\d{9}$/.test(formData.phoneNumber.trim())) {
            setError(
                "Enter a valid 10-digit Indian mobile number."
            );
            return;
        }

        setLoading(true);

        try {
            const response = await api.post(
                "/auth/register",
                {
                    ...formData,
                    name: formData.name.trim(),
                    email: formData.email.trim().toLowerCase(),
                    phoneNumber: formData.phoneNumber.trim(),
                }
            );

            setMessage(
                response.data?.message ||
                "Email OTP sent successfully."
            );

            setEmailOtp("");
            setResendSeconds(60);
            setStep(2);
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

    const handleEmailOtpVerification = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setMessage("");
        setError("");

        if (!/^\d{6}$/.test(emailOtp)) {
            setError("Enter a valid 6-digit OTP.");
            return;
        }

        setLoading(true);

        try {
            await api.post(
                "/auth/register/verify-email",
                {
                    target: formData.email.trim().toLowerCase(),
                    otp: emailOtp,
                }
            );

            setMessage(
                "Registration successful! Redirecting to login..."
            );

            setTimeout(() => {
                navigate("/login");
            }, 1500);
        } catch (error) {
            console.error(
                "Email OTP verification failed:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Email OTP verification failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleResendOtp = async () => {
        if (loading || resendSeconds > 0) {
            return;
        }

        setMessage("");
        setError("");
        setLoading(true);

        try {
            const response = await api.post(
                "/auth/register/resend-otp",
                {
                    target: formData.email.trim().toLowerCase(),
                }
            );

            setEmailOtp("");
            setResendSeconds(60);

            setMessage(
                response.data?.message ||
                "A new email OTP has been sent successfully."
            );
        } catch (error) {
            console.error("Resending email OTP failed:", error);

            setError(
                error.response?.data?.message ||
                "Could not resend the OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h1>
                {step === 1
                    ? "Create Account"
                    : "Verify Email"}
            </h1>

            {step === 1 && (
                <form
                    onSubmit={handleRegister}
                    className="auth-form"
                >
                    <input
                        type="text"
                        name="name"
                        placeholder="Name"
                        value={formData.name}
                        onChange={handleChange}
                        disabled={loading}
                        required
                    />

                    <input
                        type="email"
                        name="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        disabled={loading}
                        required
                    />

                    <input
                        type="password"
                        name="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleChange}
                        minLength={8}
                        disabled={loading}
                        required
                    />

                    <input
                        type="password"
                        name="confirmPassword"
                        placeholder="Confirm Password"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        minLength={8}
                        disabled={loading}
                        required
                    />

                    <input
                        type="tel"
                        name="phoneNumber"
                        placeholder="10-digit Indian mobile number"
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        pattern="[6-9][0-9]{9}"
                        maxLength={10}
                        inputMode="numeric"
                        disabled={loading}
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Sending Email OTP..."
                            : "Register"}
                    </button>
                </form>
            )}

            {step === 2 && (
                <form
                    onSubmit={handleEmailOtpVerification}
                    className="auth-form"
                >
                    <p>
                        Enter the 6-digit OTP sent to{" "}
                        <strong>{formData.email}</strong>.
                    </p>

                    <input
                        type="text"
                        name="emailOtp"
                        placeholder="Email OTP"
                        value={emailOtp}
                        onChange={(event) =>
                            setEmailOtp(
                                event.target.value
                                    .replace(/\D/g, "")
                                    .slice(0, 6)
                            )
                        }
                        maxLength={6}
                        pattern="[0-9]{6}"
                        inputMode="numeric"
                        autoComplete="one-time-code"
                        disabled={loading}
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Please wait..."
                            : "Verify Email"}
                    </button>

                    <button
                        type="button"
                        onClick={handleResendOtp}
                        disabled={loading || resendSeconds > 0}
                    >
                        {loading
                            ? "Please wait..."
                            : resendSeconds > 0
                                ? `Resend OTP in ${resendSeconds}s`
                                : "Resend OTP"}
                    </button>
                </form>
            )}

            {message && (
                <p className="success-message">
                    {message}
                </p>
            )}

            {error && (
                <p className="error-message">
                    {error}
                </p>
            )}
        </div>
    );
}

export default Register;