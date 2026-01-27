package com.liubs.jareditor.entity;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2026/1/27
 */
public class SplitResult {
    private List<String> parts;
    private List<String> separators;

    public SplitResult(List<String> parts, List<String> separators) {
        this.parts = parts;
        this.separators = separators;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

    public List<String> getSeparators() {
        return separators;
    }

    public void setSeparators(List<String> separators) {
        this.separators = separators;
    }

    public String split0(){
        return parts.get(0);
    }
    public String split1(){
        return parts.get(1);
    }
    public String separator0(){
        return separators.get(0);
    }
    public String filePath0(){
        String s = separator0();
        return split0()+s.substring(0,s.length()-2);
    }



    @Override
    public String toString() {
        return "SplitResult{" +
                "parts=" + parts +
                ", separators=" + separators +
                '}';
    }
}
