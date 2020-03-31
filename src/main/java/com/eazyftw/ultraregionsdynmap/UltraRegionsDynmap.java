package com.eazyftw.ultraregionsdynmap;

import com.eazyftw.api.EZApi;
import com.eazyftw.api.color.EZMessage;
import com.eazyftw.api.utils.PluginCheck;
import com.eazyftw.ultraregionsdynmap.cmds.UltraRegionsDynmapCmd;
import com.eazyftw.ultraregionsdynmap.regions.RegionsManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;

public final class UltraRegionsDynmap extends JavaPlugin {

    private static UltraRegionsDynmap i;

    private static RegionsManager regionsManager;

    @Override
    public void onEnable() {
        long time = System.currentTimeMillis();
        i = this;

        saveDefaultConfig();
        EZApi.init(this);
        EZMessage.text("%prefix% Loading...").console();

        if(!PluginCheck.checkPlugin("UltraRegions")) {
            EZMessage.text("%prefix% &cCould not find UltraRegions! This plugin is needed for this addon to work!").console();
            getPluginLoader().disablePlugin(this);
            return;
        }
        if(!PluginCheck.checkPlugin("dynmap")) {
            EZMessage.text("%prefix% &cCould not find Dynmap! This plugin is needed for this addon to work!").console();
            getPluginLoader().disablePlugin(this);
            return;
        }
        if (!Bukkit.getPluginManager().getPlugin("UltraRegions").isEnabled() || !Bukkit.getPluginManager().getPlugin("dynmap").isEnabled()) {
            EZMessage.text("%prefix% &cEither Dynmap or UltraRegions is disabled!").console();
            getPluginLoader().disablePlugin(this);
            return;
        }

        regionsManager = new RegionsManager();
        regionsManager.start();

        this.getCommand("urd").setExecutor(new UltraRegionsDynmapCmd());
        this.getCommand("urd").setTabCompleter(new UltraRegionsDynmapCmd());

        EZMessage.text("%prefix% Successfully loaded &9UltraRegions Dynmap&7 in &e" + (System.currentTimeMillis() - time) + "ms&7!").console();
    }

    @Override
    public void onDisable() {
        regionsManager.stop();

        for (AreaMarker marker : regionsManager.getAllRegionsCuboid().values()) marker.deleteMarker();
        for (CircleMarker marker : regionsManager.getAllRegionsSphere().values()) marker.deleteMarker();

        regionsManager.getAllRegionsCuboid().clear();
        regionsManager.getAllRegionsSphere().clear();

        regionsManager.getMarkerSet().deleteMarkerSet();

        EZMessage.text("%prefix% Goodbye! Hope to see you soon!").console();
    }

    public static int getRefreshTimeInTicks() {
        return getInstance().getConfig().getInt("RefreshInterval");
    }

    public static int getRefreshTimeInMinutes() {
        return getInstance().getConfig().getInt("RefreshInterval") * 20 * 60;
    }

    public static String getLayerName() {
        return getInstance().getConfig().getString("LayerName");
    }

    public static String getMarkerLineColor() {
        return getInstance().getConfig().getString("MarkerLineColor").replace("#", "");
    }

    public static String getMarkerFillColor() {
        return getInstance().getConfig().getString("MarkerFillColor").replace("#", "");
    }

    public static int getMarkerLineWeight() {
        return getInstance().getConfig().getInt("MarkerLineWeight");
    }

    public static double getMarkerLineOpacity() {
        return getInstance().getConfig().getDouble("MarkerLineOpacity");
    }

    public static double getMarkerFillOpacity() {
        return getInstance().getConfig().getDouble("MarkerFillOpacity");
    }

    public static boolean use3D() {
        return getInstance().getConfig().getBoolean("Use3D");
    }

    public static UltraRegionsDynmap getInstance() {
        return i;
    }

    public static RegionsManager getRegionsManager() {
        return regionsManager;
    }
}