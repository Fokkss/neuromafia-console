package neuromafia.bot

import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameState
import neuromafia.core.model.Team
import neuromafia.msg.Language

import neuromafia.core.model.ConsoleColors

class HumanPlayerController(
    private val language: Language,
    private val io: HumanIo = ConsoleHumanIo()
) : PlayerController {
    override fun chooseDaySpeech(
        state: GameState,
        playerId: Int
    ): PlayerAction.DaySpeech {
        val player = state.playerById(playerId)

        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "day speech",
            actionNameRu = "дневная речь"
        )

        io.writeLine(message(
            en = "${ConsoleColors.GREEN}Your role: ${player.role} ${ConsoleColors.RESET}",
            ru = "${ConsoleColors.GREEN}Ваша роль: ${player.role} ${ConsoleColors.RESET}"
        ))

        io.writeLine(message(
            en = "${ConsoleColors.CYAN}Enter your public speech:${ConsoleColors.RESET}",
            ru = "${ConsoleColors.CYAN}Введите речь для обсуждения:${ConsoleColors.RESET}"
        ))

        val speech = io.readLine()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: message(
                en = "I am not sure yet.",
                ru = "Пока не уверен, нужно присмотреться."
            )

        val allowedNominationIds = state.alivePlayers()
            .filter { it.id != playerId }
            .filter { it.id !in state.nominatedPlayerIds }
            .map { it.id }

        val nominatedPlayerId = chooseOptionalTarget(
            promptEn = "${ConsoleColors.CYAN}Nominate a player or press Enter to skip.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выставьте игрока или нажмите Enter, чтобы пропустить.${ConsoleColors.RESET}",
            allowedTargetIds = allowedNominationIds
        )

        return PlayerAction.DaySpeech(
            playerId = playerId,
            message = speech,
            nominatedPlayerId = nominatedPlayerId
        )
    }

    override fun chooseDayVote(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>
    ): PlayerAction.DayVote {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "day voting",
            actionNameRu = "дневное голосование"
        )

        val allowedTargetIds = nominatedPlayerIds
            .map { state.playerById(it) }
            .filter { it.alive }
            .map { it.id }

        val targetId = chooseOptionalTarget(
            promptEn = "${ConsoleColors.CYAN}Vote for a nominated player or press Enter to skip.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Проголосуйте за выставленного игрока или нажмите Enter, чтобы пропустить.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.DayVote(
            voterId = playerId,
            targetId = targetId
        )
    }

    override fun chooseMafiaKillVote(
        state: GameState,
        playerId: Int
    ): PlayerAction.MafiaKillVote {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "mafia kill vote",
            actionNameRu = "ночной выбор мафии"
        )

        val allowedTargetIds = state.alivePlayers()
            .filter { it.role.team != Team.MAFIA }
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a non-mafia player to kill.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите не-мафию для ночного убийства.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.MafiaKillVote(
            mafiaId = playerId,
            targetId = targetId
        )
    }

    override fun chooseGodfatherKillDecision(
        state: GameState,
        playerId: Int,
        candidateIds: List<Int>
    ): PlayerAction.GodfatherKillDecision {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "godfather kill decision",
            actionNameRu = "решение дона по убийству"
        )

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose final mafia kill target from tied candidates.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите финальную цель мафии из кандидатов.${ConsoleColors.RESET}",
            allowedTargetIds = candidateIds
        )

        return PlayerAction.GodfatherKillDecision(
            godfatherId = playerId,
            targetId = targetId
        )
    }

    override fun chooseGodfatherCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.GodfatherCommissarCheck {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "godfather commissar check",
            actionNameRu = "проверка дона на комиссара"
        )

        val allowedTargetIds = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a player to check whether they are commissar.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите игрока, чтобы проверить, не коммиссар ли он.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.GodfatherCommissarCheck(
            godfatherId = playerId,
            targetId = targetId
        )
    }

    override fun chooseCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.CommissarCheck {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "commissar check",
            actionNameRu = "проверка комиссара"
        )

        val allowedTargetIds = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a player to check whether they are mafia.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите игрока, чтобы проверить, относится ли он к мафии.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.CommissarCheck(
            commissarId = playerId,
            targetId = targetId
        )
    }

    override fun chooseDoctorHeal(
        state: GameState,
        playerId: Int
    ): PlayerAction.DoctorHeal {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "doctor heal",
            actionNameRu = "лечение доктора"
        )

        val allowedTargetIds = state.alivePlayers()
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a player to protect tonight.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите игрока, которого хотите защитить этой ночью.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.DoctorHeal(
            doctorId = playerId,
            targetId = targetId
        )
    }

    override fun chooseEscortVisit(
        state: GameState,
        playerId: Int
    ): PlayerAction.EscortVisit {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "escort visit",
            actionNameRu = "ночной визит проститутки"
        )

        val allowedTargetIds = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a player to visit tonight.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите игрока, к которому хотите пойти ночью.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.EscortVisit(
            escortId = playerId,
            targetId = targetId
        )
    }

    override fun chooseManiacKill(
        state: GameState,
        playerId: Int
    ): PlayerAction.ManiacKill {
        printHumanHeader(
            state = state,
            playerId = playerId,
            actionNameEn = "maniac kill",
            actionNameRu = "ночное убийство маньяка"
        )

        val allowedTargetIds = state.alivePlayers()
            .filter { it.id != playerId }
            .map { it.id }

        val targetId = chooseRequiredTarget(
            promptEn = "${ConsoleColors.CYAN}Choose a player to kill tonight.${ConsoleColors.RESET}",
            promptRu = "${ConsoleColors.CYAN}Выберите игрока, которого хотите убить ночью.${ConsoleColors.RESET}",
            allowedTargetIds = allowedTargetIds
        )

        return PlayerAction.ManiacKill(
            maniacId = playerId,
            targetId = targetId
        )
    }

    private fun printHumanHeader(
        state: GameState,
        playerId: Int,
        actionNameEn: String,
        actionNameRu: String
    ) {
        io.writeLine("")
        io.writeLine(message(
            en = "Your turn: Player $playerId, $actionNameEn.",
            ru = "Ваш ход: игрок $playerId, $actionNameRu."
        ))
        io.writeLine(message(
            en = "Day: ${state.dayNumber}, phase: ${state.phase}",
            ru = "День: ${state.dayNumber}, фаза: ${state.phase}"
        ))
        io.writeLine(message(
            en = "Alive players: ${state.alivePlayers().map { it.id }}",
            ru = "Живые игроки: ${state.alivePlayers().map { it.id }}"
        ))
    }

    private fun chooseOptionalTarget(
        promptEn: String,
        promptRu: String,
        allowedTargetIds: List<Int>
    ): Int? {
        if (allowedTargetIds.isEmpty()) {
            io.writeLine(message(
                en = "No available targets.",
                ru = "Нет доступных целей."
            ))

            return null
        }

        io.writeLine(message(promptEn, promptRu))
        io.writeLine(message(
            en = "Allowed ids: $allowedTargetIds, empty input means skip.",
            ru = "Доступные id: $allowedTargetIds, пустой ввод значит пропуск."
        ))

        while (true) {
            val rawInput = io.readLine()

            if (rawInput == null) {
                io.writeLine(message(
                    en = "${ConsoleColors.YELLOW}Input stream is closed. Empty input will be used.${ConsoleColors.RESET}",
                    ru = "${ConsoleColors.YELLOW}Поток ввода закрыт. Будет использован пустой ввод.${ConsoleColors.RESET}"
                ))

                return null
            }

            val input = rawInput.trim()

            if (input.isBlank()) {
                return null
            }

            val id = input.toIntOrNull()

            if (id != null && id in allowedTargetIds) {
                return id
            }

            io.writeLine(message(
                en = "${ConsoleColors.RED}Invalid target. Try again.${ConsoleColors.RESET}",
                ru = "${ConsoleColors.RED}Некорректная цель. Попробуйте ещё раз.${ConsoleColors.RESET}"
            ))
        }
    }

    private fun chooseRequiredTarget(
        promptEn: String,
        promptRu: String,
        allowedTargetIds: List<Int>
    ): Int {
        require(allowedTargetIds.isNotEmpty()) {
            "No available targets."
        }

        io.writeLine(message(promptEn, promptRu))
        io.writeLine(message(
            en = "Allowed ids: $allowedTargetIds.",
            ru = "Доступные id: $allowedTargetIds."
        ))

        while (true) {
            val input = io.readLine()
                ?.trim()
                .orEmpty()

            val id = input.toIntOrNull()

            if (id != null && id in allowedTargetIds) {
                return id
            }

            io.writeLine(message(
                en = "${ConsoleColors.RED}Invalid target. Try again.${ConsoleColors.RESET}",
                ru = "${ConsoleColors.RED}Некорректная цель. Попробуйте ещё раз.${ConsoleColors.RESET}"
            ))
        }
    }

    private fun message(
        en: String,
        ru: String
    ): String {
        return when (language) {
            Language.EN -> en
            Language.RU -> ru
        }
    }
}