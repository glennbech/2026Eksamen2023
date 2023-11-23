terraform {
  backend "s3" {
    bucket = "pgr301-2021-terraform-state"
    key    = "${var.candidate}-state/apprunner-actions.state"
    region = "eu-north-1"
  }
}