package com.cookbook;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class userId {
  private SimpleStringProperty sun;
  private SimpleStringProperty mon;
  private SimpleStringProperty tue;
  private SimpleStringProperty wed;
  private SimpleStringProperty thu;
  private SimpleStringProperty fri;
  private SimpleStringProperty sat;

  userId() {
    this.sun = new SimpleStringProperty("");
    this.mon = new SimpleStringProperty("");
    this.tue = new SimpleStringProperty("");
    this.wed = new SimpleStringProperty("");
    this.thu = new SimpleStringProperty("");
    this.fri = new SimpleStringProperty("");
    this.sat = new SimpleStringProperty("");
  }

  userId(String sun, String mon, String tue, String wed, String thu, String fri, String sat) {
    this.sun = new SimpleStringProperty(sun);
    this.mon = new SimpleStringProperty(mon);
    this.tue = new SimpleStringProperty(tue);
    this.wed = new SimpleStringProperty(wed);
    this.thu = new SimpleStringProperty(thu);
    this.fri = new SimpleStringProperty(fri);
    this.sat = new SimpleStringProperty(sat);
  }

  public void setsun(String sun) {
    this.sun.set(sun);
  }

  public String getsun() {
    return sun.get();
  }

  public void setmon(String mon) {
    this.mon.set(mon);
  }

  public String getmon() {
    return mon.get();
  }

  public void settue(String tue) {
    this.tue.set(tue);
  }

  public String gettue() {
    return tue.get();
  }

  public void setwed(String wed) {
    this.wed.set(wed);
  }

  public String getwed() {
    return wed.get();
  }

  public void setthu(String thu) {
    this.thu.set(thu);
  }

  public String getthu() {
    return thu.get();
  }

  public void setfri(String fri) {
    this.fri.set(fri);
  }

  public String getfri() {
    return fri.get();
  }

  public void setsat(String sat) {
    this.sat.set(sat);
  }

  public String getsat() {
    return sat.get();
  }

}
