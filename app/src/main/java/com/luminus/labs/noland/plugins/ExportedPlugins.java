package com.luminus.labs.noland.plugins;

import com.luminus.labs.noland.plugins.AppShortcut.AppShortcutIsland;
import com.luminus.labs.noland.plugins.BatteryPlugin.BatteryPlugin;
import com.luminus.labs.noland.plugins.MediaSession.MediaSessionPlugin;
import com.luminus.labs.noland.plugins.Notification.NotificationPlugin;

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
