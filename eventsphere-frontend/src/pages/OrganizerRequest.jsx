import { useEffect, useState } from "react";
import api from "../services/api";

function OrganizerRequest() {
    const [reason, setReason] = useState("");
    const [requests, setRequests] = useState([]);

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        const loadRequests = async () => {
            try {
                const response = await api.get(
                    "/organizer-requests/me"
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

        loadRequests();
    }, []);

    const fetchRequests = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get(
                "/organizer-requests/me"
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

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!reason.trim()) {
            setError("Please provide a reason.");
            return;
        }

        try {
            setSubmitting(true);

            await api.post(
                "/organizer-requests",
                {
                    reason: reason.trim(),
                }
            );

            setReason("");

            setSuccess(
                "Organizer request submitted successfully."
            );

            await fetchRequests();
        } catch (error) {
            console.error(
                "Failed to submit organizer request:",
                error
            );

            setError(
                error.response?.data?.message ||
                "Failed to submit organizer request."
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="organizer-request-page">

            <div className="organizer-request-header">
                <h1>Request Organizer Access</h1>

                <p>
                    Submit a request to become an EventSphere
                    organizer. An administrator will review
                    your request.
                </p>
            </div>

            <form
                onSubmit={handleSubmit}
                className="organizer-request-form"
            >
                <label htmlFor="reason">
                    Why do you want to become an organizer?
                </label>

                <textarea
                    id="reason"
                    value={reason}
                    onChange={(event) =>
                        setReason(event.target.value)
                    }
                    placeholder="Explain why you want to organize events..."
                    maxLength={1000}
                    rows={6}
                    disabled={submitting}
                    required
                />

                <div>
                    <span>
                        {reason.length}/1000
                    </span>
                </div>

                <button
                    type="submit"
                    disabled={submitting}
                >
                    {submitting
                        ? "Submitting..."
                        : "Submit Request"}
                </button>
            </form>

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

            <section className="organizer-request-history">

                <div>
                    <h2>My Organizer Requests</h2>

                    <p>
                        Track the status of your previous
                        organizer requests.
                    </p>
                </div>

                {loading ? (
                    <p>
                        Loading requests...
                    </p>
                ) : requests.length === 0 ? (
                    <p>
                        You have not submitted any organizer
                        requests yet.
                    </p>
                ) : (
                    <div>
                        {requests.map((request) => (
                            <div
                                key={request.id}
                                className="organizer-request-card"
                            >
                                <div>
                                    <strong>
                                        Request #{request.id}
                                    </strong>

                                    <span>
                                        {request.status}
                                    </span>
                                </div>

                                <p>
                                    {request.reason}
                                </p>

                                <small>
                                    Submitted:{" "}
                                    {new Date(
                                        request.createdAt
                                    ).toLocaleString()}
                                </small>

                                {request.reviewedAt && (
                                    <small>
                                        Reviewed:{" "}
                                        {new Date(
                                            request.reviewedAt
                                        ).toLocaleString()}
                                    </small>
                                )}

                                {request.reviewedByName && (
                                    <small>
                                        Reviewed by:{" "}
                                        {request.reviewedByName}
                                    </small>
                                )}
                            </div>
                        ))}
                    </div>
                )}

            </section>
        </div>
    );
}

export default OrganizerRequest;