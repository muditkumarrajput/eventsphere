import { useEffect, useState } from "react";
import api from "../services/api";

function AdminOrganizerRequests() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [processingId, setProcessingId] = useState(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        let cancelled = false;

        const loadRequests = async () => {
            try {
                const response = await api.get(
                    "/organizer-requests"
                );

                if (!cancelled) {
                    setRequests(response.data);
                    setError("");
                }
            } catch (error) {
                console.error(
                    "Failed to load organizer requests:",
                    error
                );

                if (!cancelled) {
                    setError(
                        error.response?.data?.message ||
                        "Failed to load organizer requests."
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        loadRequests();

        return () => {
            cancelled = true;
        };
    }, []);

    const fetchRequests = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get(
                "/organizer-requests"
            );

            setRequests(response.data);
        } catch (error) {
            console.error(
                "Failed to load organizer requests:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to load organizer requests."
            );
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (requestId) => {
        try {
            setProcessingId(requestId);
            setError("");
            setSuccess("");

            await api.patch(
                `/organizer-requests/${requestId}/approve`
            );

            setSuccess(
                `Organizer request #${requestId} approved successfully.`
            );

            await fetchRequests();
        } catch (error) {
            console.error(
                "Failed to approve organizer request:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to approve organizer request."
            );
        } finally {
            setProcessingId(null);
        }
    };

    const handleReject = async (requestId) => {
        try {
            setProcessingId(requestId);
            setError("");
            setSuccess("");

            await api.patch(
                `/organizer-requests/${requestId}/reject`
            );

            setSuccess(
                `Organizer request #${requestId} rejected successfully.`
            );

            await fetchRequests();
        } catch (error) {
            console.error(
                "Failed to reject organizer request:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to reject organizer request."
            );
        } finally {
            setProcessingId(null);
        }
    };

    if (loading) {
        return (
            <div className="admin-organizer-requests-page">
                <h1>Organizer Requests</h1>
                <p>Loading organizer requests...</p>
            </div>
        );
    }

    return (
        <div className="admin-organizer-requests-page">

            <div className="admin-organizer-requests-header">
                <h1>Organizer Requests</h1>

                <p>
                    Review and manage requests from users who want
                    to become EventSphere organizers.
                </p>
            </div>

            {success && (
                <p className="success-message">
                    {success}
                </p>
            )}

            {error && (
                <p className="error-message">
                    {error}
                </p>
            )}

            {requests.length === 0 ? (
                <div className="empty-admin-requests">
                    <h2>No Organizer Requests</h2>

                    <p>
                        There are currently no organizer requests
                        to review.
                    </p>
                </div>
            ) : (
                <div className="admin-organizer-request-list">

                    {requests.map((request) => {

                        const isProcessing =
                            processingId === request.id;

                        return (
                            <div
                                key={request.id}
                                className="admin-organizer-request-card"
                            >

                                <div className="admin-request-card-header">

                                    <div>
                                        <h2>
                                            Request #{request.id}
                                        </h2>

                                        <p>
                                            Submitted{" "}
                                            {new Date(
                                                request.createdAt
                                            ).toLocaleString()}
                                        </p>
                                    </div>

                                    <span
                                        className={`request-status ${request.status.toLowerCase()}`}
                                    >
                                        {request.status}
                                    </span>

                                </div>

                                <div className="admin-request-user">

                                    <p>
                                        <strong>
                                            User:
                                        </strong>{" "}
                                        {request.userName}
                                    </p>

                                    <p>
                                        <strong>
                                            Email:
                                        </strong>{" "}
                                        {request.userEmail}
                                    </p>

                                </div>

                                <div className="admin-request-reason">

                                    <h3>
                                        Reason
                                    </h3>

                                    <p>
                                        {request.reason}
                                    </p>

                                </div>

                                {request.reviewedAt && (
                                    <div className="admin-request-review">

                                        <p>
                                            <strong>
                                                Reviewed:
                                            </strong>{" "}
                                            {new Date(
                                                request.reviewedAt
                                            ).toLocaleString()}
                                        </p>

                                        {request.reviewedByName && (
                                            <p>
                                                <strong>
                                                    Reviewed by:
                                                </strong>{" "}
                                                {
                                                    request.reviewedByName
                                                }
                                            </p>
                                        )}

                                    </div>
                                )}

                                {request.status === "PENDING" && (
                                    <div className="admin-request-actions">

                                        <button
                                            className="approve-button"
                                            onClick={() =>
                                                handleApprove(
                                                    request.id
                                                )
                                            }
                                            disabled={isProcessing}
                                        >
                                            {isProcessing
                                                ? "Processing..."
                                                : "Approve"}
                                        </button>

                                        <button
                                            className="reject-button"
                                            onClick={() =>
                                                handleReject(
                                                    request.id
                                                )
                                            }
                                            disabled={isProcessing}
                                        >
                                            {isProcessing
                                                ? "Processing..."
                                                : "Reject"}
                                        </button>

                                    </div>
                                )}

                            </div>
                        );
                    })}

                </div>
            )}

        </div>
    );
}

export default AdminOrganizerRequests;