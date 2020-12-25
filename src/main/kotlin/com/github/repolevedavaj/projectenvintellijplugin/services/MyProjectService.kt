package com.github.repolevedavaj.projectenvintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.repolevedavaj.projectenvintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
