# DevTimeTracker API Documentation

## Swagger UI
Swagger UI is available at:
```
http://localhost:8080/swagger-ui/index.html
```

---

## Running the API with Docker
To start the API, navigate to the `Docker/` directory and run:
```sh
docker-compose up -d
```
This will start the necessary services in detached mode.

---

## Database Access
The API uses PostgreSQL as its database. You can access it using a database client or directly via Docker.

### Connecting via DBeaver
- **Type:** PostgreSQL
- **Host:** `localhost`
- **Port:** `5433`
- **Database:** `coding_tracker`
- **Username:** `user`
- **Password:** `password`

### Connecting via Docker CLI
To access the database container, run:
```sh
docker exec -it docker-postgres-1 bash
```
Once inside the container, connect to PostgreSQL with:
```sh
psql -U user -d coding_tracker
```
This will open a PostgreSQL session for interacting with the database.

---

## API Endpoints
The API provides various endpoints for tracking coding activity. You can explore them using Swagger UI.

### Authentication
- **POST** `/auth/login` - Authenticate and receive a JWT token.
- **POST** `/auth/register` - Create a new user.

### Project Tracking
- **GET** `/projects` - Retrieve all tracked projects.
- **POST** `/projects` - Add a new project.
- **GET** `/projects/{id}` - Get details of a specific project.

### File Tracking
- **GET** `/files` - Get a list of tracked files.
- **POST** `/files` - Track a new file.
- **GET** `/files/{id}` - Get details of a specific file.

### Statistics
- **GET** `/stats/projects` - Get project-related coding time statistics.
- **GET** `/stats/files` - Get file-specific coding time statistics.

