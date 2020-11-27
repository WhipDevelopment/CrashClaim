package net.crashcraft.crashclaim.visualize.api;

import org.bukkit.Material;

public enum VisualColor {
    GOLD,
    RED,
    GREEN,
    YELLOW,
    DARK_RED,
    DARK_GREEN,
    WHITE;

    private Material material;

    VisualColor(){

    }

    public void setMaterial(Material material){
        this.material = material;
    }

    public Material getMaterial(){
        return material;
    }
}
