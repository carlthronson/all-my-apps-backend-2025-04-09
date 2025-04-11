package personal.carl.thronson.http;

import java.net.URI;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import personal.carl.thronson.jobsearch.data.entity.JobSearchJobListingEntity;

public class JobSummaryReader extends DocReader<JobSearchJobListingEntity> {

    @Override
    public List<JobSearchJobListingEntity> readDoc(Document doc) {
        List<JobSearchJobListingEntity> jobs = new ArrayList<JobSearchJobListingEntity>();
        Elements elements = doc.select("li");
        for (Element element : elements) {
            try {
                JobSearchJobListingEntity job = buildJob(element);
//                System.out.println(job);
//                if (isBadLocation(job.getLocation())) {
////                  System.out.println("bad location: " + job.getLocation());
//                    continue;
//                }
//                getDetails(job);
//                System.out.println(job.getPublishedAt());
                jobs.add(job);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return jobs;
    }

    private static List<String> list = new ArrayList<>();
    static {
        list.add("San Francisco, CA");
        list.add("Los Angeles, CA");
        list.add("San Diego, CA");
        list.add("Sacramento, CA");
        list.add("Irvine, CA");
        list.add("Carlsbad, CA");
        list.add("Pleasanton, CA");
        list.add("San Leandro, CA");
        list.add("Oakland, CA");
        list.add("Torrance, CA");
        list.add("Santa Ana, CA");
        list.add("Newport Beach, CA");
        list.add("Costa Mesa, CA");
        list.add("El Segundo, CA");
        list.add("Pasadena, CA");
        list.add("Long Beach, CA");
        list.add("Lake Forest, CA");
        list.add("San Bernardino, CA");
        list.add("Santa Monica, CA");
        list.add("Dublin, CA");
        list.add("Anaheim, CA");
        list.add("San Clemente, CA");
    }

    public static boolean isBadLocation(String location) {
        return (!location.endsWith(", CA")) || list.contains(location);
    }

    public static void getDetails(JobSearchJobListingEntity job) throws Exception {
        try {
            URI uri = new URI(job.getLinkedinurl());
            System.out.println(uri);
//        if (true) return;
            URL url = HttpUtils.connect(uri);
            Document doc = Jsoup.parse(url, 5000);
            Elements elements = doc.select("body");
            for (Element element : elements) {
                Elements children = element
                        .select("section.core-rail div.salary");
                for (Element child : children) {
                    job.setSalary(child.text());
                }
            }
        } catch (Exception ex) {
//          ex.printStackTrace();
        }
    }

    private JobSearchJobListingEntity buildJob(Element element) throws Exception {
        JobSearchJobListingEntity job = new JobSearchJobListingEntity();

        Elements divs = element.select("a.base-card__full-link");
        Element propertyElement = divs.first();
        String href = propertyElement.attr("href");
        // Has side effect of setting linkedinid
        job.setLinkedinurl(href);
        
        URI uri = new URI(href);
        String path = uri.getPath(); // This will drop off the query parameters
        String jobId = path.substring(path.lastIndexOf("-") + 1);
        Long linkedinid = Long.valueOf(jobId);
        job.setLinkedinid(linkedinid);

        Elements elements = element.select("h3.base-search-card__title");
        String text = elements.first().text();
        job.setName(text);

        elements = element.select("h4.base-search-card__subtitle");
        text = elements.first().text();
        job.setCompanyName(text);

        elements = element.select("span.job-search-card__location");
        text = elements.first().text();
        job.setLocation(text);

        elements = element.select("time");
        text = elements.first().text();
//        System.out.println(text);
//        job.setPublishedat(text);

        OffsetDateTime publishedAt = TimestampParser.extracted(text);
        job.setPublishedAt(publishedAt);
//        System.out.println(job);
        return job;
    }

    public void addDetails(JobSearchJobListingEntity job) throws Exception {
        URI uri = new URI(job.getLinkedinurl());
        URL url = HttpUtils.connect(uri);
        Document doc = Jsoup.parse(url, 5000);
        Elements elements = doc.select(".job-details-jobs-unified-top-card__job-insight");
        for (Element selected : elements) {
//            System.out.println(selected.text());
        }
    }

//    private void getTime(Element element, Properties properties) {
//        String propertyName = "publishedAt";
//        String selector = "time.job-search-card__listdate";
//        selector = "time";
//        try {
//            Elements divs = element.select(selector);
//            Element propertyElement = divs.first();
//            String propertyValue = propertyElement.attr("datetime");
//            properties.setProperty(propertyName, propertyValue);
//        } catch (Exception e) {
//            System.out.println("Could not find property: " + propertyName);
//        }
//    }

//    private String getTitle(Document doc, Properties properties) {
//        Elements elements = doc.select("h1");
//        System.out.println(elements.size());
//        for (Element element : elements) {
//            properties.setProperty("title", element.text());
//            return element.text();
//        }
//        return null;
//    }

//    private String getH4s(Document doc, Properties properties) {
//        Elements elements = doc.select("h4 span");
//        System.out.println(elements.size());
//        int index = 0;
//        properties.setProperty("companyName", elements.get(index++).text());
//        properties.setProperty("location", elements.get(index++).text());
//        properties.setProperty("postedTime", elements.get(index++).text());
//        properties.setProperty("applicationsCount", elements.get(index++).text());
//        return null;
//    }
}
