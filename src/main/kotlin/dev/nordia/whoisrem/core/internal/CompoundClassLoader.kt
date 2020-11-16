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

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class CompoundClassLoader(private val loaders: Iterable<ClassLoader>) : ClassLoader() {
    override fun getResource(name: String): URL? {
        for (loader in loaders) {
            return loader.getResource(name) ?: continue
        }
        return null
    }

    override fun getResourceAsStream(name: String): InputStream? {
        for (loader in loaders) {
            return loader.getResourceAsStream(name) ?: continue
        }
        return null
    }

    override fun getResources(name: String): Enumeration<URL> {
        val urls = ArrayList<URL>()

        for (loader in loaders) {
            try {
                val resources = loader.getResources(name)

                while (resources.hasMoreElements()) {
                    val resource = resources.nextElement()

                    if (resource != null && !urls.contains(resource)) {
                        urls.add(resource)
                    }
                }
            } catch (e: IOException) {
                // ignored
            }
        }

        return Collections.enumeration(urls)
    }

    override fun loadClass(name: String): Class<*> {
        for (loader in loaders) {
            try {
                return loader.loadClass(name)
            } catch (e: ClassNotFoundException) {
                // ignored
            }
        }
        throw ClassNotFoundException()
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        return loadClass(name)
    }
}