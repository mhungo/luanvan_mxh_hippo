package vn.edu.stu.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;

public class GetReviewUrl {
    public static Observable<Document> getJsoupContent(String url) {
        return Observable.fromCallable(() -> {
            try {
                Document document = Jsoup.connect(url).timeout(0)
                        .get();
                return document;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
