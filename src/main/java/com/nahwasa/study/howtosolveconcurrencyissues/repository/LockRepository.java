package com.nahwasa.study.howtosolveconcurrencyissues.repository;

import com.nahwasa.study.howtosolveconcurrencyissues.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Named Lock 사용 시 실무에서 사용할 때엔 datasource를 분리해 사용하는게 좋음.
 * - 동일한걸 사용하면 커넥션풀이 부족해질 수 있음.
 */
public interface LockRepository extends JpaRepository<Stock, Long> {

    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);
}
