# =============================================================================
# MicroMart - S3 Bucket for Product Images
# =============================================================================
# Creates an S3 bucket for storing product images with:
# - Versioning enabled
# - Server-side encryption
# - Lifecycle policies
# - CORS configuration for direct uploads
# - Block public access (secure by default)
#
# Learning Points:
# - S3 bucket naming (globally unique)
# - Server-side encryption options
# - Lifecycle policies for cost management
# - CORS for browser uploads
# - Block public access settings
# =============================================================================

# -----------------------------------------------------------------------------
# Generate unique bucket name if not provided
# -----------------------------------------------------------------------------
locals {
  s3_bucket_name = var.s3_bucket_name != "" ? var.s3_bucket_name : "${local.name_prefix}-product-images-${data.aws_caller_identity.current.account_id}"
}

# -----------------------------------------------------------------------------
# S3 Bucket
# -----------------------------------------------------------------------------
resource "aws_s3_bucket" "product_images" {
  bucket = local.s3_bucket_name

  tags = merge(local.common_tags, {
    Name    = local.s3_bucket_name
    Purpose = "Product images storage"
  })
}

# -----------------------------------------------------------------------------
# Bucket Versioning
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_versioning" "product_images" {
  bucket = aws_s3_bucket.product_images.id

  versioning_configuration {
    status = var.s3_enable_versioning ? "Enabled" : "Disabled"
  }
}

# -----------------------------------------------------------------------------
# Server-Side Encryption
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_server_side_encryption_configuration" "product_images" {
  bucket = aws_s3_bucket.product_images.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"  # Use KMS for additional security
    }
    bucket_key_enabled = true
  }
}

# -----------------------------------------------------------------------------
# Block Public Access (Security Best Practice)
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_public_access_block" "product_images" {
  bucket = aws_s3_bucket.product_images.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# -----------------------------------------------------------------------------
# CORS Configuration (for direct browser uploads)
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_cors_configuration" "product_images" {
  bucket = aws_s3_bucket.product_images.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST"]
    allowed_origins = ["*"]  # Restrict to your domain in production
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

# -----------------------------------------------------------------------------
# Lifecycle Rules (Cost Management)
# -----------------------------------------------------------------------------
resource "aws_s3_bucket_lifecycle_configuration" "product_images" {
  count  = var.s3_lifecycle_expiration_days > 0 ? 1 : 0
  bucket = aws_s3_bucket.product_images.id

  rule {
    id     = "expire-old-versions"
    status = "Enabled"

    # Delete old versions after specified days
    noncurrent_version_expiration {
      noncurrent_days = var.s3_lifecycle_expiration_days
    }

    # Transition to cheaper storage class after 30 days
    noncurrent_version_transition {
      noncurrent_days = 30
      storage_class   = "STANDARD_IA"
    }

    # Delete incomplete multipart uploads after 7 days
    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }

  rule {
    id     = "transition-to-ia"
    status = "Enabled"

    filter {
      prefix = "products/"
    }

    # Move to Infrequent Access after 90 days
    transition {
      days          = 90
      storage_class = "STANDARD_IA"
    }

    # Move to Glacier after 365 days
    transition {
      days          = 365
      storage_class = "GLACIER"
    }
  }
}

# -----------------------------------------------------------------------------
# Bucket Policy (Optional - for CloudFront or specific access patterns)
# -----------------------------------------------------------------------------
# Uncomment if you need a bucket policy
# resource "aws_s3_bucket_policy" "product_images" {
#   bucket = aws_s3_bucket.product_images.id
#   policy = data.aws_iam_policy_document.s3_bucket_policy.json
# }
#
# data "aws_iam_policy_document" "s3_bucket_policy" {
#   statement {
#     sid       = "AllowCloudFrontAccess"
#     effect    = "Allow"
#     principals {
#       type        = "Service"
#       identifiers = ["cloudfront.amazonaws.com"]
#     }
#     actions   = ["s3:GetObject"]
#     resources = ["${aws_s3_bucket.product_images.arn}/*"]
#   }
# }
