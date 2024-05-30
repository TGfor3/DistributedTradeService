# API Documentation

## Trade Transaction API

Our Trade Transaction service can be accessed through calls to the static IP address consisting of the IP address of the device running the T2Deploy container, and port 5000

### Register a client

The endpoing for registering a client is `{IP Address}:5000/register`. Send an HTTP POST request

```
{
    "clientID":0,
    "amount":1000
}
```

### Purchase a stock

The endpoing for buying a stock for example, Netflix (NFLX) is `{IP Address}:5000/buy`. Send an HTTP POST request

```
{
    "clientID": 0,
    "ticker": "NFLX",
    "amount": 3
}
```

### Sell a stock

The endpoing for selling a stock, for example Apple (AAPL) is `{IP Address}:5000/sell`. Send an HTTP POST request

```
{
    "clientID": 0,
    "ticker": "AAPL",
    "amount": 2
}
```
