package com.android.labassist.endUser;

import com.android.labassist.ComplaintStatus;

public class UserComplaint {
    private String labName;
    private String pc;
    private String issue;
    private ComplaintStatus status;
    private long reportedDate;
    public UserComplaint(String labName, String pc, String issue, ComplaintStatus status, long reportedDate) {
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

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public long getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(long reportedDate) {
        this.reportedDate = reportedDate;
    }
}