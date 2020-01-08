package test;

import com.collectz.qlombok.PresenceCheck;

@PresenceCheck
public class TestPresenceField {

    private Integer fieldInteger;

    private String fieldString;

    public Integer getFieldInteger() {

        return fieldInteger;
    }

    public void setFieldInteger(Integer fieldInteger) {

        this.fieldInteger = fieldInteger;
    }

    public String getFieldString() {

        return fieldString;
    }

    public void setFieldString(String fieldString) {

        this.fieldString = fieldString;
    }
}
