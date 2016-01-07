package org.datasand.codec.bytearray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.datasand.codec.*;
import org.datasand.codec.observers.DefaultPOJOClassExtractor;
import org.datasand.codec.observers.IAugmetationObserver;
import org.datasand.codec.observers.IChildAttributeObserver;
import org.datasand.codec.observers.IClassExtractorObserver;
import org.datasand.codec.observers.IMethodFilterObserver;
import org.datasand.codec.observers.ITypeAttributeObserver;

/**
 * Created by root on 1/7/16.
 */
public class Observers {

    private List<IChildAttributeObserver> modelChildIdentifierObservers = new ArrayList<IChildAttributeObserver>();
    private List<ITypeAttributeObserver> modelTypeIdentifierObservers = new ArrayList<ITypeAttributeObserver>();
    private List<IMethodFilterObserver> methodFilterObservers = new ArrayList<IMethodFilterObserver>();
    private IClassExtractorObserver classExtractor = new DefaultPOJOClassExtractor();
    private IAugmetationObserver augmentationObserver = null;

    public Observers(){

    }

    public IClassExtractorObserver getClassExtractor(){
        return this.classExtractor;
    }
}
