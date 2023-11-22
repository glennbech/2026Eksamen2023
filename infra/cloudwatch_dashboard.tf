resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.prefix
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
        "metrics": [
          ["${var.prefix}", "deviations_face_cover.value"],
          ["${var.prefix}", "deviations_head_cover.value"],
          ["${var.prefix}", "deviations_hand_cover.value"]
        ],
        "period": 30,
        "stat": "Sum",
        "region": "eu-west-1",
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
        "view": "gauge",
        "yAxis": {
          "left": {
            "min": 0,
            "max": 20000
          }
        },
        "metrics": [
          ["${var.prefix}",
            "scanning_ppe_latency.avg",
            "exception", "none",
            "method", "scanForAllPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.prefix}",
            "scanning_ppe_head_latency.avg",
            "exception", "none",
            "method", "scanForHeadPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.prefix}",
            "scanning_ppe_face_latency.avg",
            "exception", "none",
            "method", "scanForFacePPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"],
          ["${var.prefix}",
            "scanning_ppe_hands_latency.avg",
            "exception", "none",
            "method", "scanForHandPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"]
        ],
        "period": 30,
        "stat": "Average",
        "region": "eu-west-1",
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
          ["${var.prefix}", "face.value"],
          ["${var.prefix}", "left_hand.value"],
          ["${var.prefix}", "right_hand.value"],
          ["${var.prefix}", "head.value"]
        ],
        "stat": "Maximum",
        "period": 30,
        "region": "eu-west-1",
        "title": "Maximum number of deviations for each bodypart"
      }
    }
  ]
}
DASHBOARD
}
