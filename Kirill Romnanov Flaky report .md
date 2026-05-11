# Flaky Test Report

**Name:** Lastname, Firstname

## Flaky Test 1

**Test name:** de.seuhd.worldcup.FileBettingServiceTest#fresh service has no bets

**Root cause:**
The test uses a shared file (SHARED_BET_FILE) that persists across test runs. Due to the random test execution order (@TestMethodOrder(MethodOrderer.Random::class)), the test "save bets to the shared file" may run before this test, leaving bets in the file. When this test creates a new FileBettingService instance, it reads the existing file, finding bets, and thus fails the assertion that getBets().size == 0.

**Fix:**
Added @BeforeEach annotation to a method that deletes the SHARED_BET_FILE before each test. This isolates the test state by ensuring each test starts with a clean file, eliminating the dependency on execution order.

## Flaky Test 2

**Test name:** de.seuhd.worldcup.WorldCupTest#standings are stable when multiple teams tie on all criteria

**Root cause:**
The StandingsService.calculate method uses IdentityHashMap to accumulate team statistics, and then sorts the results. IdentityHashMap does not guarantee iteration order, so the order of teams fed into the sorting function can vary between runs. When teams have identical points, goal difference, and goals for, the stable sort preserves the input order, leading to non-deterministic output order.

**Fix:**
Modified the sorting comparator in StandingsService to include .thenBy { it.team.id }, providing a deterministic tie-breaker based on team ID. This ensures consistent ordering even when all primary criteria are equal.

## Flaky Test 3

**Test name:** de.seuhd.worldcup.FileBettingServiceTest#test file betting with threads

**Root cause:**
The FileBettingService.placeBet method reads the entire file, modifies the in-memory map, and writes back the entire file. Without synchronization, concurrent calls from multiple threads can interleave reads and writes, causing bets to be overwritten or lost.

**Fix:**
Made the placeBet and getBets methods synchronized on the FileBettingService instance. This ensures thread-safe access to the file-backed storage, preventing race conditions.

## Flaky Test 4

**Test name:** de.seuhd.worldcup.WorldCupTest#evaluate returns zero when no bets are placed

**Root cause:**
The BettingService caches the evaluation result to avoid recomputation. The clear() method resets the bets map but does not invalidate the cachedResult. If a previous test cached a result, and clear() is called, the cache remains, causing evaluate() to return the stale cached result instead of recalculating with the empty bets.

**Fix:**
Modified the clear() method to set cachedResult = null, ensuring that the cache is invalidated whenever bets are cleared, forcing reevaluation.

## Flaky Test 5

**Test name:** de.seuhd.worldcup.WorldCupTest#load json from network

**Root cause:**
The test has a @Timeout of 300 milliseconds, but the loadJsonFromNetwork method makes HTTP requests with connect and read timeouts of 3-5 seconds. In environments with slow network or high latency, the test may timeout before the network call completes, causing intermittent failures.

**Fix:**
Increased the @Timeout annotation to 10000 milliseconds, allowing sufficient time for the network operation to complete reliably.
