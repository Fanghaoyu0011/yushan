package Rss.rss;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;

public class RichTextHandle extends HTMLEditorKit.ParserCallback {
     private static RichTextHandle html2Text = new RichTextHandle();

     StringBuffer s;
     public RichTextHandle(){

     }
     public void parse(String str) throws IOException {
         InputStream iin = new ByteArrayInputStream(str.getBytes());
         Reader in = new InputStreamReader(iin);
         s = new StringBuffer();
         ParserDelegator delegator = new ParserDelegator();
        delegator.parse(in,this,Boolean.TRUE);
        iin.close();
        in.close();
     }

     public void handleText(char[] text,int pos){s.append(text);}

     public String getText(){return s.toString();}

     public static String getContent(String str){
         try {
             html2Text.parse(str);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return html2Text.getText();
     }

}
