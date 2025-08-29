package com.inhatc.localit.api.naver;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 네이버 뉴스 API의 응답을 담는 최상위 클래스입니다.
 * 실제 뉴스 아이템 목록을 가지고 있습니다.
 */
public class NaverNewsResponse {

    // JSON 데이터에서 "items"라는 이름의 배열을 이 List에 매핑합니다.
    @SerializedName("items")
    private List<Item> items;

    // 외부에서 뉴스 아이템 목록을 가져갈 수 있도록 하는 메서드입니다.
    public List<Item> getItems() {
        return items;
    }

    /**
     * 개별 뉴스 아이템 하나를 나타내는 클래스입니다.
     * 제목, 원문 링크, 요약 내용 등의 정보를 가집니다.
     */
    public static class Item {

        // JSON의 "title"을 이 변수에 매핑
        @SerializedName("title")
        private String title;

        // JSON의 "originallink"를 이 변수에 매핑
        @SerializedName("originallink")
        private String originalLink;

        // JSON의 "link"를 이 변수에 매핑 (네이버 뉴스 링크)
        @SerializedName("link")
        private String link;

        // JSON의 "description"을 이 변수에 매핑
        @SerializedName("description")
        private String description;

        // JSON의 "pubDate"를 이 변수에 매핑 (발행일)
        @SerializedName("pubDate")
        private String pubDate;

        // --- 외부에서 각 데이터를 가져가기 위한 Getter 메서드들 ---
        public String getTitle() {
            return title;
        }

        public String getOriginalLink() {
            return originalLink;
        }

        public String getLink() {
            return link;
        }

        public String getDescription() {
            return description;
        }

        public String getPubDate() {
            return pubDate;
        }
    }
}