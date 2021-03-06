package com.sgpublic.xml;

import com.sgpublic.xml.exception.SXMLException;
import com.sgpublic.xml.helper.StringMatcher;
import com.sgpublic.xml.helper.TagMatcher;
import com.sgpublic.xml.helper.TagParser;

import java.util.ArrayList;
import java.util.Map;

public class SXMLObject {
    private final String xmlString;
    private final String rootTag;
    private final String rootTagName;
    private final Map<String, String> attrs;
    private final boolean hasInnerData;

    /**
     * 创建一个 SXMLObject 对象
     *
     * @param xmlString XML数据文本
     * @throws SXMLException 若该 XML 的根节点不完整或不存在，则抛出 SXMLException。
     */
    public SXMLObject(String xmlString) throws SXMLException {
        if (!"".equals(xmlString)) {
            String xmlData = xmlString.replaceAll("\r|\n", "")
                    .replaceAll("<\\?(.*?)\\?>", "")
                    .replaceAll("\\u0020+", " ")
                    .replaceAll(">\\u0020*<", "><")
                    .replaceAll("\\u0020*/\\u0020*>", "/>");
            StringMatcher matcher;

            matcher = new StringMatcher("<(.*?)>", xmlData);
            if (matcher.find()) {
                rootTag = matcher.getFoundString();
                TagParser parser = new TagParser(rootTag);
                rootTagName = parser.getTagName();
                attrs = parser.getAttrMap();
                TagMatcher tagMatcher = matchTag(xmlData, rootTagName);
                if (tagMatcher.matches()) {
                    this.xmlString = xmlData;
                    hasInnerData = tagMatcher.getType() == TagMatcher.TYPE_NORMAL;
                    return;
                }
            }
        }
        throw new SXMLException(SXMLException.NOT_A_XML_DATA);
    }

    /**
     * 判断当前 XML 节点是否有内容
     *
     * @return 返回当前 XML 节点是否有子节点
     */
    public boolean hasInnerData() {
        return hasInnerData;
    }

    /**
     * 判断当前 XML 节点是否有指定标签名称的子节点。
     *
     * @param tagName 子节点标签名称
     * @return 返回当前 XML 节点是否有该标签名称的子节点。
     */
    public boolean isTagNull(String tagName){
        if (hasInnerData){
            StringMatcher matcher = new StringMatcher("<" + tagName, xmlString);
            return !matcher.find();
        } else {
            return true;
        }
    }

    /**
     * 获取当前节点中指定子节点标签名称的子节点。
     *
     * @param tagName 子节点标签名称
     * @return 获取到的 SXMLObject。
     * @throws SXMLException 若当前 XML 节点中没有该子节点或该子节点标签不完整，则抛出 SXMLException。
     */
    public SXMLObject getXMLObject(String tagName) throws SXMLException {
        if (hasInnerData) {
            String innerString = xmlString.replace(rootTag, "")
                    .replaceAll("</" + rootTagName + ">", "");
            TagMatcher tagMatcher = matchTag(innerString, tagName);
            if (tagMatcher.matches()) {
                return new SXMLObject(tagMatcher.getXmlData());
            } else {
                throw new SXMLException(SXMLException.TAG_NULL, tagName);
            }
        } else {
            throw new SXMLException(
                    SXMLException.INNER_UNAVAILABLE, rootTagName
            );
        }
    }

    /**
     * 获取节点标签中 String 类型的属性值。
     *
     * @param attrName 属性名称
     * @return 获取到的 String 类型属性值。
     * @throws SXMLException 若该节点中不存在该属性值，则抛出 SXMLException。
     */
    public String getStringAttr(String attrName) throws SXMLException {
        return getAttrValue(attrName);
    }

    /**
     * 获取节点标签中 boolean 类型的属性值。
     *
     * @param attrName 属性名称
     * @return 获取到的 boolean 类型属性值。
     * @throws SXMLException 若该节点中不存在该属性值，则抛出 SXMLException。
     */
    public boolean getBooleanAttr(String attrName) throws SXMLException {
        return Boolean.parseBoolean(getAttrValue(attrName));
    }

    /**
     * 获取节点标签中 int 类型的属性值。
     *
     * @param attrName 属性名称
     * @return 获取到的 int 类型属性值。
     * @throws SXMLException 若该节点中不存在该属性值，则抛出 SXMLException。
     */
    public int getIntAttr(String attrName) throws SXMLException {
        return Integer.parseInt(getAttrValue(attrName));
    }

    /**
     * 获取节点标签中 long 类型的属性值。
     *
     * @param attrName 属性名称
     * @return 获取到的 long 类型属性值。
     * @throws SXMLException 若该节点中不存在该属性值，则抛出 SXMLException。
     */
    public long getLongAttr(String attrName) throws SXMLException {
        return Long.parseLong(getAttrValue(attrName));
    }

    /**
     * 获取节点标签中 double 类型的属性值。
     *
     * @param attrName 属性名称
     * @return 获取到的 double 类型属性值。
     * @throws SXMLException 若该节点中不存在该属性值，则抛出 SXMLException。
     */
    public double getDoubleAttr(String attrName) throws SXMLException {
        return Double.parseDouble(getAttrValue(attrName));
    }

    /**
     * 若节点中包含多个名称相同的子节点，则可以将这些子节点创建为一个 SXMLArray 对象。
     *
     * @param tagName 子节点标签名称
     * @return 返回获取到的 SXMLArray 对象
     * @throws SXMLException 若当前 XML 节点中没有该子节点或该子节点标签不完整，则抛出 SXMLException。
     */
    public SXMLArray getXMLArray(String tagName) throws SXMLException {
        if (hasInnerData) {
            String innerString = xmlString.replace(rootTag, "")
                    .replaceAll("</" + rootTagName + ">", "");
            TagMatcher tagMatcher = matchTag(innerString, tagName);
            ArrayList<String> list = new ArrayList<>();
            if (tagMatcher.matches()) {
                while (tagMatcher.matches()){
                    list.add(tagMatcher.getXmlData());
                    innerString = innerString.replace(tagMatcher.getXmlData(), "");
                    if (innerString.length() > tagName.length() + 1){
                        tagMatcher = matchTag(innerString, tagName);
                    } else {
                        return new SXMLArray(list);
                    }
                }
                return new SXMLArray(list);
            } else {
                throw new SXMLException(SXMLException.TAG_NULL, tagName);
            }
        } else {
            throw new SXMLException(
                    SXMLException.INNER_UNAVAILABLE, rootTagName
            );
        }
    }

    /**
     * 返回经过 SXMLObject 格式化后的 XML 数据。
     *
     * @return 格式化后的 XML 数据
     */
    public String toString() {
        return xmlString;
    }

    /**
     * 判断当前 XML 节点的标签中是否有指定名称的属性值。
     *
     * @param attrName 指定名称的属性值
     * @return 返回当前 XML 节点的标签中是否有指定名称的属性值。
     */
    public boolean isAttrNull(String attrName){
        return attrs.get(attrName) == null;
    }

    /**
     * 从类内部获取到节点属性值的 String 类型
     *
     * @param attrName 属性名称
     * @return 返回
     * @throws SXMLException 若该节点中没有属性值或不存在指定的属性值，则抛出 SXMLException。
     */
    private String getAttrValue(String attrName) throws SXMLException {
        if (!attrs.isEmpty()){
            String attr = attrs.get(attrName);
            if (attr != null){
                return attr;
            } else {
                throw new SXMLException(SXMLException.ATTR_KEY_NULL);
            }
        } else {
            throw new SXMLException(SXMLException.ATTR_NULL);
        }
    }

    /**
     * 从指定的 XML 数据中按指定的子节点标签名称查找子标签
     *
     * @param xmlString 指定的 XML 数据
     * @param tagName 指定的子节点标签名称
     * @return 返回获取到的子节点信息
     * @throws SXMLException 若当前 XML 节点中没有该子节点或该子节点标签不完整，则抛出 SXMLException。
     */
    private TagMatcher matchTag(String xmlString, String tagName) throws SXMLException {
        StringMatcher matcher;
        String nowString = xmlString;
        String nowTag = new TagParser(xmlString).getTagName();
        int tagLength = nowTag.length();

        while (!tagName.equals(nowTag)){
            matcher = new StringMatcher("<" + nowTag + "(.*?)/([^\"]*?)>", nowString);
            if (matcher.find()){
                String tagRange = matcher.getFoundString();
                if (new StringMatcher("<", tagRange).find(1)){
                    matcher = new StringMatcher("</" + nowTag + ">", nowString);
                    if (matcher.find()){
                        nowString = nowString.substring(matcher.end());
                    } else {
                        throw new SXMLException(SXMLException.NOT_A_XML_DATA);
                    }
                } else {
                    nowString = nowString.substring(matcher.end());
                }
            } else {
                return new TagMatcher(false);
            }
            if (nowString.length() > tagLength + 1){
                nowTag = new TagParser(nowString).getTagName();
                tagLength = nowTag.length();
            } else {
                return new TagMatcher(false);
            }
        }
        matcher = new StringMatcher("<" + nowTag + "(.*?)/([^\"]*?)>", nowString);
        if (matcher.find()){
            String tagRange = matcher.getFoundString();
            if (new StringMatcher("<", tagRange).find(1)){
                matcher = new StringMatcher("</" + nowTag + ">", nowString);
                if (matcher.find()){
                    return new TagMatcher(
                            true,
                            TagMatcher.TYPE_NORMAL,
                            nowString.substring(0, matcher.end())
                    );
                } else {
                    throw new SXMLException(SXMLException.NOT_A_XML_DATA);
                }
            } else {
                return new TagMatcher(
                        true,
                        TagMatcher.TYPE_SIMPLIFY,
                        tagRange
                );
            }
        } else {
            return new TagMatcher(false);
        }
    }
}
