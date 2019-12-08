package net.crashcraft.whipclaim.data;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;

import java.util.HashMap;

public class MaterialName {
    private HashMap<Material, String> names;

    public MaterialName(){
        names = new HashMap<>();

        for (Material material : Material.values()){
            names.put(material,
                    StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                            CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, material.name())
                    ), ' ')));
        }
    }

    public String getMaterialName(Material material){
        return names.get(material);
    }
}