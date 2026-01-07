# =============================================================================
# MicroMart - Terraform Variables
# =============================================================================
# All configurable parameters for the infrastructure.
#
# Learning Points:
# - Variable types (string, number, bool, list, map, object)
# - Variable validation
# - Default values vs required variables
# - Sensitive variables
# =============================================================================

# -----------------------------------------------------------------------------
# General Configuration
# -----------------------------------------------------------------------------
variable "project_name" {
  description = "Name of the project, used for resource naming"
  type        = string
  default     = "micromart"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]*$", var.project_name))
    error_message = "Project name must start with a letter and contain only lowercase letters, numbers, and hyphens."
  }
}

variable "environment" {
  description = "Environment name (dev, staging, prod)"
  type        = string
  default     = "dev"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be one of: dev, staging, prod."
  }
}

variable "owner" {
  description = "Owner of the infrastructure (for tagging)"
  type        = string
  default     = "DevTeam"
}

# -----------------------------------------------------------------------------
# AWS Configuration
# -----------------------------------------------------------------------------
variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

# -----------------------------------------------------------------------------
# VPC Configuration
# -----------------------------------------------------------------------------
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"

  validation {
    condition     = can(cidrhost(var.vpc_cidr, 0))
    error_message = "VPC CIDR must be a valid IPv4 CIDR block."
  }
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets"
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway for private subnets (costs money!)"
  type        = bool
  default     = false
}

# -----------------------------------------------------------------------------
# EC2 Configuration
# -----------------------------------------------------------------------------
variable "ec2_instance_type" {
  description = "EC2 instance type for Docker host"
  type        = string
  default     = "t3.medium"

  validation {
    condition     = can(regex("^t[23]\\.(micro|small|medium|large|xlarge|2xlarge)$", var.ec2_instance_type))
    error_message = "Instance type must be a valid t2 or t3 instance type."
  }
}

variable "ec2_key_name" {
  description = "Name of the SSH key pair for EC2 access"
  type        = string
  default     = ""
}

variable "ec2_root_volume_size" {
  description = "Size of the root EBS volume in GB"
  type        = number
  default     = 30

  validation {
    condition     = var.ec2_root_volume_size >= 20 && var.ec2_root_volume_size <= 100
    error_message = "Root volume size must be between 20 and 100 GB."
  }
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = []  # Empty = no SSH access from internet
}

# -----------------------------------------------------------------------------
# RDS Configuration
# -----------------------------------------------------------------------------
variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "Allocated storage for RDS in GB"
  type        = number
  default     = 20
}

variable "rds_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "15.4"
}

variable "rds_master_username" {
  description = "Master username for RDS"
  type        = string
  default     = "postgres"
  sensitive   = true
}

variable "rds_master_password" {
  description = "Master password for RDS (min 8 characters)"
  type        = string
  sensitive   = true

  validation {
    condition     = length(var.rds_master_password) >= 8
    error_message = "RDS master password must be at least 8 characters."
  }
}

variable "rds_multi_az" {
  description = "Enable Multi-AZ for RDS (costs more, but high availability)"
  type        = bool
  default     = false
}

variable "rds_skip_final_snapshot" {
  description = "Skip final snapshot when destroying RDS"
  type        = bool
  default     = true  # Set to false for production!
}

variable "rds_backup_retention_period" {
  description = "Number of days to retain RDS backups"
  type        = number
  default     = 7
}

# -----------------------------------------------------------------------------
# S3 Configuration
# -----------------------------------------------------------------------------
variable "s3_bucket_name" {
  description = "Name of the S3 bucket for product images (must be globally unique)"
  type        = string
  default     = ""  # Will be auto-generated if empty

  validation {
    condition     = var.s3_bucket_name == "" || can(regex("^[a-z0-9][a-z0-9.-]*[a-z0-9]$", var.s3_bucket_name))
    error_message = "S3 bucket name must be lowercase and can only contain letters, numbers, hyphens, and periods."
  }
}

variable "s3_enable_versioning" {
  description = "Enable versioning for S3 bucket"
  type        = bool
  default     = true
}

variable "s3_lifecycle_expiration_days" {
  description = "Days after which to expire old object versions (0 = disabled)"
  type        = number
  default     = 90
}

# -----------------------------------------------------------------------------
# Application Configuration
# -----------------------------------------------------------------------------
variable "jwt_secret" {
  description = "JWT secret for authentication (min 32 characters)"
  type        = string
  sensitive   = true
  default     = ""

  validation {
    condition     = var.jwt_secret == "" || length(var.jwt_secret) >= 32
    error_message = "JWT secret must be at least 32 characters."
  }
}
