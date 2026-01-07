# =============================================================================
# MicroMart - RDS PostgreSQL Infrastructure
# =============================================================================
# Creates RDS PostgreSQL instance for all microservices.
#
# Note: Using a single RDS instance with multiple databases is more cost-effective
# for dev/staging. For production, consider separate instances per service.
#
# Learning Points:
# - RDS subnet groups
# - Parameter groups for PostgreSQL tuning
# - Multi-AZ for high availability
# - Encryption at rest
# - Automated backups
# =============================================================================

# -----------------------------------------------------------------------------
# DB Subnet Group
# -----------------------------------------------------------------------------
# RDS instances are placed in private subnets
resource "aws_db_subnet_group" "main" {
  name        = "${local.name_prefix}-db-subnet-group"
  description = "Subnet group for RDS instances"
  subnet_ids  = aws_subnet.private[*].id

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-db-subnet-group"
  })
}

# -----------------------------------------------------------------------------
# DB Parameter Group
# -----------------------------------------------------------------------------
# Custom PostgreSQL parameters for optimization
resource "aws_db_parameter_group" "postgres" {
  family      = "postgres15"
  name        = "${local.name_prefix}-postgres-params"
  description = "Custom parameter group for MicroMart PostgreSQL"

  # Connection settings
  parameter {
    name  = "max_connections"
    value = "100"
  }

  # Logging settings (useful for debugging)
  parameter {
    name  = "log_statement"
    value = "ddl"  # Log DDL statements
  }

  parameter {
    name  = "log_min_duration_statement"
    value = "1000"  # Log queries taking > 1 second
  }

  # Performance settings
  parameter {
    name  = "shared_buffers"
    value = "{DBInstanceClassMemory/4}"  # 25% of memory
    apply_method = "pending-reboot"
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-postgres-params"
  })
}

# -----------------------------------------------------------------------------
# RDS PostgreSQL Instance
# -----------------------------------------------------------------------------
resource "aws_db_instance" "postgres" {
  identifier = "${local.name_prefix}-postgres"

  # Engine configuration
  engine               = "postgres"
  engine_version       = var.rds_engine_version
  instance_class       = var.rds_instance_class
  allocated_storage    = var.rds_allocated_storage
  max_allocated_storage = var.rds_allocated_storage * 2  # Autoscaling

  # Database configuration
  db_name  = "micromart"  # Default database
  username = var.rds_master_username
  password = var.rds_master_password
  port     = local.service_ports.postgres

  # Network configuration
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = var.rds_multi_az

  # Parameter group
  parameter_group_name = aws_db_parameter_group.postgres.name

  # Storage configuration
  storage_type          = "gp3"
  storage_encrypted     = true

  # Backup configuration
  backup_retention_period = var.rds_backup_retention_period
  backup_window           = "03:00-04:00"  # UTC
  maintenance_window      = "Mon:04:00-Mon:05:00"  # UTC

  # Deletion protection
  deletion_protection = var.environment == "prod" ? true : false
  skip_final_snapshot = var.rds_skip_final_snapshot
  final_snapshot_identifier = var.rds_skip_final_snapshot ? null : "${local.name_prefix}-postgres-final-snapshot"

  # Performance Insights (free for 7 days retention)
  performance_insights_enabled          = true
  performance_insights_retention_period = 7

  # Enhanced monitoring (optional - requires IAM role)
  # monitoring_interval = 60
  # monitoring_role_arn = aws_iam_role.rds_monitoring.arn

  # Auto minor version upgrades
  auto_minor_version_upgrade = true

  # Copy tags to snapshots
  copy_tags_to_snapshot = true

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-postgres"
  })

  lifecycle {
    prevent_destroy = false  # Set to true for production!
  }
}

# -----------------------------------------------------------------------------
# Database Initialization Script (for reference)
# -----------------------------------------------------------------------------
# Note: Run this SQL after RDS is created to set up databases
#
# CREATE DATABASE user_db;
# CREATE DATABASE product_db;
# CREATE DATABASE order_db;
#
# -- Create application user (optional, more secure than using master)
# CREATE USER micromart_app WITH PASSWORD 'your-app-password';
# GRANT ALL PRIVILEGES ON DATABASE user_db TO micromart_app;
# GRANT ALL PRIVILEGES ON DATABASE product_db TO micromart_app;
# GRANT ALL PRIVILEGES ON DATABASE order_db TO micromart_app;

# -----------------------------------------------------------------------------
# Secrets Manager for RDS Credentials (Optional but recommended)
# -----------------------------------------------------------------------------
resource "aws_secretsmanager_secret" "rds_credentials" {
  name        = "${local.name_prefix}/rds/credentials"
  description = "RDS master credentials for MicroMart"

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-rds-credentials"
  })
}

resource "aws_secretsmanager_secret_version" "rds_credentials" {
  secret_id = aws_secretsmanager_secret.rds_credentials.id
  secret_string = jsonencode({
    username = var.rds_master_username
    password = var.rds_master_password
    host     = aws_db_instance.postgres.address
    port     = aws_db_instance.postgres.port
    dbname   = aws_db_instance.postgres.db_name
    engine   = "postgres"
  })
}
