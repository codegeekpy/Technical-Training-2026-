from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base, SessionLocal
from app.config import get_settings
import app.models  # noqa: F401 — register all models with Base

from app.routers.auth import router as auth_router
from app.routers.venues import router as venues_router
from app.routers.events import router as events_router
from app.routers.users import router as users_router
from app.routers.notifications import router as notifications_router

settings = get_settings()

app = FastAPI(
    title="Anurag University Event Management System",
    description="Digital event proposal, venue booking, and coordinator approval portal.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.frontend_url, "http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(venues_router)
app.include_router(events_router)
app.include_router(users_router)
app.include_router(notifications_router)


@app.on_event("startup")
def on_startup():
    Base.metadata.create_all(bind=engine)
    _seed_initial_data()


def _seed_initial_data():
    from app.models.user import User, UserRole
    from app.models.venue import Venue, VenueType
    from app.auth.security import get_password_hash

    db = SessionLocal()
    try:
        if db.query(User).count() == 0:
            admin = User(
                full_name="System Admin",
                email="admin@anurag.edu.in",
                hashed_password=get_password_hash("admin@123"),
                role=UserRole.admin,
                department="Administration",
            )
            coordinator = User(
                full_name="Dr. Ramesh Kumar",
                email="coordinator@anurag.edu.in",
                hashed_password=get_password_hash("coord@123"),
                role=UserRole.coordinator,
                department="Event Management",
            )
            dean = User(
                full_name="Prof. Sunita Sharma",
                email="dean@anurag.edu.in",
                hashed_password=get_password_hash("dean@123"),
                role=UserRole.dean,
                department="Dean's Office",
            )
            student = User(
                full_name="Arjun Reddy",
                email="student@anurag.edu.in",
                hashed_password=get_password_hash("student@123"),
                role=UserRole.student,
                department="Computer Science",
            )
            db.add_all([admin, coordinator, dean, student])
            db.commit()

        if db.query(Venue).count() == 0:
            venues = [
                Venue(name="Sri Venkateswara Auditorium", venue_type=VenueType.auditorium, capacity=500, location="Main Block, Ground Floor", description="Main university auditorium with full AV setup"),
                Venue(name="Seminar Hall A", venue_type=VenueType.seminar_hall, capacity=150, location="Academic Block 1, 2nd Floor", description="Seminar hall with projector and mic"),
                Venue(name="Seminar Hall B", venue_type=VenueType.seminar_hall, capacity=150, location="Academic Block 2, 1st Floor", description="Seminar hall with projector"),
                Venue(name="Computer Lab 301", venue_type=VenueType.lab, capacity=60, location="IT Block, 3rd Floor", description="Computer lab with 60 workstations"),
                Venue(name="Computer Lab 302", venue_type=VenueType.lab, capacity=60, location="IT Block, 3rd Floor", description="Computer lab with 60 workstations"),
                Venue(name="Conference Room", venue_type=VenueType.conference_room, capacity=30, location="Admin Block, 1st Floor", description="Board room with video conferencing"),
                Venue(name="Open Ground", venue_type=VenueType.open_ground, capacity=2000, location="Campus Grounds", description="Open space for large events"),
            ]
            db.add_all(venues)
            db.commit()
    finally:
        db.close()


@app.get("/", tags=["Health"])
def root():
    return {"status": "ok", "app": "Anurag University Event Management System", "version": "1.0.0"}
