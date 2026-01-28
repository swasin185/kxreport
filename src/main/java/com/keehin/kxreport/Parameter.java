package com.keehin.kxreport;

public class Parameter {
    private String app;
    private String db;
    private String report;
    private String comCode;
    private String comName;
    private String startDate;
    private String endDate;
    private String fromId;
    private String toId;
    private String option;
    private String idList;
    private String saveFile;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = blankToNull(app);
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = blankToNull(db);
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = blankToNull(report);
    }

    public String getComCode() {
        return comCode;
    }

    public void setComCode(String comCode) {
        this.comCode = blankToNull(comCode);
    }

    public String getComName() {
        return comName;
    }

    public void setComName(String comName) {
        this.comName = blankToNull(comName);
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = blankToNull(startDate);
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = blankToNull(endDate);
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = blankToNull(fromId);
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = blankToNull(toId);
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = blankToNull(option);
    }

    public String getIdList() {
        return idList;
    }

    public void setIdList(String idList) {
        this.idList = blankToNull(idList);
    }

    public String getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(String saveFile) {
        this.saveFile = blankToNull(saveFile);
    }

    // --- Helper method ---
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
