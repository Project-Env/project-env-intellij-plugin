package io.projectenv.intellijplugin.listeners

import com.intellij.util.messages.Topic

object ProjectEnvTopics {

    val TOOLS_TOPIC: Topic<ProjectEnvToolsListener> =
        Topic.create("Project-Env tools", ProjectEnvToolsListener::class.java)
}
