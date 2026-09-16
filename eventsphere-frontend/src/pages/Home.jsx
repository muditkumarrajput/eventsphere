import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Home() {
    const { isAuthenticated } = useAuth();

    return (
        <main className="home-page">

            {/* =====================================================
                HERO SECTION
               ===================================================== */}

            <section className="home-hero">

                <div className="home-hero-content">

                    <span className="home-badge">
                        ✨ Your events. Your experience.
                    </span>

                    <h1>
                        Discover Moments
                        <span> Worth Remembering.</span>
                    </h1>

                    <p>
                        Discover exciting events, book your tickets,
                        manage your bookings, and keep everything
                        organized in one place with EventSphere.
                    </p>

                    <div className="home-hero-actions">

                        <Link
                            to="/events"
                            className="home-primary-button"
                        >
                            Explore Events
                        </Link>

                        {!isAuthenticated && (
                            <Link
                                to="/register"
                                className="home-secondary-button"
                            >
                                Create Account
                            </Link>
                        )}

                    </div>

                </div>

                <div className="home-hero-visual">

                    <div className="hero-main-card">

                        <div className="hero-card-icon">
                            🎟️
                        </div>

                        <div>
                            <span>Upcoming Experience</span>

                            <strong>
                                Find Your Next Event
                            </strong>
                        </div>

                    </div>

                    <div className="hero-floating-card hero-floating-top">
                        🎵 Live Events
                    </div>

                    <div className="hero-floating-card hero-floating-bottom">
                        🎉 Unforgettable Moments
                    </div>

                </div>

            </section>


            {/* =====================================================
                FEATURES SECTION
               ===================================================== */}

            <section className="home-features">

                <div className="home-section-heading">

                    <span>
                        WHY EVENTSPHERE?
                    </span>

                    <h2>
                        Everything you need for your events
                    </h2>

                    <p>
                        A simple platform for discovering,
                        booking, and managing memorable events.
                    </p>

                </div>

                <div className="home-feature-grid">

                    <div className="home-feature-card">

                        <div className="home-feature-icon">
                            🔎
                        </div>

                        <h3>
                            Discover Events
                        </h3>

                        <p>
                            Browse available events and find
                            experiences that match your interests.
                        </p>

                    </div>

                    <div className="home-feature-card">

                        <div className="home-feature-icon">
                            🎟️
                        </div>

                        <h3>
                            Easy Booking
                        </h3>

                        <p>
                            Choose your tickets and complete your
                            booking through a simple guided flow.
                        </p>

                    </div>

                    <div className="home-feature-card">

                        <div className="home-feature-icon">
                            💳
                        </div>

                        <h3>
                            Secure Payments
                        </h3>

                        <p>
                            Follow a clear payment workflow and
                            keep track of your payment status.
                        </p>

                    </div>

                    <div className="home-feature-card">

                        <div className="home-feature-icon">
                            📊
                        </div>

                        <h3>
                            Manage Everything
                        </h3>

                        <p>
                            Keep track of bookings, favorites,
                            notifications, and event activity.
                        </p>

                    </div>

                </div>

            </section>


            {/* =====================================================
                CALL TO ACTION
               ===================================================== */}

            <section className="home-cta">

                <div>

                    <span className="home-cta-badge">
                        🚀 Start exploring
                    </span>

                    <h2>
                        Your next great experience
                        is waiting.
                    </h2>

                    <p>
                        Explore EventSphere and discover events
                        you won't want to miss.
                    </p>

                </div>

                <Link
                    to="/events"
                    className="home-cta-button"
                >
                    Browse Events →
                </Link>

            </section>

        </main>
    );
}

export default Home;
