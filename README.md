[![Java CI with Maven](https://github.com/daalfa/hostfully-spring-boot/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/daalfa/hostfully-spring-boot/actions/workflows/maven.yml)

# Booking Service for Home Showing
This is a simple booking service for home showing.  
It is a Spring Boot application with H2 database.  

A Guest/Visitor/Client can book a time to visit a property.  
The Host/Owner/Manager can block out times for a property.  

Guests can:
* View bookings
* Create bookings
* Cancel bookings
* Update bookings
* Delete bookings

Hosts can:
* View blockings
* Create blockingss
* Delete blockings

If a visitation is booked and the host blocks out the same time, the showing is cancelled.  
A guest cannot book a showing if the host has blocked out that date.

## Getting Started
* [/api/guest/bookings](http://localhost:8080/api/guest/bookings)
* [/api/host/blockings](http://localhost:8080/api/host/blockings)
* [/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Bookings work with a timeslot of 1 hour.  
Blockings work with a timeslot of 1 day.
