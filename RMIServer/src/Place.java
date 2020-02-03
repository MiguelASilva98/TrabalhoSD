import java.io.Serializable;

public class Place implements Serializable {
    private String postalCode;
    private String locality;
    private static final long serialVersionUID = 7588980448693010399L;

    public String getPostalCode() {
        return postalCode;
    }

    public String getLocality() {
        return locality;
    }

    public Place(String postalCode, String locality) {
        this.postalCode = postalCode;
        this.locality = locality;
    }

    @Override
    public String toString() {
        return postalCode + " - " + locality;
    }

    @Override
    public boolean equals(Object obj) {
        return this.postalCode.equals(((Place)obj).postalCode);
    }
}