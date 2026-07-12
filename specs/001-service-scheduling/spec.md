# Feature Specification: Unified Service Scheduler

**Feature Branch**: `001-service-scheduling`

**Created**: 2026-07-12

**Status**: Draft

**Input**: Scenario A of the Keyloop technical assessment: reliably create, retrieve, and list dealership service appointments.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a Confirmed Appointment (Priority: P1)

A booking agent requests service for a known customer and vehicle at a selected dealership and start
time. The system assigns an active qualified technician and active service bay that are available for
the entire service duration, then confirms the persisted appointment.

**Why this priority**: Reliable booking replaces the core manual process and delivers the primary value.

**Independent Test**: Submit a valid request against known resources and verify exactly one confirmed
appointment is retained with every association and the calculated interval.

**Acceptance Scenarios**:

1. **Given** a known customer and their vehicle, a valid dealership and service type, an active
   qualified technician, and an active available bay, **When** an appointment is requested, **Then**
   exactly one confirmed appointment is persisted with all associations and calculated times.
2. **Given** an available technician without the required qualification, **When** an appointment is
   requested, **Then** that technician is not assigned.
3. **Given** one busy eligible technician or bay and another eligible available resource, **When** an
   appointment is requested, **Then** an available valid combination is assigned.
4. **Given** an existing appointment ending at the requested start time, **When** another valid
   appointment is requested, **Then** the existing appointment does not cause an overlap conflict.
5. **Given** a successfully processed request is repeated with the same retry identity, **When** it is
   submitted again, **Then** the original outcome is returned without another confirmed appointment.

---

### User Story 2 - Reject an Unavailable or Invalid Appointment (Priority: P1)

A booking agent receives a clear outcome when references are invalid or no eligible technician-and-bay
combination is free, without any partial or conflicting booking being retained.

**Why this priority**: Preventing double-booking and partial records is as essential as successful booking.

**Independent Test**: Submit conflicting, concurrent, and invalid requests and verify distinguishable
outcomes and the absence of unintended appointments or assignments.

**Acceptance Scenarios**:

1. **Given** no qualified technician or service bay is available for the full interval, **When** an
   appointment is requested, **Then** no appointment is created and an availability conflict is returned.
2. **Given** a confirmed appointment occupying a technician and bay, **When** another request overlaps
   its interval, **Then** those resources are not assigned to the second appointment.
3. **Given** two simultaneous requests competing for the final available technician and bay, **When**
   both are processed, **Then** at most one is confirmed and neither resource is double-booked.
4. **Given** an unknown or inconsistent customer, vehicle, dealership, or service type, **When** the
   request is submitted, **Then** no appointment is created and the relevant invalid or not-found outcome
   is returned.

---

### User Story 3 - Retrieve a Confirmed Appointment (Priority: P2)

A booking agent or downstream client retrieves a persisted appointment and reviews its booking and
assigned-resource details.

**Why this priority**: Consumers need evidence of the confirmed booking after creation.

**Independent Test**: Retrieve a known appointment identifier and compare every returned field with the
persisted booking; request an unknown identifier and verify a not-found outcome.

**Acceptance Scenarios**:

1. **Given** a persisted confirmed appointment, **When** its identifier is requested, **Then** its
   customer, vehicle, dealership, service type, technician, bay, start, end, and status are returned.
2. **Given** an unknown appointment identifier, **When** it is requested, **Then** an appointment
   not-found outcome is returned without exposing internal details.

---

### User Story 4 - List Appointments (Priority: P3)

A booking agent lists persisted appointments and may narrow the results by dealership and UTC date range.

**Why this priority**: Reviewable lists support verification and daily booking oversight.

**Independent Test**: Create appointments across dealerships and dates, then verify unfiltered and
filtered lists include exactly the matching persisted records.

**Acceptance Scenarios**:

1. **Given** persisted appointments, **When** appointments are listed without filters, **Then** all
   appointments visible to the scenario are returned in deterministic order.
2. **Given** appointments across dealerships, **When** a dealership filter is supplied, **Then** only
   appointments for that dealership are returned.
3. **Given** appointments across dates, **When** a valid UTC date range is supplied, **Then** only
   appointments whose start time falls within the half-open range are returned.

### Edge Cases

- A zero, negative, malformed, or non-UTC desired start time is rejected before allocation.
- An inactive technician or bay is ineligible even when otherwise available.
- A qualified technician at another dealership is ineligible.
- Cancelled appointments do not block resources; confirmed appointments do.
- An appointment sharing only an endpoint with another appointment is allowed.
- A request overlapping by any positive duration conflicts for the occupied resource.
- A vehicle paired with a customer other than its recorded owner is rejected as inconsistent.
- An invalid or reversed list date range is rejected; a valid range with no matches returns an empty list.
- If persistence or allocation fails, no appointment or partial assignment remains.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept an appointment request containing a customer, vehicle,
  dealership, service type, desired start time, and a retry identity.
- **FR-002**: The system MUST validate required values and distinguish invalid input, missing records,
  inconsistent references, availability conflicts, and operational failures.
- **FR-003**: The system MUST verify that the vehicle belongs to the submitted customer.
- **FR-004**: Each service type MUST define a positive fixed duration and required qualification.
- **FR-005**: The system MUST calculate the end time from the requested start and service duration.
- **FR-006**: All stored appointment timestamps MUST represent UTC instants.
- **FR-007**: Only active technicians and bays belonging to the requested dealership are eligible.
- **FR-008**: An eligible technician MUST possess the service type's required qualification.
- **FR-009**: Only confirmed appointments MUST block assigned resources; cancelled appointments MUST
  NOT block either resource.
- **FR-010**: Appointment intervals MUST use `[start, end)` semantics: two intervals overlap exactly
  when each starts before the other ends, and touching endpoints MUST NOT overlap.
- **FR-011**: The system MUST select an eligible technician and bay that remain unblocked for the
  complete requested interval.
- **FR-012**: Resource selection MUST be deterministic when multiple equivalent combinations exist.
- **FR-013**: Resource availability revalidation and appointment persistence MUST occur in one
  indivisible booking operation.
- **FR-014**: Concurrent booking attempts MUST result in at most one confirmed appointment for any
  overlapping use of the same technician or bay.
- **FR-015**: A confirmed appointment MUST persist its customer, vehicle, dealership, service type,
  technician, bay, start time, end time, and status before confirmation is returned.
- **FR-016**: A failed request MUST leave no appointment or partial resource assignment.
- **FR-017**: Repeating a request with the same retry identity and request content MUST NOT create a
  duplicate appointment and MUST return HTTP 201 with the original representation and Location.
- **FR-018**: Reusing a retry identity with different request content MUST be rejected as invalid.
- **FR-019**: Users MUST be able to retrieve an appointment by identifier with all required details.
- **FR-020**: Users MUST be able to list appointments with optional dealership and UTC start-time range
  filters, using deterministic ordering.
- **FR-021**: Failure outcomes MUST be understandable and machine-distinguishable without customer
  personal information or internal implementation details.

### Key Entities

- **Customer**: The person receiving service; owns one or more vehicles.
- **Vehicle**: The serviced vehicle; belongs to exactly one customer for this assessment.
- **Dealership**: The location that owns eligible technicians and service bays.
- **Service Type**: The requested work, including fixed duration and required qualification.
- **Qualification**: A capability held by technicians and required by service types.
- **Technician**: A dealership resource with active status and one or more qualifications.
- **Service Bay**: An active or inactive dealership resource available to included service types.
- **Appointment**: A booking associating all required entities, assigned resources, UTC interval, and
  status of at least Confirmed or Cancelled.
- **Retry Identity**: A client-supplied identity linking repeated delivery of the same booking request.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of all 14 acceptance scenarios in this specification pass in automated verification.
- **SC-002**: Across all overlap and simultaneous-request tests, zero confirmed appointments overlap
  for the same technician or bay.
- **SC-003**: 100% of confirmed appointments retain all six required entity/resource associations plus
  calculated start, end, and status.
- **SC-004**: 100% of tested invalid, missing, conflict, and operational outcomes are distinguishable.
- **SC-005**: Replaying the same successful request 10 times produces exactly one confirmed appointment.
- **SC-006**: A fresh reviewer can follow repository instructions and demonstrate creation, conflict,
  retrieval, listing, back-to-back booking, and competing requests without undocumented setup.

## Assumptions

- Desired appointment time is the start time; duration is fixed by service type.
- Customers and vehicles exist before booking, and each vehicle has one customer owner.
- Technicians may hold multiple qualifications; any active bay at the dealership supports included services.
- The system assigns resources automatically using deterministic ordering.
- Statuses include at least Confirmed and Cancelled; only Confirmed blocks availability.
- Date-range filtering applies to appointment start times and uses a half-open UTC interval.
- Authentication and authorization are outside Scenario A; callers are treated as authorized booking users.

## Scope Classification *(mandatory)*

- **Required**: Confirmed appointment creation, atomic allocation, qualification and overlap rules,
  retry-safe submission with a required idempotency key, retrieval, filtered listing, validation,
  conflict handling, and persistence. Idempotency is core because FR-017 and FR-018 require safe retries.
- **Optional**: Additional read-only filters or demonstration data that do not alter required behavior.
- **Future**: Cancellation actions, rescheduling, operating hours, shifts, breaks, notifications,
  payments, authentication, authorization, and a production user interface.

## Traceability *(mandatory)*

| Requirement | User Story | Acceptance Scenario |
|-------------|------------|---------------------|
| FR-001–FR-018, FR-021 | US1, US2 | US1-AS1–AS5; US2-AS1–AS4 |
| FR-019, FR-021 | US3 | US3-AS1–AS2 |
| FR-020, FR-021 | US4 | US4-AS1–AS3 |
