package com.x.wallet.ui.data;

/**
 * Created by wuliang on 18-3-30.
 */

public class TokenItem {
    private int id;
    private String shortname;
    private String wholename;
    private String address;
    private long order;

    public TokenItem(int id, String shortname, String wholename, String address, long order) {
        this.id = id;
        this.shortname = shortname;
        this.wholename = wholename;
        this.address = address;
        this.order = order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getWholename() {
        return wholename;
    }

    public void setWholename(String wholename) {
        this.wholename = wholename;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "TokenItem{" +
                "id=" + id +
                ", shortname='" + shortname + '\'' +
                ", wholename='" + wholename + '\'' +
                ", address='" + address + '\'' +
                ", order=" + order +
                '}';
    }
}
