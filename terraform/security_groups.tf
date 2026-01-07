# =============================================================================
# MicroMart - Security Groups
# =============================================================================
# Defines security groups for all components with least-privilege access.
#
# Security Groups:
# - ALB Security Group: HTTP/HTTPS from internet
# - EC2 Security Group: From ALB, SSH (optional)
# - RDS Security Group: PostgreSQL from EC2 only
#
# Learning Points:
# - Security group ingress/egress rules
# - Referencing other security groups
# - Least privilege principle
# - CIDR blocks vs security group references
# =============================================================================

# -----------------------------------------------------------------------------
# ALB Security Group
# -----------------------------------------------------------------------------
# Allows HTTP/HTTPS traffic from anywhere
resource "aws_security_group" "alb" {
  name        = "${local.name_prefix}-alb-sg"
  description = "Security group for Application Load Balancer"
  vpc_id      = aws_vpc.main.id

  # HTTP from anywhere
  ingress {
    description = "HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS from anywhere
  ingress {
    description = "HTTPS from internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # All outbound traffic
  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-alb-sg"
  })
}

# -----------------------------------------------------------------------------
# EC2 (Docker Host) Security Group
# -----------------------------------------------------------------------------
# Allows traffic from ALB and optional SSH access
resource "aws_security_group" "ec2" {
  name        = "${local.name_prefix}-ec2-sg"
  description = "Security group for EC2 Docker host"
  vpc_id      = aws_vpc.main.id

  # API Gateway port from ALB
  ingress {
    description     = "API Gateway from ALB"
    from_port       = local.service_ports.gateway
    to_port         = local.service_ports.gateway
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  # Eureka port from ALB (for dashboard access)
  ingress {
    description     = "Eureka from ALB"
    from_port       = local.service_ports.eureka
    to_port         = local.service_ports.eureka
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  # SSH access (only if CIDR blocks are specified)
  dynamic "ingress" {
    for_each = length(var.allowed_ssh_cidrs) > 0 ? [1] : []
    content {
      description = "SSH access"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = var.allowed_ssh_cidrs
    }
  }

  # Allow all traffic within the security group (for Docker containers)
  ingress {
    description = "All traffic from self"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  # All outbound traffic
  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-ec2-sg"
  })
}

# -----------------------------------------------------------------------------
# RDS Security Group
# -----------------------------------------------------------------------------
# Only allows PostgreSQL connections from EC2 instances
resource "aws_security_group" "rds" {
  name        = "${local.name_prefix}-rds-sg"
  description = "Security group for RDS PostgreSQL"
  vpc_id      = aws_vpc.main.id

  # PostgreSQL from EC2 only
  ingress {
    description     = "PostgreSQL from EC2"
    from_port       = local.service_ports.postgres
    to_port         = local.service_ports.postgres
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  # No outbound rules needed for RDS (AWS manages it)
  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-rds-sg"
  })
}

# -----------------------------------------------------------------------------
# Kafka Security Group (for future MSK or self-managed Kafka)
# -----------------------------------------------------------------------------
resource "aws_security_group" "kafka" {
  name        = "${local.name_prefix}-kafka-sg"
  description = "Security group for Kafka"
  vpc_id      = aws_vpc.main.id

  # Kafka from EC2
  ingress {
    description     = "Kafka from EC2"
    from_port       = local.service_ports.kafka
    to_port         = local.service_ports.kafka
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  # Zookeeper (if using)
  ingress {
    description     = "Zookeeper from EC2"
    from_port       = 2181
    to_port         = 2181
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  # Internal Kafka communication
  ingress {
    description = "Kafka internal"
    from_port   = 9092
    to_port     = 9094
    protocol    = "tcp"
    self        = true
  }

  egress {
    description = "All outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.common_tags, {
    Name = "${local.name_prefix}-kafka-sg"
  })
}
