package com.bachlinh.order.mail.model;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.apache.http.HttpStatus;

public class GmailSendingResult {
    private int statusCode;
    private String detail;

    public GmailSendingResult(GoogleJsonResponseException exception) {
        this.statusCode = exception.getStatusCode();
        this.detail = exception.getMessage();
    }

    public GmailSendingResult() {
        this.statusCode = HttpStatus.SC_OK;
        this.detail = "Success";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
