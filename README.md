# Cinema Tickets – Java 25

A clean, maintainable implementation of a Cinema Ticket Service, developed as part of the DWP Digital Engineering coding exercise.

## Overview

The solution provides a robust implementation of a ticket purchasing service that:

- Validates input and enforces all business rules
- Calculates the total payment amount
- Determines the number of seats to reserve
- Integrates with external payment and seat reservation services
- Calling external services to complete payment and seat allocation
- Uses modern Java features for clarity and maintainability

## Business Rules

- Ticket types: **Adult** £25, **Child** £15 and **Infant** £0
- Maximum of **25 tickets** per purchase
- **Infant** tickets are free and do not require a seat
- Number of **Infants cannot exceed** the number of Adults (each infant sits on an adult’s lap)
- **Child and Infant** tickets require at least one Adult ticket
- **Account ID** must be greater than zero

## Design Highlights

- **Fail-fast validation** to reject invalid requests early
- **Immutable domain model** using Java `record` for concise domain data representation
- **Centralised ticket pricing** using an immutable `Map.of(...)` to associate ticket types with their prices
- **Single responsibility methods** for readability and maintainability
- **Dependency injection** for external services to improve loose coupling and testability
- **Stream-based aggregation** for concise and expressive calculations

## Test Suite Cover

The JUnit 5 and Mockito test suite covers:

- Successful adult, child, and infant ticket purchases.
- Correct payment calculations and seat allocations.
- Invalid account IDs, including null, zero, and negative values.
- Missing or null ticket requests.
- Zero and negative ticket quantities.
- Null ticket types.
- Child and Infant tickets purchased without an Adult ticket.
- Infant-to-adult ratio validation.
- The 25-ticket purchase limit, including boundary cases.
- Verification that invalid purchases do not invoke external services.

## Running the Project
- Java 25
- Apache Maven

#### Run the Test Suite
```bash
mvn test
```

#### Build the project and run its verification lifecycle
```bash
mvn clean install
```

## Project Structure
```
src/
├── main/
│   └── java/
│       ├── thirdparty/
│       │   ├── paymentgateway/
│       │   └── seatbooking/
│       └── uk/gov/dwp/uc/pairtest/
│           ├── domain/
│           │   └── TicketTypeRequest.java
│           ├── exception/
│           │   └── InvalidPurchaseException.java
│           ├── TicketService.java
│           └── TicketServiceImpl.java
└── test/
    └── java/
        └── uk/gov/dwp/uc/pairtest/
            └── TicketServiceImplTest.java
```