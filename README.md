# Grid07 Backend Assignment

## Tech Stack
- Java 17+, Spring Boot 3.x
- PostgreSQL (source of truth)
- Redis (gatekeeper)
- Docker Compose

## Setup

```bash
docker-compose up -d
./mvnw spring-boot:run
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/posts | Create a post |
| POST | /api/posts/{id}/like | Like a post |
| POST | /api/posts/{id}/comments | Add a comment |

## How Thread Safety is Guaranteed (Phase 2)

### Horizontal Cap (100 bot replies per post)
Redis `INCR` command is atomic by nature — it is a single-threaded operation at the Redis level. When 200 concurrent bot requests hit the API simultaneously, each thread calls `redisTemplate.opsForValue().increment("post:{id}:bot_count")`. Redis processes these INCR commands one at a time in its single-threaded event loop. The returned value is the post-increment count, so whichever thread gets back a value > 100 is guaranteed to be rejected. No two threads can read-then-write simultaneously — the increment and read happen as one atomic unit. This guarantees exactly 100 bot replies, never 101.

### Cooldown Cap (10-minute bot-human cooldown)
Redis `SET key value EX ttl NX` (set if not exists) is an atomic operation. The check and set happen together — there is no window between checking if a key exists and setting it where another thread can slip in. Spring's `redisTemplate.opsForValue().set(key, value, duration)` combined with `hasKey()` ensures the cooldown key is set with a TTL of 10 minutes atomically.

### Vertical Cap (depth > 20)
Checked in application logic before any Redis or DB operation. Since depth_level is provided in the request and validated before touching Redis or PostgreSQL, no concurrency issue exists here.

### Statelessness
All counters, cooldowns, and pending notifications are stored exclusively in Redis — no Java HashMaps, static variables, or in-memory state. The Spring Boot application can be horizontally scaled and all instances will share the same Redis state.

### Data Integrity
Redis guardrails run before any PostgreSQL write. If Redis rejects a request (429), the database transaction never opens. This ensures the DB only contains data that passed all guardrails.

## Notification Engine (Phase 3)
- Bot interaction → check 15-min user notification cooldown in Redis
- If cooldown active → push to Redis List (`user:{id}:pending_notifs`)
- If no cooldown → log direct push notification, set 15-min cooldown
- `@Scheduled` CRON runs every 5 minutes → sweeps all pending notifications → logs summarized message → clears Redis list