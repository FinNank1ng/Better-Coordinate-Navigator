import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class JarClassScanner {

    private static final String[] TARGET_CLASSES = {
            "xaero.map.element.render.ElementReader",
//            "xaero.common.IXaeroMinimap",
//            "xaero.common.XaeroMinimapSession",
//
//            "xaero.common.minimap.waypoints.WaypointSet",
//            "xaero.common.minimap.waypoints.WaypointsManager",
//            "xaero.common.minimap.waypoints.WaypointWorld",
//            "xaero.common.minimap.waypoints.Waypoint",
//            "xaero.hud.minimap.waypoint.WaypointPurpose",
//            "xaero.common.minimap.waypoints.WaypointColor"
    };

    public static void main(String[] args) throws Exception {

        Path minimapJar = Paths.get(
                System.getProperty("user.home"),
                ".gradle",
                "caches",
                "modules-2",
                "files-2.1",
                "xaero.minimap",
                "xaerominimap-forge-1.20.1",
                "26.4.2",
                "97d10d2aa8b4789b5dc8821a57313b2996970268",
                "xaerominimap-forge-1.20.1-26.4.2-dev.jar"
        );

        System.out.println("==================================================");
        System.out.println("Xaero Minimap API Scanner");
        System.out.println("==================================================");
        System.out.println("JAR:");
        System.out.println(minimapJar);
        System.out.println();

        URLClassLoader loader =
                new URLClassLoader(
                        new URL[]{
                                minimapJar.toUri().toURL()
                        },
                        ClassLoader.getSystemClassLoader()
                );

        for (String className : TARGET_CLASSES) {

            System.out.println();
            System.out.println("##################################################");
            System.out.println("CLASS: " + className);
            System.out.println("##################################################");

            try {

                Class<?> clazz =
                        Class.forName(
                                className,
                                false,
                                loader
                        );

                printClass(clazz);

            } catch (Throwable e) {

                System.out.println(
                        "FAILED: " + e
                );
            }
        }

        loader.close();
    }

    private static void printClass(Class<?> clazz) {

        System.out.println();

        System.out.println("=== SUPER CLASS ===");

        Class<?> superClass =
                clazz.getSuperclass();

        if (superClass != null) {
            System.out.println(
                    superClass.getName()
            );
        }

        System.out.println();

        System.out.println("=== INTERFACES ===");

        for (Class<?> iface :
                clazz.getInterfaces()) {

            System.out.println(
                    iface.getName()
            );
        }

        System.out.println();

        System.out.println("=== CONSTRUCTORS ===");

        Arrays.stream(
                        clazz.getDeclaredConstructors()
                )
                .forEach(
                        constructor ->
                                System.out.println(
                                        constructor
                                )
                );

        System.out.println();

        System.out.println("=== DECLARED FIELDS ===");

        Arrays.stream(
                        clazz.getDeclaredFields()
                )
                .forEach(
                        field ->
                                System.out.println(
                                        field
                                )
                );

        System.out.println();

        System.out.println("=== DECLARED METHODS ===");

        Arrays.stream(
                        clazz.getDeclaredMethods()
                )
                .sorted(
                        (a, b) ->
                                a.getName()
                                        .compareTo(
                                                b.getName()
                                        )
                )
                .forEach(
                        method ->
                                System.out.println(
                                        method
                                )
                );

        System.out.println();

        System.out.println("=== PUBLIC METHODS ===");

        Arrays.stream(
                        clazz.getMethods()
                )
                .sorted(
                        (a, b) ->
                                a.getName()
                                        .compareTo(
                                                b.getName()
                                        )
                )
                .forEach(
                        method ->
                                System.out.println(
                                        method
                                )
                        );

    }
}

/**
 *
 * XaeroMinimapSession
 *         │
 *         └── getWaypointsManager()
 *                 │
 *                 ▼
 *         WaypointsManager
 *                 │
 *                 ├── getCurrentWorld()
 *                 │       │
 *                 │       └── getCurrentSet()
 *                 │               │
 *                 │               └── WaypointSet
 *                 │                       │
 *                 │                       └── Waypoint
 *                 │
 *                 └── getWorld(...)
 */


/**
 * HoveredMapElementHolder 专门保存当前鼠标悬停的元素和对应 Renderer。
 * MapElementReader 本身就有 isHoveredOnMap(...)、交互框计算，以及最重要的 getTooltip(...)。
 * GuiMap 内部直接保存了 viewed、viewedOnMousePress 等悬停元素状态，而且有 getHoverTarget() 和 hoveredElementTooltipHelper(...)。
 * WaypointReader 继承了 MapElementReader，并且明确实现了 Waypoint 的 interaction box、hover/interactable、Tooltip 相关能力。
 */


/**
 *              GuiMap
 *                │
 *          getHoverTarget()
 *                │
 *                ▼
 *    HoveredMapElementHolder
 *                │
 *         ┌──────┴──────┐
 *         │             │
 *      Element      ElementRenderer
 *         │             │
 *         ▼             │
 *    ElementReader ◄────┘
 *         │
 *         │ getTooltip()
 *         ▼
 *       Tooltip
 *
 *
 *  ================================================
 *
 *       ElementReader
 * └── getTooltip(Object, Object, boolean)
 *       ↓
 *       Tooltip
 */