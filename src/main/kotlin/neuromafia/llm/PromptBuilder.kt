package neuromafia.llm

import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class PromptBuilder {
    fun buildDaySpeechRequest(
        state: GameState,
        playerId: Int,
        language: LlmLanguage
    ): LlmRequest {
        val player = state.playerById(playerId)

        return LlmRequest(
            systemPrompt = baseSystemPrompt(language),
            userPrompt = """
                ${language.instruction()}
                
                ${jsonLanguageRule(language)}

                You are Player ${player.id}.
                Your role is ${player.role}.
                Current day: ${state.dayNumber}.
                Current phase: ${state.phase}.

                Alive players:
                ${alivePlayersText(state)}

                Already nominated players:
                ${state.nominatedPlayerIds.ifEmpty { listOf("none") }}

                Task:
                Make a short day speech and optionally nominate one alive player.

                Rules:
                - Do not reveal hidden information unless your role really knows it.
                - Return ONLY valid JSON.
                - publicReasoning must be short and visible to observers.
                - speech must be what the player says publicly.
                - targetId can be null if you do not nominate anyone.

                JSON format:
                {
                  "publicReasoning": "short visible reasoning",
                  "speech": "public speech",
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent(),
            language = language
        )
    }

    fun buildDayVoteRequest(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>,
        language: LlmLanguage
    ): LlmRequest {
        val player = state.playerById(playerId)

        return LlmRequest(
            systemPrompt = baseSystemPrompt(language),
            userPrompt = """
                ${language.instruction()}
                
                ${jsonLanguageRule(language)}

                You are Player ${player.id}.
                Your role is ${player.role}.
                Current day: ${state.dayNumber}.
                Current phase: ${state.phase}.

                Alive players:
                ${alivePlayersText(state)}

                You can vote only for nominated players:
                $nominatedPlayerIds

                Task:
                Choose one nominated player to vote for or skip.

                Return ONLY valid JSON.

                JSON format:
                {
                  "publicReasoning": "short visible reason for vote",
                  "speech": null,
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent(),
            language = language
        )
    }

    fun buildTargetChoiceRequest(
        state: GameState,
        playerId: Int,
        task: String,
        allowedTargetIds: List<Int>,
        language: LlmLanguage
    ): LlmRequest {
        val player = state.playerById(playerId)

        return LlmRequest(
            systemPrompt = baseSystemPrompt(language),
            userPrompt = """
                ${language.instruction()}
                
                ${jsonLanguageRule(language)}

                You are Player ${player.id}.
                Your role is ${player.role}.
                Current day: ${state.dayNumber}.
                Current phase: ${state.phase}.

                Alive players:
                ${alivePlayersText(state)}

                Allowed target ids:
                $allowedTargetIds

                Task:
                $task

                Return ONLY valid JSON.

                JSON format:
                {
                  "publicReasoning": "short visible reasoning",
                  "speech": null,
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent(),
            language = language
        )
    }

    private fun baseSystemPrompt(language: LlmLanguage): String {
        return when (language) {
            LlmLanguage.EN -> """
                You are an LLM player in a Mafia game simulator.
                You must play according to your role and available information.
                Do not output private hidden chain-of-thought.
                Output only concise public reasoning and the requested JSON action.
            """.trimIndent()

            LlmLanguage.RU -> """
                Ты LLM-игрок в симуляторе игры Мафия.
                Играй согласно своей роли и доступной информации.
                Не выводи скрытую цепочку рассуждений.
                Выводи только короткое публичное объяснение и JSON с действием.
            """.trimIndent()
        }
    }

    private fun alivePlayersText(state: GameState): String {
        return state.alivePlayers()
            .joinToString(separator = "\n") { player ->
                publicPlayerLine(player)
            }
    }

    private fun publicPlayerLine(player: Player): String {
        return "Player ${player.id}: alive=${player.alive}"
    }

    private fun jsonLanguageRule(language: LlmLanguage): String {
        return when (language) {
            LlmLanguage.EN ->
                "Important: publicReasoning and speech must be in English."

            LlmLanguage.RU ->
                "Важно: publicReasoning и speech должны быть на русском языке. Не пиши речь игрока на английском."
        }
    }
}