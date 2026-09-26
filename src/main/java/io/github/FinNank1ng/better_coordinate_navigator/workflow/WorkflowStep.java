package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 工作流中的一个步骤
 */
public class WorkflowStep {

    private final UUID id;

    /*
     * 引用 QuestMarker 的 UUID
     */
    private UUID markerId;

    /*
     * 到达判定范围
     */
    private double triggerRadius;

    /*
     * 玩家到达此步骤后执行的动作
     */
    private final List<WorkflowAction> actions =
            new ArrayList<>();


    public WorkflowStep(
            UUID id,
            UUID markerId,
            double triggerRadius
    ) {
        this.id = Objects.requireNonNull(
                id,
                "WorkflowStep id 不能为 null"
        );

        setMarkerId(markerId);
        setTriggerRadius(triggerRadius);
    }


    public static WorkflowStep create(
            UUID markerId
    ) {
        return new WorkflowStep(
                UUID.randomUUID(),
                markerId,
                2.0
        );
    }


    public static WorkflowStep create(
            UUID markerId,
            double triggerRadius
    ) {
        return new WorkflowStep(
                UUID.randomUUID(),
                markerId,
                triggerRadius
        );
    }


    public UUID getId() {
        return id;
    }


    public UUID getMarkerId() {
        return markerId;
    }


    public void setMarkerId(UUID markerId) {

        this.markerId = Objects.requireNonNull(
                markerId,
                "WorkflowStep markerId 不能为 null"
        );
    }


    public double getTriggerRadius() {
        return triggerRadius;
    }


    public void setTriggerRadius(
            double triggerRadius
    ) {

        if (!Double.isFinite(triggerRadius)) {
            throw new IllegalArgumentException(
                    "WorkflowStep triggerRadius 必须是有限数字"
            );
        }

        if (triggerRadius <= 0) {
            throw new IllegalArgumentException(
                    "WorkflowStep triggerRadius 必须大于 0"
            );
        }

        this.triggerRadius = triggerRadius;
    }


    /*
     * 添加一个动作
     */
    public void addAction(
            WorkflowAction action
    ) {

        Objects.requireNonNull(
                action,
                "WorkflowAction 不能为 null"
        );

        actions.add(action);
    }

    public void addAction(int index, WorkflowAction action) {
        Objects.requireNonNull(action, "WorkflowAction 不能为 null");

        if (index < 0 || index > actions.size()) {
            throw new IndexOutOfBoundsException(
                    "WorkflowAction 插入位置越界: "
                            + index
                            + ", 当前动作数量: "
                            + actions.size()
            );
        }

        actions.add(index, action);
    }

    /*
     * 删除一个动作
     */
    public WorkflowAction removeAction(
            int index
    ) {

        checkActionIndex(index);

        return actions.remove(index);
    }


    /*
     * 获取动作
     */
    public WorkflowAction getAction(
            int index
    ) {

        checkActionIndex(index);

        return actions.get(index);
    }


    private void checkActionIndex(int index) {

        if (index < 0 || index >= actions.size()) {

            throw new IndexOutOfBoundsException(
                    "WorkflowAction index 越界: "
                            + index
                            + ", 当前动作数量: "
                            + actions.size()
            );
        }
    }


    public List<WorkflowAction> getActions() {

        return Collections.unmodifiableList(
                actions
        );
    }


    /*
     * 替换指定位置的 Action
     */
    public void setAction(
            int index,
            WorkflowAction action
    ) {

        if (action == null) {
            return;
        }

        if (index < 0
                || index >= actions.size()) {

            return;
        }

        actions.set(
                index,
                action
        );
    }


    public int getActionCount() {
        return actions.size();
    }


    @Override
    public String toString() {

        return "WorkflowStep{"
                + "id=" + id
                + ", markerId=" + markerId
                + ", triggerRadius=" + triggerRadius
                + ", actions=" + actions.size()
                + '}';
    }
}