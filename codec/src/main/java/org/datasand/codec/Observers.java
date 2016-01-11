/*
 * Copyright (c) 2015 DataSand,Sharon Aicler and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.datasand.codec;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.datasand.codec.observers.DefaultChildAttributeObserver;
import org.datasand.codec.observers.DefaultMethodFilterObserver;
import org.datasand.codec.observers.DefaultPOJOClassExtractor;
import org.datasand.codec.observers.DefaultPOJOTypeAttribute;
import org.datasand.codec.observers.IAugmetationObserver;
import org.datasand.codec.observers.IChildAttributeObserver;
import org.datasand.codec.observers.IClassExtractorObserver;
import org.datasand.codec.observers.IMethodFilterObserver;
import org.datasand.codec.observers.ITypeAttributeObserver;

/**
 * @author - Sharon Aicler (saichler@gmail.com)
 */
public class Observers {

    public static final Observers instance = new Observers();

    private List<IChildAttributeObserver> modelChildIdentifierObservers = new ArrayList<IChildAttributeObserver>();
    private List<ITypeAttributeObserver> modelTypeIdentifierObservers = new ArrayList<ITypeAttributeObserver>();
    private List<IMethodFilterObserver> methodFilterObservers = new ArrayList<IMethodFilterObserver>();
    private IClassExtractorObserver classExtractor = new DefaultPOJOClassExtractor();
    private IAugmetationObserver augmentationObserver = null;

    private Observers(){
        modelChildIdentifierObservers.add(new DefaultChildAttributeObserver());
        modelTypeIdentifierObservers.add(new DefaultPOJOTypeAttribute());
        methodFilterObservers.add(new DefaultMethodFilterObserver());
    }

    public IClassExtractorObserver getClassExtractor(){
        return this.classExtractor;
    }

    public boolean isValidModelMethod(Method m){
        for(IMethodFilterObserver rule:this.methodFilterObservers){
            if(!rule.isValidModelMethod(m))
                return false;
        }
        return true;
    }

    public boolean isValidModelAttribute(VColumn vColumn){
        for(IMethodFilterObserver rule:this.methodFilterObservers){
            if(!rule.isValidAttribute(vColumn))
                return false;
        }
        return true;
    }

    public boolean isChildAttribute(VColumn vColumn){
        for(IChildAttributeObserver rule:this.modelChildIdentifierObservers){
            if(rule.isChildAttribute(vColumn))
                return true;
        }
        return false;
    }

    public boolean isChildAttribute(VTable vTable){
        for(IChildAttributeObserver rule:this.modelChildIdentifierObservers){
            if(rule.isChildAttribute(vTable))
                return true;
        }
        return false;
    }

    public boolean isTypeAttribute(VColumn vColumn){
        for(ITypeAttributeObserver rule:this.modelTypeIdentifierObservers){
            if(rule.isTypeAttribute(vColumn))
                return true;
        }
        return false;
    }

    public boolean supportAugmentations(VTable vTable){
        for(IChildAttributeObserver rule:this.modelChildIdentifierObservers){
            if(rule.supportAugmentation(vTable))
                return true;
        }
        return false;
    }

}
