package com.eazyftw.ultraregionsdynmap.regions;

import com.eazyftw.api.color.EZMessage;
import com.eazyftw.ultraregionsdynmap.UltraRegionsDynmap;
import me.TechsCode.UltraRegions.UltraRegions;
import me.TechsCode.UltraRegions.objects.ManagedWorld;
import me.TechsCode.UltraRegions.objects.Region;
import me.TechsCode.UltraRegions.selection.CuboidSelection;
import me.TechsCode.UltraRegions.selection.SphereSelection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.CircleMarker;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.MarkerSet;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RegionsManager {

    private boolean stop;

    private MarkerSet markerSet;

    private Map<String, AreaMarker> allRegionsCuboid = new HashMap<>();
    private Map<String, CircleMarker> allRegionsSphere = new HashMap<>();

    public RegionsManager() {
        setupMarkerSet();
    }

    public void setupMarkerSet() {
        DynmapAPI dynmapAPI = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        markerSet = dynmapAPI.getMarkerAPI().getMarkerSet("ultraregions.markerset");

        final String layerName = UltraRegionsDynmap.getLayerName();
        if(markerSet == null) {
            markerSet = dynmapAPI.getMarkerAPI().createMarkerSet("ultraregions.markerset", layerName, null, false);
        } else {
            markerSet.setMarkerSetLabel(layerName);
        }

        markerSet.setLayerPriority(10);
        markerSet.setHideByDefault(false);
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(stop) {
                    stop = false;
                    this.cancel();
                }
                update();
            }
        }.runTaskTimer(UltraRegionsDynmap.getInstance(), 20L, UltraRegionsDynmap.getRefreshTimeInMinutes());
    }

    public void stop() {
        this.stop = true;
    }

    public void stopThenStart() {
        stop();
        start();
    }

    public void update() {
        Map<String, AreaMarker> newRegionsCuboid = new HashMap<>();
        Map<String, CircleMarker> newRegionsSphere = new HashMap<>();

        HashMap<World, Region[]> regions = UltraRegions.getAPI().getWorlds().get().stream().collect(Collectors.toMap(ManagedWorld::getBukkitWorld, ManagedWorld::getRegions, (a, b) -> b, HashMap::new));

        if(regions.size() > 0) {
            for(Map.Entry<World, Region[]> r : regions.entrySet()) {
                for(Region region : r.getValue()) {
                    if(region == null) continue;
                    if(region.getName() == null) continue;
                    if(region.getName().equals("Global")) continue;
                    if(region.getSelection() instanceof CuboidSelection) {
                        createRegionMarkerCuboid(r.getKey(), region, newRegionsCuboid);
                    } else if(region.getSelection() instanceof SphereSelection) {
                        createRegionMarkerCircle(r.getKey(), region, newRegionsSphere);
                    }
                }
            }
        }

        allRegionsCuboid.values().forEach(GenericMarker::deleteMarker);
        allRegionsSphere.values().forEach(GenericMarker::deleteMarker);

        allRegionsCuboid.clear();
        allRegionsCuboid = newRegionsCuboid;

        allRegionsSphere.clear();
        allRegionsSphere = newRegionsSphere;
    }

    public void createRegionMarkerCuboid(World world, Region region, Map<String, AreaMarker> regionsMap) {
        if(region.getName().equals("Global")) return;

        final String markerId = "Region_" + region.getUuid();
        final String worldName = world.getName();

        CuboidSelection selection = (CuboidSelection) region.getSelection();

        Location lowerBounds = selection.getA().getLocationInWorld(world);
        Location higherBounds = selection.getB().getLocationInWorld(world);
        if(lowerBounds == null | higherBounds == null) return;

        double[] x = new double[4];
        double[] z = new double[4];
        x[0] = lowerBounds.getX();
        z[0] = lowerBounds.getZ();
        x[1] = lowerBounds.getX();
        z[1] = higherBounds.getZ() + 1.0;
        x[2] = higherBounds.getX() + 1.0;
        z[2] = higherBounds.getZ() + 1.0;
        x[3] = higherBounds.getX() + 1.0;
        z[3] = lowerBounds.getZ();

        AreaMarker marker = allRegionsCuboid.remove(markerId);
        if(marker == null) {
            marker = markerSet.createAreaMarker(markerId, region.getName(), false, worldName, x, z, false);
            if (marker == null) return;
        } else {
            marker.setCornerLocations(x, z);
            marker.setLabel(region.getName());
        }
        if(UltraRegionsDynmap.use3D()) marker.setRangeY(higherBounds.getY() + 1.0, lowerBounds.getY());

        setMarkerCuboidStyle(marker);

        marker.setDescription(formatInfoWindow(region));

        regionsMap.put(markerId, marker);
    }

    public void createRegionMarkerCircle(World world, Region region, Map<String, CircleMarker> regionsMap) {
        if(region.getName().equals("Global")) return;

        final String markerId = "Region_" + region.getUuid();
        final String worldName = world.getName();

        SphereSelection selection = (SphereSelection) region.getSelection();

        CircleMarker marker = allRegionsSphere.remove(markerId);
        if(marker == null) {
            marker = markerSet.createCircleMarker(markerId, region.getName(), false, worldName, selection.getCenter().getX(), selection.getCenter().getY(), selection.getCenter().getZ(), selection.getRadius() + 1.0, selection.getRadius() + 1.0, false);
            if (marker == null) return;
        } else {
            marker.setRadius(selection.getRadius() + 1.0, selection.getRadius() + 1.0);
            marker.setCenter(worldName, selection.getCenter().getX(), selection.getCenter().getY(), selection.getCenter().getZ());
            marker.setLabel(region.getName());
        }

        setMarkerCircleStyle(marker);

        marker.setDescription(formatInfoWindow(region));

        regionsMap.put(markerId, marker);
    }

    private void setMarkerCircleStyle(CircleMarker marker) {

        int lineColor = 0xFF0000;
        int fillColor = 0xFF0000;

        try {
            lineColor = Integer.parseInt(UltraRegionsDynmap.getMarkerLineColor(), 16);
            fillColor = Integer.parseInt(UltraRegionsDynmap.getMarkerFillColor(), 16);
        } catch (Exception ex) {
            EZMessage.text("%prefix% &cInvalid style color specified. Defaulting to red!").console();
        }

        int lineWeight = UltraRegionsDynmap.getMarkerLineWeight();
        double lineOpacity = UltraRegionsDynmap.getMarkerLineOpacity();
        double fillOpacity = UltraRegionsDynmap.getMarkerFillOpacity();

        marker.setLineStyle(lineWeight, lineOpacity, lineColor);
        marker.setFillStyle(fillOpacity, fillColor);
    }

    private void setMarkerCuboidStyle(AreaMarker marker) {

        int lineColor = 0xFF0000;
        int fillColor = 0xFF0000;

        try {
            lineColor = Integer.parseInt(UltraRegionsDynmap.getMarkerLineColor(), 16);
            fillColor = Integer.parseInt(UltraRegionsDynmap.getMarkerFillColor(), 16);
        } catch (Exception ex) {
            EZMessage.text("%prefix% &cInvalid style color specified. Defaulting to red!").console();
        }

        int lineWeight = UltraRegionsDynmap.getMarkerLineWeight();
        double lineOpacity = UltraRegionsDynmap.getMarkerLineOpacity();
        double fillOpacity = UltraRegionsDynmap.getMarkerFillOpacity();

        marker.setLineStyle(lineWeight, lineOpacity, lineColor);
        marker.setFillStyle(fillOpacity, fillColor);
    }

    private String formatInfoWindow(Region region) {
        return "<div class=\"regioninfo\">" +
                "<center>" +
                "<div class=\"infowindow\">"+
                "<span style=\"font-weight:bold;\">" + region.getName() + "</span><br/>" +
                (region.getSelection() instanceof CuboidSelection ? "Blocks: " + region.getSelection().getBlockSize() : "Radius: " + ((SphereSelection)region.getSelection()).getRadius()) +
                "</div>" +
                "</center>" +
                "</div>";
    }

    public Map<String, AreaMarker> getAllRegionsCuboid() {
        return allRegionsCuboid;
    }

    public Map<String, CircleMarker> getAllRegionsSphere() {
        return allRegionsSphere;
    }

    public MarkerSet getMarkerSet() {
        return markerSet;
    }
}