package com.nahwasa.study.howtosolveconcurrencyissues.transaction;

import com.nahwasa.study.howtosolveconcurrencyissues.service.StockService;

public class TransactionStockService {

    private StockService stockService;

    public TransactionStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        startTransaction();

        stockService.decrease(id, quantity);

        endTransaction();
    }

    private void startTransaction() {
    }

    private void endTransaction() {
    }
}
