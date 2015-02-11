package org.datasand.codec;

import java.util.LinkedList;
import java.util.List;
/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class LeftOvers {
    private List<Object> leftOverList = new LinkedList<Object>();

    public LeftOvers() {
    }

    public void addLeftOver(Object o) {
        this.leftOverList.add(o);
    }

    public List<Object> getLeftOvers() {
        return this.leftOverList;
    }
}
