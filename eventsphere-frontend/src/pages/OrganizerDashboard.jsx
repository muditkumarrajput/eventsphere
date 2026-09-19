import { useEffect, useState } from "react";
import api from "../services/api";

function OrganizerDashboard() {
    const [dashboard, setDashboard] = useState(null);
    const [eventInsights, setEventInsights] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [insightsError, setInsightsError] = useState("");

    const [cancellingEventId, setCancellingEventId] = useState(null);
    const [cancelError, setCancelError] = useState("");

    const [showCreateForm, setShowCreateForm] = useState(false);
    const [creatingEvent, setCreatingEvent] = useState(false);
    const [createError, setCreateError] = useState("");
    const [createSuccess, setCreateSuccess] = useState("");

    const [eventForm, setEventForm] = useState({
        title: "",
        description: "",
        location: "",
        eventDate: "",
        capacity: "",
        ticketPrice: "",
        category: "WORKSHOP",
    });

    useEffect(() => {
        const fetchDashboardData = async () => {
            setLoading(true);
            setError("");
            setInsightsError("");

            try {
                const [
                    dashboardResponse,
                    insightsResponse,
                ] = await Promise.all([
                    api.get("/dashboard"),
                    api.get("/dashboard/events"),
                ]);

                setDashboard(
                    dashboardResponse.data
                );

                setEventInsights(
                    insightsResponse.data
                );
            } catch (error) {
                console.error(
                    "Failed to load dashboard:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load dashboard."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const handleFormChange = (event) => {
        const { name, value } = event.target;

        setEventForm((currentForm) => ({
            ...currentForm,
            [name]: value,
        }));
    };

    const handleCreateEvent = async (event) => {
        event.preventDefault();

        setCreatingEvent(true);
        setCreateError("");
        setCreateSuccess("");

        try {
            const response = await api.post(
                "/events",
                {
                    title: eventForm.title,
                    description: eventForm.description,
                    location: eventForm.location,
                    eventDate: eventForm.eventDate,
                    capacity: Number(eventForm.capacity),
                    ticketPrice: eventForm.ticketPrice,
                    category: eventForm.category,
                }
            );

            setEventForm({
                title: "",
                description: "",
                location: "",
                eventDate: "",
                capacity: "",
                ticketPrice: "",
                category: "WORKSHOP",
            });

            setCreateSuccess(
                `Event "${response.data.title}" created successfully.`
            );

            setShowCreateForm(false);

            const [
                dashboardResponse,
                insightsResponse,
            ] = await Promise.all([
                api.get("/dashboard"),
                api.get("/dashboard/events"),
            ]);

            setDashboard(
                dashboardResponse.data
            );

            setEventInsights(
                insightsResponse.data
            );
        } catch (error) {
            console.error(
                "Failed to create event:",
                error
            );

            if (error.response?.data?.message) {
                setCreateError(
                    error.response.data.message
                );
            } else if (
                error.response?.data
            ) {
                setCreateError(
                    "Please check the event details and try again."
                );
            } else {
                setCreateError(
                    "Failed to create event."
                );
            }
        } finally {
            setCreatingEvent(false);
        }
    };

    const handleCancelEvent = async (eventId) => {
        const confirmed = window.confirm(
            "Are you sure you want to cancel this event? All active bookings for this event will also be cancelled."
        );

        if (!confirmed) {
            return;
        }

        setCancellingEventId(eventId);
        setCancelError("");

        try {
            await api.patch(
                `/events/${eventId}/cancel`
);

setEventInsights((currentEvents) =>
    currentEvents.map((event) =>
        event.eventId === eventId
            ? {
                ...event,
                status: "CANCELLED",
            }
            : event
    )
);

setDashboard((currentDashboard) => {
    if (!currentDashboard) {
        return currentDashboard;
    }

    return {
        ...currentDashboard,
        totalEvents:
            Math.max(
                currentDashboard.totalEvents - 1,
                0
            ),
        upcomingEvents:
            Math.max(
                currentDashboard.upcomingEvents - 1,
                0
            ),
    };
});
} catch (error) {
    console.error(
        "Failed to cancel event:",
        error
    );

    if (error.response?.data?.message) {
        setCancelError(
            error.response.data.message
        );
    } else {
        setCancelError(
            "Failed to cancel event."
        );
    }
} finally {
    setCancellingEventId(null);
}
};

if (loading) {
    return (
        <div className="dashboard-page">

            <div className="dashboard-header">

                <div>

                    <h1>
                        Organizer Dashboard
                    </h1>

                    <p>
                        Overview of your events,
                        bookings, tickets, and revenue.
                    </p>

                </div>

            </div>

            <p className="dashboard-message">
                Loading dashboard...
            </p>

        </div>
    );
}

if (error || !dashboard) {
    return (
        <div className="dashboard-page">

            <h1>
                Organizer Dashboard
            </h1>

            <p className="favorites-error">
                {error ||
                    "Dashboard data not available."}
            </p>

        </div>
    );
}

return (
    <div className="dashboard-page">

        <div className="dashboard-header">

            <div>

                <h1>
                    Organizer Dashboard
                </h1>

                <p>
                    Overview of your events,
                    bookings, tickets, and revenue.
                </p>

            </div>

            <button
                className="create-event-button"
                onClick={() => {
                    setShowCreateForm(
                        (current) => !current
                    );
                    setCreateError("");
                    setCreateSuccess("");
                }}
            >
                {showCreateForm
                    ? "Close Form"
                    : "Create Event"}
            </button>

        </div>

        {createSuccess && (
            <p className="success-message">
                {createSuccess}
            </p>
        )}

        {createError && (
            <p className="favorites-error">
                {createError}
            </p>
        )}

        {cancelError && (
            <p className="favorites-error">
                {cancelError}
            </p>
        )}

        {showCreateForm && (
            <div className="create-event-card">

                <div className="dashboard-section-header">

                    <div>

                        <h2>
                            Create New Event
                        </h2>

                        <p>
                            Add a new event to your
                            EventSphere organizer account.
                        </p>

                    </div>

                </div>

                <form
                    className="create-event-form"
                    onSubmit={handleCreateEvent}
                >

                    <div className="form-group">

                        <label htmlFor="title">
                            Event Title
                        </label>

                        <input
                            id="title"
                            name="title"
                            type="text"
                            value={eventForm.title}
                            onChange={handleFormChange}
                            placeholder="Enter event title"
                            maxLength={255}
                            required
                        />

                    </div>

                    <div className="form-group">

                        <label htmlFor="description">
                            Description
                        </label>

                        <textarea
                            id="description"
                            name="description"
                            value={
                                eventForm.description
                            }
                            onChange={handleFormChange}
                            placeholder="Describe your event"
                            maxLength={2000}
                            rows="5"
                            required
                        />

                    </div>

                    <div className="form-group">

                        <label htmlFor="location">
                            Location
                        </label>

                        <input
                            id="location"
                            name="location"
                            type="text"
                            value={eventForm.location}
                            onChange={handleFormChange}
                            placeholder="Enter event location"
                            maxLength={255}
                            required
                        />

                    </div>

                    <div className="form-group">

                        <label htmlFor="eventDate">
                            Event Date & Time
                        </label>

                        <input
                            id="eventDate"
                            name="eventDate"
                            type="datetime-local"
                            value={eventForm.eventDate}
                            onChange={handleFormChange}
                            required
                        />

                    </div>

                    <div className="create-event-form-row">

                        <div className="form-group">

                            <label htmlFor="capacity">
                                Capacity
                            </label>

                            <input
                                id="capacity"
                                name="capacity"
                                type="number"
                                value={eventForm.capacity}
                                onChange={handleFormChange}
                                placeholder="50"
                                min="1"
                                required
                            />

                        </div>

                        <div className="form-group">

                            <label htmlFor="ticketPrice">
                                Ticket Price
                            </label>

                            <input
                                id="ticketPrice"
                                name="ticketPrice"
                                type="number"
                                value={eventForm.ticketPrice}
                                onChange={handleFormChange}
                                placeholder="999"
                                min="0"
                                step="0.01"
                                required
                            />

                        </div>

                    </div>

                    <div className="form-group">

                        <label htmlFor="category">
                            Category
                        </label>

                        <select
                            id="category"
                            name="category"
                            value={eventForm.category}
                            onChange={handleFormChange}
                            required
                        >

                            <option value="CONFERENCE">
                                Conference
                            </option>

                            <option value="WORKSHOP">
                                Workshop
                            </option>

                            <option value="SEMINAR">
                                Seminar
                            </option>

                            <option value="MEETUP">
                                Meetup
                            </option>

                            <option value="WEBINAR">
                                Webinar
                            </option>

                            <option value="CONCERT">
                                Concert
                            </option>

                            <option value="SPORTS">
                                Sports
                            </option>

                            <option value="CULTURAL">
                                Cultural
                            </option>

                            <option value="FESTIVAL">
                                Festival
                            </option>

                            <option value="OTHER">
                                Other
                            </option>

                        </select>

                    </div>

                    <button
                        type="submit"
                        className="submit-create-event-button"
                        disabled={creatingEvent}
                    >
                        {creatingEvent
                            ? "Creating Event..."
                            : "Create Event"}
                    </button>

                </form>

            </div>
        )}

        <div className="dashboard-summary-grid">

            <div className="dashboard-card">

                    <span>
                        Total Events
                    </span>

                <strong>
                    {dashboard.totalEvents}
                </strong>

            </div>

            <div className="dashboard-card">

                    <span>
                        Upcoming Events
                    </span>

                <strong>
                    {dashboard.upcomingEvents}
                </strong>

            </div>

            <div className="dashboard-card">

                    <span>
                        Completed Events
                    </span>

                <strong>
                    {dashboard.completedEvents}
                </strong>

            </div>

            <div className="dashboard-card">

                    <span>
                        Total Bookings
                    </span>

                <strong>
                    {dashboard.totalBookings}
                </strong>

            </div>

            <div className="dashboard-card">

                    <span>
                        Tickets Sold
                    </span>

                <strong>
                    {dashboard.ticketsSold}
                </strong>

            </div>

            <div className="dashboard-card revenue-card">

                    <span>
                        Total Revenue
                    </span>

                <strong>
                    ₹{dashboard.totalRevenue}
                </strong>

            </div>

        </div>

        <div className="dashboard-insights">

            <div className="dashboard-section-header">

                <div>

                    <h2>
                        Event Insights
                    </h2>

                    <p>
                        Performance of your individual
                        events.
                    </p>

                </div>

            </div>

            {insightsError && (
                <p className="favorites-error">
                    {insightsError}
                </p>
            )}

            {eventInsights.length === 0 ? (
                <div className="empty-dashboard">

                    <h3>
                        No event insights available
                    </h3>

                    <p>
                        You don't have any event
                        analytics yet.
                    </p>

                </div>
            ) : (
                <div className="insights-grid">

                    {eventInsights.map((event) => {

                        const occupancy =
                            Math.min(
                                Math.max(
                                    Number(
                                        event.occupancyPercentage
                                    ) || 0,
                                    0
                                ),
                                100
                            );

                        const isCancelled =
                            event.status === "CANCELLED";

                        const isCancelling =
                            cancellingEventId ===
                            event.eventId;

                        return (
                            <div
                                className="insight-card"
                                key={event.eventId}
                            >

                                <div className="insight-card-header">

                                    <h3>
                                        {event.title}
                                    </h3>

                                    <span
                                        className={`event-status ${
                                            isCancelled
                                                ? "cancelled"
                                                : "active"
                                        }`}
                                    >
                                            {event.status ||
                                                "ACTIVE"}
                                        </span>

                                </div>

                                <div className="insight-info">

                                    <div>

                                            <span>
                                                Capacity
                                            </span>

                                        <strong>
                                            {event.capacity}
                                        </strong>

                                    </div>

                                    <div>

                                            <span>
                                                Tickets Sold
                                            </span>

                                        <strong>
                                            {event.ticketsSold}
                                        </strong>

                                    </div>

                                    <div>

                                            <span>
                                                Remaining Seats
                                            </span>

                                        <strong>
                                            {event.remainingSeats}
                                        </strong>

                                    </div>

                                    <div>

                                            <span>
                                                Occupancy
                                            </span>

                                        <strong>
                                            {event.occupancyPercentage}%
                                        </strong>

                                    </div>

                                    <div>

                                            <span>
                                                Revenue
                                            </span>

                                        <strong>
                                            ₹{event.revenue}
                                        </strong>

                                    </div>

                                </div>

                                <div className="occupancy-section">

                                    <div className="occupancy-header">

                                            <span>
                                                Occupancy
                                            </span>

                                        <strong>
                                            {event.occupancyPercentage}%
                                        </strong>

                                    </div>

                                    <div className="occupancy-bar">

                                        <div
                                            className="occupancy-progress"
                                            style={{
                                                width: `${occupancy}%`,
                                            }}
                                        />

                                    </div>

                                </div>

                                {!isCancelled && (
                                    <button
                                        className="cancel-event-button"
                                        onClick={() =>
                                            handleCancelEvent(
                                                event.eventId
                                            )
                                        }
                                        disabled={
                                            isCancelling
                                        }
                                    >
                                        {isCancelling
                                            ? "Cancelling..."
                                            : "Cancel Event"}
                                    </button>
                                )}

                            </div>
                        );
                    })}

                </div>
            )}

        </div>

    </div>
);
}

export default OrganizerDashboard;