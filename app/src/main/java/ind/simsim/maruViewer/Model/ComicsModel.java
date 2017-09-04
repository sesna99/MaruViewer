package ind.simsim.maruViewer.Model;

/**
 * Created by jack on 2016. 12. 8..
 */

public class ComicsModel {
    private String title;
    private String image;
    private String imageName;
    private String link;
    private String comicsUrl;
    private String episodeUrl;

    public ComicsModel(){
    }

    public ComicsModel(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public ComicsModel(String title, String image, String link) {
        this.title = title;
        this.image = image;
        this.link = link;
    }

    public ComicsModel(String title, String image, String comicsUrl, String episodeUrl) {
        this.title = title;
        this.image = image;
        this.comicsUrl = comicsUrl;
        this.episodeUrl = episodeUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getComicsUrl() {
        return comicsUrl;
    }

    public void setComicsUrl(String comicsUrl) {
        this.comicsUrl = comicsUrl;
    }

    public String getEpisodeUrl() {
        return episodeUrl;
    }

    public void setEpisodeUrl(String episodeUrl) {
        this.episodeUrl = episodeUrl;
    }
}
