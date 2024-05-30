import threading

class TransactionIDGenerator:
    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        # Ensure that instance creation is thread-safe
        with cls._lock:
            if cls._instance is None:
                cls._instance = super(TransactionIDGenerator, cls).__new__(cls)
                # Initialize the file storage
                cls._instance.file_storage = "transaction_ticker.txt"
                # Initialize the transaction ID from the file or set to 0
                cls._instance.txnID = cls._instance.read_txn_id()
                # Initialize the instance lock
                cls._instance.instance_lock = threading.Lock()
        return cls._instance

    def get_and_increment(self):
        # Ensure thread-safe incrementation of the transaction ID
        with self.instance_lock:
            current_id = self.txnID
            self.txnID += 1
            self.write_txn_id(self.txnID)
            return current_id

    def read_txn_id(self):
        try:
            with open(self.file_storage, 'r') as file:
                return int(file.read().strip())
        except (IOError, ValueError):
            return 0  # Return 0 if the file doesn't exist or the content is not an integer

    def write_txn_id(self, txn_id):
        with open(self.file_storage, 'w') as file:
            file.write(str(txn_id))

    def bootup(self):
        # Method to reset the transaction ID to 0 and write to file
        with self.instance_lock:
            self.txnID = 0
            self.write_txn_id(self.txnID)
