package com.nahwasa.study.howtosolveconcurrencyissues.facade;

import com.nahwasa.study.howtosolveconcurrencyissues.domain.Stock;
import com.nahwasa.study.howtosolveconcurrencyissues.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Named Lock을 사용한 동시성 테스트에서는")
class NamedLockStockFacadeTest {

    @Autowired private StockRepository stockRepository;
    @Autowired private NamedLockStockFacade namedLockStockFacade;

    /**
     * Named Lock을 통한 해결!
     */
    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    private Long initStockAndGetId() {
        Stock stock = new Stock(1L, 100L);
        return stockRepository.save(stock).getId();
    }

    @Test
    @DisplayName("quantity 100에서 동시에 100개의 요청을 해 1씩 감소하면 quantity는 0이어야 한다.")
    void stock_decrease_concurrency() throws InterruptedException {
        long id = initStockAndGetId();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    namedLockStockFacade.decrease(id, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Stock stock = stockRepository.findById(id).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}