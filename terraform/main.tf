# =============================================================================
# MicroMart - Main Terraform Configuration
# =============================================================================
# This is the root module that orchestrates all infrastructure components.
#
# Learning Points:
# - Terraform modules for reusable infrastructure
# - AWS provider configuration
# - Resource dependencies and ordering
# - Local values for DRY configuration
# =============================================================================

terraform {
  required_version = ">= 1.0.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend configuration for state management
  # Uncomment and configure for team environments
  # backend "s3" {
  #   bucket         = "micromart-terraform-state"
  #   key            = "infrastructure/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "terraform-state-lock"
  # }
}

# -----------------------------------------------------------------------------
# Provider Configuration
# -----------------------------------------------------------------------------
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Repository  = "https://github.com/Poojithvsc/micromart-project"
    }
  }
}

# -----------------------------------------------------------------------------
# Local Values
# -----------------------------------------------------------------------------
locals {
  # Common naming prefix
  name_prefix = "${var.project_name}-${var.environment}"

  # Common tags (in addition to default_tags)
  common_tags = {
    Owner = var.owner
  }

  # Database configurations
  databases = {
    user_db = {
      name = "user_db"
      port = 5432
    }
    product_db = {
      name = "product_db"
      port = 5432
    }
    order_db = {
      name = "order_db"
      port = 5432
    }
  }

  # Service ports
  service_ports = {
    eureka   = 8761
    gateway  = 8080
    user     = 8081
    product  = 8082
    order    = 8083
    kafka    = 9092
    postgres = 5432
  }
}

# -----------------------------------------------------------------------------
# Data Sources
# -----------------------------------------------------------------------------
# Get available AZs in the region
data "aws_availability_zones" "available" {
  state = "available"
}

# Get latest Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Get current AWS account ID
data "aws_caller_identity" "current" {}

# Get current region
data "aws_region" "current" {}
