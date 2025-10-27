package com.android.labassist.endUser;

public class Complaint {
    private String labName;
    private String pc;
    private String issue;
    private String status;
    private String reportedDate;
    public Complaint(String labName, String pc, String issue, String status, String reportedDate) {
        this.labName = labName;
        this.pc = pc;
        this.issue = issue;
        this.status = status;
        this.reportedDate = reportedDate;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(String reportedDate) {
        this.reportedDate = reportedDate;
    }
}
