In this group project assignment, your task as a group is to effectively and reliably build
an online store application. We will refer the online store application as Store throughout
this group project description, but you can name your own store. The store operates in a
pure-online manner, and the stock are distributed in various warehouses. Your store
should sell physical items instead of virtual items. Your store should also sell legal
products. If you are unsure, please consult your tutor.
Enterprise applications usually need to integrate with a number of third-party systems to
fulfill business requirements. You need to integrate your Store with 3 other external
enterprise applications (Bank, DeliveryCo and EmailService), along with several in-house
applications/components. Integration between these applications is required in order to
support the following workflow through which Store customers can make orders.
The Store application should:
• Authenticate the user through username and password
o The hashed version of the password should be stored in the database.
o You should use this account during the
demonstration: username: customer and password: COMP5348.
• Allow users to place the order
When an order is placed, the Store application should:
• Find a warehouse that can fulfill the complete order
o In case one warehouse cannot fulfill the complete order, then
Store application should find the proper warehouses to fulfill the complete
order.
• Request a transfer of payments/funds from the customer's account to the
Store’s account using the services provided by the Bank
o If the Bank transfer is successful, the Store sends a delivery request
to DeliveryCo, advising that the items are ready for pick-up from the
nominated warehouse(s).
• DeliveryCo notifies the Store when the delivery request has been received, when
the goods have been picked up from the warehouse(s), when they’re on the way to
the customer, and when they’ve been delivered.
• The customer is notified via email (using the EmailService), when DeliveryCo has
picked up the goods (and they’re now in their depot), when they’re on the delivery
truck, and when DeliveryCo claims that the delivery is complete.
The stock levels in the chosen warehouse should also be updated accordingly once
an order is processed successfully and completely (stock checked, bank transfer
OK, delivery pickup arranged).
• If the order processing fails for any reason, the customer is notified via email
(using the EmailService) that there is a problem with their order and that the
purchase order has been cancelled.
• The customer can also cancel their order before Store sends a delivery request
to DeliveryCo. When an order is cancelled, the Store application should:
o Update the warehouse(s) stock level accordingly.
o Refund the payment transferred to the customer account. The customer
should be notified by email of the status of refund (using the EmailService).
Your task as a group is to implement this application in a way to make it robust and
realistic, and also to include whatever additional functionality and schema that are needed
to meet all of the requirements listed above. In particular, you need to design and
implement the inter-application communication so that the application provides high
levels of availability and robustness.
For purposes of this group assignment, we have made the following assumptions:
• There is no need to implement the functionality of the Shopping Cart. We assume
that the user can only purchase one item in various quantities at a time.
• The DeliveryCo simply needs to update the status of the package after a random
time interval (e.g., around 5 seconds, but you can adjust this based on your
demonstration plan). This means that around 5 seconds after the delivery request
has been received, the goods will be picked up from the warehouse(s). The
DeliveryCo also might lose some of the packages at each stage of the delivery
process. The package loss rate can be set around 5% and adjusted based on your
demonstration plan.
• The EmailProvider simply needs to print out that a message was sent to a certain
address for this assignment (which is the current implementation). It does not need
to send an actual email.
• As for the warehouse component in the system, each warehouse will have its own
stock level records, and not all warehouses will hold stocks for all items. An
individual order can either be dispatched from a single warehouse (if the complete
order is available a single warehouse), or from multiple of warehouses if the
complete order is not available in one warehouse. DeliveryCo should handle the
process of picking multiple items from different warehouses, and you do not need
to support inter-warehouse transfers in the Store application. You will have to
implement the functionality that allows the order processing code to find out
which Warehouse(s) can fulfill the order (has all the requested items in stock). For
the purposes of this assignment, all warehouses that can fulfill the order are
equally good – that is, you don’t have to find the least number of warehouses for
an order.