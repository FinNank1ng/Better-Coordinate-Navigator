package io.github.FinNank1ng.better_coordinate_navigator.data;

import io.github.FinNank1ng.better_coordinate_navigator.workflow.Workflow;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * 客户端工作流缓存
 */
public final class ClientWorkflowCache {

    /*
     * 服务器同步到客户端的全部工作流
     */
    private static final List<Workflow> WORKFLOWS =
            new CopyOnWriteArrayList<>();

    private ClientWorkflowCache() {
    }

    /*
     * 设置客户端工作流缓存
     */
    public static void set(
            List<Workflow> workflows
    ) {

        WORKFLOWS.clear();

        if (workflows != null) {

            WORKFLOWS.addAll(
                    workflows
            );
        }
    }

    /*
     * 获取全部客户端工作流
     */
    public static List<Workflow> getWorkflows() {

        return WORKFLOWS;
    }

    /*
     * 根据 UUID 获取工作流
     */
    public static Workflow getWorkflow(
            UUID id
    ) {

        if (id == null) {
            return null;
        }

        for (Workflow workflow :
                WORKFLOWS) {

            if (workflow.getId().equals(id)) {

                return workflow;
            }
        }

        return null;
    }

    /*
     * 根据名称获取工作流
     */
    public static Workflow getWorkflow(
            String name
    ) {

        if (name == null) {
            return null;
        }

        String target = name.trim();

        if (target.isEmpty()) {
            return null;
        }

        Workflow result = null;

        for (Workflow workflow :
                WORKFLOWS) {

            if (!workflow.getName()
                    .equalsIgnoreCase(target)) {
                continue;
            }

            /*
             * 如果存在多个同名工作流
             * 不返回不确定的结果
             */
            if (result != null) {
                return null;
            }

            result = workflow;
        }

        return result;
    }

    /*
     * 查找全部同名工作流
     */
    public static List<Workflow> findWorkflows(
            String name
    ) {

        if (name == null) {
            return List.of();
        }

        String target = name.trim();

        if (target.isEmpty()) {
            return List.of();
        }

        List<Workflow> result = new java.util.ArrayList<>();

        for (Workflow workflow :
                WORKFLOWS) {

            if (workflow.getName()
                    .equalsIgnoreCase(target)) {

                result.add(workflow);
            }
        }

        return result;
    }

    /*
     * 清空客户端工作流缓存
     */
    public static void clear() {

        WORKFLOWS.clear();
    }
}