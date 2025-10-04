package com.zeks.jetbrains.task.stringsearch

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware

class RunSearchAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val service = project.getService(TextSearchService::class.java)
        val toolWindow = service.toolWindowContent ?: return

        val textToSearch = toolWindow.textToSearch() ?: return
        val directory = toolWindow.directory()

        ApplicationManager.getApplication().invokeLater {
            toolWindow.clearResult()
        }
        project.getService(TextSearchService::class.java).run(textToSearch, directory) { occurrence ->
            ApplicationManager.getApplication().invokeLater {
                toolWindow.addResult(occurrence)
            }
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}