package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * BCN 工作流
 *
 * 一个 Workflow 表示一套可以被玩家执行的游戏内流程
 */
public class Workflow {

    /*
     * 工作流唯一 ID
     */
    private final UUID id;

    /*
     * 工作流显示名称
     */
    private String name;

    /*
     * 当前工作流包含的全部步骤
     */
    private final List<WorkflowStep> steps =
            new ArrayList<>();


    public Workflow(
            UUID id,
            String name
    ) {

        this.id = Objects.requireNonNull(
                id,
                "Workflow id 不能为 null"
        );

        setName(name);
    }


    /*
     * 创建一个新的 Workflow。
     */
    public static Workflow create(String name) {

        return new Workflow(
                UUID.randomUUID(),
                name
        );
    }


    /*
     * 添加一个步骤到工作流末尾
     */
    public void addStep(WorkflowStep step) {

        Objects.requireNonNull(
                step,
                "WorkflowStep 不能为 null"
        );

        steps.add(step);
    }


    /*
     * 在指定位置插入步骤
     */
    public void addStep(
            int index,
            WorkflowStep step
    ) {

        Objects.requireNonNull(
                step,
                "WorkflowStep 不能为 null"
        );

        if (index < 0 || index > steps.size()) {

            throw new IndexOutOfBoundsException(
                    "WorkflowStep 插入位置越界: "
                            + index
            );
        }

        steps.add(
                index,
                step
        );
    }


    /*
     * 删除指定位置的步骤
     */
    public WorkflowStep removeStep(int index) {

        checkStepIndex(index);

        return steps.remove(index);
    }


    /*
     * 调整步骤位置
     */
    public void moveStep(
            int fromIndex,
            int toIndex
    ) {

        checkStepIndex(fromIndex);

        if (toIndex < 0 || toIndex >= steps.size()) {

            throw new IndexOutOfBoundsException(
                    "WorkflowStep 目标位置越界: "
                            + toIndex
            );
        }

        if (fromIndex == toIndex) {
            return;
        }

        WorkflowStep step =
                steps.remove(fromIndex);

        steps.add(
                toIndex,
                step
        );
    }


    /*
     * 获取指定步骤
     */
    public WorkflowStep getStep(int index) {

        checkStepIndex(index);

        return steps.get(index);
    }


    private void checkStepIndex(int index) {

        if (index < 0 || index >= steps.size()) {

            throw new IndexOutOfBoundsException(
                    "WorkflowStep index 越界: "
                            + index
                            + ", 当前步骤数量: "
                            + steps.size()
            );
        }
    }


    public UUID getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {

        Objects.requireNonNull(
                name,
                "Workflow name 不能为 null"
        );

        String trimmed =
                name.trim();

        if (trimmed.isEmpty()) {

            throw new IllegalArgumentException(
                    "Workflow name 不能为空"
            );
        }

        this.name = trimmed;
    }


    /*
     * 对外提供只读列表
     */
    public List<WorkflowStep> getSteps() {

        return Collections.unmodifiableList(
                steps
        );
    }


    public int getStepCount() {
        return steps.size();
    }


    public boolean isEmpty() {
        return steps.isEmpty();
    }


    @Override
    public String toString() {

        return "Workflow{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", steps=" + steps.size()
                + '}';
    }
}