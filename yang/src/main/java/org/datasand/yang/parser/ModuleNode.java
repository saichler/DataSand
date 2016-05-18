package org.datasand.yang.parser;


/**
 * Created by sharonaicler on 5/18/16.
 */
public class ModuleNode extends YangNode {
    public ModuleNode(String data, int startPoint, YangNode.NameAndType nameAndType){
        super(data,startPoint,getPackageName(data, startPoint),nameAndType);
    }

    private static final String getPackageName(String data,int startPoint){
        return "org.datasand.model";
    }

    public void generateCode(){
        appendPackageName();
        appendInterfaceName();
    }
}
