package Rss.rss;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlServiceEnglish {
    private static final Logger log = LoggerFactory.getLogger(XmlServiceEnglish.class);
    public static void sendmail(String url){

        String xmlStr = GetUrl.sendGet(url);

        log.info("本次遍历使用的url是：{}",url);
        Document document = null;
        StringReader stringReader = new StringReader(xmlStr);
        InputSource inputSource = new InputSource(stringReader);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputSource);
            NodeList bookList = document.getElementsByTagName("item");//获取所有的集合
            for (int i = 0; i < bookList.getLength(); i++) {//遍历每一个rss节点
                Node book = bookList.item(i); //通过 item(i)方法 获取一个rss节点，nodelist的索引值从0开始
                NodeList childNodes = book.getChildNodes(); //解析hang节点的子节点
                Map<String, String> map = new HashMap<>();
                log.info("创建存储翻译前文章的map集合:map");
                HashMap<String, String> transMap = new HashMap<>();
                log.info("创建存储翻译后文章的map集合：tramsMap");
                Map<String, String> mapTitle = new HashMap<>();
                HashMap<String, String> mapAbstract = new HashMap<>();
                HashMap<String, String> mapContent = new HashMap<>();
                for (int k = 0; k < childNodes.getLength(); k++) {
                    if (childNodes.item(k).getNodeType() == Node.ELEMENT_NODE) {
                        if (childNodes.item(k).getNodeName() == "title") {
                            String title = childNodes.item(k).getFirstChild().getNodeValue();
                            map.put("title",title);
                            log.info("本篇文章翻译之前标题为：{}",title);
                            mapTitle.put("english", title);
                            String tramsTitle = HttpFormBakUtil.doPost("https://www.yushanshuju.com/ords/datatech/ofsi/baidutrans_en", mapTitle, null, 10000);
                            transMap.put("title", JsonUtil.stringToJson(tramsTitle));
                            log.info("本篇文章翻译之后标题为：{}",JsonUtil.stringToJson(tramsTitle));
                        } else if (childNodes.item(k).getNodeName() == "link") {
                            String source = childNodes.item(k).getFirstChild().getNodeValue();
                            int index = source.indexOf("/");
                            String source1 = source.substring(source.indexOf("/", (index + 1)));
                            String source2 = source1.substring(1, source1.indexOf("/", (index + 1)));
                            transMap.put("source", source2);
                            map.put("source", source2);
                        } else if (childNodes.item(k).getNodeName() == "author") {
                            String creator = childNodes.item(k).getFirstChild().getNodeValue();
                            log.info("本篇文章的作者是:{}",creator);
                            map.put("author", creator);
                            transMap.put("author", creator);
                        } else if (childNodes.item(k).getNodeName() == "pubDate") {
                            String pubDate = childNodes.item(k).getFirstChild().getNodeValue();
                            pubDate = DataUtil.changeData(pubDate);
                            log.info("本篇文章的更新时间为:{}",pubDate);
                            map.put("issue_time", pubDate);
                            transMap.put("issue_time", pubDate);
                        } else if (childNodes.item(k).getNodeName() == "description") {
                            String content = childNodes.item(k).getFirstChild().getNodeValue();
                            map.put("abstract",content);
                            log.info("本篇文章翻译前的摘要为：{}",content);
                            mapAbstract.put("english", content);
                            String tramsAbstract = HttpFormBakUtil.doPost("https://www.yushanshuju.com/ords/datatech/ofsi/baidutrans_en", mapAbstract, null, 10000);
                            transMap.put("abstract", RichTextHandle.getContent(JsonUtil.stringToJson(tramsAbstract)));
                            log.info("本篇文章翻译后的摘要为：{}",JsonUtil.stringToJson(tramsAbstract));
                        } else if (childNodes.item(k).getNodeName() == "content:encoded") {
                            String content = childNodes.item(k).getFirstChild().getNodeValue();
                            map.put("content",content);
                            System.out.println("翻译前正文为："+content);
                            log.info("本篇文章翻译前正文是：{}",content);
                            content = content.trim().replaceAll("\\s*|\t|\r|\n", "");
                            String[] split = content.split("<p>");
                            String contentTramsAll = "";
                            for (int j =1;j<split.length;j++){
                                mapContent.put("english","<p>"+split[j]);
                                String tramsContent = HttpFormBakUtil.doPost("https://www.yushanshuju.com/ords/datatech/ofsi/baidutrans_en", mapContent, null, 10000);
                                contentTramsAll += JsonUtil.stringToJson(tramsContent);
                            }
                            transMap.put("content", contentTramsAll);
                            log.info("本篇文章翻译后正文为：{}",contentTramsAll);
                        }
                    }
                }
                map.put("page_type", "行业智库");
                transMap.put("page_type", "行业智库");
                log.info("翻译前插入的整篇文章的map集合为：{}",map);
                log.info("翻译之后插入的整篇文章的tramsMap集合为：{}",transMap);
                String DoPost = HttpFormBakUtil.doPost("https://www.yushanshuju.com/ords/datatech/ofsi/add_news", map, null, 10000);
                Thread.sleep(5000);
                String TramsDoPost = HttpFormBakUtil.doPost("https://www.yushanshuju.com/ords/datatech/ofsi/add_news", transMap, null, 10000);
                Thread.sleep(5000);
                log.info("翻译前文章插入结果为：{}",DoPost);
                log.info("翻译后文章插入结果为：{}",TramsDoPost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}