package test;

public class TestPresenceField {

    private Boolean $FIELD_INTEGER_PRESENCE = false;

    private Boolean $FIELD_STRING_PRESENCE = false;

    private Integer fieldInteger;

    private String fieldString;

    public TestPresenceField() {

    }

    public Integer getFieldInteger() {

        return this.fieldInteger;
    }

    public void setFieldInteger(Integer fieldInteger) {

        this.$FIELD_INTEGER_PRESENCE = true;
        this.fieldInteger = fieldInteger;
    }

    public String getFieldString() {

        return this.fieldString;
    }

    public void setFieldString(String fieldString) {

        this.$FIELD_STRING_PRESENCE = true;
        this.fieldString = fieldString;
    }

    public Boolean hasFieldInteger() {

        return this.$FIELD_INTEGER_PRESENCE;
    }

    public Boolean hasFieldString() {

        return this.$FIELD_STRING_PRESENCE;
    }
}
