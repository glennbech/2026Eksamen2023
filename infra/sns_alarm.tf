resource "aws_cloudwatch_metric_alarm" "threshold" {
  alarm_name  = "${var.candidate}-threshold"
  namespace   = var.candidate
  metric_name = "head.value"

  comparison_operator = "GreaterThanThreshold"
  threshold           = "8"
  evaluation_periods  = "2"
  period              = "60"
  statistic           = "Maximum"

  alarm_description = "This alarm goes when deviations of people in the bucket scanned without head PPE exceeds 8"
  alarm_actions     = [aws_sns_topic.user_updates.arn]
}

resource "aws_sns_topic" "user_updates" {
  name = "${var.candidate}-alarm-topic"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  topic_arn = aws_sns_topic.user_updates.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

