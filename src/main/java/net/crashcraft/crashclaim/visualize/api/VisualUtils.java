package net.crashcraft.crashclaim.visualize.api;

import java.util.ArrayList;

public class VisualUtils {
    public static ArrayList<Integer> getLine(int Length){
        ArrayList<Integer> LineDots = new ArrayList<Integer>(){};
        LineDots.add(1);
        LineDots.add(Length-1);
        int Parts = (int) Math.ceil((float) Length/18);
        int PartLength = (int) Math.ceil((float) Length/Parts);
        for (int i=PartLength; i<Length ; i+=PartLength){
            LineDots.add(i-2);
            LineDots.add(i-1);
            LineDots.add(i);
            if(((Length % 2 == 0) && (Parts % 2 == 0)) || ((Length % 2 != 0) && (Parts % 2 != 0))){
                LineDots.add(i+1);
            }
        }
        return LineDots;
    }
}
