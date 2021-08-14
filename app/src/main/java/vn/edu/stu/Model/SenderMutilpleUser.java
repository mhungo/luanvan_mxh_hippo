package vn.edu.stu.Model;

import java.util.List;

public class SenderMutilpleUser {
    public Data data;
    public List<String> to;

    public SenderMutilpleUser(Data data, List<String> to) {
        this.data = data;
        this.to = to;
    }
}
