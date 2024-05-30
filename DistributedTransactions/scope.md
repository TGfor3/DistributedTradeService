## Trade Server Scope

## Project Description
The Automated Trading System is designed to facilitate the trading of stocks, by automating the process from order placement to execution. The system aims to improve efficiency, reduce human error, and provide a seamless trading experience for users. It includes features for both buying and selling, along with handling errors and unusual trading paths.

## Project Objectives
- To automate the buying and selling of stocks.
- To provide a reliable and efficient trading platform.
- To minimize the risk of human error.
- To handle both typical and atypical trading scenarios.

## Features and Functions
- **Order Placement**: Users can place buy or sell orders within the system.
- **Order Execution**: The system executes orders based on predefined criteria.
- **Trade Confirmation**: Users receive confirmation upon the successful execution of a trade, or in case of an error.
- **Error Handling**: The system identifies and handles errors in order processing.
- **Alternative Trading Paths**: The system manages less common trading scenarios ("Sad Paths") effectively.

## Use Cases
- **Buy Process**: Detailed in the 'HappyBuy.jpg.png' BPMN diagram, this process outlines the steps taken when a user places a buy order.
    - *Scenario: Buying a stock with sufficient funds and stock availability*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And I have sufficient funds to purchase a specific number of shares
       And there are enough shares available of the specified stock
       When I place a buy order for a certain number of shares of the stock
       Then the buy order should be successfully executed
       And the cost of the purchase should be deducted from my account
       And the purchased shares should be added to my portfolio
- **Sell Process**: As depicted in 'HappySell.jpg', this process describes the automated system's response to a sell order.
    - *Scenario: Selling a stock with sufficient share ownership*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And I own enough shares of the stock to cover the sell order
       When I place a sell order for all my shares of the stock
       Then the sell order should be successfully executed
       And the proceeds from the sale should be credited to my account
       And the shares should be removed from my portfolio
- **Sad Path Handling**: The 'SadPath.png' and 'SadPath1.png' diagrams provide insight into the system's handling of non-standard trading scenarios, like when a user submits an illegal trade. 
     - *Scenario: Buying a stock with insufficient funds*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And the specified stock has enough shares available for trading
       But I do not have sufficient funds to cover the cost of the buy trade
       When I attempt to place a buy order for a certain number of shares of the stock
       Then the trade should not be executed
       And an error message should be displayed stating insufficient funds
       And my account funds should remain unchanged
       And no shares should be added to my portfolio
- **Error Handling in Worker Crashes**: Referenced in 'WorkerCrashes.png', this diagram explains the system's response to worker process failures.
    - *Scenario: Executing a trade but the worker crashes*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And I own enough shares of the stock to cover the sell order
       When I place a sell order for all my shares of the stock
       But the worker crashes
       Then RabbitMQ will detect that the transaction was not executed
       And the transaction will return to the queue
       To be picked up by another worker
- **Error Handling in CAS Fails**: The 'CASFails.png' BPMN diagram shows the system's error handling when there is a failure in the Cient Account System.
    - *Scenario: Executing a transaction but the CAS fails*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And I own enough shares of the stock to cover the sell order
       When I place a sell order for all my shares of the stock
       But the CAS fails
       The system will detect the failure
       And undo any successful updates
       And launch a new CAS worker
       And the transaction will return to the queue
       To be picked up by another worker
- **Error Handling in BIS Fails**: Illustrated in 'BISFails.png', this outlines how the system deals with failures in the Brokerage Information System (BIS).
     - *Scenario: Executing a transaction but the BIS fails*
       Given I am a validated user
       And I have specified a valid stock name that is traded on the exchange
       And I own enough shares of the stock to cover the sell order
       When I place a sell order for all my shares of the stock
       But the BIS fails
       The system will detect the failure
       And undo any successful updates
       And launch a new BIS worker
       And the transaction will return to the queue
       To be picked up by another worker
