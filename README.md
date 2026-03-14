# WebFlux Log Stream Processing Lab

## 프로젝트 목적

본 프로젝트는 **Spring MVC와 Spring WebFlux의 처리 구조를 이해하고**,  
**로그 스트림을 처리하는 서버 구조를 실험하기 위해 진행한 학습용 프로젝트**입니다.

특히 다음 내용을 **직접 구현과 실험을 통해 확인하는 것**을 목표로 했습니다.

- **Spring Boot 애플리케이션 실행 구조**
- **IoC / DI 기반 Bean 관리**
- **Thread-per-request 모델 (Spring MVC)**
- **Event Loop 기반 Non-blocking 처리 (WebFlux)**
- **Reactive Stream 처리 구조**
- **로그 스트림 기반 실시간 분석 파이프라인**

본 프로젝트는 **실서비스 구현 이전 단계의 학습 및 실험 목적 프로젝트**입니다.

---

# 1. Spring MVC 처리 구조 실험

## 목표

**Spring MVC가 요청을 처리하는 내부 구조**를 직접 확인한다.

## 실험 내용

- `Controller / Service` 구조 구현
- **생성자 기반 DI**
- **Bean 생성 시점 로그 출력**
- **객체 인스턴스 식별값 확인**
- **요청 처리 스레드 확인**

## 확인한 내용

### Bean 생성

```text
>>> TestService constructor called
>>> TestController constructor called
```

Spring Boot 실행 시 **Bean이 생성되고 DI가 수행됨**을 확인하였다.

---

### Singleton Scope

여러 요청에서도 **동일 인스턴스가 사용되는 것**을 확인하였다.

```text
Controller instance = TestController@75beef1b
Service instance = TestService@506a1372
```

즉 `Spring Bean`은 기본적으로 **Singleton Scope**로 동작한다.

---

### Thread-per-request 모델

요청마다 **서로 다른 스레드가 할당되는 것**을 확인하였다.

```text
http-nio-8080-exec-1
http-nio-8080-exec-2
http-nio-8080-exec-6
```

즉 Spring MVC는 다음 구조로 동작한다.

```
요청 1개 → 스레드 1개
```

---

# 2. Spring MVC vs WebFlux 성능 비교 실험

## 실험 환경

- **AWS EC2** `t3.micro`
- 부하 테스트 도구 **k6**

---

## Spring MVC 테스트

### 조건

- **100 VUs**
- **20 seconds**

### 결과

```text
http_req_duration avg ≈ 125ms
throughput ≈ 792 req/s
```

`Thread blocking (sleep)`을 추가했을 때 **처리량이 크게 감소하는 것**을 확인하였다.

---

## Spring WebFlux 테스트

동일 조건에서 **WebFlux 서버 테스트** 진행.

### 결과

```text
http_req_duration avg ≈ 30ms
throughput ≈ 3282 req/s
```

---

## 확인한 차이

### Spring MVC

- **Thread-per-request**
- **Blocking I/O**

### Spring WebFlux

- **Event Loop**
- **Non-blocking I/O**

대량 동시 요청 환경에서 **WebFlux가 높은 처리량을 유지하는 것**을 확인하였다.

---

# 3. 로그 수집 서버 구현

로그 수집 API 구현

```
POST /logs
```

### DTO

`LogRequest`

```text
host
level
message
```

로그는 내부 **메모리 버퍼**에 저장된다.

```java
ConcurrentLinkedQueue<LogRequest>
```

---

### 최근 로그 조회 API

```
GET /logs/recent
```

---

# 4. 로그 분석 기능 구현

로그 분석 기능을 **별도 컴포넌트로 분리**하였다.

```
LogAnalyzer
```

### 분석 기능

- Host별 로그 수
- Level별 로그 수
- Failed Login 탐지
- 경보 로그 탐지

### 제공 API

```
GET /logs/stats
GET /logs/alerts
GET /logs/hosts
GET /logs/levels
```

---

# 5. Reactive Stream 도입

로그 수집 시 **Reactive Stream으로 발행하도록 구현**하였다.

```java
Sinks.Many<LogRequest>
```

### 처리 구조

```
POST /logs
   ↓
Sink emit
   ↓
Flux<LogRequest>
```

즉 다음 구조가 된다.

```
로그 입력 → 스트림 발행
```

---

# 6. 실시간 로그 처리

Reactive Subscriber를 추가하여 **로그 입력 시 즉시 처리되는 구조**를 확인하였다.

```java
getLogStream()
    .subscribe(...)
```

출력 예시

```text
[STREAM] host=server1 level=WARN message=failed login
```

---

# 7. 실시간 경보 스트림

다음 조건에 해당하는 로그를 **경보 로그로 필터링**하였다.

- `WARN`
- `ERROR`
- `failed login`
- `suspicious`
- `access denied`

Reactive Stream Filter 적용

```
Flux
 → filter
 → subscribe
```

출력 예시

```text
[ALERT-STREAM]
```

---

# 8. Window 기반 로그 분석

Reactive Window 연산 적용

```java
bufferTimeout(100, Duration.ofSeconds(5))
```

즉 다음 구조로 동작한다.

```
로그 스트림
 → 5초 단위 batch
 → 집계
```

결과

```text
[ALERT-WINDOW] total=7 error=3
```

---

# 9. Dashboard 상태 관리

Reactive 집계 결과를 **메모리에 저장**한다.

```
AlertSummaryResponse
```

### API 제공

```
GET /logs/alert-summary
```

### 응답 예시

```json
{
  "total": 8,
  "errorCount": 3
}
```

---

# 10. 학습 결과

본 프로젝트를 통해 다음 내용을 **직접 구현 및 실험을 통해 확인하였다.**

### Spring 구조

- **IoC / DI**
- **Bean lifecycle**
- **Singleton Scope**

### 서버 처리 모델

- **Thread-per-request**
- **Event Loop 기반 처리**

### Reactive Stream

- `Flux`
- `Sink`
- `filter`
- `subscribe`
- `window processing`

### 로그 스트림 처리 구조

```
Log Ingestion
      ↓
Stream Processing
      ↓
Aggregation
      ↓
Dashboard API
```

---

# 11. 향후 확장 방향

본 프로젝트는 **로그 분석 서버 구조 학습을 위한 실험 프로젝트**이다.

실서비스 구조에서는 다음과 같은 구조로 확장할 예정이다.

```
Log Ingestion
      ↓
Stream Processor
      ↓
AI Model Inference
      ↓
Detection Result
      ↓
Dashboard State
      ↓
Dashboard API
```

즉 서버는 다음 역할을 수행한다.

- 로그 스트림 수집
- AI 모델 추론 요청
- 추론 결과 대시보드 제공

---

# 프로젝트 성격

**Learning / Experiment Project**

Spring MVC와 Spring WebFlux의 동작 원리 및  
로그 스트림 처리 구조를 이해하기 위한 **학습용 프로젝트**입니다.