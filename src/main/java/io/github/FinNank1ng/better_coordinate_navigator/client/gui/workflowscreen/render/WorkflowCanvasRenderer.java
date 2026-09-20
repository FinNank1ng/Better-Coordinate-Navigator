package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.render;

import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState;
import io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen.WorkflowScreenState.NodePosition;
import io.github.FinNank1ng.better_coordinate_navigator.data.ClientQuestCache;
import io.github.FinNank1ng.better_coordinate_navigator.data.QuestMarker;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowAction;
import io.github.FinNank1ng.better_coordinate_navigator.workflow.WorkflowStep;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/*
 * 工作流画布渲染器
 *
 * 负责画布背景、网格、连接线、流程节点以及缩放信息
 */
public class WorkflowCanvasRenderer {

    private static final int COLOR_BACKGROUND = 0xFF0B1118;
    private static final int COLOR_PANEL_ALT = 0xFF17212D;
    private static final int COLOR_NODE = 0xFF182432;
    private static final int COLOR_NODE_HOVER = 0xFF1D3043;
    private static final int COLOR_NODE_SELECTED = 0xFF203B55;
    private static final int COLOR_BORDER = 0xFF283747;
    private static final int COLOR_BORDER_HOVER = 0xFF3E5F7D;
    private static final int COLOR_ACCENT = 0xFF67B8FF;
    private static final int COLOR_TEXT = 0xFFE8EEF5;
    private static final int COLOR_TEXT_SECONDARY = 0xFFA8B3BF;
    private static final int COLOR_TEXT_MUTED = 0xFF6E7C8B;
    private static final int COLOR_SUCCESS = 0xFF72D69A;

    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 34;
    private static final int NODE_WIDTH = 230;
    private static final int NODE_HEIGHT = 150;

    private final WorkflowScreenState state;

    private int screenWidth;
    private int screenHeight;
    private int sidebarWidth;
    private int mouseX;
    private int mouseY;

    public WorkflowCanvasRenderer(
            WorkflowScreenState state
    ) {

        this.state = state;
    }

    /*
     * 绘制整个 Canvas
     */
    public void renderCanvas(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight,
            int sidebarWidth,
            int mouseX,
            int mouseY
    ) {

        updateContext(
                screenWidth,
                screenHeight,
                sidebarWidth,
                mouseX,
                mouseY
        );

        drawCanvas(graphics);
    }

    /*
     * 绘制 Canvas 缩放信息
     */
    public void renderZoomInfo(
            GuiGraphics graphics,
            int screenWidth,
            int screenHeight
    ) {

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        drawCanvasZoomInfo(graphics);
    }


    /*
     * 获取当前 Minecraft 字体
     */
    private Font getFont() {

        return Minecraft.getInstance().font;
    }


    /*
     * 判断坐标是否位于区域内
     */
    private boolean inside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    /*
     * 更新渲染上下文
     */
    private void updateContext(
            int screenWidth,
            int screenHeight,
            int sidebarWidth,
            int mouseX,
            int mouseY
    ) {

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.sidebarWidth = sidebarWidth;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    private void drawCanvas(
                GuiGraphics graphics
        ) {

            int canvasX =
                    sidebarWidth;

            int canvasTop =
                    HEADER_HEIGHT;

            int canvasBottom =
                    screenHeight - FOOTER_HEIGHT;

            graphics.fill(
                    canvasX,
                    canvasTop,
                    screenWidth,
                    canvasBottom,
                    COLOR_BACKGROUND
            );

            /*
             * Canvas 裁剪区域
             */
            graphics.enableScissor(
                    canvasX,
                    canvasTop,
                    screenWidth,
                    canvasBottom
            );

            /*
             * 网格
             */
            drawGrid(
                    graphics,
                    canvasX,
                    canvasTop,
                    screenWidth,
                    canvasBottom
            );

            if (state.getCurrentWorkflow() == null) {

                drawCanvasEmpty(
                        graphics,
                        canvasX,
                        canvasTop,
                        screenWidth - canvasX,
                        canvasBottom - canvasTop
                );

                graphics.flush();

                graphics.disableScissor();

                return;
            }

            /*
             * 连接线
             */
            drawConnections(
                    graphics
            );

            /*
             * 节点
             */
            for (WorkflowStep step :
                    state.getCurrentWorkflow().getSteps()) {

                drawNode(
                        graphics,
                        step
                );
            }

            /*
             * 提交 Canvas
             */
            graphics.flush();

            graphics.disableScissor();
        }

    private void drawCanvasZoomInfo(
                GuiGraphics graphics
        ) {
            String text =
                    String.format(
                            Locale.ROOT,
                            "%.0f%%",
                            state.getZoom() * 100.0D
                    );

            int textWidth =
                    getFont().width(text);

            int x =
                    screenWidth
                            - textWidth
                            - 14;

            int y =
                    screenHeight
                            - FOOTER_HEIGHT
                            - 16;

            graphics.drawString(
                    getFont(),
                    Component.literal(text),
                    x,
                    y,
                    COLOR_TEXT_SECONDARY
            );
        }

    private void drawGrid(
                GuiGraphics graphics,
                int left,
                int top,
                int right,
                int bottom
        ) {

            int gridSize =
                    Math.max(
                            12,
                            (int) (
                                    32 * state.getZoom()
                            )
                    );

            int offsetX =
                    mod(
                            (int) state.getPanX(),
                            gridSize
                    );

            int offsetY =
                    mod(
                            (int) state.getPanY(),
                            gridSize
                    );

            int gridColor =
                    0xFF111923;

            for (
                    int x = left + offsetX;
                    x < right;
                    x += gridSize
            ) {

                graphics.fill(
                        x,
                        top,
                        x + 1,
                        bottom,
                        gridColor
                );
            }

            for (
                    int y = top + offsetY;
                    y < bottom;
                    y += gridSize
            ) {

                graphics.fill(
                        left,
                        y,
                        right,
                        y + 1,
                        gridColor
                );
            }
        }

    private void drawCanvasEmpty(
                GuiGraphics graphics,
                int x,
                int y,
                int width,
                int height
        ) {

            String title =
                    "还没有流程节点";

            String description =
                    "点击左上角“+”开始建立工作流";

            int centerX =
                    x + width / 2;

            int centerY =
                    y + height / 2;

        graphics.drawString(
                getFont(),
                Component.literal(title),
                centerX
                        - getFont().width(title) / 2,
                centerY - 12,
                COLOR_TEXT
        );

        graphics.drawString(
                getFont(),
                Component.literal(description),
                centerX
                        - getFont().width(description) / 2,
                centerY + 12,
                COLOR_TEXT_MUTED
        );
        }

    private void drawConnections(
                GuiGraphics graphics
        ) {

            List<WorkflowStep> steps =
                    state.getCurrentWorkflow().getSteps();

            for (int i = 0;
                 i < steps.size() - 1;
                 i++) {

                WorkflowStep from =
                        steps.get(i);

                WorkflowStep to =
                        steps.get(i + 1);

                NodePosition fromPosition =
                        state.getNodePositions().get(
                                from.getId()
                        );

                NodePosition toPosition =
                        state.getNodePositions().get(
                                to.getId()
                        );

                if (fromPosition == null
                        || toPosition == null) {
                    continue;
                }

                double fromX =
                        worldToScreenX(
                                fromPosition.getX()
                                        + NODE_WIDTH / 2.0
                        );

                double fromY =
                        worldToScreenY(
                                fromPosition.getY()
                                        + NODE_HEIGHT
                        );

                double toX =
                        worldToScreenX(
                                toPosition.getX()
                                        + NODE_WIDTH / 2.0
                        );

                double toY =
                        worldToScreenY(
                                toPosition.getY()
                        );

                drawWorkflowConnection(
                        graphics,
                        fromX,
                        fromY,
                        toX,
                        toY
                );
            }
        }

    private void drawWorkflowConnection(
                GuiGraphics graphics,
                double startX,
                double startY,
                double endX,
                double endY
        ) {

            int thickness =
                    (int) Math.round(
                            clamp(
                                    2.0D
                                            + (
                                            state.getZoom()
                                                    - 0.45D
                                    ) * 0.8D,
                                    2.0D,
                                    3.0D
                            )
                    );

            int x1 =
                    (int) startX;

            int y1 =
                    (int) startY;

            int x2 =
                    (int) endX;

            int y2 =
                    (int) endY;

        int arrowSize =
                Math.max(
                        5,
                        Math.min(
                                9,
                                (int) (
                                        8 * state.getZoom()
                                )
                        )
                );

            int arrowY =
                    y2
                            - arrowSize
                            - 2;

            int middleY;

            if (y2 > y1) {

                middleY =
                        y1
                                + Math.max(
                                14,
                                (y2 - y1) / 2
                        );

            } else {

                middleY =
                        y1
                                - Math.max(
                                14,
                                (y1 - y2) / 2
                        );
            }

            graphics.fill(
                    x1 - thickness / 2,
                    Math.min(
                            y1,
                            middleY
                    ),
                    x1 + thickness / 2 + 1,
                    Math.max(
                            y1,
                            middleY
                    ),
                    COLOR_BORDER_HOVER
            );

            int minX =
                    Math.min(
                            x1,
                            x2
                    );

            int maxX =
                    Math.max(
                            x1,
                            x2
                    );

            graphics.fill(
                    minX,
                    middleY - thickness / 2,
                    maxX + 1,
                    middleY + thickness / 2 + 1,
                    COLOR_BORDER_HOVER
            );

            graphics.fill(
                    x2 - thickness / 2,
                    Math.min(
                            middleY,
                            arrowY
                    ),
                    x2 + thickness / 2 + 1,
                    Math.max(
                            middleY,
                            arrowY
                    ),
                    COLOR_BORDER_HOVER
            );

            graphics.fill(
                    x2 - arrowSize,
                    arrowY,
                    x2 + arrowSize + 1,
                    arrowY + thickness,
                    COLOR_ACCENT
            );

            graphics.fill(
                    x2 - arrowSize + 2,
                    arrowY + thickness,
                    x2 + arrowSize - 1,
                    arrowY + thickness + 2,
                    COLOR_ACCENT
            );

            graphics.fill(
                    x2 - arrowSize + 4,
                    arrowY + thickness + 2,
                    x2 + arrowSize - 3,
                    arrowY + thickness + 4,
                    COLOR_ACCENT
            );
        }

    private void drawNode(
                GuiGraphics graphics,
                WorkflowStep step
        ) {

            NodePosition position =
                    state.getNodePositions().get(
                            step.getId()
                    );

            if (position == null) {
                return;
            }

            int x =
                    (int) worldToScreenX(
                            position.getX()
                    );

            int y =
                    (int) worldToScreenY(
                            position.getY()
                    );

            int nodeWidth =
                    Math.max(
                            150,
                            (int) (
                                    NODE_WIDTH * state.getZoom()
                            )
                    );

            int nodeHeight =
                    Math.max(
                            82,
                            (int) (
                                    NODE_HEIGHT * state.getZoom()
                            )
                    );

            boolean selected =
                    step.getId().equals(
                            state.getSelectedStepId()
                    );

            boolean hovered =
                    inside(
                            this.mouseX,
                            this.mouseY,
                            x,
                            y,
                            nodeWidth,
                            nodeHeight
                    );

            int fillColor =
                    selected
                            ? COLOR_NODE_SELECTED
                            : hovered
                            ? COLOR_NODE_HOVER
                            : COLOR_NODE;

            int borderColor =
                    selected
                            ? COLOR_ACCENT
                            : hovered
                            ? COLOR_BORDER_HOVER
                            : COLOR_BORDER;

            graphics.fill(
                    x,
                    y,
                    x + nodeWidth,
                    y + nodeHeight,
                    fillColor
            );

            graphics.fill(
                    x,
                    y,
                    x + nodeWidth,
                    y + 2,
                    borderColor
            );

            graphics.fill(
                    x,
                    y + nodeHeight - 2,
                    x + nodeWidth,
                    y + nodeHeight,
                    borderColor
            );

            graphics.fill(
                    x,
                    y,
                    x + 2,
                    y + nodeHeight,
                    borderColor
            );

            graphics.fill(
                    x + nodeWidth - 2,
                    y,
                    x + nodeWidth,
                    y + nodeHeight,
                    borderColor
            );

            QuestMarker marker =
                    findMarker(
                            step.getMarkerId()
                    );

            String markerName =
                    marker == null
                            ? "目标标点不存在"
                            : marker.name;

            String stepText =
                    String.format(
                            Locale.ROOT,
                            "%02d",
                            getStepIndex(step) + 1
                    );

            String actionCountText =
                    step.getActionCount()
                            + " Action";

            boolean compact =
                    state.getZoom() < 0.80D;

            double textScale =
                    compact
                            ? 0.82D
                            : clamp(
                            0.86D
                                    + (
                                    state.getZoom()
                                            - 0.8D
                            ) * 0.14D,
                            0.86D,
                            1.0D
                    );

            int padding =
                    compact
                            ? 10
                            : Math.max(
                            12,
                            (int) (
                                    14 * state.getZoom()
                            )
                    );

            int titleY =
                    y + padding;

            drawNodeText(
                    graphics,
                    stepText,
                    x + padding,
                    titleY,
                    selected
                            ? COLOR_ACCENT
                            : COLOR_TEXT_MUTED,
                    textScale
            );

            int actionWidth =
                    scaledFontWidth(
                            actionCountText,
                            textScale
                    );

            int actionX =
                    x
                            + nodeWidth
                            - padding
                            - actionWidth;

            int stepWidth =
                    scaledFontWidth(
                            stepText,
                            textScale
                    );

            int markerX =
                    x
                            + padding
                            + stepWidth
                            + (
                            compact
                                    ? 14
                                    : 18
                    );

            int markerAvailableWidth =
                    actionX
                            - markerX
                            - (
                            compact
                                    ? 8
                                    : 12
                    );

            markerName =
                    trimTextToWidth(
                            markerName,
                            Math.max(
                                    30,
                                    markerAvailableWidth
                            ),
                            textScale
                    );

            drawNodeText(
                    graphics,
                    markerName,
                    markerX,
                    titleY,
                    COLOR_TEXT,
                    textScale
            );

            drawNodeText(
                    graphics,
                    actionCountText,
                    actionX,
                    titleY,
                    COLOR_SUCCESS,
                    textScale
            );

            int dividerY =
                    y
                            + (
                            compact
                                    ? 30
                                    : Math.max(
                                    34,
                                    (int) (
                                            42 * state.getZoom()
                                    )
                            )
                    );

            graphics.fill(
                    x + padding,
                    dividerY,
                    x + nodeWidth - padding,
                    dividerY + 1,
                    COLOR_BORDER
            );

            if (compact) {

                drawNodeText(
                        graphics,
                        "目标",
                        x + padding,
                        y + 40,
                        COLOR_TEXT_MUTED,
                        textScale
                );

                String compactTarget =
                        trimTextToWidth(
                                markerName,
                                nodeWidth
                                        - padding * 2,
                                textScale
                        );

                drawNodeText(
                        graphics,
                        compactTarget,
                        x + padding + 38,
                        y + 40,
                        COLOR_TEXT_SECONDARY,
                        textScale
                );

                String triggerText =
                        String.format(
                                Locale.ROOT,
                                "触发 %.1fm",
                                step.getTriggerRadius()
                        );

                drawNodeText(
                        graphics,
                        triggerText,
                        x + padding,
                        y + 60,
                        COLOR_TEXT_SECONDARY,
                        textScale
                );

                if (step.getActionCount() > 0) {

                    WorkflowAction action =
                            step.getAction(0);

                    drawNodeText(
                            graphics,
                            "• "
                                    + getActionTitle(
                                    action.getType()
                            ),
                            x + padding + 75,
                            y + 60,
                            COLOR_TEXT_MUTED,
                            textScale
                    );
                }

            } else {

                int contentPadding =
                        Math.max(
                                12,
                                (int) (
                                        16 * state.getZoom()
                                )
                        );

                int targetLabelY =
                        y
                                + (
                                int) (
                                53 * state.getZoom()
                        );

                int targetNameY =
                        y
                                + (
                                int) (
                                73 * state.getZoom()
                        );

                drawNodeText(
                        graphics,
                        "目标",
                        x + contentPadding,
                        targetLabelY,
                        COLOR_TEXT_MUTED,
                        textScale
                );

                String targetText =
                        marker == null
                                ? "目标标点不存在"
                                : marker.name;

                targetText =
                        trimTextToWidth(
                                targetText,
                                nodeWidth
                                        - contentPadding * 2,
                                textScale
                        );

                drawNodeText(
                        graphics,
                        targetText,
                        x + contentPadding,
                        targetNameY,
                        COLOR_TEXT_SECONDARY,
                        textScale
                );

                int triggerY =
                        y
                                + (
                                int) (
                                98 * state.getZoom()
                        );

                drawNodeText(
                        graphics,
                        "触发范围",
                        x + contentPadding,
                        triggerY,
                        COLOR_TEXT_MUTED,
                        textScale
                );

                String triggerText =
                        String.format(
                                Locale.ROOT,
                                "%.1f m",
                                step.getTriggerRadius()
                        );

                int triggerWidth =
                        scaledFontWidth(
                                triggerText,
                                textScale
                        );

                drawNodeText(
                        graphics,
                        triggerText,
                        x + nodeWidth
                                - contentPadding
                                - triggerWidth,
                        triggerY,
                        COLOR_TEXT_SECONDARY,
                        textScale
                );

                int actionLabelY =
                        y
                                + (
                                int) (
                                121 * state.getZoom()
                        );

                drawNodeText(
                        graphics,
                        "ACTION",
                        x + contentPadding,
                        actionLabelY,
                        COLOR_TEXT_MUTED,
                        textScale
                );

                if (step.getActionCount() > 0) {

                    WorkflowAction action =
                            step.getAction(0);

                    int actionBoxX =
                            x + contentPadding;

                    int actionBoxY =
                            y
                                    + (
                                    int) (
                                    136 * state.getZoom()
                            );

                    int actionBoxWidth =
                            nodeWidth
                                    - contentPadding * 2;

                    int actionBoxHeight =
                            Math.max(
                                    20,
                                    (int) (
                                            32 * state.getZoom()
                                    )
                            );

                    if (actionBoxY
                            + actionBoxHeight
                            <= y + nodeHeight - 8) {

                        graphics.fill(
                                actionBoxX,
                                actionBoxY,
                                actionBoxX
                                        + actionBoxWidth,
                                actionBoxY
                                        + actionBoxHeight,
                                COLOR_PANEL_ALT
                        );

                        graphics.fill(
                                actionBoxX,
                                actionBoxY,
                                actionBoxX + 2,
                                actionBoxY
                                        + actionBoxHeight,
                                COLOR_ACCENT
                        );

                        String actionText =
                                "• "
                                        + getActionTitle(
                                        action.getType()
                                );

                        drawNodeText(
                                graphics,
                                actionText,
                                actionBoxX + 10,
                                actionBoxY + 7,
                                COLOR_TEXT_SECONDARY,
                                textScale
                        );
                    }
                }
            }

            drawNodePort(
                    graphics,
                    x + nodeWidth / 2,
                    y,
                    state.getZoom()
            );

            drawNodePort(
                    graphics,
                    x + nodeWidth / 2,
                    y + nodeHeight,
                    state.getZoom()
            );
        }

    private void drawNodeText(
                GuiGraphics graphics,
                String text,
                int x,
                int y,
                int color,
                double scale
        ) {

            graphics.pose().pushPose();

            graphics.pose().translate(
                    x,
                    y,
                    0
            );

            graphics.pose().scale(
                    (float) scale,
                    (float) scale,
                    1.0F
            );

            graphics.drawString(
                    getFont(),
                    Component.literal(text),
                    0,
                    0,
                    color
            );

            graphics.pose().popPose();
        }

    private int scaledFontWidth(
                String text,
                double scale
        ) {
        return (int) Math.ceil(
                getFont().width(text)
                        * scale
        );
    }

    private String trimTextToWidth(
                String text,
                int maxWidth,
                double scale
        ) {

            if (text == null
                    || text.isEmpty()) {

                return "";
            }

            if (scaledFontWidth(
                    text,
                    scale
            ) <= maxWidth) {

                return text;
            }

            String suffix =
                    "...";

            int suffixWidth =
                    scaledFontWidth(
                            suffix,
                            scale
                    );

            if (suffixWidth >= maxWidth) {
                return "";
            }

            StringBuilder builder =
                    new StringBuilder();

            for (int i = 0;
                 i < text.length();
                 i++) {

                builder.append(
                        text.charAt(i)
                );

                String candidate =
                        builder.toString();

                if (
                        scaledFontWidth(
                                candidate,
                                scale
                        ) + suffixWidth
                                > maxWidth
                ) {

                    builder.deleteCharAt(
                            builder.length() - 1
                    );

                    break;
                }
            }

            return builder
                    + suffix;
        }

    private void drawNodePort(
                GuiGraphics graphics,
                int centerX,
                int centerY,
                double zoom
        ) {

            int size =
                    Math.max(
                            6,
                            Math.min(
                                    10,
                                    (int) (
                                            8 * zoom
                                    )
                            )
                    );

            int half =
                    size / 2;

            graphics.fill(
                    centerX - half,
                    centerY - half,
                    centerX + half + 1,
                    centerY + half + 1,
                    COLOR_ACCENT
            );
        }
    /*
     * 根据 UUID 查找任务点
     */
    private QuestMarker findMarker(
            UUID markerId
    ) {

        if (markerId == null) {
            return null;
        }

        for (QuestMarker marker :
                ClientQuestCache.getMarkers()) {

            if (markerId.equals(marker.getId())) {
                return marker;
            }
        }

        return null;
    }

    /*
     * 获取步骤下标
     */
    private int getStepIndex(
            WorkflowStep step
    ) {

        if (step == null
                || state.getCurrentWorkflow() == null) {
            return -1;
        }

        List<WorkflowStep> steps =
                state.getCurrentWorkflow().getSteps();

        for (int i = 0; i < steps.size(); i++) {

            if (step.getId().equals(steps.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    /*
     * 获取 Action 显示名称
     */
    private String getActionTitle(
            WorkflowAction.Type type
    ) {

        return switch (type) {
            case EXECUTE_COMMAND -> "执行命令";
            case GIVE_ITEM -> "发放物品";
            case ENABLE_MARKER -> "启用标点";
            case DISABLE_MARKER -> "禁用标点";
            case MESSAGE -> "发送消息";
            case SOUND -> "播放声音";
        };
    }

    /*
     * 世界坐标转换到屏幕坐标
     */
    private double worldToScreenX(
            double worldX
    ) {

        return sidebarWidth
                + state.getPanX()
                + worldX * state.getZoom();
    }

    private double worldToScreenY(
            double worldY
    ) {

        return HEADER_HEIGHT
                + state.getPanY()
                + worldY * state.getZoom();
    }

    /*
     * 限制数值范围
     */
    private double clamp(
            double value,
            double min,
            double max
    ) {

        return Math.max(
                min,
                Math.min(max, value)
        );
    }

    /*
     * 处理负数取模
     */
    private int mod(
            int value,
            int divisor
    ) {

        int result = value % divisor;

        return result < 0
                ? result + divisor
                : result;
    }

}
