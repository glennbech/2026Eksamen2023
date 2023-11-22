package com.example.s3rekognition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the response sent back from the Apprunner service
 *
 */
public class PPEResponse implements Serializable {

    String bucketName;
    List<PPEClassificationResponse> results;
    Map<String, Integer> bodyPartViolations;
    public PPEResponse() {
    }

    public PPEResponse(String bucketName, List<PPEClassificationResponse> results) {
        this.bucketName = bucketName;
        this.results = results;
    }

    public PPEResponse(String bucketName, List<PPEClassificationResponse> results, Map<String, Integer> bodyPartViolations) {
        this.bucketName = bucketName;
        this.results = results;
        this.bodyPartViolations = bodyPartViolations;
    }

    public Map<String, Integer> getBodyPartViolations() {
        return bodyPartViolations;
    }

    public void setBodyPartViolations(Map<String, Integer> bodyPartViolations) {
        this.bodyPartViolations = bodyPartViolations;
    }

    public String getBucketName() {
        return bucketName;
    }

    public List<PPEClassificationResponse> getResults() {
        return results;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setResults(List<PPEClassificationResponse> results) {
        this.results = results;
    }
}

