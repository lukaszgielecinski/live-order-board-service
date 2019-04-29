# live-order-board-service

## Assumptions: 
* LiveOrderSummaryRecord will be displayed as a single aggregation record but for cancelling purposes the user will be able to expand it and display orders which make it up.
* Client uses dependency Injection framework (eg. Spring/Guice) and following part of the implementation will include configuration class/module to bind implementation to interfaces.