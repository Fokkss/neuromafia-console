package neuromafia.bot

import kotlinx.coroutines.runBlocking
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameState
import neuromafia.core.model.Team
import neuromafia.dev.DevLog
import neuromafia.llm.LlmActionParser
import neuromafia.llm.LlmLanguage
import neuromafia.llm.LlmProvider
import neuromafia.llm.PromptBuilder
import kotlin.random.Random

class LlmPlayerController(
    private val provider: LlmProvider,
    private val language: LlmLanguage,
    private val promptBuilder: PromptBuilder = PromptBuilder(),
    private val parser: LlmActionParser = LlmActionParser(),
    private val random: Random = Random.Default
) : PlayerController {
    override fun chooseDaySpeech(
        state: GameState,
        playerId: Int
    ): PlayerAction.DaySpeech {
        val parsed = askAndParse(
            promptBuilder.buildDaySpeechRequest(
                state = state,
                playerId = playerId,
                language = language
            )
        )

        val nominationTargetId = sanitizeDayNominationTarget(
            state = state,
            playerId = playerId,
            targetId = parsed.targetId
        )

        return PlayerAction.DaySpeech(
            playerId = playerId,
            message = parsed.speech ?: parsed.publicReasoning,
            nominatedPlayerId = nominationTargetId
        )
    }

    override fun chooseDayVote(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>
    ): PlayerAction.DayVote {
        val parsed = askAndParse(
            promptBuilder.buildDayVoteRequest(
                state = state,
                playerId = playerId,
                nominatedPlayerIds = nominatedPlayerIds,
                language = language
            )
        )

        val targetId = if (parsed.skip) {
            null
        } else {
            sanitizeDayVoteTarget(
                targetId = parsed.targetId,
                nominatedPlayerIds = nominatedPlayerIds
            )
        }

        return PlayerAction.DayVote(
            voterId = playerId,
            targetId = targetId
        )
    }

    override fun chooseMafiaKillVote(
        state: GameState,
        playerId: Int
    ): PlayerAction.MafiaKillVote {
        val allowedTargets = state.alivePlayers()
            .filter { it.role.team != Team.MAFIA }
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose a non-mafia alive player for mafia night kill.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.MafiaKillVote(
            mafiaId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    override fun chooseGodfatherKillDecision(
        state: GameState,
        playerId: Int,
        candidateIds: List<Int>
    ): PlayerAction.GodfatherKillDecision {
        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose final mafia kill target from tied candidates.",
                allowedTargetIds = candidateIds,
                language = language
            )
        )

        return PlayerAction.GodfatherKillDecision(
            godfatherId = playerId,
            targetId = requireTarget(parsed.targetId, candidateIds)
        )
    }

    override fun chooseGodfatherCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.GodfatherCommissarCheck {
        val allowedTargets = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose one alive player to check whether he is COMMISSAR.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.GodfatherCommissarCheck(
            godfatherId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    override fun chooseCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.CommissarCheck {
        val allowedTargets = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose one alive player to check whether he belongs to mafia team.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.CommissarCheck(
            commissarId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    override fun chooseDoctorHeal(
        state: GameState,
        playerId: Int
    ): PlayerAction.DoctorHeal {
        val allowedTargets = state.alivePlayers()
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose one alive player to protect tonight.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.DoctorHeal(
            doctorId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    override fun chooseEscortVisit(
        state: GameState,
        playerId: Int
    ): PlayerAction.EscortVisit {
        val allowedTargets = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose one alive player to visit tonight. That player will not speak or vote the next day.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.EscortVisit(
            escortId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    override fun chooseManiacKill(
        state: GameState,
        playerId: Int
    ): PlayerAction.ManiacKill {
        val allowedTargets = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val parsed = askAndParse(
            promptBuilder.buildTargetChoiceRequest(
                state = state,
                playerId = playerId,
                task = "Choose one alive player to kill tonight.",
                allowedTargetIds = allowedTargets,
                language = language
            )
        )

        return PlayerAction.ManiacKill(
            maniacId = playerId,
            targetId = requireTarget(parsed.targetId, allowedTargets)
        )
    }

    private fun askAndParse(request: neuromafia.llm.LlmRequest): neuromafia.llm.LlmActionResponse {
        return try {
            DevLog.info("Sending request to LLM")

            val response = runBlocking {
                provider.ask(request)
            }

            DevLog.info("Received response from LLM")

            try {
                parser.parse(response.content)
            } catch (exception: Exception) {
                DevLog.info("Failed to parse LLM response, using fallback. Error: ${exception.message}")

                fallbackActionResponse()
            }
        } catch (exception: Exception) {
            DevLog.info("LLM request failed, using fallback. Error: ${exception.message}")

            fallbackActionResponse()
        }
    }

    private fun sanitizeDayVoteTarget(
        targetId: Int?,
        nominatedPlayerIds: List<Int>
    ): Int? {
        if (targetId == null) {
            return null
        }

        if (targetId !in nominatedPlayerIds) {
            DevLog.info("LLM voted for non-nominated player $targetId, vote skipped")
            return null
        }

        return targetId
    }

    private fun sanitizeDayNominationTarget(
        state: GameState,
        playerId: Int,
        targetId: Int?
    ): Int? {
        if (targetId == null) {
            return null
        }

        val target = state.players.firstOrNull { it.id == targetId }

        if (target == null) {
            DevLog.info("LLM nominated unknown player $targetId, nomination ignored")
            return null
        }

        if (!target.alive) {
            DevLog.info("LLM nominated killed player $targetId, nomination ignored")
            return null
        }

        if (targetId == playerId) {
            DevLog.info("LLM tried to nominate itself, nomination ignored")
            return null
        }

        if (targetId in state.nominatedPlayerIds) {
            DevLog.info("LLM nominated already nominated player $targetId, nomination ignored")
            return null
        }

        return targetId
    }

    private fun fallbackActionResponse(): neuromafia.llm.LlmActionResponse {
        return when (language) {
            LlmLanguage.EN -> neuromafia.llm.LlmActionResponse(
                publicReasoning = "Fallback action because LLM response was invalid.",
                speech = "I am not sure yet.",
                targetId = null,
                skip = true
            )

            LlmLanguage.RU -> neuromafia.llm.LlmActionResponse(
                publicReasoning = "Запасное действие, потому что ответ LLM был некорректным.",
                speech = "Пока не уверен, нужно присмотреться.",
                targetId = null,
                skip = true
            )
        }
    }

    private fun requireTarget(
        targetId: Int?,
        allowedTargetIds: List<Int>
    ): Int {
        require(targetId != null) {
            "LLM did not return targetId."
        }

        if (targetId in allowedTargetIds) {
            return targetId!!
        }

        val fallbackTargetId = allowedTargetIds.random(random)

        require(targetId in allowedTargetIds) {
            "LLM returned targetId $targetId, but allowed targets are $allowedTargetIds."
        }

        return targetId
    }
}