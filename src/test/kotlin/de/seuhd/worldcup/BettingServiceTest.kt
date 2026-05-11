package de.seuhd.worldcup

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BettingServiceTest {

    private fun match(id: Int, home: String, away: String, hs: Int?, aws: Int?) =
        Match(
            matchId = id,
            round = "Matchday 1",
            date = "2026-06-01",
            homeTeam = home,
            awayTeam = away,
            homeScore = hs,
            awayScore = aws,
            ground = "Test Stadium"
        )

    @BeforeTest
    fun resetBets() {
        BettingService.clear()
    }

    // ── evaluateBonus ──────────────────────────────────────────────────────────

    @Test
    fun `evaluateBonus awards 3 points for an exact score prediction`() {
        val bet = Bet(1, Prediction.HOME_WIN, 2, 1)
        BettingService.placeBet(bet)
        val matches = listOf(match(1, "A", "B", 2, 1))
        val bonus = BettingService.evaluateBonus(matches)
        assertEquals(3, bonus)
    }

    @Test
    fun `evaluateBonus awards 1 point for correct outcome without exact score`() {
        val bet = Bet(1, Prediction.HOME_WIN)
        BettingService.placeBet(bet)
        val matches = listOf(match(1, "A", "B", 2, 1))
        val bonus = BettingService.evaluateBonus(matches)
        assertEquals(1, bonus)
    }

    @Test
    fun `evaluateBonus awards 0 points for a wrong prediction`() {
        val bet = Bet(1, Prediction.AWAY_WIN)
        BettingService.placeBet(bet)
        val matches = listOf(match(1, "A", "B", 2, 1))
        val bonus = BettingService.evaluateBonus(matches)
        assertEquals(0, bonus)
    }

    @Test
    fun `evaluateBonus ignores unplayed matches`() {
        val bet = Bet(1, Prediction.HOME_WIN, 2, 1)
        BettingService.placeBet(bet)
        val matches = listOf(match(1, "A", "B", null, null))
        val bonus = BettingService.evaluateBonus(matches)
        assertEquals(0, bonus)
    }

    // ── removeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `removeBet removes an existing bet so it no longer affects evaluation`() {
        val bet = Bet(1, Prediction.HOME_WIN)
        BettingService.placeBet(bet)
        val matches = listOf(match(1, "A", "B", 2, 1))
        val resultBefore = BettingService.evaluate(matches)
        assertEquals(1, resultBefore.correct)
        BettingService.removeBet(1)
        val resultAfter = BettingService.evaluate(matches)
        assertEquals(0, resultAfter.correct)
    }

    @Test
    fun `removeBet does nothing when no bet exists for that matchId`() {
        // Just call it, should not throw
        BettingService.removeBet(999)
    }

    // ── changeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `changeBet updates the prediction for an existing bet`() {
        val bet1 = Bet(1, Prediction.HOME_WIN)
        BettingService.placeBet(bet1)
        val bet2 = Bet(1, Prediction.AWAY_WIN)
        BettingService.changeBet(bet2)
        val matches = listOf(match(1, "A", "B", 0, 2))
        val result = BettingService.evaluate(matches)
        assertEquals(1, result.correct)
    }

    @Test
    fun `changeBet throws when no bet exists for that matchId`() {
        val bet = Bet(1, Prediction.HOME_WIN)
        assertFailsWith<IllegalArgumentException> {
            BettingService.changeBet(bet)
        }
    }
}