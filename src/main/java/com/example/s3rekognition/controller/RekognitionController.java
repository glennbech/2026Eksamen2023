package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {

    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;
    private Integer headViolations = 0;
    private Integer handViolations = 0;
    private Integer faceViolations = 0;
    private Integer imagesScanned = 0;
    private Map<String, Integer> bodyPartViolations;
    private final MeterRegistry meterRegistry;
    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());
    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        initializeViolations();
        this.meterRegistry = meterRegistry;
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
    }

    private void initializeViolations() {
        bodyPartViolations = new HashMap<>(){{
            put("PEOPLE", 0);
            put("FACE", 0);
            put("HEAD", 0);
            put("LEFT_HAND", 0);
            put("RIGHT_HAND", 0);
        }};
    }

    private double getViolations(String key) {
        return bodyPartViolations.getOrDefault(key, 0);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Face Protective Gear Violations.
     * Then gathers images scanned, together with the amount of images
     * that contain a violation.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @Timed(value = "scanning_ppe_face_latency", description = "latency of scanning images for face ppe")
    @GetMapping(value = "/scan-ppe-face", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForFacePPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();
        int violations = 0;
        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result, "FACE");
            if (violation) violations++;

            logger.info("scanning " + image.getKey() + "for face cover violation, result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }
        imagesScanned = images.size();
        faceViolations = violations;

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Headgear Violations.
     * Then gathers images scanned, together with the amount of images
     * that contain a violation.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @Timed(value = "scanning_ppe_head_latency", description = "latency of scanning images for head ppe")
    @GetMapping(value = "/scan-ppe-head", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForHeadPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();
        int violations = 0;
        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("HEAD_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result, "HEAD");
            if (violation) violations++;

            logger.info("scanning " + image.getKey() + "for head cover violation, result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }
        imagesScanned = images.size();
        headViolations = violations;

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Hand Violations.
     * Then gathers images scanned, together with the amount of images
     * that contain a violation.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @Timed(value = "scanning_ppe_hands_latency", description = "latency of scanning images for hand ppe")
    @GetMapping(value = "/scan-ppe-hand", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForHandPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();
        int violations = 0;
        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("HAND_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result, "LEFT_HAND");
            if (!violation) {
                violation = isViolation(result, "RIGHT_HAND");
            }
            if (violation) violations++;

            logger.info("scanning " + image.getKey() + "for hand cover violation, result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }
        imagesScanned = images.size();
        handViolations = violations;

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Head, face and hand protective Gear Violations.
     * Any body part in each image that registers a violation, will be
     * saved in a HashMap with keys corresponding each body part,
     * and values being the amount of found violations for that particular
     * body part.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @Timed(value = "scanning_ppe_latency", description = "latency of scanning all images for all sorts of PPE")
    @GetMapping(value = "/scan-ppe-all", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForAllPPE(@RequestParam String bucketName) {

        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        List<String> equipmentTypes = List.of("HEAD_COVER", "FACE_COVER", "HAND_COVER");
        initializeViolations();
        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes(equipmentTypes));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // Figure out what body parts are missing ppe
            for (String scannedBodyPart : bodyPartViolations.keySet()) {
                logger.info("body part " + scannedBodyPart);
                result.getPersons().forEach(person -> {
                    person.getBodyParts().forEach(bodyPart -> {
                        if (bodyPart.getName().equals(scannedBodyPart) && bodyPart.getEquipmentDetections().isEmpty()) {
                            // Update the count in the map
                            bodyPartViolations.put(scannedBodyPart, bodyPartViolations.get(scannedBodyPart)+1);
                        }
                    });
                });
            }

            logger.info("scanning " + image.getKey() + " for overall PPE violations");
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            bodyPartViolations.put("PEOPLE", bodyPartViolations.get("PEOPLE")+result.getPersons().size());
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount);
            classificationResponses.add(classification);
        }
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses, bodyPartViolations);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * Detects if the image has a protective gear violation for the FACE body part-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result, String bodyParts) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals(bodyParts)
                        && bodyPart.getEquipmentDetections().isEmpty());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        Gauge.builder("head_violations", this, value -> {
            double images = imagesScanned;
            double violations = headViolations;

            return (images > 0) ? (violations / images) * 100 : 0;
        }).register(meterRegistry);

        Gauge.builder("hand_violations", this, value -> {
            double images = imagesScanned;
            double violations = handViolations;

            return (images > 0) ? (violations / images) * 100 : 0;
        }).register(meterRegistry);

        Gauge.builder("face_violations", this, value -> {
            double images = imagesScanned;
            double violations = faceViolations;

            return (images > 0) ? (violations / images) * 100 : 0;
        }).register(meterRegistry);

        Gauge.builder("people_scanned", this, controller -> controller.getViolations("PEOPLE"))
                .description("Count of amount of people scanned")
                .register(meterRegistry);

        Gauge.builder("head", this, controller -> controller.getViolations("HEAD"))
                .description("Count of amount of head covers missing")
                .register(meterRegistry);

        Gauge.builder("face", this, controller -> controller.getViolations("FACE"))
                .description("Count of amount of face covers missing")
                .register(meterRegistry);

        Gauge.builder("left_hand", this, controller -> controller.getViolations("LEFT_HAND"))
                .description("Count of amount of left hand covers missing")
                .register(meterRegistry);

        Gauge.builder("right_hand", this, controller -> controller.getViolations("RIGHT_HAND"))
                .description("Count of amount of right hand covers missing")
                .register(meterRegistry);
    }
}
