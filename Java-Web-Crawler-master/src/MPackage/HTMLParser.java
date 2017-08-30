package MPackage;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import java.io.BufferedReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.regex.Pattern;
import static MPackage.JenkinsHash.*;
public class HTMLParser {
    private final static Pattern Filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
            + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf"
            + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    public static int getPR(String domain) {
        String result = "";
        long hash = JenkinsHash.hash(("info:" + domain).getBytes());
        String url = "http://toolbarqueries.google.com/tbr?client=navclient-auto&hl=en&"
                + "ch=6" + hash + "&ie=UTF-8&oe=UTF-8&features=Rank&q=info:" + domain;
        try {
            URLConnection conn = new URL(url).openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String input;
            while ((input = br.readLine()) != null) {
                result = input.substring(input.lastIndexOf(":") + 1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if ("".equals(result)) {
            return 0;
        } else {
            return Integer.valueOf(result);
        }

    }

    public static void getLinks(CrawlerQueue crawledSites, int t, int numberOfLinksToCrawl, String s) throws IOException {
        String url = crawledSites.getNextLink();
        Elements links = null;
        Document dc = null;
        Document doc = null;
        String lnk;
        int pageRank = 0;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(3000).get();
            putData1(url, doc.title());
            System.out.println("Thread: " + t +" URL: "+s + " added: " + url);
            crawledSites.addCrawledSites(url);
            crawledSites.incCount();
            links = doc.select("a[href]");
        } catch (Exception e) {
            links = null;
        } finally {
            if (links != null && crawledSites.getQueueLength() < (numberOfLinksToCrawl * 5)) {
                for (Element link : links) {
                    lnk = link.absUrl("href");
                    if (shouldVisit(lnk)) {
                        crawledSites.addListOfSites(lnk);
                    }
                }
            }
            links = null;
        }
    }

    public static String getDomain(String url) throws MalformedURLException {
        String cleanUrl = url.toLowerCase().trim();
        URL link = new URL(cleanUrl);
        String domain = link.getHost();
        System.out.println(domain);
        return domain;
    }
    public static boolean shouldVisit(String url) {
        String cleanUrl = url.toLowerCase().trim();
        boolean shouldVisit = true;
        if (url.contains("wikipedia")) {
            if (!url.contains("en.wiki")) {
                shouldVisit = false;
            }
        }
        if(url.contains("dmoz - world")){
            if(!url.contains("english")){
                shouldVisit = false;
            }
        }
        if (Filters.matcher(cleanUrl).matches()) {
            shouldVisit = false;
        }
        return shouldVisit;

    }
}
