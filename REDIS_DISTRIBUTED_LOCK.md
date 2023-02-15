* 레디스 분산락을 통한 여러 서버에서의 동시성 해결!

* Lettuce
  * setnx 명령어를 활용하여 분산락 구현
  * spin lock 방식 
  * (락을 획득하려는 스레드가 락을 사용할 수 있는지 반복적으로 확인하면서 락획득. 개발자가 직접 구현해줘야함.)
* Redisson
  * pub-sub 기반으로 Lock 구현 제공 
  * (채널을 하나 만들어서 락을 점유중인 스레드가 락 획득하려고 대기중에 스레드에게 해제를 알려주면 안내를 받은 스레드가 락 획득을 시도하는 방식. 별도의 리트라이 로직 작성X)

* 환경 세팅
```
docker pull redis
docker run --name myredis -d -p 6379:6379 redis
```

* Lettuce 활용 연습
  * redis-cli에서 실습 (MYSQL의 Named Lock과 거의 비슷 - 레디스를 활용한다는 점과 세션관리에 신경을 안써도 된다는 차이점이 있음.)
    * ```docker ps``` 후 컨테이너 id 복사
    * ```docker exec -it {container id} redis-cli```
    * ```setnx 1 lock``` 명령어를 통해 락을 획득
    * ```setnx 1 lock``` 이번엔 실패
    * ```del 1``` 명령어를 통해 락 해제
    * ```setnx 1 lock``` 성공
    * ```del 1``` 다시 해제

* Redisson 활용 연습
  * 위와 마찬가지로 redis-cli에서 실습. A와 B 두 터미널에 redis-cli 실행
    * A 터미널에서 ```subscribe ch1``` 명령어를 통해 채널 구독
    * B 터미널에서 ```publish ch1 hello``` 명령어를 통해 메시지 전송
    * A 터미널 보면 '3) "hello"' 라고 메시지 뜸.
  * 위처럼 메시지 보내서 락 해제를 하라고 메시지를 보내므로 위 스핀락보다 락 획득 시도가 적어서 레디스 부하를 줄일 수 있음.

* Lettuce vs Redisson
  * Lettuce
    * 구현이 간단하다.
    * spring data redis를 이용하면 lettuce가 기본이기 때문에 별도의 라이브러리를 사용하지 않아도 된다.
    * spin lock 방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수 있다.
  * Redisson
    * 락 획득 재시도를 기본으로 제공한다.
    * pub-sub 방식으로 구현이 되어있기 때문에 lettuce와 비교했을 때 redis에 부하가 덜 간다.
    * 별도의 라이브러리를 사용해야 한다. (redisson-spring-boot-starter)
    * lock을 라이브러리 차원에서 제공해주기 때문에 라이브러리 사용법을 공부해야 한다.
  * 실무에서
    * 재시도가 필요하지 않은 lock은 lettuce
    * 재시도가 필요한 경우에는 redisson
    * 즉 둘을 혼용!

* Mysql DB Lock vs Redis Lock
  * Mysql
    * 이미 Mysql을 사용하고 있다면 별도의 비용없이 사용가능
    * 어느정도의 트래픽까지는 문제없이 활용 가능
    * Redis 보다는 성능이 좋지 않다.
  * Redis
    * 활용중인 Redis가 없다면 별도의 구축비용과 인프라 관리비용이 발생한다.
    * Mysql 보다 성능이 좋다.
  * 실무에서
    * 비용적 여유가 없다면 Mysql로 충분히 해결가능하면 Mysql
    * 비용적 있거나 Mysql로 처리 불가능할 정도의 트래픽이라면 Redis

* spin lock vs mutex
  * spin lock
    * 자원에 락이 걸려 있는 경우 얻을 때 까지 무한 루프(sleep 하긴 하지만 아무튼) 돌면서 다른 테스크에 CPU를 양보하지 않음.
    * 자원을 단시간 내에 얻을 수 있다면 컨텍스트 스위칭 비용이 들지 않으므로 효율이 좋지만
    * 그렇지 않을 경우 오히려 CPU 효율이 떨어짐.
  * mutex
    * 자원에 락이 걸려 있는 경우 얻을 때 까지 기다리며 컨텍스트 스위칭을 실행함.
    * 즉, CPU 양보 가능. 자원을 얻기까지 오래걸린다면 다른 작업할 수 있으니 좋음.
    * 자원이 단시간에 얻을 수 있게 될 경우엔 컨텍스트 스위칭 비용이 낭비됨.

