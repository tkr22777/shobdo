package objects;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Meaning {

    String id;
    int type;
    String value;
    String example;

    public Meaning(String id, int type, String value, String example) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.example = example;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    @Override
    public String toString() {
        return "Meaning{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                ", example='" + example + '\'' +
                '}';
    }
}