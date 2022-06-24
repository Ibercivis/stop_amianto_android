package com.ibercivis.stopamianto.clases;

public class Observacion {
    int id;
    int hasPhoto;
    String user;
    String date;
    Double latitude;
    Double longitude;
    String build;
    String quantity;
    String info;
    String foto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHasPhoto() {
        return hasPhoto;
    }

    public void setHasPhoto(int hasPhoto) {
        this.hasPhoto = hasPhoto;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }


    public Observacion(int id, int hasPhoto, String user, String date, Double latitude, Double longitude, String build, String quantity, String info, String foto) {
        this.id = id;
        this.hasPhoto = hasPhoto;
        this.user = user;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.build = build;
        this.quantity = quantity;
        this.info = info;
        this.foto = foto;
    }
}
