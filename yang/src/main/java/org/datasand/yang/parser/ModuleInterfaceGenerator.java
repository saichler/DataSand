package org.datasand.yang.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by saichler on 5/17/16.
 */
public class ModuleInterfaceGenerator {
    public void generate(YangNode node){
        final String namespace = YangParser.extractValue("namespace",node.getValuePoint(),";",node.getData(),true);
        final String revision = YangParser.extractValue("revision",node.getValuePoint(),"{",node.getData(),true);

        final String packageName[] = getPackageName(node.getName(),namespace,revision);

        node.app("package "+packageName[0]+";\n\n",0);
        node.app("public interface "+node.getName()+" {\n",0);
        node.app("}",0);
        System.out.println(node.getJavaInteface());
        File srcDir = new File(packageName[1]);
        if(!srcDir.exists()){
            srcDir.mkdirs();
        }
        File destFile = new File(srcDir, node.getName()+".java");
        try {
            FileOutputStream out = new FileOutputStream(destFile);
            out.write(node.getJavaInteface().toString().getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String[] getPackageName(String moduleName,String namespace,String revision){
        StringBuilder sb = new StringBuilder("org.datasand.model.");
        StringBuilder sb1 = new StringBuilder("./model/src/main/java/org/datasand/model");
        sb.append(moduleName.toLowerCase());
        StringTokenizer tokens = new StringTokenizer(namespace,":");
        while(tokens.hasMoreTokens()){
            sb.append(".");
            sb1.append("/");
            sb.append(tokens.nextToken().toLowerCase());
            sb1.append(tokens.nextToken().toLowerCase());
        }
        tokens = new StringTokenizer(revision,"-");
        sb.append(".r");
        sb1.append("/r");
        while(tokens.hasMoreTokens()){
            String token = tokens.nextToken();
            sb.append(token);
            sb1.append(token);
        }
        return new String[]{sb.toString(),sb1.toString()};
    }
}
