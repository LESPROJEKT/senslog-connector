package org.senslog.lite.rest.bean;

/**
 * Bean that represents Exception message
 * @author mkepka
 *
 */
public class ExceptionBean {
    private String type;
    private String message;

    /**
     * Empty constructor
     */
    public ExceptionBean(){
    }

    /**
     * Main constructor
     * @param type
     * @param message
     */
    public ExceptionBean(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{\"type\":\"" + type + "\", \"message\":\"" + message + "\"}";
    }
}