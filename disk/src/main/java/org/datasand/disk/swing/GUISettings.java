package org.datasand.disk.swing;


import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class GUISettings {
    public static final Font FONT_VERY_SMALL = new Font("Verdana",Font.PLAIN,9);
    public static final Font FONT_SMALL = new Font("Verdana",Font.PLAIN,10);
    public static final Font FONT = new Font("Verdana",Font.PLAIN,11);
    public static final Font FONT_BOLD = new Font("Verdana",Font.BOLD,12);
    public static Font FONT_LARGE_BOLD = new Font("Verdana",Font.BOLD,30);
    public static Font FONT_CONSOLE = new Font("Verdana",Font.BOLD,10);
    public static Font FONT_TINY = new Font("Verdana",Font.PLAIN,9);
    public static final Color LABEL_FOREGROUND = new Color(69,90,115);
    public static final Color List_BACKGROUND = new Color(245,245,245);
    public static final Color List_FOREGROUND = new Color(220,220,220);
    public static final Font FONT_LIST = new Font("Verdana",Font.BOLD,14);
    public static final Image icon = new ImageIcon("logo2.jpg").getImage();

    //public static Color GRADIENT_START = new Color(40,80,110);
    public static Color GRADIENT_START = new Color(255,255,255);
    //public static Color GRADIENT_END = new Color(170,170,190);
    public static Color GRADIENT_END = new Color(40,110,180);
    public static Color GRADIENT_END2 = new Color(200,200,200);

    public static GradientPaint GRADIENT = new GradientPaint(0, 0,GUISettings.GRADIENT_START,
            100, 20, GUISettings.GRADIENT_END, false);

    //public static Color G1 = new Color(50,80,145);
    //public static Color G2 = new Color(200,200,200);

    public static Color G1 = new Color(210,210,210);


    public static Color LINK_COLOR = new Color(150,150,150);
    public static Color High_COLOR = new Color(80,120,200);

    public static void setUI(){
        try{
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
            UIManager.put("Component.font", GUISettings.FONT);
            UIManager.put("Label.font", GUISettings.FONT);
            UIManager.put("Label.foreground", GUISettings.LABEL_FOREGROUND);
            UIManager.put("Table.font", GUISettings.FONT_SMALL);
            UIManager.put("TextArea.font", GUISettings.FONT_BOLD);
            UIManager.put("Panel.font", GUISettings.FONT);
            UIManager.put("CheckBox.font", GUISettings.FONT);
            UIManager.put("MenuBar.font", GUISettings.FONT);
            UIManager.put("Menu.font", GUISettings.FONT);
            UIManager.put("TextField.font", GUISettings.FONT);
            UIManager.put("MenuItem.font", GUISettings.FONT);
            UIManager.put("ToolBar.font", GUISettings.FONT);
            UIManager.put("Table.background", List_BACKGROUND);
            //UIManager.put("Button.font", GUISettings.FONT_SMALL);	
            UIManager.put("CheckBox.background", Color.white);
            UIManager.put("Panel.background", List_BACKGROUND);
            UIManager.put("ScrollPane.background", GUISettings.List_BACKGROUND);
            UIManager.put("SplitPane.background", GUISettings.List_BACKGROUND);
            UIManager.put("RadioButton.background", GUISettings.List_BACKGROUND);
            UIManager.put("Button.background", GUISettings.List_BACKGROUND);
            UIManager.put("Tree.background", GUISettings.List_BACKGROUND);
            UIManager.put("DefaultTreeCellRenderer.background", GUISettings.List_BACKGROUND);
            UIManager.put("Viewport.background", GUISettings.List_BACKGROUND);
            UIManager.put("TextArea.background", GUISettings.List_BACKGROUND);
            UIManager.put("LayeredPane.background", GUISettings.List_BACKGROUND);
            UIManager.put("RootPane.background", GUISettings.List_BACKGROUND);
            UIManager.put("CheckBox.background", GUISettings.List_BACKGROUND);
            UIManager.put("Window.background", GUISettings.List_BACKGROUND);
            UIManager.put("Frame.background", GUISettings.List_BACKGROUND);
            UIManager.put("Dialog.background", GUISettings.List_BACKGROUND);
            UIManager.put("Container.background", GUISettings.List_BACKGROUND);
            UIManager.put("Component.background", GUISettings.List_BACKGROUND);
            UIManager.put("TabbedPane.background", GUISettings.List_BACKGROUND);

        }catch(Exception err){
            err.printStackTrace();
        }
    }

    public static void resetGradient(int w,int h){
        GRADIENT = new GradientPaint(0, 0,GUISettings.GRADIENT_START,
                w/2, 0, GUISettings.GRADIENT_END, false);
    }

    public static void center(Component c){
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        c.setLocation((d.width-c.getWidth())/2, (d.height-c.getHeight())/2);
    }

    private static JTextField temp = new JTextField();
    public static Dimension geStringWidth(String value,Font font){
        if(value==null)
            return new Dimension(0,0);
        FontMetrics fm = temp.getFontMetrics(font);
        if(value.indexOf("\n")==-1){
            return new Dimension(fm.stringWidth(value),fm.getHeight());
        }else{
            int max = 0;
            int lines = 0;
            StringTokenizer tokens = new StringTokenizer(value,"\n");
            while(tokens.hasMoreTokens()){
                String token = tokens.nextToken();
                lines++;
                int wd = fm.stringWidth(token);
                if(wd>max)
                    max = wd;
            }
            max+=15;
            return new Dimension(max,lines*fm.getHeight());
        }
    }

    public static void showInfoDialog(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    public static boolean showConfirmDialog(String message){
        int result = JOptionPane.showConfirmDialog(null,message);
        if(result==JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }

    public static String showInputDialog(String message){
        return JOptionPane.showInputDialog(message);
    }

    public static String[] seperateToLines(String str,int w,Font f){
        if(str==null)
            return new String[]{""};
        List<String> result = new LinkedList<String>();
        StringBuffer buff = new StringBuffer();
        int fw = geStringWidth("a",f).width;
        int charsInLine = w/fw;
        if(charsInLine<=0){
            return new String[]{str};
        }

        do{
            int currentIndex = charsInLine;
            if(str.length()>currentIndex){
                String line = str.substring(0,currentIndex);
                int index = line.indexOf("\n");
                if(index!=-1){
                    result.add(line.substring(0,index).trim());
                    str = str.substring(index).trim();
                    continue;
                }
                char c = str.charAt(currentIndex);
                do{
                    currentIndex--;
                    c = str.charAt(currentIndex);
                }while(c!=32 && c!='\n');
                result.add(str.substring(0,currentIndex).trim());
                str = str.substring(currentIndex).trim();
            }
        }while(str.length()>charsInLine);
        result.add(str);

        return result.toArray(new String[0]);
    }

    public static String seperateToLines(String str,JTextArea area,Font f){
        StringBuffer result = new StringBuffer();
        int w = area.getSize().width;
        int fw = geStringWidth("a",f).width;
        int charsInLine = w/fw;
        if(charsInLine<=0){
            return str;
        }

        do{
            int currentIndex = charsInLine;
            if(str.length()>currentIndex){
                String line = str.substring(0,currentIndex);
                int index = line.indexOf("\n");
                if(index!=-1){
                    result.append(line.substring(0,index).trim()).append("\n");
                    str = str.substring(index).trim();
                    continue;
                }
                char c = str.charAt(currentIndex);
                do{
                    currentIndex--;
                    c = str.charAt(currentIndex);
                }while(c!=32 && c!='\n');

                result.append(str.substring(0,currentIndex).trim()).append("\n");
                str = str.substring(currentIndex).trim();
            }
        }while(str.length()>charsInLine);
        result.append(str);

        return result.toString();
    }
}