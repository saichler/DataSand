package org.datasand.yang.parser;


import java.util.StringTokenizer;

/**
 * Created by sharonaicler on 5/18/16.
 */
public class ModuleNode extends YangNode {

    public ModuleNode(String data, int startPoint, YangNodeAttributes yangNodeAttributes){
        super(data,startPoint, yangNodeAttributes);
    }

    public static final void setPackageName(YangNodeAttributes yangNodeAttributes, String data, int startPoint){
        int valuePoint = data.indexOf("{",startPoint);
        String namespace = YangParser.extractValue("namespace",valuePoint+1,";",data,true);
        final String revision = YangParser.extractValue("revision ",valuePoint+1,"{",data,true);

        StringBuilder packageName = new StringBuilder("org.datasand.model");
        StringBuilder filePath = new StringBuilder("./model/src/main/java/org/datasand/model");
        //packageName.append(YangParser.formatElementName(yangNodeAttributes.getName()).toLowerCase());
        //filePath.append(YangParser.formatElementName(yangNodeAttributes.getName()).toLowerCase());
        int lastSlash = namespace.lastIndexOf("/");
        if(lastSlash!=-1){
            namespace = namespace.substring(lastSlash+1);
        }
        StringTokenizer tokens = new StringTokenizer(namespace, ":");
        if(namespace.indexOf(":")!=-1) {
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                packageName.append(".");
                filePath.append("/");
                packageName.append(token.toLowerCase());
                filePath.append(token.toLowerCase());
            }
        }else{
            packageName.append(".").append(namespace);
            filePath.append("/").append(namespace);
        }
        tokens = new StringTokenizer(revision,"-");
        packageName.append(".r");
        filePath.append("/r");
        while(tokens.hasMoreTokens()){
            String token = tokens.nextToken();
            packageName.append(token);
            filePath.append(token);
        }
        yangNodeAttributes.setPackageName(packageName.toString());
        yangNodeAttributes.setFilePath(filePath.toString());
    }

}
