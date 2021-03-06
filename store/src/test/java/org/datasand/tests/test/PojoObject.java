package org.datasand.tests.test;

import java.util.ArrayList;
import java.util.List;


public class PojoObject {
    private int testIndex;
    private String testString;
    private boolean testBoolean;
    private long testLong;
    private short testShort;
    private SubPojoObject subPojo = null;
    private List<SubPojoList> list = new ArrayList<>();

    public PojoObject(){
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public boolean isTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public long getTestLong() {
        return testLong;
    }

    public void setTestLong(long testLong) {
        this.testLong = testLong;
    }

    public short getTestShort() {
        return testShort;
    }

    public void setTestShort(short testShort) {
        this.testShort = testShort;
    }

    public int getTestIndex() {
        return testIndex;
    }

    public void setTestIndex(int testIndex) {
        this.testIndex = testIndex;
    }

    public SubPojoObject getSubPojo() {
        return subPojo;
    }

    public void setSubPojo(SubPojoObject subPojo) {
        this.subPojo = subPojo;
    }

    public void setList(List<SubPojoList> lst){this.list=lst;}
    public List<SubPojoList> getList(){return this.list;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PojoObject that = (PojoObject) o;

        if (testIndex != that.testIndex) return false;
        if (testBoolean != that.testBoolean) return false;
        if (testLong != that.testLong) return false;
        if (testShort != that.testShort) return false;
        if (!testString.equals(that.testString)) return false;
        if (!subPojo.equals(that.subPojo)) return false;
        return list.equals(that.list);

    }

    @Override
    public int hashCode() {
        int result = testIndex;
        result = 31 * result + testString.hashCode();
        result = 31 * result + (testBoolean ? 1 : 0);
        result = 31 * result + (int) (testLong ^ (testLong >>> 32));
        result = 31 * result + (int) testShort;
        result = 31 * result + subPojo.hashCode();
        result = 31 * result + list.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PojoObject{" +
                "testIndex=" + testIndex +
                ", testString='" + testString + '\'' +
                ", testBoolean=" + testBoolean +
                ", testLong=" + testLong +
                ", testShort=" + testShort +
                ", subPojo=" + subPojo +
                ", list=" + list +
                '}';
    }
}
