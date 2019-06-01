package com.mnevent.hz.bean;

import java.util.List;

/**
 * Created by zyand on 2019/1/8.
 */

public class PriceDatailsBean {


    /**
     * code : 2
     * good : {"images":[{"image":"http://lbx.freshtribes.com/goodsimageAR/10021_1.jpg"}],"texts":[{"text":"19,99 USD"},{"text":"Ons"},{"text":"Blue"},{"text":"52-20-138"},{"text":"Blue color, TAC Polarized, UV400"},{"text":"Men and women"},{"text":"Ons classic TR 90 sunglasses with a TAC polarized grey lenses. Comes in a hard case with a cleaning cloth. Very light, strong  a"}],"titles":[{"title":"Price"},{"title":"Brand"},{"title":"Color"},{"title":"Size"},{"title":"Lenses color and material"},{"title":"Suitable for"},{"title":"Text description and pictures"}]}
     * message : Success(Chinese成功)
     */

    private String code;
    private GoodBean good;
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GoodBean getGood() {
        return good;
    }

    public void setGood(GoodBean good) {
        this.good = good;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class GoodBean {
        private List<ImagesBean> images;
        private List<TextsBean> texts;
        private List<TitlesBean> titles;

        public List<ImagesBean> getImages() {
            return images;
        }

        public void setImages(List<ImagesBean> images) {
            this.images = images;
        }

        public List<TextsBean> getTexts() {
            return texts;
        }

        public void setTexts(List<TextsBean> texts) {
            this.texts = texts;
        }

        public List<TitlesBean> getTitles() {
            return titles;
        }

        public void setTitles(List<TitlesBean> titles) {
            this.titles = titles;
        }

        public static class ImagesBean {
            /**
             * image : http://lbx.freshtribes.com/goodsimageAR/10021_1.jpg
             */

            private String image;

            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
            }
        }

        public static class TextsBean {
            /**
             * text : 19,99 USD
             */

            private String text;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }
        }

        public static class TitlesBean {
            /**
             * title : Price
             */

            private String title;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }
    }
}
