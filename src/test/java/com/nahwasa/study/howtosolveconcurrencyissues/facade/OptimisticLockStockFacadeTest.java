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
@DisplayName("Optimistic Lock을 사용한 동시성 테스트에서는")
class OptimisticLockStockFacadeTest {

    @Autowired private StockRepository stockRepository;
    @Autowired private OptimisticLockStockFacade optimisticLockStockFacade;

    /**
     * Optimistic Lock을 통한 해결!
     * - 장점 : 별도의 락을 잡지 않으므로 성능이 좋음.
     * - 단점 : 실패 시 재시도 로직을 개발자가 직접 작성해야 함 (Facade 만든거처럼) 또한 충돌이 빈번하다면 성능이 떨어질 수 있음.
     *
     * 충돌이 빈번하다면 Pessimistic Lock, 아니라면 Optimistic Lock을 사용하는 것이 좋음.
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
                    optimisticLockStockFacade.decrease(id, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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