package io.github.FinNank1ng.better_coordinate_navigator.integration;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XaeroWaypointHelper {

    public static void addQuestWaypoint(String name, double x, double y, double z, String dimension) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        String world = mc.level.dimension().location().toString().replace(":", "_");

        File dir = new File(mc.gameDirectory, "XaeroWaypoints/" + world);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, "waypoints.txt");

        String line = String.format(
                "waypoint:%s:%d:%d:%d:255:1:0:gui.xaero_default:false:0:0:quest%n",
                name,
                (int)x,
                (int)y,
                (int)z
        );

        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}