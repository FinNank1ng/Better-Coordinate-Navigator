package io.github.FinNank1ng.better_coordinate_navigator.client.gui.workflowscreen;

import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/*
 * 工作流界面的运行状态
 */
public class WorkflowScreenState {

    final List<Workflow> workflows = new ArrayList<>();
    final Set<UUID> pinnedWorkflows = new HashSet<>();
    final Map<UUID, NodePosition> nodePositions = new HashMap<>();

    Workflow currentWorkflow;

    int selectedWorkflowIndex = -1;
    UUID selectedStepId;
    int selectedActionIndex = -1;

    boolean sidebarCollapsed = false;

    boolean markerPickerOpen = false;
    boolean actionPickerOpen = false;
    boolean workflowMenuOpen = false;
    boolean nodeMenuOpen = false;
    boolean renameDialogOpen = false;

    UUID replacingMarkerStepId;
    UUID actionPickerStepId;
    UUID workflowMenuWorkflowId;
    UUID nodeMenuStepId;

    int nodeMenuX;
    int nodeMenuY;

    UUID hoveredNodeId;
    long hoveredNodeStartTime;

    double sidebarScroll = 0.0D;
    double markerScroll = 0.0D;

    double zoom = 1.0D;

    double panX = 0.0D;
    double panY = 0.0D;

    boolean panning = false;

    int panStartMouseX;
    int panStartMouseY;

    double panStartX;
    double panStartY;

    UUID draggingNodeId;

    double dragOffsetX;
    double dragOffsetY;

    boolean dirty = false;

    String statusText = "已保存";
    int statusColor;

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public Set<UUID> getPinnedWorkflows() {
        return pinnedWorkflows;
    }

    public Map<UUID, NodePosition> getNodePositions() {
        return nodePositions;
    }

    public Workflow getCurrentWorkflow() {
        return currentWorkflow;
    }

    public UUID getSelectedStepId() {
        return selectedStepId;
    }

    public int getSelectedActionIndex() {
        return selectedActionIndex;
    }

    public void setSelectedActionIndex(
            int selectedActionIndex
    ) {
        this.selectedActionIndex =
                selectedActionIndex;
    }

    public double getZoom() {
        return zoom;
    }

    public double getPanX() {
        return panX;
    }

    public double getPanY() {
        return panY;
    }

    public boolean isSidebarCollapsed() {
        return sidebarCollapsed;
    }

    public double getSidebarScroll() {
        return sidebarScroll;
    }

    public void setSidebarScroll(
            double sidebarScroll
    ) {

        this.sidebarScroll = sidebarScroll;
    }
    public String getStatusText() {
        return statusText;
    }

    public int getStatusColor() {
        return statusColor;
    }

    /*
     * 节点菜单坐标
     */
    public int getNodeMenuX() {
        return nodeMenuX;
    }

    public int getNodeMenuY() {
        return nodeMenuY;
    }

    /*
     * 任务点弹窗滚动位置
     */
    public double getMarkerScroll() {
        return markerScroll;
    }

    public void setMarkerScroll(
            double markerScroll
    ) {

        this.markerScroll = markerScroll;
    }

    WorkflowScreenState(
            int initialStatusColor
    ) {

        this.statusColor = initialStatusColor;
    }

    /*
     * 节点位置
     */
    public static class NodePosition {

        double x;
        double y;

        NodePosition(
                double x,
                double y
        ) {

            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}