# Ecommerce microservice platform
<hr>

## Purpose

An implementation of a platform that unites Sellers and Buyers of products into a single place.

Each Seller puts at sale his products, as well setting the available quantities and price.

Shipping costs are taken into account (national vs international).

Payments are processed using a test Stripe API account.

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

An SDK is provided under `./ecommerce-sdk` to avoid code repetition between microservice communication.


## Installation

### Requirements

To launch the whole architecture, you would need:

- jdk17
- maven
- docker
- docker-compose

It is recommended to leave around 3Gb of disk memory, as well as 3Gb of RAM before launching.
Make sure nothing is running on ports 80, 443 and 8080.

### Launch

On the root folder of the project, simply run `bash ./launch.sh` if in Linux or MacOS.

It should take between 5 and 15 minutes to fully setup, depending on the machine you are running.

## TODO's
- Unit and e2e tests;
- Some bugfixing;
- Create more ADMIN role actions through the architecture;


## Usage

Import the Postman collection and environment variables into your local instance.
If you pretend to continue using SSL verification (as the SSL certificate provided for NGINX is self-signed and, therefore, will not be trusted by default),
you have to trust the CA certificate as mentioned above.


1. Create a Buyer and a Seller by running their respective `/register` POST endpoints on their respective folders. You may try to login immediately and receive a session token, which will be automatically saved as a variable (one for buyer and another for seller). Note that session tokens only last for 5 minutes before you have to login again!
2. Create a new Product by invoking the POST request of `registerProduct` under the Product Service folder.
3. Invoke the `addItem` request on the Cart Service folder as is. This will add new items to your cart.
4. Check the `getCart` endpoint on the same folder to see if the items were correctly allocated to your cart;
5. Place an order by invoking `placeOrder` under the same folder. This will launch a series of actions among many microservices. To keep track of the order status, check the
`getOrderStatus` item under the Order Service folder and pay attention to the `status` value, which should be `PENDING_PAYMENT`.
6. To finish the order and pay, you should invoke the `pay` endpoint under Payment Service. This will call Stripe API to finish the call.
7. You can check your order history under `getBuyerOrderHistory` under Order Service folder.