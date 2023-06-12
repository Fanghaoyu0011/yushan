package Rss.rss;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

import java.util.Map;


public class XmlService {
    private static final Logger log = LoggerFactory.getLogger(XmlService.class);
    public void getBlogs(String rssUrl, String postUrl){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();//创建一个DocumentBuilderFactory的对象
        try {
            log.info("创建DocumentBuilder对象");
            DocumentBuilder db = dbf.newDocumentBuilder();
            log.info("通过DocumentBuilder对象加载rss");
            Document document = db.parse(String.valueOf(rssUrl));
            NodeList rssList = document.getElementsByTagName("item");
            log.info("获取xml文件所有节点的集合:{}",rssList);
            for (int i = 0; i < rssList.getLength(); i++) {//遍历每一个rss节点
                 log.info("=================下面开始遍历第{}}篇文章的内容=================",i+1);
                Node item = rssList.item(i); //通过 item(i)方法 获取一个rss节点，nodelist的索引值从0开始
                log.info("解析item节点的子节点");
                NodeList childNodes = item.getChildNodes();
                Map<String, String> map = new HashMap<>();
                log.info("开始便利item子节点的集合并将其添加进map集合");
                for (int k = 0; k < childNodes.getLength(); k++) {
                    if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE) {
                        if (childNodes.item(k).getNodeName() == "title") {
                            String title = childNodes.item(k).getFirstChild().getNodeValue();
                            map.put("title",title);
                        } else if (childNodes.item(k).getNodeName() == "link") {
                            String source = childNodes.item(k).getFirstChild().getNodeValue();
                            int index = source.indexOf("/");
                            String source1 = source.substring(source.indexOf("/", (index + 1)));
                            String source2 = source1.substring(1, source1.indexOf("/", (index + 1)));
                            map.put("source",source2);
                        } else if (childNodes.item(k).getNodeName() == "author") {
                            String creator = childNodes.item(k).getFirstChild().getNodeValue();
                            map.put("author",creator);
                        } else if (childNodes.item(k).getNodeName() == "pubDate") {
                            String pubDate = childNodes.item(k).getFirstChild().getNodeValue();
                           pubDate = new DataUtil().changeData(pubDate);
                            map.put("issue_time",pubDate);
                        }else if (childNodes.item(k).getNodeName() == "description"){
                            String content = childNodes.item(k).getFirstChild().getNodeValue();
                            String abstract1 = content.substring(content.indexOf("<p>")+3, content.indexOf("</p>"));
                            map.put("abstract",abstract1);
                            map.put("content",content);
                        }
                    }
                }
                map.put("page_type","行业智库");
                String doPost = HttpFormBakUtil.doPost(postUrl,map,null,1000);
                log.info("post请求的结果{}",doPost);
                log.info("======================结束遍历第{}篇文章的内容=================",i+1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
