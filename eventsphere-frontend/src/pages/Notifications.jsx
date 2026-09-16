import { useEffect, useState } from "react";
import api from "../services/api";

function Notifications() {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [markingReadId, setMarkingReadId] = useState(null);

    useEffect(() => {
        const fetchNotifications = async () => {
            try {
                const response = await api.get(
                    "/notifications"
                );

                setNotifications(response.data);
            } catch (error) {
                console.error(
                    "Failed to load notifications:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load notifications."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        const fetchUnreadCount = async () => {
            try {
                const response = await api.get(
                    "/notifications/unread/count"
                );

                setUnreadCount(response.data);
            } catch (error) {
                console.error(
                    "Failed to load unread notification count:",
                    error
                );
            }
        };

        fetchNotifications();
        fetchUnreadCount();
    }, []);

    const markAsRead = async (notificationId) => {
        const notification = notifications.find(
            (currentNotification) =>
                currentNotification.id === notificationId
        );

        if (!notification || notification.isRead) {
            return;
        }

        setError("");
        setMarkingReadId(notificationId);

        try {
            await api.put(
                `/notifications/${notificationId}/read`
);

setNotifications((currentNotifications) =>
    currentNotifications.map(
        (currentNotification) =>
            currentNotification.id ===
            notificationId
                ? {
                    ...currentNotification,
                    isRead: true,
                }
                : currentNotification
    )
);

setUnreadCount((currentCount) =>
    Math.max(currentCount - 1, 0)
);
} catch (error) {
    console.error(
        "Failed to mark notification as read:",
        error
    );

    if (error.response?.data?.message) {
        setError(
            error.response.data.message
        );
    } else {
        setError(
            "Failed to mark notification as read."
        );
    }
} finally {
    setMarkingReadId(null);
}
};

if (loading) {
    return (
        <div className="notifications-page">

            <div className="notifications-header">

                <h1>
                    Notifications
                </h1>

            </div>

            <p className="notifications-message">
                Loading notifications...
            </p>

        </div>
    );
}

return (
    <div className="notifications-page">

        <div className="notifications-header">

            <div>

                <h1>
                    Notifications
                </h1>

                <p>
                    Stay updated with your event activity.
                </p>

            </div>

            <div className="unread-count">

                    <span>
                        Unread
                    </span>

                <strong>
                    {unreadCount}
                </strong>

            </div>

        </div>

        {error && (
            <p className="favorites-error">
                {error}
            </p>
        )}

        {notifications.length === 0 ? (
            <div className="empty-notifications">

                <h2>
                    No notifications
                </h2>

                <p>
                    You don't have any notifications yet.
                </p>

            </div>
        ) : (
            <div className="notifications-list">

                {notifications.map(
                    (notification) => {

                        const isMarkingRead =
                            markingReadId ===
                            notification.id;

                        return (
                            <div
                                className={
                                    notification.isRead
                                        ? "notification-card read"
                                        : "notification-card unread"
                                }
                                key={
                                    notification.id
                                }
                            >

                                <div className="notification-content">

                                    <div className="notification-header">

                                        <h2>
                                            {
                                                notification.title
                                            }
                                        </h2>

                                        {!notification.isRead && (
                                            <span className="unread-badge">
                                                    New
                                                </span>
                                        )}

                                    </div>

                                    <p className="notification-message">
                                        {
                                            notification.message
                                        }
                                    </p>

                                    <p className="notification-date">
                                        {new Date(
                                            notification.createdAt
                                        ).toLocaleString()}
                                    </p>

                                </div>

                                {!notification.isRead && (
                                    <button
                                        className="mark-read-button"
                                        onClick={() =>
                                            markAsRead(
                                                notification.id
                                            )
                                        }
                                        disabled={
                                            isMarkingRead
                                        }
                                    >
                                        {isMarkingRead
                                            ? "Marking..."
                                            : "Mark as Read"}
                                    </button>
                                )}

                            </div>
                        );
                    }
                )}

            </div>
        )}

    </div>
);
}

export default Notifications;
