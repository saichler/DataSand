package org.datasand.codec;


public class SubPojoObject {
    private int number = -1;
    private String string = null;
    public void setNumber(int n){
        this.number = n;
    }
    public int getNumber(){
        return this.number;
    }
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubPojoObject that = (SubPojoObject) o;

        if (number != that.number) return false;
        return string != null ? string.equals(that.string) : that.string == null;

    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }
}
