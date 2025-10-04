package com.zeks.jetbrains.task.stringsearch

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class StopSearchAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        project.getService(TextSearchService::class.java).stop()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
}