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

* Lettus 활용 연습
  * redis-cli에서 실습 (MYSQL의 Named Lock과 거의 비슷 - 레디스를 활용한다는 점과 세션관리에 신경을 안써도 된다는 차이점이 있음.)
    * ```docker ps``` 후 컨테이너 id 복사
    * ```docker exec -it {container id} redis-cli```
    * ```setnx 1 lock``` 명령어를 통해 락을 획득
    * ```setnx 1 lock``` 이번엔 실패
    * ```del 1``` 명령어를 통해 락 해제
    * ```setnx 1 lock``` 성공
    * ```del 1``` 다시 해제
    * 