# REST API Specification — Sri Lanka Bus Journey Planner

Base URL: `http://localhost:8080/api/v1`

---

## 1. Stop Endpoints

### 1.1 Search Stops
Search routable bus stops by query string using PostgreSQL trigram similarity.

- **URL**: `/stops/search`
- **Method**: `GET`
- **Query Parameters**:
  - `q` (required, string): Query string (e.g. `Colombo`, `Kandy`, `Pettah`)
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Colombo Fort",
    "nameEn": "Colombo Fort",
    "nameSi": "කොළඹ කොටුව",
    "nameTa": "கொழும்பு கோட்டை",
    "normalizedName": "colombo fort",
    "district": "Colombo",
    "province": "Western",
    "latitude": 6.9344,
    "longitude": 79.8540
  }
]
```

### 1.2 Get Stop Details
Get details of a specific bus stop by ID.

- **URL**: `/stops/{id}`
- **Method**: `GET`
- **Response**: `200 OK`

---

## 2. Route Endpoints

### 2.1 List Routable Routes
List routable bus routes in the pilot dataset with optional filters.

- **URL**: `/routes`
- **Method**: `GET`
- **Query Parameters**:
  - `routeNumber` (optional, string): Route number filter (e.g. `1`, `17`)
  - `serviceType` (optional, string): Service type filter (`NORMAL`, `SEMI_LUXURY`)
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "routeNumber": "01",
    "name": "Colombo - Kandy",
    "originStop": { "id": 1, "name": "Colombo Fort" },
    "destinationStop": { "id": 50, "name": "Kandy Goods Shed" },
    "serviceType": "NORMAL",
    "province": "Inter-Provincial",
    "interProvincial": true
  }
]
```

### 2.2 Get Route Details & Stopping Sequence
Get route metadata along with ordered stopping sequence, fare stages, and distance.

- **URL**: `/routes/{id}`
- **Method**: `GET`
- **Response**: `200 OK`
```json
{
  "route": {
    "id": 1,
    "routeNumber": "01",
    "name": "Colombo - Kandy",
    "serviceType": "NORMAL"
  },
  "stops": [
    {
      "stopSequence": 1,
      "fareStage": 1,
      "distanceFromOriginKm": 0.0,
      "pickupAllowed": true,
      "dropoffAllowed": false,
      "stop": { "id": 1, "name": "Colombo Fort" }
    }
  ],
  "totalStops": 45,
  "availableServiceTypes": ["NORMAL", "SEMI_LUXURY"]
}
```

---

## 3. Departure Endpoints

### 3.1 Live Stop Timetable / Departures
Get scheduled bus departures from a specific stop for a given date and time.

- **URL**: `/departures`
- **Method**: `GET`
- **Query Parameters**:
  - `stopId` (required, integer): Boarding stop ID
  - `date` (optional, string format `YYYY-MM-DD`): Date (defaults to today in `Asia/Colombo`)
  - `time` (optional, string format `HH:mm`): Time (defaults to current time in `Asia/Colombo`)
- **Response**: `200 OK`
```json
{
  "stop": { "id": 1, "name": "Colombo Fort" },
  "requestedDate": "2026-08-19",
  "requestedTime": "14:30:00",
  "departures": [
    {
      "routeId": 1,
      "routeNumber": "01",
      "routeName": "Colombo - Kandy",
      "serviceType": "NORMAL",
      "boardingStop": { "id": 1, "name": "Colombo Fort" },
      "destination": "Kandy Goods Shed",
      "scheduledDeparture": "14:45:00",
      "tripId": 101
    }
  ],
  "totalCount": 1
}
```

---

## 4. Journey Planner Endpoint

### 4.1 Search Journeys
Find direct and 1-transfer itineraries between origin and destination stops.

- **URL**: `/journeys/search`
- **Method**: `GET`
- **Query Parameters**:
  - `fromStopId` (required, integer): Origin stop ID
  - `toStopId` (required, integer): Destination stop ID
  - `date` (optional, string format `YYYY-MM-DD`): Departure date
  - `time` (optional, string format `HH:mm`): Departure time
- **Response**: `200 OK`
```json
{
  "fromStop": { "id": 1, "name": "Colombo Fort" },
  "toStop": { "id": 50, "name": "Kandy Goods Shed" },
  "requestedDate": "2026-08-19",
  "requestedTime": "14:30:00",
  "journeys": [
    {
      "journeyId": "J-DIRECT-1-101",
      "departureTime": "14:45:00",
      "arrivalTime": "17:45:00",
      "durationMinutes": 180,
      "transferCount": 0,
      "totalFare": 455.00,
      "fareStatus": "COMPLETE",
      "labels": ["RECOMMENDED", "FASTEST", "CHEAPEST"],
      "legs": [
        {
          "legIndex": 1,
          "routeId": 1,
          "routeNumber": "01",
          "routeName": "Colombo - Kandy",
          "serviceType": "NORMAL",
          "tripId": 101,
          "boardingStop": { "id": 1, "name": "Colombo Fort" },
          "dropOffStop": { "id": 50, "name": "Kandy Goods Shed" },
          "departureTime": "14:45:00",
          "arrivalTime": "17:45:00",
          "durationMinutes": 180,
          "fare": 455.00,
          "fareType": "EXACT_POINT_TO_POINT",
          "fareStatus": "EXACT"
        }
      ]
    }
  ],
  "totalResults": 1,
  "dataNote": "Pilot Dataset (29 routable routes). Fares strictly follow official July 2026 NTC records."
}
```

---

## 5. Official NTC Fare Calculation Policy

1. **Exact Point-to-Point (`EXACT_POINT_TO_POINT`)**:
   Returned when an exact fare record exists in the NTC route fare matrix for the specific origin and destination pair.
2. **Endpoint-Only (`FULL_ENDPOINT_ONLY`)**:
   Returned when only full route endpoint fare is available in NTC Document E.
3. **Unpriced (`FARE_UNAVAILABLE`)**:
   Returned when intermediate stop fare is not explicitly published by NTC. In accordance with strict NTC policy, **no fares are estimated or interpolated**.
