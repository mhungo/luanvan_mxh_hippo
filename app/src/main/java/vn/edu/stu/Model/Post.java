package vn.edu.stu.Model;

public class Post {
    private String postid;
    private String postvideo;
    private String description;
    private String publisher;
    private String poststatus;
    private String postrules;
    private String posttype;

    public Post(String postid, String postvideo, String description, String publisher, String poststatus, String postrules, String posttype) {
        this.postid = postid;
        this.postvideo = postvideo;
        this.description = description;
        this.publisher = publisher;
        this.poststatus = poststatus;
        this.postrules = postrules;
        this.posttype = posttype;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostvideo() {
        return postvideo;
    }

    public void setPostvideo(String postvideo) {
        this.postvideo = postvideo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPoststatus() {
        return poststatus;
    }

    public void setPoststatus(String poststatus) {
        this.poststatus = poststatus;
    }

    public String getPostrules() {
        return postrules;
    }

    public void setPostrules(String postrules) {
        this.postrules = postrules;
    }

    public String getPosttype() {
        return posttype;
    }

    public void setPosttype(String posttype) {
        this.posttype = posttype;
    }
}
