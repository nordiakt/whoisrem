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

package dev.nordia.whoisrem.core.internal

import dev.nordia.whoisrem.core.commands.Command
import dev.nordia.whoisrem.core.commands.CommandMapping
import dev.nordia.whoisrem.core.events.PluginEnabledEvent
import dev.nordia.whoisrem.core.internal.commands.AnnotationConfigCommand
import dev.nordia.whoisrem.core.internal.commands.CommandsExecutor
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.event.Listener
import org.bukkit.help.HelpMap
import org.bukkit.inventory.ItemFactory
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicesManager
import org.bukkit.plugin.messaging.Messenger
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.util.CachedServerIcon
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
@ComponentScan("dev.nordia.whoisrem")
class AutoConfiguration {
    @EventListener
    @ExperimentalStdlibApi
    fun onPluginEnabled(event: PluginEnabledEvent) {
        Bukkit.getPluginManager().registerEvents(CommandsExecutor(buildList {
            addAll(event.context.getBeansOfType<Any>().values.flatMap {
                it::class.java.declaredMethods
                    .filter { method -> method.isAnnotationPresent(CommandMapping::class.java) }
                    .map { method -> AnnotationConfigCommand(event.context, it, method.apply { isAccessible = true }) }
            })
            addAll(event.context.getBeansOfType<Command>().values.filter { it.aliases.isNotEmpty() })
        }), event.plugin)

        event.context.getBeansOfType<Listener>().forEach { (_, listener) ->
            Bukkit.getPluginManager().registerEvents(listener, event.plugin)
        }
    }

    @Bean
    fun pluginManager(): PluginManager = Bukkit.getPluginManager()

    @Bean
    fun itemFactory(): ItemFactory = Bukkit.getItemFactory()

    @Bean
    fun servicesManager(): ServicesManager = Bukkit.getServicesManager()

    @Bean
    fun serverIcon(): CachedServerIcon = Bukkit.getServerIcon()

    @Bean
    fun messenger(): Messenger = Bukkit.getMessenger()

    @Bean
    fun helpMap(): HelpMap = Bukkit.getHelpMap()

    @Bean
    fun server(): Server = Bukkit.getServer()

    @Bean
    fun consoleCommandSender(): ConsoleCommandSender = Bukkit.getConsoleSender()

    @Bean
    fun scoreboardManager(): ScoreboardManager = Bukkit.getScoreboardManager()

    @Bean
    fun bukkitScheduler(): BukkitScheduler = Bukkit.getScheduler()
}