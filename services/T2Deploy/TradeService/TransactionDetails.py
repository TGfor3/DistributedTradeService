import struct
from enum import Enum

class TransactionType(Enum):
    BUY = 0
    SELL = 1
    NEW_CLIENT = 2

class CompletionType(Enum):
    SUCCESS = 0
    FAILURE = 1
    UNFINISHED = 2

class TransactionDetails:
    def __init__(self, client_id, ticker, stock_amount, transaction_type, lockin_price, transaction_id):
        self.client_id = client_id
        self.ticker = ticker
        self.stock_amount = stock_amount
        self.transaction_type = transaction_type
        self.lockin_price = lockin_price
        self.transaction_id = transaction_id
        self.completion_status = CompletionType.UNFINISHED

    def get_dollar_value(self):
        return self.lockin_price * self.stock_amount
    
    def getTicker(self):
        return self.ticker

    def serialize(self):
        ticker_bytes = self.ticker.encode('utf-8')
        ticker_length = len(ticker_bytes)
        format_str = f'>i i {ticker_length}s i i i q i'
        return struct.pack(format_str,
                           self.client_id,
                           ticker_length,
                           ticker_bytes,
                           self.stock_amount,
                           self.transaction_type.value,
                           self.lockin_price,
                           self.transaction_id,
                           self.completion_status.value)

    @staticmethod
    def deserialize(data_bytes):
        header_format = '>i i'
        header_size = struct.calcsize(header_format)
        client_id, ticker_length = struct.unpack(header_format, data_bytes[:header_size])
        ticker_start = header_size
        ticker_end = ticker_start + ticker_length
        ticker_bytes = data_bytes[ticker_start:ticker_end]
        ticker = ticker_bytes.decode('utf-8')
        body_start = ticker_end
        body_format = '>i i i q i'
        stock_amount, transaction_type, lockin_price, transaction_id, completion_status = struct.unpack(body_format, data_bytes[body_start:])
        return TransactionDetails(client_id, ticker, stock_amount, TransactionType(transaction_type),
                                  lockin_price, transaction_id)

    def __str__(self):
        return (f"TransactionDetails(Client ID: {self.client_id}, Stock ID: {self.ticker}, "
                f"Stock Amount: {self.stock_amount}, Transaction Type: {self.transaction_type.name}, "
                f"Lock-in Price: ${self.lockin_price:.2f}, Transaction ID: {self.transaction_id}, "
                f"Completion Status: {self.completion_status.name})")


    def __len__(self):
        # Calculate the length of the serialized data
        return len(self.serialize())

"""
# Example Usage
transaction = TransactionDetails(123, "AAPL", 50, TransactionType.BUY, 100, 7890, CompletionType.UNFINISHED)
serialized_data = transaction.serialize()
deserialized_transaction = TransactionDetails.deserialize(serialized_data)
"""