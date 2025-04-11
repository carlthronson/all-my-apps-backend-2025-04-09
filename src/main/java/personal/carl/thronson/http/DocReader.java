package personal.carl.thronson.http;

import java.util.List;

import org.jsoup.nodes.Document;

public abstract class DocReader<T> {

    public T readItem(T s) {
        return s;
    }

    public abstract List<T> readDoc(Document doc);

//    public getProperty(Element element, String selector) {
//        Elements selectedelements = element.select(selector);
//        Element propertyElement = selectedelements.first();
//        String propertyValue = propertyElement.text();
//        return propertyValue;
//    }

}
