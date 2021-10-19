# Ecommerce microservice platform
<hr>

## Purpose

An implementation of a platform that unites Sellers and Buyers of products into a single place.

Each Seller puts at sale his products, as well setting the available quantities and price.

Shipping costs are taken into account (national vs international) and charged to the Buyer. The idea is that the Seller is in charge of sending the parcels himself, outside the scope of this project.

Shipping costs go as follows:
- For national purchases, a fixed tax of $2 is paid per item type being transferred;
- Also, a $1 rate has to be paid per unit of weight of each item;
- For international purchases, a fixed tax of $5 is paid per item type being transferred;
- Also, a $2 rate has to be paid per unit of weight of each item;

In the end, VAT applies at a constant rate of 23% added up to the cost.

Payments are processed using a test Stripe API account in dollars. The idea is that the platform retains all the payments and periodically transfers the proper funds to the respective sellers. This last operation, however, is out of the scope of this project.

Postman collections under `./postman`.
Note that you should trust the CA certificate `./postman/Tiountchik CA_cert.pem`on Postman if you wish to keep SSL verification active. This is because the certificate that provides for the HTTPS connections behind the NGINX proxy is self-signed and needs to be trusted explicitly.

To do so, upload the file under Settings -> Certificates -> CA Certificates.

### Microservice description

- buyer-service: responsible for maintaining buyer user information and provide authentication;
- cart-service: create a shopping cart for the buyer exclusively;
- order-service: will persist and manage the purchase process;
- payment-service: deals with Stripe API payments. For more info check: https://stripe.com/docs/testing;
- product-service: runs the seller's product catalog. Also allows buyers to search for new items;
- seller-service: equivalent to the buyer-service but for sellers.

Other services running are NGINX, which provides a reverse-proxy and whose configuration is under `./proxy-config`. Also, a Keycloak instance running two Realms (one for the Buyers and another for the Sellers), is launched by Docker on the `docker-compose.yaml` file but the initial configuration can be found under `./sso-server-config`.

Microservices communicate with each other asynchronously through Kafka Topics.

An SDK is provided under `./ecommerce-sdk` to avoid code repetition between microservice communication.

Database DDL operations are versioned with Liquibase.

Swagger is available for each microservice. The URLs are the following after launch:


| Microservice | URL |
| :---: | :---: | 
| buyer-service | https://localhost/api/buyer/swagger-ui.html
| cart-service| https://localhost/api/cart/swagger-ui.html
| order-service | https://localhost/api/order/swagger-ui.html
| payment-service | https://localhost/api/payment/swagger-ui.html
| product-service | https://localhost/api/product/swagger-ui.html
| seller-service | https://localhost/api/seller/swagger-ui.html

## Installation

### Requirements

To launch the whole architecture, you would need:

- jdk17
- maven
- docker
- docker-compose

It is recommended to leave around 3Gb of disk memory, as well as 3Gb of RAM before launching.
Make sure nothing is running on ports 80, 443, 2181, 9092 and 29092. 

### Launch

On the root folder of the project, simply run `bash ./launch.sh` if in Linux or MacOS.

It should take between 5 and 15 minutes to fully setup, depending on the machine you are running.

## Security roles

To login application-wise as an admin, you should user the respective login endpoints available in postman for Buyer and Seller.

### Buyer default admin
username: admin-buyer@ecommerce.com

password: password

### Seller default admin
username: admin-seller@ecommerce.com

password: password

### Promote and demote users

By default, an admin is created on startup for each realm. To promote and demote users, visit https://localhost/auth and enter:

username: admin

password: Pa55w0rd

By clicking on a specific user under Users -> Show all, you will see the Roles tab where you are able to assign and remove roles for users.

## Usage

Import the Postman collection and environment variables into your local instance.
If you pretend to continue using SSL verification (as the SSL certificate provided for NGINX is self-signed and, therefore, will not be trusted by default),
you have to trust the CA certificate as mentioned above.


1. Create a Buyer and a Seller by running their respective `/register` POST endpoints on their respective folders. You may try to login immediately and receive a session token, which will be automatically saved as a variable (one for buyer and another for seller). Note that session tokens only last for 5 minutes before you have to login again!
2. Create a new Product by invoking the POST request of `registerProduct` under the Product Service folder.
3. Invoke the `addItem` request on the Cart Service folder as is. This will add new items to your cart.
4. Check the `getCart` endpoint on the same folder to see if the items were correctly allocated to your cart. Here, you will receive a requestId (because the architecture needs to check whether there is enough stock for the item);
5. Then, the result of the insertion will be visible under Cart Service -> `addOrUpdateItemStatus`;
6. Place an order by invoking `placeOrder` under the same folder. This will launch a series of actions among many microservices. To keep track of the order status, check the
`getOrderStatus` item under the Order Service folder and pay attention to the `status` value, which should be `PENDING_PAYMENT`.
7. To finish the order and pay, you should invoke the `pay` endpoint under Payment Service. This will call Stripe API to finish the call.
8. You can check your order history under `getBuyerOrderHistory` under Order Service folder.

## Admin actions

Generally, admins have a broader range of influence over the endpoint calls. While regular users can only manipulate data under their sphere of influence, admins should be able to CRUD everything.


## Tests

To run all the tests simply run:
```
mvn test
```

On the project root directory. Should take around 2.5 minutes.

Tests do not cover the entire architecture. However, some notable test classes:
- e2e: CartControllerIT
- integration: PaymentServiceIT
- unit: BuyerServiceUT

Occasionally, tests might take an infinite time due to a race condition identified [here](https://github.com/liquibase/liquibase-cache/issues/1). To circumvent this, unfortunately, the only way is to rerun the tests until they don't block each other. 