package com.tdtu.myapplication.model.certificate;

public class Certificate {
    private String certificateId;  // Unique identifier for the certificate
    private String studentId;      // Reference to the student who owns this certificate
    private String title;          // Title of the certificate
    private String issuingOrganization; // Organization that issued the certificate
    private String issueDate;      // Date when the certificate was issued
    private String description;    // Optional description of the certificate

    // Empty constructor required for Firestore's automatic data mapping
    public Certificate() {
    }

    // Constructor with parameters
    public Certificate(String certificateId, String studentId, String title, String issuingOrganization, String issueDate, String description) {
        this.certificateId = certificateId;
        this.studentId = studentId;
        this.title = title;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
        this.description = description;
    }

    // Getters and Setters
    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssuingOrganization() {
        return issuingOrganization;
    }

    public void setIssuingOrganization(String issuingOrganization) {
        this.issuingOrganization = issuingOrganization;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    // Additional methods as needed
}

