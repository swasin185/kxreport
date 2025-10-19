package com.keehin.kxreport;

public class Parameter {
    private String app;
    private String db;
    private String report;
    private String comCode;
    private String comName;
    private String fromDate;
    private String toDate;
    private String fromId;
    private String toId;
    private String option;
    private String idList;

    public String getApp() {
        return this.app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getDb() {
        return this.db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getReport() {
        return this.report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getComCode() {
        return this.comCode;
    }

    public void setComCode(String comCode) {
        this.comCode = comCode;
    }

    public String getComName() {
        return this.comName;
    }

    public void setComName(String comName) {
        this.comName = comName;
    }

    public String getFromDate() {
        if ("".equals(fromDate))
            return null;
        return this.fromDate;
    }

    public void setFromDate(String fromDate) {
        if (!"".equals(fromDate))
            this.fromDate = fromDate;
    }

    public String getToDate() {
        if ("".equals(toDate))
            return null;
        return this.toDate;
    }

    public void setToDate(String toDate) {
        if (!"".equals(toDate))
            this.toDate = toDate;
    }

    public String getFromId() {
        return this.fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return this.toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getOption() {
        return this.option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getIdList() {
        return this.idList;
    }

    public void setIdList(String idList) {
        this.idList = idList;
    }

}