/*
  In the first set of code:

  aws_apprunner_service resource is creating an AWS App Runner service.
  It specifies the configuration for the underlying compute instances,
  including the IAM role that App Runner should assume when running those instances.
  The IAM role is created by the aws_iam_role resource
  named "2026-apprunner-role," and its assume role policy is
  defined by the data.aws_iam_policy_document.assume_role block.

  In the second set of code:

  data.aws_iam_policy_document.policy is defining an IAM policy
  document with broad permissions for Rekognition, S3, and CloudWatch.

  aws_iam_policy is creating an IAM policy named "kjell-apr-policy-thingy"
  using the policy document defined above.

  aws_iam_role_policy_attachment is attaching the IAM policy created above to the IAM role "2026-apprunner-role."
  This attachment links the policy's permissions to the IAM role, which
  means that the permissions defined in the policy are granted to the IAM role.
*/

resource "aws_apprunner_service" "service" {
  service_name = var.candidate
  /*
    In summary, the instance_configuration block is specifying the IAM role
    that App Runner should assume when running the underlying
    compute instances for our service.
    This IAM role is defined by the aws_iam_role.role_for_apprunner_service resource.
  */
  instance_configuration {
    cpu    = 256
    memory = 1024
    instance_role_arn = aws_iam_role.role_for_apprunner_service.arn
  }

  source_configuration {
    authentication_configuration {
      access_role_arn = "arn:aws:iam::244530008913:role/service-role/AppRunnerECRAccessRole"
    }
    image_repository {
      image_configuration {
        port = "8080"
      }
      image_identifier      = var.image
      image_repository_type = "ECR"
    }
    auto_deployments_enabled = true
  }
}
/*
  Declare a new AWS IAM role resource with the name 'role_for_apprunner_service'
  In Terraform, aws_iam_role is a resource type provided by the AWS provider,
  and we are assigning it the name
    'role_for_apprunner_service'
  for reference within our Terraform configuration.
  Give it an appropriate name that makes sense within our code
*/
resource "aws_iam_role" "role_for_apprunner_service" {
  name               = "${var.candidate}-apprunner-role"
  assume_role_policy = data.aws_iam_policy_document.assume_role.json
}

/*
  Grants permission for App Runner to assume this IAM role.
*/
data "aws_iam_policy_document" "assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["tasks.apprunner.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "policy" {
  statement {
    effect    = "Allow"
    actions   = ["rekognition:*"]
    resources = ["*"]
  }
  
  statement  {
    effect    = "Allow"
    actions   = ["s3:*"]
    resources = ["*"]
  }

  statement  {
    effect    = "Allow"
    actions   = ["cloudwatch:*"]
    resources = ["*"]
  }
}

resource "aws_iam_policy" "policy" {
  name        = "${var.candidate}-apprunner-policy"
  description = "Policy for apprunner instance"
  policy      = data.aws_iam_policy_document.policy.json
}

resource "aws_iam_role_policy_attachment" "attachment" {
  role       = aws_iam_role.role_for_apprunner_service.name
  policy_arn = aws_iam_policy.policy.arn
}
