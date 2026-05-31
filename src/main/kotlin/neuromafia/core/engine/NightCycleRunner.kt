package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class NightCycleRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runNight(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run night after game is finished."
        }

        require(state.phase == Phase.NIGHT_MAFIA) {
            "Night cycle must start from NIGHT_MAFIA."
        }

        DevLog.info("Night cycle started")

        var currentState = state

        currentState = NightMafiaRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runMafiaKillVoting(currentState)

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        currentState = GodfatherRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runGodfatherNight(currentState)

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        currentState = DoctorRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runDoctorNight(currentState)

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        currentState = CommissarRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runCommissarNight(currentState)

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        currentState = EscortRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runEscortNight(currentState)

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        currentState = ManiacRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runManiacNight(currentState)

        currentState = NightResultsRunner.resolveNightKills(currentState)

        if (currentState.finished) {
            DevLog.info("Night cycle finished the game")
            return currentState
        }

        currentState = PhaseManager.finishCurrentNightPhase(currentState)

        DevLog.info("Night cycle finished")

        return currentState
    }
}