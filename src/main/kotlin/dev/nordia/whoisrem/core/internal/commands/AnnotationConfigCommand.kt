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
import dev.nordia.whoisrem.core.commands.CommandMapping
import org.bukkit.entity.Player
import org.springframework.context.ApplicationContext
import java.lang.reflect.Method

internal class AnnotationConfigCommand(private val context: ApplicationContext, private val obj: Any, private val method: Method) : Command() {
    private val annotation = method.getAnnotation(CommandMapping::class.java)

    override val aliases = if (annotation.aliases.isNotEmpty()) annotation.aliases.toList() else listOf(method.name)
    override val permission = if (annotation.permission == "") null else annotation.permission
    override val permissionMessage = if (annotation.permissionMessage == "") null else annotation.permissionMessage

    override fun invoke(sender: Player, label: String, args: List<String>) {
        method.invoke(obj, *method.parameters.map {
            when {
                it.type == String::class.java -> label
                it.type == Player::class.java -> sender
                it.type == Command::class.java -> this
                it.parameterizedType.typeName == "java.util.List<java.lang.String>" -> args
                else -> context.getBean(it.type)
            }
        }.toTypedArray())
    }
}