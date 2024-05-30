package edu.yu.capstone.T2.mongo_node;

public enum MongoDBSchema {
    CLIENT_PORTFOLIO("clientPortfolio"),
    TRANSACTIONS("transactions"),
    STOCK_INVENTORY("stockInventory"),
    STAGE_CLIENT_PORTFOLIO("stageClientPortfolio"),
    STAGE_STOCK_INVENTORY("stageStockInventory");

    private final String tableName;

    MongoDBSchema(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    // Add more key names as needed for each collection
    public enum KeyNames {
        CLIENT_ID("clientID"),
        TICKER("ticker"),
        TIMESTAMP("timestamp"),
        LAST_UPDATED("lastUpdated"),
        TXN_ID("txnID"),
        QUANTITY("quantity"),
        BUY_SELL("buySell"),
        BUY("BUY"),
        SELL("SELL"),
        CASH("CASH"),
        PRICE("price");

        private final String keyName;

        KeyNames(String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
    }
}

