[![Fabric](https://img.shields.io/badge/Loader-Fabric-7BE0C3?style=for-the-badge)](https://fabricmc.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x_%7C_26.x-A9B8FF?style=for-the-badge)](https://minecraft.net/)
[![License](https://img.shields.io/badge/License-GPL--3.0-FFC36B?style=for-the-badge)](LICENSE.txt)

# EN

**AnyBind** is a client-side Fabric mod that lets you create custom keybinds directly inside the vanilla Minecraft controls menu.

Use any keyboard key or mouse button, add Ctrl / Shift / Alt requirements, assign several trigger keys to the same bind, and run useful actions like opening links, launching apps, running scripts, or sending chat commands.

### ✨ Features

- **Native controls menu integration** - Custom binds appear inside `Options -> Controls -> Key Binds`
- **Multiple trigger keys** - One bind can have several keys or mouse buttons assigned to it
- **Modifier combos** - Require `Ctrl`, `Shift`, and/or `Alt` for a bind
- **Custom bind editor** - Name, enable/disable, trigger keys, action type, and action parameters
- **Native file picker** - Select files, folders, apps, scripts, and working directories without typing paths; script filters follow the selected interpreter
- **Dangerous action protection** - File/app and command actions require the global command execution switch
- **Per-bind confirmation** - Dangerous actions can ask before running, with an optional "don't ask again" checkbox
- **Client-side only** - Install it on your client; servers do not need the mod

### ⚙️ Action Modes

- **Open URL** - Opens an `http://` or `https://` link in the default browser
- **Open file / app** - Opens an existing file, folder, or application through the operating system
- **Run command** - Runs a command or script with a selected interpreter
- **Chat / command** - Sends a chat message, or runs an in-game command when the text starts with `/`

### 🖥️ Command Interpreters

- **cmd.exe** - Classic Windows commands, `.bat`, and `.cmd` files
- **Windows PowerShell** - PowerShell 5 scripts and commands
- **PowerShell 7** - Uses `pwsh` if it is installed
- **bash** - Linux/macOS shell, Git Bash, or WSL on Windows
- **sh** - Portable POSIX shell mode
- **Direct** - Starts a program directly without wrapping it in a shell

### 🕹️ How To Use

1. Open `Options -> Controls -> Key Binds`.
2. Scroll to the `AnyBind` category.
3. Click `Add bind`.
4. Open the editor with the gear button.
5. Set the bind name, trigger key(s), modifiers, and action.
6. For command/file actions, enable `Command execution` in the editor.
7. When a dangerous action runs, confirm it. Enable `Don't ask again for this bind` if you trust it.

### 🔐 Safety Notes

- Command execution is disabled by default.
- `Open URL` only allows `http://` and `https://`.
- `Open file / app` checks that the path exists.
- `Run command` runs on your computer, not inside Minecraft.
- Only enable command execution for binds you created and trust.

### 📦 Dependencies

- **Fabric Loader** - Required
- **Fabric API** - Required
- **Minecraft** - Builds are provided for `1.21.1-1.21.8`, `1.21.9-1.21.11`, `26.1-26.1.2`, and `26.2`

### 🧰 Development

Launch the development client from the folder for the version you want:

```powershell
.\gradlew.bat runClient
cd versions\26.1-26.1.2
.\gradlew.bat runClient
```

Use `.\build-all.bat` from the project root to build every supported range into `dist`.

### 🔗 Links

- **Issues:** Use the repository issues page for bug reports and suggestions.
- **License:** [GPL-3.0](LICENSE.txt)

---

# RU

**AnyBind** - клиентский Fabric-мод, который позволяет создавать кастомные бинды прямо внутри ванильного меню управления Minecraft.

Можно назначать любые клавиши и кнопки мыши, добавлять требования `Ctrl` / `Shift` / `Alt`, указывать несколько триггеров на один бинд и запускать разные действия: открывать ссылки, запускать приложения, выполнять скрипты или отправлять команды в чат.

### ✨ Особенности

- **Интеграция в ванильное меню управления** - Бинды появляются в `Настройки -> Управление -> Назначение клавиш`
- **Несколько клавиш на один бинд** - Один бинд может срабатывать от разных клавиш или кнопок мыши
- **Комбинации с модификаторами** - Можно требовать зажатый `Ctrl`, `Shift` и/или `Alt`
- **Редактор бинда** - Название, включение/выключение, клавиши, тип действия и параметры действия
- **Удобный выбор файлов** - Файлы, папки, приложения, скрипты и рабочие каталоги можно выбирать без ручного ввода пути; фильтры скриптов зависят от интерпретатора
- **Защита опасных действий** - Открытие файлов/приложений и выполнение команд требуют включённого тумблера
- **Подтверждение для каждого бинда** - Опасное действие может спрашивать перед запуском, с галочкой "больше не спрашивать"
- **Полностью клиентский мод** - Ставится только на клиент, серверу мод не нужен

### ⚙️ Режимы действий

- **Открыть URL** - Открывает `http://` или `https://` ссылку в браузере
- **Открыть файл / приложение** - Открывает существующий файл, папку или приложение через систему
- **Выполнить команду** - Запускает команду или скрипт через выбранный интерпретатор
- **Чат / команда** - Отправляет сообщение в чат или выполняет игровую команду, если текст начинается с `/`

### 🖥️ Интерпретаторы команд

- **cmd.exe** - Классические Windows-команды, `.bat` и `.cmd` файлы
- **Windows PowerShell** - PowerShell 5 команды и `.ps1` скрипты
- **PowerShell 7** - Использует `pwsh`, если он установлен
- **bash** - Linux/macOS shell, Git Bash или WSL на Windows
- **sh** - POSIX shell-режим
- **Direct** - Запускает программу напрямую, без shell-обёртки

### 🕹️ Как пользоваться

1. Открой `Настройки -> Управление -> Назначение клавиш`.
2. Пролистай до категории `AnyBind`.
3. Нажми `Добавить бинд`.
4. Открой редактор через кнопку с шестерёнкой.
5. Укажи название, клавишу/клавиши, модификаторы и действие.
6. Для команд и открытия файлов включи `Выполнение команд` в редакторе.
7. При запуске опасного действия подтверди его. Если доверяешь бинду, включи галочку `Больше не спрашивать для этого бинда`.

### 🔐 Безопасность

- Выполнение команд выключено по умолчанию.
- `Открыть URL` разрешает только `http://` и `https://`.
- `Открыть файл / приложение` проверяет, что путь существует.
- `Выполнить команду` запускается на твоём компьютере, не внутри Minecraft.
- Включай выполнение команд только для биндов, которым доверяешь.

### 📦 Зависимости

- **Fabric Loader** - Обязательно
- **Fabric API** - Обязательно
- **Minecraft** - Есть сборки для `1.21.1-1.21.8`, `1.21.9-1.21.11`, `26.1-26.1.2` и `26.2`

### 🧰 Разработка и запуск

Запускай dev-клиент из папки нужной версии:

```powershell
.\gradlew.bat runClient
cd versions\26.1-26.1.2
.\gradlew.bat runClient
```

Команда `.\build-all.bat` из корня собирает все поддерживаемые диапазоны в папку `dist`.

### 🔗 Ссылки

- **Issues:** Баги и предложения лучше писать в issues репозитория.
- **License:** [GPL-3.0](LICENSE.txt)
