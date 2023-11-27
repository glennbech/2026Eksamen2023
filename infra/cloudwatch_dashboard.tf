resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.candidate
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "view": "gauge",
        "yAxis": {
          "left": {
            "min": 0,
            "max": 100
          }
        },
        "metrics": [
          ["${var.candidate}", "head_violations.value", { "label": "Head violations"}],
          ["${var.candidate}", "hand_violations.value", { "label": "Hand violations"}],
          ["${var.candidate}", "face_violations.value", { "label": "Face violations"}]
        ],
        "period": 30,
        "stat": "Maximum",
        "region": "${var.region}",
        "title": "Percentage of violations based on images scanned"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 16,
      "width": 14,
      "height": 8,
      "properties": {
        "metrics": [
          ["${var.candidate}",
            "scanning_ppe_latency.avg",
            "exception", "none",
            "method", "scanForAllPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController",
            { "label": "All PPE latency"}],
          ["${var.candidate}",
            "scanning_ppe_head_latency.avg",
            "exception", "none",
            "method", "scanForHeadPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController",
            { "label": "Head latency"}],
          ["${var.candidate}",
            "scanning_ppe_face_latency.avg",
            "exception", "none",
            "method", "scanForFacePPE",
            "class", "com.example.s3rekognition.controller.RekognitionController",
            { "label": "Face latency"}],
          ["${var.candidate}",
            "scanning_ppe_hands_latency.avg",
            "exception", "none",
            "method", "scanForHandPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController",
            { "label": "Hand latency"}]
        ],
        "period": 30,
        "stat": "Average",
        "region": "${var.region}",
        "title": "Average latency of requests per scanning methods"
      }
    },
    {
      "type": "metric",
      "x": 20,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "view": "bar",
        "stacked": false,
        "metrics": [
          ["${var.candidate}", "people_scanned.value", { "label": "People scanned in bucket"}],
          ["${var.candidate}", "face.value", { "label": "Faces without PPE"}],
          ["${var.candidate}", "left_hand.value", { "label": "Left hands without PPE"}],
          ["${var.candidate}", "right_hand.value", { "label": "Right hand without PPE"}],
          ["${var.candidate}", "head.value", { "label": "Heads without PPE"}]
        ],
        "stat": "Maximum",
        "period": 30,
        "region": "${var.region}",
        "title": "Individual number of deviations for each body part"
      }
    }
  ]
}
DASHBOARD
}
