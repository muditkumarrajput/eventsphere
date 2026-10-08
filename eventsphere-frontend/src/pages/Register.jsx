import { useState } from "react";
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

    const [mobileOtp, setMobileOtp] = useState("");
    const [emailOtp, setEmailOtp] = useState("");

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

        if (loading) {
            return;
        }

        setMessage("");
        setError("");

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            const response = await api.post(
                "/auth/register",
                formData
            );

            setMessage(
                response.data?.message ||
                "Mobile OTP sent successfully."
            );

            setStep(2);
        } catch (error) {
            console.error(
                "Registration failed:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleMobileOtpVerification = async (event) => {
        event.preventDefault();

        if (loading) {
            return;
        }

        setMessage("");
        setError("");
        setLoading(true);

        try {
            const response = await api.post(
                "/auth/register/verify-mobile",
                {
                    target: formData.phoneNumber,
                    otp: mobileOtp,
                }
            );

            setMessage(
                response.data?.message ||
                "Mobile number verified. Email OTP sent."
            );

            setStep(3);
        } catch (error) {
            console.error(
                "Mobile OTP verification failed:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Invalid mobile OTP. Please try again."
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
        setLoading(true);

        try {
            await api.post(
                "/auth/register/verify-email",
                {
                    target: formData.email,
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
                "Invalid email OTP. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">

            <h1>
                {step === 1 && "Create Account"}
                {step === 2 && "Verify Mobile Number"}
                {step === 3 && "Verify Email"}
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
                        placeholder="Phone Number"
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        disabled={loading}
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Sending OTP..."
                            : "Register"}
                    </button>

                </form>
            )}

            {step === 2 && (
                <form
                    onSubmit={handleMobileOtpVerification}
                    className="auth-form"
                >

                    <p>
                        Enter the 6-digit OTP sent to your
                        mobile number.
                    </p>

                    <input
                        type="text"
                        name="mobileOtp"
                        placeholder="Mobile OTP"
                        value={mobileOtp}
                        onChange={(event) =>
                            setMobileOtp(event.target.value)
                        }
                        maxLength={6}
                        pattern="[0-9]{6}"
                        inputMode="numeric"
                        disabled={loading}
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Verifying..."
                            : "Verify Mobile"}
                    </button>

                </form>
            )}

            {step === 3 && (
                <form
                    onSubmit={handleEmailOtpVerification}
                    className="auth-form"
                >

                    <p>
                        Enter the 6-digit OTP sent to your
                        email address.
                    </p>

                    <input
                        type="text"
                        name="emailOtp"
                        placeholder="Email OTP"
                        value={emailOtp}
                        onChange={(event) =>
                            setEmailOtp(event.target.value)
                        }
                        maxLength={6}
                        pattern="[0-9]{6}"
                        inputMode="numeric"
                        disabled={loading}
                        required
                    />

                    <button
                        type="submit"
                        disabled={loading}
                    >
                        {loading
                            ? "Verifying..."
                            : "Verify Email"}
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