package vn.edu.stu.Model;

import java.util.ArrayList;

public class ModelCity {
    private ArrayList<City> LtsItem;
    private int TotalDoanhNghiep;

    public ModelCity(ArrayList<City> ltsItem, int totalDoanhNghiep) {
        LtsItem = ltsItem;
        TotalDoanhNghiep = totalDoanhNghiep;
    }

    public ModelCity() {
    }

    public ArrayList<City> getLtsItem() {
        return LtsItem;
    }

    public void setLtsItem(ArrayList<City> ltsItem) {
        LtsItem = ltsItem;
    }

    public int getTotalDoanhNghiep() {
        return TotalDoanhNghiep;
    }

    public void setTotalDoanhNghiep(int totalDoanhNghiep) {
        TotalDoanhNghiep = totalDoanhNghiep;
    }

    @Override
    public String toString() {
        return "ModelCity{" +
                "LtsItem=" + LtsItem +
                ", TotalDoanhNghiep=" + TotalDoanhNghiep +
                '}';
    }
}
