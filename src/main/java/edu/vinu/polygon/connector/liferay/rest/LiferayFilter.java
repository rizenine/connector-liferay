package edu.vinu.polygon.connector.liferay.rest;

/**
 * Created by Justin Stanczak
 */
public class LiferayFilter {
    public String byName;
    public String byUid;
    public String byEmailAddress;

    @Override
    public String toString() {
        return "LiferayFilter{" +
                "byName='" + byName + '\'' +
                ", byUid=" + byUid +
                ", byEmailAddress='" + byEmailAddress + '\'' +
                '}';
    }
}
