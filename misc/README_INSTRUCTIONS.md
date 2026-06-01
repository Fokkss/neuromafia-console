# neuromafia — README & Instructions

Игра Мафия, поддерживает английскую и русскую локализации, имеет два игровых режима:
- OBSERVE (наблюдаем)
- HUMAN (играем против ботов/заглушки/LLM)
---

## REMARK (Demo video)

Ссылка на видео-пример использования: 

https://youtu.be/UAprPw2CHCg

## 1. Main features

* CLI app с цветными логами
* Несколько режимов игры
* Локализация
* Возможность смотреть DEV логи
* Отладка на "stub"-ботах-заглушках
* Возможность выбрать несколбко ролей кроме привычных, отключить коммисара (шерифа) и др.
* Получаем ответы от LLM (provider – OpenRouter) с помощью ktor запросами по API
* Есть возможность использования в контейнерах с Docker
* Написанные тесты, mock и обычные, для проверки (минимальной) работы блоков логики

---

## 2. Supported roles

| Role        | Meaning                               |
| ----------- |---------------------------------------|
| `CIVILIAN`  | Civilian / мирный житель              |
| `MAFIA`     | Mafia                                 |
| `GODFATHER` | Mafia don / дон мафии / Крестный отец |
| `COMMISSAR` | Sheriff / commissar / коммиссар       |
| `DOCTOR`    | Doctor / доктор                       |
| `ESCORT`    | Prostitute / escort / проститутка     |
| `MANIAC`    | Maniac  / маньяк                      |

### 2.1 Rules

За основу взяты основные правила с ТЗ, а также добавлены новые роли:

Дон Мафии (GODFATHER) –– Решает, кого убивают мафия после выставления кандидатов ими, а также имеет отдельную фазу, в которой проверяет людей на коммиссарство

Маньяк (MANIAC) –– Дополнительно убивает и имеет отдельную фазу ночи. Играет со стороны мирных

Проститутка (ESCORT) –– Человек, с которым она спит, лишается права голоса и речи на следующий день. Если ее убили, то умирает и человек, с которым она спала. Впрочем, если убили только его, то сама она не умрет (успела сбежать)

Доктор (DOCTOR) –– В ночь выбирает, кого лечит, защищает от убийства и спасает даже если к игроку "приклеилась" проститутка, а ее убили (ESCORT_LINK)

---

## 3. Game modes

Их 2:

### 3.1. Observe mode

В нем игрок наблюдает за игрой

```bash
--mode observe
```

Примеры:

```bash
./gradlew run --args="--run-game --mode observe --bot random --players 10 --mafia 3 --max-rounds 3 --doctor --escort --maniac --lang ru"
```

```bash
./gradlew run --args="--run-game --mode observe --bot llm --provider openrouter --model openai/gpt-oss-20b --players 6 --mafia 2 --max-rounds 1 --lang ru"
```

### 3.2. Human mode

Один игрок –– вы, настоящий человек, который играет с ботами (указывайте id)

```bash
--mode human --human-player 1
```

Пример с ботами "random", которые принимают решения случайно и равновероятностно:

```bash
./gradlew run --args="--run-game --mode human --human-player 1 --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru"
```

Рекомендуется запускать интерактивно через installDist:
```bash
./gradlew installDist
./build/install/neuromafia/bin/neuromafia --run-game --mode human --human-player 1 --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru
```

На Windows:

```powershell
.\gradlew.bat installDist
.\build\install\neuromafia\bin\neuromafia.bat --run-game --mode human --human-player 1 --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru
```

---

## 4. Bot types

Всего есть три типа ботов:

### 4.1. `random`

```bash
--bot random
```

`random` использует `RandomPlayerController`.

Режим не предусматривает использование API ключей, боты принимают решения рандомно

### 4.2. `llm`

```bash
--bot llm --provider openrouter --model openai/gpt-oss-20b
```

`llm` использует `LlmPlayerController` и своего провайдера

Режим требует `OPENROUTER_API_KEY`, также, к сожалению, реализованна только поддержка OpenRouter как провайдера, иначе работает fallback

### 4.3. `stub`

```bash
--bot stub
```

`stub` использует `StubLlmProvider`.

Заменяет llm, также общаясь JSON запросами, но при этом заранее прописанными разработчиком. Было полезно для отладки

---

## 5. Language support

Поддержка русского и английского:

```bash
--lang en
```

```bash
--lang ru
```

---

## 6. Requirements

### Required

* JDK 25
* Gradle Wrapper

Один из форматов использования:

```bash
./gradlew
```

Windows:

```powershell
.\gradlew.bat
```

### Optional

* Docker.

---

## 7. Build and test

### macOS / Linux

```bash
./gradlew clean test
./gradlew build
```

### Windows PowerShell

```powershell
.\gradlew.bat clean test
.\gradlew.bat build
```

---

## 8. Show CLI help

### macOS / Linux

```bash
./gradlew run --args="--help"
```

### Windows PowerShell

```powershell
.\gradlew.bat run --args="--help"
```

---

## 9. Basic run examples

### Создать игру без полной симуляции

```bash
./gradlew run --args="--lang ru --debug --players 10 --mafia 3 --doctor --escort --maniac"
```

### OBSERVE с random

```bash
./gradlew run --args="--run-game --mode observe --bot random --players 10 --mafia 3 --max-rounds 3 --doctor --escort --maniac --lang ru"
```

### Игра с ботами + random

```bash
./gradlew run --args="--run-game --mode human --human-player 1 --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru"
```

Еще более удобный интерактивный вариант с installDIst:

```bash
./gradlew installDist
./build/install/neuromafia/bin/neuromafia --run-game --mode human --human-player 1 --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru
```

---

## 10. OpenRouter API key

Для LLM нужен API ключ как переменная окружения, вот как его получить:

На сайте OpenROuter генерируем ключ и вставляем его следующей командой в проект:

```text
OPENROUTER_API_KEY
```

### macOS / Linux

```bash
export OPENROUTER_API_KEY="sk-or-your-key"
```

### Windows PowerShell

```powershell
$env:OPENROUTER_API_KEY="sk-or-your-key"
```

---

## 11. Run LLM mode

### Небольшой smoke-test:

```bash
./gradlew run --args="--run-game --mode observe --bot llm --provider openrouter --model openai/gpt-oss-20b --players 6 --mafia 2 --max-rounds 1 --lang ru"
```

### Полная ролевая игра (тест на 1 оборот, обороты указываем через флаг --max-rounds):

```bash
./gradlew run --args="--run-game --mode observe --bot llm --provider openrouter --model openai/gpt-oss-20b --players 10 --mafia 3 --max-rounds 1 --doctor --escort --maniac --lang ru"
```

### HUMAN + LLM:

```bash
./gradlew run --args="--run-game --mode human --human-player 1 --bot llm --provider openrouter --model openai/gpt-oss-20b --players 6 --mafia 2 --lang ru"
```

Рекомендуется также использование через установленный дистрибутив:

```bash
./gradlew installDist
./build/install/neuromafia/bin/neuromafia --run-game --mode human --human-player 1 --bot llm --provider openrouter --model openai/gpt-oss-20b --players 6 --mafia 2 --max-rounds 1 --lang ru
```

## 11.1 Important note

Если не указывать обороты и пытаться играть полноценную игру, то приложение может упасть из-за задержки запросов.

В целях безопасности была реализована заглушка для подобных случаев, но, коненчо, будет неприятно, если под конец боты будут отвечать только ими)

---

## 12. Docker

Реализована поддержка контейнеризации и исполнения в Docker

### Собрать образ

```bash
docker build -t neuromafia:local .
```

### Запуск help в контейнере

```bash
docker run --rm neuromafia:local --help
```

### Запуск игры с random

```bash
docker run --rm neuromafia:local \
  --run-game \
  --mode observe \
  --bot random \
  --players 10 \
  --mafia 3 \
  --max-rounds 3 \
  --doctor \
  --escort \
  --maniac \
  --lang ru
```

## 12.1 Important note

На Windows символ `\`  заменяем на `

### LLM

```bash
docker run --rm \
  -e OPENROUTER_API_KEY="$OPENROUTER_API_KEY" \
  neuromafia:local \
  --run-game \
  --mode observe \
  --bot llm \
  --provider openrouter \
  --model openai/gpt-oss-20b \
  --players 6 \
  --mafia 2 \
  --max-rounds 1 \
  --lang ru
```

На Windows:

```powershell
docker run --rm `
  -e OPENROUTER_API_KEY="$env:OPENROUTER_API_KEY" `
  neuromafia:local `
  --run-game `
  --mode observe `
  --bot llm `
  --provider openrouter `
  --model openai/gpt-oss-20b `
  --players 6 `
  --mafia 2 `
  --max-rounds 1 `
  --lang ru
```

---

## 13. Create local ZIP/TAR distribution

### macOS / Linux

```bash
./gradlew clean distZip distTar
```

Сгенерированные фалйы положатся в:

```text
build/distributions/
```

Пример:

```text
build/distributions/neuromafia-1.0.0.zip
build/distributions/neuromafia-1.0.0.tar
```

### Windows

```powershell
.\gradlew.bat clean distZip distTar
```

---

## 14. Test downloaded ZIP locally

### macOS / Linux

Собрать ZIP:

```bash
./gradlew clean distZip
```

Распаковать:

```bash
rm -rf /tmp/neuromafia-test
mkdir -p /tmp/neuromafia-test
unzip build/distributions/neuromafia-*.zip -d /tmp/neuromafia-test
```

help:

```bash
/tmp/neuromafia-test/neuromafia-*/bin/neuromafia --help
```

random:

```bash
/tmp/neuromafia-test/neuromafia-*/bin/neuromafia --run-game --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru
```

### Windows

Собрать ZIP:

```powershell
.\gradlew.bat clean distZip
```

Распаковать:

```powershell
Remove-Item -Recurse -Force .\tmp-neuromafia-test -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path .\tmp-neuromafia-test
Expand-Archive .\build\distributions\neuromafia-*.zip -DestinationPath .\tmp-neuromafia-test
```

help:

```powershell
.\tmp-neuromafia-test\neuromafia-1.0.0\bin\neuromafia.bat --help
```

random:

```powershell
.\tmp-neuromafia-test\neuromafia-1.0.0\bin\neuromafia.bat --run-game --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru
```

---

## 15. Troubleshooting

### Docker daemon is not running

Если докер не работает не подключается к DockerAPI, используем Docker Desktop или Colima:

Docker Desktop:

```bash
open -a Docker
```

Colima:

```bash
colima start
```

### OpenRouter API key is missing

Установите:

```bash
export OPENROUTER_API_KEY="sk-or-your-key"
```

Windows:

```powershell
$env:OPENROUTER_API_KEY="sk-or-your-key"
```

### LLM игра медленная

Каждое действие требует вызова по API, поэтому если хочется проверки, сверяемся со след. ключами:

```bash
--players 6 --mafia 2 --max-rounds 1
```

### Если ключа вообще нет, используем random ботов:

```bash
./gradlew run --args="--run-game --bot random --players 6 --mafia 2 --max-rounds 1 --lang ru"
```

---