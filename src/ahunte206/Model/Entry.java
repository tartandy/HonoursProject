package ahunte206.Model;

public class Entry implements Comparable<Entry>{
    private String key;
    private Double value;

    public Entry(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    //compares two values together and returns the result
    public int compareTo(Entry o) {
        return this.getValue().compareTo(o.getValue());
    }
}
