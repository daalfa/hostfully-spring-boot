[![Java CI with Maven](https://github.com/daalfa/hostfully-spring-boot/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/daalfa/hostfully-spring-boot/actions/workflows/maven.yml)

# Booking Service for Home Showing
This is a simple booking service for home showing.  
It is a Spring Boot application with H2 database.  

A Guest/Visitor/Client can book a time to visit a property.  
The Host/Owner/Manager can block out times for a property.  

Guests can:
* View bookings
* Create bookings
* Update bookings (change details, cancel or re-book)
* Delete bookings

Hosts can:
* View blockings
* Create blockings
* Update blockings
* Delete blockings

## Getting Started
* [/api/guest/bookings](http://localhost:8080/api/guest/bookings)
* [/api/host/blockings](http://localhost:8080/api/host/blockings)
* [/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Booking payload
```json
{
    "id": 1,
    "name": "booking name",
    "description": "booking description",
    "startDate": "2024-01-01 09:30:00",
    "endDate": "2024-01-01 10:30:00",
    "isCanceled": false,
    "property": {
        "id": 1
    }
}
```

### Blocking payload
```json
{
    "id": 1,
    "name": "Block name",
    "startDate": "2024-01-01 01:00:00",
    "endDate": "2024-01-02 02:00:00",
    "property": {
        "id": 1
    }
}
```

## Validations
Cancel or re-book a canceled booking can be achieved with PUT request.  
When a Block is created, it will cancel Bookings that overlap or are within that period.  
You cannot create a canceled booking.


## Architecture
This Spring Boot service was created with [spring initializr](https://start.spring.io/).  
Database is H2 with Liquibase to migrate and changelog.  
Table have proper indexes.  
Tests with Junit5 and AssertJ.  
Jacoco test coverage is enforced to 90%.  
Github action for CICD is configured.  