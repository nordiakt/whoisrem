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

package dev.nordia.whoisrem.core

import dev.nordia.whoisrem.core.events.PluginDisabledEvent
import dev.nordia.whoisrem.core.events.PluginEnabledEvent
import dev.nordia.whoisrem.core.internal.CompoundClassLoader
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.annotation.AnnotationDescription
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.DefaultResourceLoader

abstract class JavaPlugin : JavaPlugin {
    private val application: Class<*>
    private lateinit var context: ConfigurableApplicationContext

    constructor() {
        val packageName = this::class.qualifiedName?.substringBeforeLast(".") ?: ""

        this.application = ByteBuddy()
            .subclass(Any::class.java)
            .name("$packageName.Application")
            .annotateType(AnnotationDescription.Builder.ofType(SpringBootApplication::class.java)
                    .defineArray("scanBasePackages", packageName)
                    .build())
            .make().load(this.javaClass.classLoader).loaded
    }

    constructor(application: Class<*>) {
        this.application = application
    }

    final override fun onEnable() {
        context = SpringApplicationBuilder(application).apply {
            application().apply {
                resourceLoader = DefaultResourceLoader(CompoundClassLoader(listOf(javaClass.classLoader, Thread.currentThread().contextClassLoader)))

                configureApplication(this)
            }
        }.run()

        context.beanFactory.registerSingleton("plugin", this)

        (context as ApplicationEventPublisher).publishEvent(PluginEnabledEvent(this, context, this))

        onEnable(context)
    }

    final override fun onDisable() {
        (context as ApplicationEventPublisher).publishEvent(PluginDisabledEvent(this, context, this))

        onDisable(context)
    }

    open fun configureApplication(application: SpringApplication) = Unit

    open fun onEnable(context: ApplicationContext) = Unit

    open fun onDisable(context: ApplicationContext) = Unit
}