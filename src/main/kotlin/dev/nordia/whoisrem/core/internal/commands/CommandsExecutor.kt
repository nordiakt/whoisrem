/*
   Copyright 2020 Dmitry Prokhorov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package dev.nordia.whoisrem.core.internal.commands

import dev.nordia.whoisrem.core.commands.Command
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

internal class CommandsExecutor(private val commands: List<Command>) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerCommandPreProcess(event: PlayerCommandPreprocessEvent) {
        val command = commands.find { it.aliases.any { a -> event.message.startsWith("/$a", true) } } ?: return

        if (command.permission == null || event.player.hasPermission(command.permission)) {
            val label = command.aliases.find { a -> event.message.startsWith("/$a", true) }!!

            command.invoke(event.player, label, event.message.substringAfter(label).split(" ").filter(CharSequence::isNotBlank))
        } else if (command.permissionMessage != null) {
            event.player.sendMessage(command.permissionMessage)
        }

        event.isCancelled = true
    }
}