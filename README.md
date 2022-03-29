# Parking Lot Attendant API
## Purpose
The intent of this helper is to ease the day-to-day duties of the parking lot attendants that are on shift as well as provide on-demand reporting of currently parked vehicles and the total revenue collected.

### Supported methods
| HTTP verb | API path | Notes |
| --------- | -------- | ----- |
| GET | /api/v1/parking-lots/{parkingLotId} | [Reference](#parking-lot-status-report) |
| POST | /api/v1/parking-lots | [Reference](#check-availability-and-reserve) |
| PATCH | /api/v1/parking-lots/{parkingReservationId} | [Reference](#admit-vehicle) |

----

### Parking Lot Status Report
Generates and presents a summary of currently parked vehicles and the total revenue collected.
``` json
GET /api/v1/parking-lots/ParkingLot1
{
    "parkedVehicleCounts": {
        "MONSTERTRUCK": 4,
        "CAR": 8
    },
    "parkedVehicleRates": {
        "MONSTERTRUCK": 15.0,
        "CAR": 5.0
    },
    "totalRevenue": 100.0
}
```

----

#### Check Availability and Reserve
Based on the given payload, checks if the given parking lot has space(s) available to accommodate parking the given vehicle. If there is space available, a reservation is created and the URI to invoke to [admit the vehicle](#admit-vehicle) is given in the response `Location` header. The reservation id (used in the admit API) is `base64` encoded to obfuscate the record id.
```
POST /api/v1/parking-lots
{
    "parkingLotId": "ParkingLotOne",
    "vehicleType": "CAR | MONSTERTRUCK"
}
```

----

#### Admit Vehicle
Admit the vehicle into the lot for parking and update the lot records accordingly. From this point, the [parking lot summary](#parking-lot-status-report) will reflect this newly parked vehicle.

----