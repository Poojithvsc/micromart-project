# =============================================================================
# MicroMart - Terraform Outputs
# =============================================================================
# Outputs useful information after terraform apply.
#
# Learning Points:
# - Output values for reference
# - Sensitive outputs (masked in logs)
# - Output descriptions for documentation
# =============================================================================

# -----------------------------------------------------------------------------
# VPC Outputs
# -----------------------------------------------------------------------------
output "vpc_id" {
  description = "ID of the VPC"
  value       = aws_vpc.main.id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = aws_vpc.main.cidr_block
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = aws_subnet.private[*].id
}

# -----------------------------------------------------------------------------
# EC2 Outputs
# -----------------------------------------------------------------------------
output "ec2_instance_id" {
  description = "ID of the EC2 Docker host instance"
  value       = aws_instance.docker_host.id
}

output "ec2_public_ip" {
  description = "Public IP address of the EC2 instance"
  value       = aws_eip.docker_host.public_ip
}

output "ec2_public_dns" {
  description = "Public DNS name of the EC2 instance"
  value       = aws_eip.docker_host.public_dns
}

output "ec2_ssh_command" {
  description = "SSH command to connect to EC2 (if key pair configured)"
  value       = var.ec2_key_name != "" ? "ssh -i ${var.ec2_key_name}.pem ec2-user@${aws_eip.docker_host.public_ip}" : "SSH not configured - use SSM Session Manager"
}

output "ec2_ssm_command" {
  description = "SSM Session Manager command to connect to EC2"
  value       = "aws ssm start-session --target ${aws_instance.docker_host.id}"
}

# -----------------------------------------------------------------------------
# RDS Outputs
# -----------------------------------------------------------------------------
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "rds_address" {
  description = "RDS instance hostname"
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "RDS instance port"
  value       = aws_db_instance.postgres.port
}

output "rds_database_name" {
  description = "RDS default database name"
  value       = aws_db_instance.postgres.db_name
}

output "rds_connection_string" {
  description = "PostgreSQL connection string template"
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/<database_name>"
}

output "rds_secrets_arn" {
  description = "ARN of the Secrets Manager secret containing RDS credentials"
  value       = aws_secretsmanager_secret.rds_credentials.arn
}

# -----------------------------------------------------------------------------
# S3 Outputs
# -----------------------------------------------------------------------------
output "s3_bucket_name" {
  description = "Name of the S3 bucket for product images"
  value       = aws_s3_bucket.product_images.id
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket"
  value       = aws_s3_bucket.product_images.arn
}

output "s3_bucket_domain" {
  description = "Domain name of the S3 bucket"
  value       = aws_s3_bucket.product_images.bucket_regional_domain_name
}

# -----------------------------------------------------------------------------
# Security Group Outputs
# -----------------------------------------------------------------------------
output "security_group_alb_id" {
  description = "ID of the ALB security group"
  value       = aws_security_group.alb.id
}

output "security_group_ec2_id" {
  description = "ID of the EC2 security group"
  value       = aws_security_group.ec2.id
}

output "security_group_rds_id" {
  description = "ID of the RDS security group"
  value       = aws_security_group.rds.id
}

# -----------------------------------------------------------------------------
# Application URLs
# -----------------------------------------------------------------------------
output "application_urls" {
  description = "URLs to access the application"
  value = {
    api_gateway = "http://${aws_eip.docker_host.public_ip}:8080"
    eureka      = "http://${aws_eip.docker_host.public_ip}:8761"
    swagger_user    = "http://${aws_eip.docker_host.public_ip}:8081/swagger-ui.html"
    swagger_product = "http://${aws_eip.docker_host.public_ip}:8082/swagger-ui.html"
    swagger_order   = "http://${aws_eip.docker_host.public_ip}:8083/swagger-ui.html"
  }
}

# -----------------------------------------------------------------------------
# Environment Variables for Application
# -----------------------------------------------------------------------------
output "environment_variables" {
  description = "Environment variables to configure in the application"
  value = {
    DB_HOST           = aws_db_instance.postgres.address
    DB_PORT           = aws_db_instance.postgres.port
    AWS_REGION        = var.aws_region
    S3_BUCKET         = aws_s3_bucket.product_images.id
    EUREKA_URL        = "http://localhost:8761/eureka/"
    KAFKA_SERVERS     = "localhost:9092"
  }
  sensitive = false
}

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
output "deployment_summary" {
  description = "Summary of deployed resources"
  value = <<-EOT

  ============================================================
  MicroMart Infrastructure Deployment Summary
  ============================================================

  Environment: ${var.environment}
  Region: ${var.aws_region}

  VPC:
    - VPC ID: ${aws_vpc.main.id}
    - CIDR: ${aws_vpc.main.cidr_block}

  EC2 Docker Host:
    - Instance ID: ${aws_instance.docker_host.id}
    - Public IP: ${aws_eip.docker_host.public_ip}
    - Instance Type: ${var.ec2_instance_type}

  RDS PostgreSQL:
    - Endpoint: ${aws_db_instance.postgres.endpoint}
    - Instance Class: ${var.rds_instance_class}

  S3 Bucket:
    - Name: ${aws_s3_bucket.product_images.id}

  Next Steps:
  1. Create databases: user_db, product_db, order_db
  2. Build and push Docker images
  3. SSH/SSM into EC2 and start services

  ============================================================
  EOT
}
