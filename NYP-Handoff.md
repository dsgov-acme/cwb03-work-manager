## Work left to do

As this is a fork of the DSGov framework and with the DSGov framework being purpose-built to working with data that
was meant to be short-lived, it didn't meet all the expectations of an application that is very stateful. Also,
the DSGov framework was built to have it be update-able by the client themselves. For the NYC Para-transit system where
the processes are known ahead of time and the data we would be working with, having a more strongly typed process and
data structure would have been the preferred approach. As such, below is an explanation of future improvements/changes
we would like to make to the work-manager application:

- Move away from the Schema (dynamic_schema) table and its tables being used to hold the data structure for our entities
  - Create tables more strongly-typed to our needs: pretty much one table per schema that was created
    - CommonAddress
    - MTAConversation (not implemented in any form yet)
    - MTADriver (also not implemented in any form yet)
    - MTAEmergencyContact
    - MTALocation
    - MTAPaymentMethod
    - MTAPersonalCareAttendant
    - MTAPromiseTime
    - MTAReservation
    - MTARide
    - MTARider
    - MTARiderAccommodations
    - MTARoute
  - This allows us to better optimize performance of queries as well as make it easier to query for nested fields in our
    data structure.
- Move away from the Form Config (form_configuration) table and its related tables being used to hold the data structure
  for the frontend in lieu of creating custom components in the web repo.
- Get rid of the usage of Camunda in lieu of implementing the workflows in the applications themselves
  - This will eliminate the need for custom JavaDelegate classes that were used to perform programmatic operations
    within a Camunda workflow.
- Custom roles specific to this engagement were not implemented and/or enforced
