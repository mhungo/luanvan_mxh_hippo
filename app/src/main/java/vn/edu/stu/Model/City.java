package vn.edu.stu.Model;

public class City {
    private String Type, SolrID, ID, Title, STT, Created, Updated, TotalDoanhNghiep;

    public City(String type, String solrID, String ID, String title, String STT, String created, String updated, String totalDoanhNghiep) {
        Type = type;
        SolrID = solrID;
        this.ID = ID;
        Title = title;
        this.STT = STT;
        Created = created;
        Updated = updated;
        TotalDoanhNghiep = totalDoanhNghiep;
    }

    public City() {
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getSolrID() {
        return SolrID;
    }

    public void setSolrID(String solrID) {
        SolrID = solrID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSTT() {
        return STT;
    }

    public void setSTT(String STT) {
        this.STT = STT;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        Created = created;
    }

    public String getUpdated() {
        return Updated;
    }

    public void setUpdated(String updated) {
        Updated = updated;
    }

    public String getTotalDoanhNghiep() {
        return TotalDoanhNghiep;
    }

    public void setTotalDoanhNghiep(String totalDoanhNghiep) {
        TotalDoanhNghiep = totalDoanhNghiep;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
