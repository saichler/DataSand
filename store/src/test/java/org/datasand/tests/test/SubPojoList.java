package org.datasand.tests.test;

public class SubPojoList {
    private String name = null;
    public void setName(String n){this.name=n;}
    public String getName(){return this.name;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubPojoList that = (SubPojoList) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
