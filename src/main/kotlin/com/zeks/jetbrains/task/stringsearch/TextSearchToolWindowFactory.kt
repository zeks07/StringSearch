package com.zeks.jetbrains.task.stringsearch

import com.intellij.find.SearchTextArea
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.plus
import com.intellij.ui.render.RenderingUtil
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import kotlin.io.path.Path

class TextSearchToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val toolWindowContent = TextSearchToolWindowContent()
        project.getService(TextSearchService::class.java).toolWindowContent = toolWindowContent

        val content = ContentFactory.getInstance().createContent(toolWindowContent.toolWindow, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class TextSearchToolWindowContent() {
    private val backgroundColor = JBUI.CurrentTheme.ToolWindow.background()
    private val foregroundColor = UIUtil.getTableForeground()

    private val stringSearchTextArea = JTextArea().apply {
        columns = 25
        rows = 1
        accessibleContext.accessibleName = TextSearchBundle.message("text.search.accessible.name")
        lineWrap = false
        wrapStyleWord = false
        text = "terminal"
        accessibleContext.accessibleName = TextSearchBundle.message("text.search.accessible.name")
        isOpaque = true
        background = backgroundColor
        foreground = foregroundColor
    }

    private val searchTextArea = SearchTextArea(stringSearchTextArea, true).apply {
        background = null
        foreground = UIUtil.getTextFieldForeground()
        preferredSize = Dimension(200, preferredSize.height)
        minimumSize = Dimension(200, minimumSize.height)
        isOpaque = false
    }

    private val directoryTextField = JTextField().apply {
        isEditable = true
        accessibleContext.accessibleName = TextSearchBundle.message("directory.accessible.name")
        text = """C:\Users\zeljk\Projects\test\Test\src"""
    }

    private val group = ActionManager.getInstance()
        .getAction("com.zeks.jetbrains.task.stringsearch.TextSearchActionGroup") as DefaultActionGroup

    private val toolbar = ActionManager.getInstance()
        .createActionToolbar("Search", group, true)

    private val model = object : DefaultTableModel(
        arrayOf("Occurrence"), 0
    ) {
        override fun isCellEditable(row: Int, column: Int) = false
        override fun getColumnClass(columnIndex: Int) = String::class.java
    }

    private var hoveredRow = -1

    private val resultTable = JBTable(model).apply {
        setShowGrid(false)
        autoscrolls = true
        fillsViewportHeight = true
        tableHeader = null
        isFocusable = false
        putClientProperty(RenderingUtil.ALWAYS_PAINT_SELECTION_AS_FOCUSED, true)
        emptyText.isShowAboveCenter = false
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        intercellSpacing = JBUI.emptySize()
        border = null
        background = backgroundColor
        rowHeight = 28

        setDefaultRenderer(Any::class.java, CellRenderer())
        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent?) {
                val point = e?.point ?: return
                val row = rowAtPoint(point)
                if (row != hoveredRow) {
                    hoveredRow = row
                    repaint()
                }
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent?) {
                if (hoveredRow != -1) {
                    hoveredRow = -1
                    repaint()
                }
            }
        })
    }

    private val resultPanel = JBScrollPane(resultTable).apply {
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        border = JBUI.Borders.empty()
        preferredSize = resultTable.preferredSize
    }

    private val controlPanel = panel {
        row {
            cell(searchTextArea)
                .resizableColumn()
                .align(AlignX.FILL)
                .applyToComponent {
                    minimumSize = Dimension(200, preferredSize.height)
                }
        }
        separator()
        row {
            cell(directoryTextField)
                .resizableColumn()
                .align(AlignX.FILL)
                .label("Directory")
        }
    }

    private val mainPanel = panel {
        row {
            cell(controlPanel)
                .resizableColumn()
                .align(AlignX.FILL)
                .applyToComponent {
                    minimumSize = Dimension(200, preferredSize.height)
                }
        }
        row {
            cell(resultPanel)
                .resizableColumn()
                .align(AlignX.FILL + AlignY.FILL)
        }.resizableRow()
    }

    private val content = panel {
        row {
            cell(toolbar.component)
                .resizableColumn()
                .align(AlignX.FILL)
        }
            .bottomGap(BottomGap.SMALL)
        row {
            cell(mainPanel)
                .resizableColumn()
                .align(AlignX.FILL + AlignY.FILL)
        }.resizableRow()
    }.apply {
        border = null
    }

    val toolWindow: JBScrollPane = JBScrollPane(content).apply {
        toolbar.targetComponent = this
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        border = null
    }


    fun textToSearch(): String? = stringSearchTextArea.text
    fun directory() = Path(directoryTextField.text)

    fun addResult(occurrence: Occurrence) {
        val newTextField = "${occurrence.file}:${occurrence.line}:${occurrence.offset}"
        model.addRow(arrayOf(newTextField))
    }

    fun clearResult() {
        model.rowCount = 0
    }

    inner class CellRenderer : DefaultTableCellRenderer() {
        private val pad = JBUI.Borders.empty(4, 8)
        private val foregroundColor = UIUtil.getTableForeground()
        private val selectionColor = UIUtil.getTableSelectionBackground(true)
        private val selectionForeground = UIUtil.getTableSelectionForeground(true)

        var selectionInsets: Insets = JBUI.emptyInsets()
        var selected = false

        init {
            isOpaque = false
            border = pad
        }

        override fun isOpaque() = false

        override fun getTableCellRendererComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            text = value.toString()
            selected = isSelected
            foreground = if (isSelected) selectionForeground else foregroundColor
            if (hasFocus) {
                border = JBUI.Borders.compound(pad, JBUI.Borders.empty())
            }
            return this
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            try {
                val oldAntiAlias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                )

                g2.color = if (selected) selectionColor else backgroundColor

                if (selected) {
                    val rectX = selectionInsets.left
                    val rectY = selectionInsets.top
                    val rectWidth = width - rectX - selectionInsets.right
                    val rectHeight = height - rectY - selectionInsets.bottom

                    val selectionArc = JBUI.scale(8)

                    g2.fillRoundRect(rectX, rectY, rectWidth, rectHeight, selectionArc, selectionArc)
                }

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntiAlias)
                super.paintComponent(g)
            } finally {
                g2.dispose()
            }
        }
    }
}