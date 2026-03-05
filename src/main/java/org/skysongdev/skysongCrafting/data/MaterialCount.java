package org.skysongdev.skysongCrafting.data;

import java.util.Objects;

public class MaterialCount {
    private String id;
    private int count;

    public MaterialCount(String id, int count) {
        this.id = id;
        this.count = count;
    }

    public String getId() {
        return id;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialCount that = (MaterialCount) o;
        return count == that.count && Objects.equals(id == null ? null : id.toLowerCase(), that.id == null ? null : that.id.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id == null ? null : id.toLowerCase(), count);
    }

    @Override
    public String toString() {
        return "MaterialCount{" +
                "id='" + id + '\'' +
                ", count=" + count +
                '}';
    }

}
