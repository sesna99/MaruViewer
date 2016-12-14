package ind.simsim.maruViewer.Service;

/**
 * Created by jack on 2016. 12. 8..
 */

public class ComicsData {
    private String title;
    private String image;
    private String imageName;
    private String link;

    public ComicsData(){
    }

    public ComicsData(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public ComicsData(String title, String image, String link) {
        this.title = title;
        this.image = image;
        this.link = link;
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
}
