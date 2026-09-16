import { useEffect, useState } from "react";
import api from "../services/api";

function OrganizerDashboard() {
    const [dashboard, setDashboard] = useState(null);
    const [eventInsights, setEventInsights] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [insightsError, setInsightsError] = useState("");

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

            {/* Dashboard Header */}

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

            {/* Summary Cards */}

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

            {/* Event Insights */}

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

                            return (
                                <div
                                    className="insight-card"
                                    key={event.eventId}
                                >

                                    <div className="insight-card-header">

                                        <h3>
                                            {event.title}
                                        </h3>

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
