import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

function Payment() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [payment, setPayment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchPayment = async () => {
            try {
                const response = await api.get(
                    `/payments/${id}`
                );

                setPayment(response.data);
            } catch (error) {
                console.error(
                    "Failed to load payment:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load payment."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchPayment();
    }, [id]);

    const updatePaymentStatus = async (status) => {
        setError("");
        setProcessing(true);

        try {
            let response;

            if (status === "SUCCESS") {
                response = await api.patch(
                    `/payments/${id}/success`
);
} else {
    response = await api.patch(
        `/payments/${id}/failure`
    );
}

setPayment(response.data);
} catch (error) {
    console.error(
        "Failed to update payment:",
        error
    );

    if (error.response?.data?.message) {
        setError(
            error.response.data.message
        );
    } else {
        setError(
            "Failed to update payment status."
        );
    }
} finally {
    setProcessing(false);
}
};

const getStatusClass = () => {
    if (!payment) {
        return "";
    }

    return `payment-status ${payment.paymentStatus.toLowerCase()}`;
};

if (loading) {
    return (
        <div className="payment-page">

            <p>
                Loading payment...
            </p>

        </div>
    );
}

if (error || !payment) {
    return (
        <div className="payment-page">

            <div className="payment-card">

                <h1>
                    Payment
                </h1>

                <p className="favorites-error">
                    {error ||
                        "Payment not found."}
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/bookings")
                    }
                >
                    ← Back to My Bookings
                </button>

            </div>

        </div>
    );
}

return (
    <div className="payment-page">

        <div className="payment-card">

            {/* Header */}

            <div className="payment-header">

                <div>

                    <h1>
                        Payment
                    </h1>

                    <p>
                        Complete your booking payment.
                    </p>

                </div>

                <span
                    className={getStatusClass()}
                >
                        {payment.paymentStatus}
                    </span>

            </div>

            {error && (
                <p className="favorites-error">
                    {error}
                </p>
            )}

            {/* Payment Information */}

            <div className="payment-info">

                <div>
                        <span>
                            Payment Reference
                        </span>

                    <strong>
                        {payment.paymentReference}
                    </strong>
                </div>

                <div>
                        <span>
                            Booking ID
                        </span>

                    <strong>
                        {payment.bookingId}
                    </strong>
                </div>

                <div>
                        <span>
                            Amount
                        </span>

                    <strong>
                        ₹{payment.amount}
                    </strong>
                </div>

                <div>
                        <span>
                            Payment Date
                        </span>

                    <strong>
                        {payment.paymentDate
                            ? new Date(
                                payment.paymentDate
                            ).toLocaleString()
                            : "Not completed"}
                    </strong>
                </div>

            </div>

            {/* Payment Simulation */}

            {payment.paymentStatus ===
                "PENDING" && (
                    <div className="payment-actions">

                        <h2>
                            Payment Simulation
                        </h2>

                        <p>
                            This project currently uses
                            a simulated payment flow.
                        </p>

                        <div className="payment-buttons">

                            <button
                                className="payment-success-button"
                                onClick={() =>
                                    updatePaymentStatus(
                                        "SUCCESS"
                                    )
                                }
                                disabled={processing}
                            >
                                {processing
                                    ? "Processing..."
                                    : "Simulate Success"}
                            </button>

                            <button
                                className="payment-failure-button"
                                onClick={() =>
                                    updatePaymentStatus(
                                        "FAILED"
                                    )
                                }
                                disabled={processing}
                            >
                                {processing
                                    ? "Processing..."
                                    : "Simulate Failure"}
                            </button>

                        </div>

                    </div>
                )}

            {/* Successful Payment */}

            {payment.paymentStatus ===
                "SUCCESS" && (
                    <div className="payment-result success">

                        <h2>
                            Payment Successful
                        </h2>

                        <p>
                            Your payment has been completed
                            successfully.
                        </p>

                        <button
                            className="book-button"
                            onClick={() =>
                                navigate(
                                    `/bookings/${payment.bookingId}`
                                )
                            }
                        >
                            View Booking
                        </button>

                    </div>
                )}

            {/* Failed Payment */}

            {payment.paymentStatus ===
                "FAILED" && (
                    <div className="payment-result failed">

                        <h2>
                            Payment Failed
                        </h2>

                        <p>
                            Your payment could not be
                            completed.
                        </p>

                        <button
                            className="back-button"
                            onClick={() =>
                                navigate(
                                    `/bookings/${payment.bookingId}`
                                )
                            }
                        >
                            View Booking
                        </button>

                    </div>
                )}

            {/* Refunded Payment */}

            {payment.paymentStatus ===
                "REFUNDED" && (
                    <div className="payment-result refunded">

                        <h2>
                            Payment Refunded
                        </h2>

                        <p>
                            This payment has been refunded.
                        </p>

                        <button
                            className="back-button"
                            onClick={() =>
                                navigate(
                                    `/bookings/${payment.bookingId}`
                                )
                            }
                        >
                            View Booking
                        </button>

                    </div>
                )}

        </div>

    </div>
);
}

export default Payment;
