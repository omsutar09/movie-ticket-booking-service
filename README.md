# Movie Ticket Booking – REST API Reference

Base URL: `http://localhost:8080` (when running the application)

---

## Read scenarios

### 1. Browse theatres and show timings by city, movie, and date

**GET** `/api/shows/browse`

| Param       | Type   | Description        |
|------------|--------|--------------------|
| cityName   | string | e.g. Mumbai        |
| movieTitle | string | e.g. Inception     |
| date       | date   | ISO date (YYYY-MM-DD) |

Example: `GET /api/shows/browse?cityName=Mumbai&movieTitle=Inception&date=2026-02-10`

---

## Write scenarios

### 2. Book movie tickets (theatre, show, seats)

**POST** `/api/bookings`

Seats are **locked (held)** for a configurable TTL (default 10 minutes) so only one user can book them while completing payment. Concurrent requests for the same seats are serialized via pessimistic DB locks.

Body (JSON):

```json
{
  "showId": 1,
  "customerEmail": "user@example.com",
  "seatNumbers": ["A1", "A2", "A3"]
}
```

- Creates a booking in **PENDING_PAYMENT**; seats move to **HELD**.
- Response includes **holdExpiresAt** (ISO date-time). Complete payment before this time via **POST** `/api/bookings/{bookingId}/pay`.
- If payment is included in the body (see “Book and pay in one request”), seats are confirmed immediately on successful payment.
- TTL is configured by `booking.hold-ttl-minutes` (default: 10). Expired holds are released every minute by a scheduled job; seats become **AVAILABLE** again and booking status becomes **EXPIRED**.

---

### 3. Theatres: create / update / delete shows

- **POST** `/api/theatres/{theatreId}/shows` – create show  
  Body: `{ "movieId": 1, "startTime": "2026-02-10T10:00", "endTime": "2026-02-10T12:00", "price": 250.0 }`

- **PUT** `/api/theatres/shows/{showId}` – update show (partial body allowed)

- **DELETE** `/api/theatres/shows/{showId}` – delete show

---

### 4. Seat inventory (allocate and update)

- **POST** `/api/theatres/shows/{showId}/seats` – allocate seats  
  Body: `{ "seatNumbers": ["A1", "A2", "B1"] }`

- **PUT** `/api/theatres/shows/{showId}/seats` – add/remove seats  
  Body: `{ "add": ["C1"], "remove": ["A1"] }` (only AVAILABLE seats can be removed)

- **GET** `/api/theatres/shows/{showId}/seats` – list seats and status (AVAILABLE / BOOKED)

---

### 5. Bulk booking and cancellation

- **POST** `/api/bookings/bulk` – multiple bookings  
  Body: `{ "bookings": [ { "showId": 1, "customerEmail": "a@x.com", "seatNumbers": ["A1"] }, ... ] }`

- **POST** `/api/bookings/{bookingId}/cancel` – cancel one booking

- **POST** `/api/bookings/cancel/bulk` – bulk cancel  
  Body: `{ "bookingIds": [1, 2, 3] }`

---

### 6. Get booking details

**GET** `/api/bookings/{bookingId}`

---

## Payment (Strategy: Credit Card & UPI)

### 7. Pay for a booking

**POST** `/api/bookings/{bookingId}/pay`

Only bookings in **PENDING_PAYMENT** can be paid. If **holdExpiresAt** has passed, the request fails with “Seat hold expired. Please create a new booking.”

**Credit card** – Body (JSON):

```json
{
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "cardExpiry": "12/28",
  "cardCvv": "123",
  "cardHolderName": "John Doe"
}
```

**UPI** – Body (JSON):

```json
{
  "paymentMethod": "UPI",
  "upiId": "user@paytm"
}
```

Returns payment details including `transactionId` and `maskedRef` (e.g. ****1111 or u***@paytm).

### 8. Get payment for a booking

**GET** `/api/bookings/{bookingId}/payment`

### 9. Book and pay in one request

**POST** `/api/bookings` – include optional `payment` in the body to pay immediately:

```json
{
  "showId": 1,
  "customerEmail": "test@example.com",
  "seatNumbers": ["A1", "A2"],
  "payment": {
    "paymentMethod": "UPI",
    "upiId": "customer@ybl"
  }
}
```

---

## Sample flow after startup

1. **Browse:** `GET /api/shows/browse?cityName=Mumbai&movieTitle=Inception&date=2026-02-10`
2. **Seats for show:** `GET /api/theatres/shows/1/seats`
3. **Book:** `POST /api/bookings` with `{ "showId": 1, "customerEmail": "test@example.com", "seatNumbers": ["A1","A2","A3"] }`

DataLoader seeds Mumbai/Delhi, movies (Inception, Titanic), theatres, two shows on 2026-02-10, and seats A1–A5, B1–B5 for each show.