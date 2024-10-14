package com.liubs.jareditor.jarbuild;

import org.objectweb.asm.commons.Remapper;

/**
 * @author Liubsyy
 * @date 2024/9/13
 */
public class PackageRemapper extends Remapper {
    private String oldPackage;
    private String newPackage;

    public PackageRemapper(String oldPackage, String newPackage) {
        this.oldPackage = oldPackage;
        this.newPackage = newPackage;
    }

    @Override
    public String map(String internalName) {
        if(internalName.startsWith(oldPackage)) {
            return internalName.replaceFirst(oldPackage
                    ,newPackage);
        }
        return internalName;
    }
}