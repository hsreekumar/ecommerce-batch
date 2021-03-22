# ecommerce-batch


This microservice exposes the apis for batch creation of orders. It queues up the requests coming in and groups them to make an aggregated order accumulating multiple orders for a customer, so as to trigger a bulk request on the order-service. It makes use of kafka for queuing requests.




It is hosted as an individual microservice and works with a eureka server, but the apis for both this module and batch microservice as exposed via api-gateway module hosted on 8080. Inter-communication with order-service is also handled by api-gateway. All the endpoints could be accessed via swagger.
