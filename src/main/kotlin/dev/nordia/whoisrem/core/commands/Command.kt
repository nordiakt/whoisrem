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

package dev.nordia.whoisrem.core.commands

import org.bukkit.entity.Player

abstract class Command {
    abstract val aliases: List<String>

    open val permission: String? = null
    open val permissionMessage: String? = null

    abstract fun invoke(sender: Player, label: String, args: List<String>)
}