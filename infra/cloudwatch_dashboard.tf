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
          ["${var.candidate}", "percentage_deviations.value"]
        ],
        "period": 30,
        "stat": "Maximum",
        "region": "${var.region}",
        "title": "Total number of deviations"
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
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.candidate}",
            "scanning_ppe_head_latency.avg",
            "exception", "none",
            "method", "scanForHeadPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.candidate}",
            "scanning_ppe_face_latency.avg",
            "exception", "none",
            "method", "scanForFacePPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.candidate}",
            "scanning_ppe_hands_latency.avg",
            "exception", "none",
            "method", "scanForHandPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"]
        ],
        "period": 30,
        "stat": "Average",
        "region": "${var.region}",
        "title": "Average latency of requests"
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
          ["${var.candidate}", "people_scanned.value"],
          ["${var.candidate}", "face.value"],
          ["${var.candidate}", "left_hand.value"],
          ["${var.candidate}", "right_hand.value"],
          ["${var.candidate}", "head.value"]
        ],
        "stat": "Maximum",
        "period": 30,
        "region": "${var.region}",
        "title": "Maximum number of deviations for each bodypart"
      }
    }
  ]
}
DASHBOARD
}
