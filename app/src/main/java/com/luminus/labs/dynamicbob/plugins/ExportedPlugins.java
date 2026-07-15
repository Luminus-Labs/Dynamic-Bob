package com.luminus.labs.dynamicbob.plugins;

import com.luminus.labs.dynamicbob.plugins.AppShortcut.AppShortcutIsland;
import com.luminus.labs.dynamicbob.plugins.BatteryPlugin.BatteryPlugin;
import com.luminus.labs.dynamicbob.plugins.MediaSession.MediaSessionPlugin;
import com.luminus.labs.dynamicbob.plugins.Notification.NotificationPlugin;

import java.util.ArrayList;

public class ExportedPlugins {
    public static ArrayList<BasePlugin> getPlugins() {
        ArrayList<BasePlugin> plugins = new ArrayList<>();
        plugins.add(new AppShortcutIsland());
        plugins.add(new BatteryPlugin());
        plugins.add(new MediaSessionPlugin());
        plugins.add(new NotificationPlugin());
        return plugins;
    }
}
