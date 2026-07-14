package com.mihai.dailyhabit

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class SavedAreaCommand {
    object GoToDaySelection : SavedAreaCommand()
    object GoToJournal : SavedAreaCommand()
}

object SavedAreaNavigator {
    private val _commands = MutableSharedFlow<SavedAreaCommand>(extraBufferCapacity = 1)
    val commands = _commands.asSharedFlow()

    fun navigateToDaySelection() {
        _commands.tryEmit(SavedAreaCommand.GoToDaySelection)
    }

    fun navigateToJournal() {
        _commands.tryEmit(SavedAreaCommand.GoToJournal)
    }
}
