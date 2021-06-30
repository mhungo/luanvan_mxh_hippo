package vn.edu.stu.Model;

import java.util.Objects;

public class Hobby {
    private String category;
    private String subCategory;
    private String title;

    public Hobby(String category, String subCategory, String title) {
        this.category = category;
        this.subCategory = subCategory;
        this.title = title;
    }

    public Hobby() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hobby hobby = (Hobby) o;
        return Objects.equals(title, hobby.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
