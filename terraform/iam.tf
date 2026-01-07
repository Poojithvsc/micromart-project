# =============================================================================
# MicroMart - IAM Roles and Policies
# =============================================================================
# Creates IAM roles for EC2 instances with least-privilege access.
#
# Roles:
# - EC2 Instance Role: S3 access, SSM, CloudWatch
#
# Learning Points:
# - IAM roles vs IAM users
# - Instance profiles for EC2
# - Policy documents with conditions
# - Principle of least privilege
# =============================================================================

# -----------------------------------------------------------------------------
# EC2 Instance Role
# -----------------------------------------------------------------------------
resource "aws_iam_role" "ec2_role" {
  name        = "${local.name_prefix}-ec2-role"
  description = "IAM role for MicroMart EC2 Docker host"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-ec2-role"
  })
}

# -----------------------------------------------------------------------------
# EC2 Instance Profile
# -----------------------------------------------------------------------------
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${local.name_prefix}-ec2-profile"
  role = aws_iam_role.ec2_role.name

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-ec2-profile"
  })
}

# -----------------------------------------------------------------------------
# S3 Access Policy
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy" "ec2_s3_access" {
  name = "${local.name_prefix}-ec2-s3-access"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "S3BucketAccess"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket",
          "s3:GetBucketLocation"
        ]
        Resource = [
          aws_s3_bucket.product_images.arn,
          "${aws_s3_bucket.product_images.arn}/*"
        ]
      },
      {
        Sid    = "S3PresignedUrls"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ]
        Resource = "${aws_s3_bucket.product_images.arn}/*"
        Condition = {
          StringEquals = {
            "s3:authType" = "REST-HEADER"
          }
        }
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# CloudWatch Logs Policy
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy" "ec2_cloudwatch_logs" {
  name = "${local.name_prefix}-ec2-cloudwatch-logs"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "CloudWatchLogs"
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:DescribeLogStreams",
          "logs:DescribeLogGroups"
        ]
        Resource = [
          "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/ec2/${var.project_name}/*",
          "arn:aws:logs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/ec2/${var.project_name}/*:log-stream:*"
        ]
      },
      {
        Sid    = "CloudWatchMetrics"
        Effect = "Allow"
        Action = [
          "cloudwatch:PutMetricData"
        ]
        Resource = "*"
        Condition = {
          StringEquals = {
            "cloudwatch:namespace" = "MicroMart/EC2"
          }
        }
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# SSM Session Manager Policy (Alternative to SSH)
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# -----------------------------------------------------------------------------
# Secrets Manager Access (for RDS credentials)
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy" "ec2_secrets_manager" {
  name = "${local.name_prefix}-ec2-secrets-manager"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "SecretsManagerAccess"
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = aws_secretsmanager_secret.rds_credentials.arn
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# ECR Access (for pulling Docker images)
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy" "ec2_ecr_access" {
  name = "${local.name_prefix}-ec2-ecr-access"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ECRAuth"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Sid    = "ECRPull"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "arn:aws:ecr:${var.aws_region}:${data.aws_caller_identity.current.account_id}:repository/${var.project_name}/*"
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# EC2 Describe (for metadata and tags)
# -----------------------------------------------------------------------------
resource "aws_iam_role_policy" "ec2_describe" {
  name = "${local.name_prefix}-ec2-describe"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "EC2Describe"
        Effect = "Allow"
        Action = [
          "ec2:DescribeTags",
          "ec2:DescribeInstances"
        ]
        Resource = "*"
      }
    ]
  })
}
