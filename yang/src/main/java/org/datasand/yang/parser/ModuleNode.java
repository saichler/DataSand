package org.datasand.yang.parser;


import java.util.StringTokenizer;

/**
 * Created by sharonaicler on 5/18/16.
 */
public class ModuleNode extends YangNode {

    public ModuleNode(String data, int startPoint, YangNode.NameAndType nameAndType){
        super(data,startPoint,nameAndType);
    }

    public static final void setPackageName(YangNode.NameAndType nameAndType,String data,int startPoint){
        int valuePoint = data.indexOf("{",startPoint);
        final String namespace = YangParser.extractValue("namespace",valuePoint+1,";",data,true);
        final String revision = YangParser.extractValue("revision ",valuePoint+1,"{",data,true);

        StringBuilder packageName = new StringBuilder("org.datasand.model.");
        StringBuilder filePath = new StringBuilder("./model/src/main/java/org/datasand/model");
        packageName.append(YangParser.formatElementName(nameAndType.getName()).toLowerCase());
        StringTokenizer tokens = new StringTokenizer(namespace,":");
        while(tokens.hasMoreTokens()){
            packageName.append(".");
            filePath.append("/");
            packageName.append(tokens.nextToken().toLowerCase());
            filePath.append(tokens.nextToken().toLowerCase());
        }
        tokens = new StringTokenizer(revision,"-");
        packageName.append(".r");
        filePath.append("/r");
        while(tokens.hasMoreTokens()){
            String token = tokens.nextToken();
            packageName.append(token);
            filePath.append(token);
        }
        nameAndType.setPackageName(packageName.toString());
        nameAndType.setFilePath(filePath.toString());
    }

}
