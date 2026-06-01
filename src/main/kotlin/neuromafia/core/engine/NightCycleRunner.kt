package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class NightCycleRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run night after game is finished."
        }

        require(state.phase == Phase.NIGHT_MAFIA) {
            "night cycle must start from NIGHT_MAFIA."
        }

        DevLog.info("night cycle started")

        var currentState = state

        currentState = runStep(currentState) {
            NightMafiaRunner(controllersByPlayerId).runMafiaKillVoting(it)
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        currentState = runStep(currentState) {
            GodfatherRunner(controllersByPlayerId).runGodfatherNight(it)
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        currentState = runStep(currentState) {
            DoctorRunner(controllersByPlayerId).runDoctorNight(it)
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        currentState = runStep(currentState) {
            CommissarRunner(controllersByPlayerId).runCommissarNight(it)
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        currentState = runStep(currentState) {
            EscortRunner(controllersByPlayerId).runEscortNight(it)
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        currentState = runStep(currentState) {
            ManiacRunner(controllersByPlayerId).runManiacNight(it)
        }

        currentState = runStep(currentState) {
            NightResultsRunner.resolveNightKills(it)
        }

        if (currentState.finished) {
            DevLog.info("night cycle finished the game")
            return currentState
        }

        currentState = runStep(currentState) {
            PhaseManager.finishCurrentNightPhase(it)
        }

        DevLog.info("night cycle finished")

        return currentState
    }

    private fun runStep(
        state: GameState,
        step: (GameState) -> GameState
    ): GameState {
        val previousState = state
        val currentState = step(state)

        onStateChanged(previousState, currentState)

        return currentState
    }
}