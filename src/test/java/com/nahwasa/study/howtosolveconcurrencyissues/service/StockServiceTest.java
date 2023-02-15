package com.nahwasa.study.howtosolveconcurrencyissues.service;

import com.nahwasa.study.howtosolveconcurrencyissues.domain.Stock;
import com.nahwasa.study.howtosolveconcurrencyissues.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockServiceTest {

    @Autowired private StockRepository stockRepository;
    @Autowired private StockService stockService;

    @BeforeEach
    public void before() {
        Stock stock = new Stock(1L, 100L);

        stockRepository.save(stock);
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("quantity는 100에서 1을 감소시키면 99 여야 한다.")
    void stock_decrease() {
        stockService.decrease(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(99L);
    }

    @Test
    @DisplayName("quantity 100에서 동시에 100개의 요청을 해 1씩 감소하면 quantity는 0이어야 한다.")
    void stock_decrease_concurrency() throws InterruptedException {
        /**
         * 1. race condition 발생하므로 테스트는 실패한다.
         * 2. StockService의 decrease에서 synchronized를 넣었는데도 실패한다. (좀 마아지긴 함)
         *   스프링에서는 Transactional을 붙이면 우리가 만든 클래스를 래핑한 클래스를 새로 만들어서 실행한다.
         *   TransactionStockService처럼. 이 때 endTransaction에서 트랜잭션 종료 시점에 DB에 업데이트한다.
         *   이 때, 실제 DB에 업데이트 하기 전에 다른 스레드가 decrease 메소드를 호출할 수 있어 문제가 발생한다.
         *   즉, synchronized 걸린건 decrease인데 TransactionStockService보면 @Transactional 붙은애가 저렇게 동작하므로
         *   decrease와 endTransaction 사이에 다른 스레드가 decrease를 호출할 수 있는 상황이다.
         * 3. 그러므로 @Transactional을 제거하면 테스트는 성공한다.
         */
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}