package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * BCN 工作流持久化数据
 */
public class WorkflowSavedData extends SavedData {

    private static final String DATA_NAME =
            "better_coordinate_navigator_workflows";

    private static final String TAG_WORKFLOWS =
            "workflows";

    private final List<Workflow> workflows =
            new ArrayList<>();

    /*
     * 获取全部工作流
     */
    public List<Workflow> getWorkflows() {
        return List.copyOf(workflows);
    }

    /*
     * 添加工作流
     */
    public void addWorkflow(
            Workflow workflow
    ) {
        workflows.add(workflow);
        setDirty();
    }

    /*
     * 删除工作流
     */
    public boolean removeWorkflow(
            UUID workflowId
    ) {

        boolean removed =
                workflows.removeIf(
                        workflow ->
                                workflow.getId().equals(
                                        workflowId
                                )
                );

        if (removed) {
            setDirty();
        }

        return removed;
    }

    /*
     * 替换工作流
     */
    public void replaceWorkflow(
            Workflow workflow
    ) {

        for (int i = 0;
             i < workflows.size();
             i++) {

            if (workflows.get(i)
                    .getId()
                    .equals(workflow.getId())) {

                workflows.set(
                        i,
                        workflow
                );

                setDirty();

                return;
            }
        }

        throw new IllegalArgumentException(
                "不存在的 Workflow: "
                        + workflow.getId()
        );
    }

    /*
     * 保存到 NBT
     */
    @Override
    public CompoundTag save(
            CompoundTag tag
    ) {

        ListTag workflowList =
                new ListTag();

        for (Workflow workflow : workflows) {

            workflowList.add(
                    writeWorkflow(workflow)
            );
        }

        tag.put(
                TAG_WORKFLOWS,
                workflowList
        );

        return tag;
    }

    /*
     * 从 NBT 读取
     */
    public static WorkflowSavedData load(
            CompoundTag tag
    ) {

        WorkflowSavedData data =
                new WorkflowSavedData();

        ListTag workflowList =
                tag.getList(
                        TAG_WORKFLOWS,
                        Tag.TAG_COMPOUND
                );

        for (int i = 0;
             i < workflowList.size();
             i++) {

            CompoundTag workflowTag =
                    workflowList.getCompound(i);

            Workflow workflow =
                    readWorkflow(workflowTag);

            data.workflows.add(
                    workflow
            );
        }

        return data;
    }

    /*
     * 写入一个 Workflow
     */
    private static CompoundTag writeWorkflow(
            Workflow workflow
    ) {

        CompoundTag tag =
                new CompoundTag();

        tag.putUUID(
                "id",
                workflow.getId()
        );

        tag.putString(
                "name",
                workflow.getName()
        );

        ListTag steps =
                new ListTag();

        for (WorkflowStep step :
                workflow.getSteps()) {

            steps.add(
                    writeStep(step)
            );
        }

        tag.put(
                "steps",
                steps
        );

        return tag;
    }

    /*
     * 读取一个 Workflow
     */
    private static Workflow readWorkflow(
            CompoundTag tag
    ) {

        Workflow workflow =
                new Workflow(
                        tag.getUUID("id"),
                        tag.getString("name")
                );

        ListTag steps =
                tag.getList(
                        "steps",
                        Tag.TAG_COMPOUND
                );

        for (int i = 0;
             i < steps.size();
             i++) {

            workflow.addStep(
                    readStep(
                            steps.getCompound(i)
                    )
            );
        }

        return workflow;
    }

    /*
     * 写入一个 WorkflowStep
     */
    private static CompoundTag writeStep(
            WorkflowStep step
    ) {

        CompoundTag tag =
                new CompoundTag();

        tag.putUUID(
                "id",
                step.getId()
        );

        tag.putUUID(
                "markerId",
                step.getMarkerId()
        );

        tag.putDouble(
                "triggerRadius",
                step.getTriggerRadius()
        );

        ListTag actions =
                new ListTag();

        for (WorkflowAction action :
                step.getActions()) {

            actions.add(
                    writeAction(action)
            );
        }

        tag.put(
                "actions",
                actions
        );

        return tag;
    }

    /*
     * 读取一个 WorkflowStep
     */
    private static WorkflowStep readStep(
            CompoundTag tag
    ) {

        WorkflowStep step =
                new WorkflowStep(
                        tag.getUUID("id"),
                        tag.getUUID("markerId"),
                        tag.getDouble("triggerRadius")
                );

        ListTag actions =
                tag.getList(
                        "actions",
                        Tag.TAG_COMPOUND
                );

        for (int i = 0;
             i < actions.size();
             i++) {

            step.addAction(
                    readAction(
                            actions.getCompound(i)
                    )
            );
        }

        return step;
    }

    /*
     * 写入一个 WorkflowAction
     */
    private static CompoundTag writeAction(
            WorkflowAction action
    ) {

        CompoundTag tag =
                new CompoundTag();

        tag.putString(
                "type",
                action.getType().name()
        );

        tag.putString(
                "data",
                action.getData()
        );

        tag.putInt(
                "count",
                action.getCount()
        );

        return tag;
    }

    /*
     * 读取一个 WorkflowAction
     */
    private static WorkflowAction readAction(
            CompoundTag tag
    ) {

        WorkflowAction.Type type =
                WorkflowAction.Type.valueOf(
                        tag.getString("type")
                );

        return WorkflowAction.of(
                type,
                tag.getString("data"),
                tag.getInt("count")
        );
    }
}