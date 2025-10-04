package com.zeks.jetbrains.task.stringsearch

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup

class TextSearchActionGroup : DefaultActionGroup() {
    override fun update(event: AnActionEvent) {
        val project = event.project ?: return

        val service = project.getService(TextSearchService::class.java)
        val actionManager = ActionManager.getInstance()

        removeAll()
        if (service.isRunning()) {
            add(actionManager.getAction("com.zeks.jetbrains.task.stringsearch.StopSearchAction"))
        } else {
            add(actionManager.getAction("com.zeks.jetbrains.task.stringsearch.RunSearchAction"))
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT
}