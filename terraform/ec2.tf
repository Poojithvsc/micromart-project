# =============================================================================
# MicroMart - EC2 Docker Host
# =============================================================================
# Creates an EC2 instance configured to run Docker containers.
#
# Features:
# - Amazon Linux 2023 with Docker pre-installed
# - Docker Compose for multi-container orchestration
# - CloudWatch agent for monitoring
# - IAM role for S3 access and SSM
# - User data script for automated setup
#
# Learning Points:
# - EC2 user data for bootstrapping
# - IAM instance profiles
# - EBS volume configuration
# - SSM Session Manager (alternative to SSH)
# =============================================================================

# -----------------------------------------------------------------------------
# EC2 Instance
# -----------------------------------------------------------------------------
resource "aws_instance" "docker_host" {
  ami                    = data.aws_ami.amazon_linux_2023.id
  instance_type          = var.ec2_instance_type
  key_name               = var.ec2_key_name != "" ? var.ec2_key_name : null
  vpc_security_group_ids = [aws_security_group.ec2.id]
  subnet_id              = aws_subnet.public[0].id  # Public subnet for direct access
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  # Root volume configuration
  root_block_device {
    volume_type           = "gp3"
    volume_size           = var.ec2_root_volume_size
    encrypted             = true
    delete_on_termination = true

    tags = merge(local.common_tags, {
      Name = "${local.name_prefix}-docker-host-root"
    })
  }

  # User data script for bootstrapping
  user_data = base64encode(templatefile("${path.module}/templates/user_data.sh", {
    project_name   = var.project_name
    environment    = var.environment
    aws_region     = var.aws_region
    s3_bucket      = aws_s3_bucket.product_images.id
    db_host        = aws_db_instance.postgres.address
    db_port        = aws_db_instance.postgres.port
    db_username    = var.rds_master_username
    db_password    = var.rds_master_password
  }))

  # Enable detailed monitoring
  monitoring = true

  # Metadata options (IMDSv2 required for security)
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"  # IMDSv2
    http_put_response_hop_limit = 1
    instance_metadata_tags      = "enabled"
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-docker-host"
  })

  lifecycle {
    ignore_changes = [ami]  # Don't recreate on AMI updates
  }

  depends_on = [
    aws_db_instance.postgres,
    aws_s3_bucket.product_images
  ]
}

# -----------------------------------------------------------------------------
# Elastic IP (Optional - for static public IP)
# -----------------------------------------------------------------------------
resource "aws_eip" "docker_host" {
  domain   = "vpc"
  instance = aws_instance.docker_host.id

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-docker-host-eip"
  })

  depends_on = [aws_internet_gateway.main]
}

# -----------------------------------------------------------------------------
# CloudWatch Log Group for EC2 Logs
# -----------------------------------------------------------------------------
resource "aws_cloudwatch_log_group" "ec2_logs" {
  name              = "/aws/ec2/${local.name_prefix}/docker-host"
  retention_in_days = 30

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-ec2-logs"
  })
}

# -----------------------------------------------------------------------------
# CloudWatch Alarms
# -----------------------------------------------------------------------------
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "${local.name_prefix}-docker-host-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "EC2 CPU utilization is above 80%"

  dimensions = {
    InstanceId = aws_instance.docker_host.id
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-cpu-alarm"
  })
}

resource "aws_cloudwatch_metric_alarm" "status_check" {
  alarm_name          = "${local.name_prefix}-docker-host-status-check"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  alarm_description   = "EC2 instance status check failed"

  dimensions = {
    InstanceId = aws_instance.docker_host.id
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-status-alarm"
  })
}
