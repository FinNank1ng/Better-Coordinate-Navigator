package io.github.FinNank1ng.better_coordinate_navigator.workflow;

import java.util.Objects;

/**
 * 工作流动作
 */
public class WorkflowAction {

    public enum Type {

        /*
         * 执行 Minecraft 命令
         */
        EXECUTE_COMMAND,

        /*
         * 给玩家物品
         */
        GIVE_ITEM,

        /*
         * 启用一个标点
         */
        ENABLE_MARKER,

        /*
         * 禁用一个标点
         */
        DISABLE_MARKER,

        /*
         * 给玩家发送消息
         */
        MESSAGE,

        /*
         * 播放声音
         */
        SOUND
    }


    private final Type type;

    /*
     * Action 的主要数据
     */
    private String data;

    /*
     * GIVE_ITEM 使用
     */
    private int count;


    private WorkflowAction(
            Type type,
            String data,
            int count
    ) {

        this.type = Objects.requireNonNull(
                type,
                "WorkflowAction type 不能为 null"
        );

        setData(data);

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "WorkflowAction count 必须大于 0"
            );
        }

        this.count = count;
    }

    /*
     * 创建执行命令动作
     */
    public static WorkflowAction executeCommand(
            String command
    ) {

        return new WorkflowAction(
                Type.EXECUTE_COMMAND,
                command,
                1
        );
    }

    /*
     * 创建给物品动作
     */
    public static WorkflowAction giveItem(
            String itemId,
            int count
    ) {

        return new WorkflowAction(
                Type.GIVE_ITEM,
                itemId,
                count
        );
    }

    /*
     * 创建启用标点动作
     */
    public static WorkflowAction enableMarker(
            String markerId
    ) {

        return new WorkflowAction(
                Type.ENABLE_MARKER,
                markerId,
                1
        );
    }

    /*
     * 创建禁用标点动作
     */
    public static WorkflowAction disableMarker(
            String markerId
    ) {

        return new WorkflowAction(
                Type.DISABLE_MARKER,
                markerId,
                1
        );
    }

    /*
     * 创建消息动作
     */
    public static WorkflowAction message(
            String message
    ) {

        return new WorkflowAction(
                Type.MESSAGE,
                message,
                1
        );
    }

    /*
     * 创建声音动作
     */
    public static WorkflowAction sound(
            String soundId
    ) {

        return new WorkflowAction(
                Type.SOUND,
                soundId,
                1
        );
    }

    /*
     * 根据持久化数据创建动作
     */
    public static WorkflowAction of(
            Type type,
            String data,
            int count
    ) {

        return new WorkflowAction(
                type,
                data,
                count
        );
    }


    public Type getType() {
        return type;
    }


    public String getData() {
        return data;
    }


    public void setData(String data) {

        Objects.requireNonNull(
                data,
                "WorkflowAction data 不能为 null"
        );

        this.data = data;
    }


    public int getCount() {
        return count;
    }


    public void setCount(int count) {

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "WorkflowAction count 必须大于 0"
            );
        }

        this.count = count;
    }


    @Override
    public String toString() {

        return "WorkflowAction{"
                + "type=" + type
                + ", data='" + data + '\''
                + ", count=" + count
                + '}';
    }
}