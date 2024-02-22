package net.crashcraft.crashclaim.data;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;

import java.util.HashMap;

public class MaterialName {
    private final HashMap<Material, String> names;

    public MaterialName(){
        names = new HashMap<>();

        for (Material material : Material.values()){
            String[] split = material.toString().split("_");

            StringBuilder builder = new StringBuilder();
            for (String s : split) {
                builder.append(StringUtils.capitalize(s.toLowerCase()));
                builder.append(" ");
            }

            String capitalize = StringUtils.capitalize(StringUtils.join(StringUtils.split(
                    CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, builder.toString())
            ), ' '));

            names.put(material, capitalize);
        }
    }

    public String getMaterialName(Material material){
        return names.get(material);
    }
}