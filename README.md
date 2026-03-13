# 🎓 Event Management System (EMS)

A smart digital platform for managing university events and venue bookings efficiently. The system allows students to submit event proposals and coordinators to review and approve them digitally — reducing manual paperwork and approval delays.

The platform ensures secure access by restricting login to university email domains (`@anurag.edu.in`) and prevents double booking of venues such as labs, auditoriums, and seminar halls.

---

## 🚀 Features

| Feature | Description |
|---|---|
| 🔐 **Domain-Restricted Login** | Only users with `@anurag.edu.in` email can access the platform |
| 📅 **Event Proposal Submission** | Students can submit event details including venue, time, and description |
| 🏫 **Venue Availability Check** | Prevents conflicts and double bookings in real time |
| ✅ **Digital Approval Workflow** | Coordinators can review and approve or reject event requests |
| 📊 **Centralized Event Dashboard** | Displays upcoming events and venue usage at a glance |
| 📩 **Notifications System** | Keeps users informed about approval status updates |

---

## 🏗️ Tech Stack

### Frontend
- ⚛️ **React.js** — Component-based UI framework
- **HTML5 / CSS3 / JavaScript** — Core web technologies
- **Axios** — HTTP client for API communication

### Backend
- ⚡ **FastAPI** (Python) — High-performance REST API framework
- **Pydantic** — Data validation and settings management

### Database
- 🐘 **PostgreSQL** — Relational database for event data, user management, and venue scheduling

---

## 📁 Project Structure

```
ems/
├── frontend/               # React.js application
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   ├── pages/          # Route-level pages
│   │   └── api/            # Axios API calls
│   └── public/
├── backend/                # FastAPI application
│   ├── main.py             # Entry point
│   ├── routes/             # API route handlers
│   ├── models/             # Pydantic models
│   └── database/           # DB connection and queries
└── README.md
```

---

## ⚙️ Getting Started

### Prerequisites

- Node.js (v18+)
- Python (v3.10+)
- PostgreSQL (v14+)

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/your-org/ems.git
cd ems/backend

# Create a virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Configure environment variables
cp .env.example .env
# Edit .env with your database credentials

# Run the server
uvicorn main:app --reload
```

### Frontend Setup

```bash
cd ../frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The app will be running at `http://localhost:3000` and the API at `http://localhost:8000`.

---

## 🔐 Access Control

Login is restricted to users with an `@anurag.edu.in` email address. Attempts to register or log in with any other domain will be rejected by the system.

---

## 📄 API Documentation

Once the backend is running, interactive API docs are available at:

- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

> Built with ❤️ for Anurag University
