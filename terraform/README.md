# MicroMart - Terraform Infrastructure

This directory contains Terraform configurations to deploy MicroMart infrastructure on AWS.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              AWS Cloud                                   │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                         VPC (10.0.0.0/16)                          │  │
│  │                                                                     │  │
│  │  ┌─────────────────────┐      ┌─────────────────────┐              │  │
│  │  │   Public Subnet     │      │   Public Subnet     │              │  │
│  │  │   (10.0.1.0/24)     │      │   (10.0.2.0/24)     │              │  │
│  │  │                     │      │                     │              │  │
│  │  │  ┌───────────────┐  │      │                     │              │  │
│  │  │  │ EC2 (Docker)  │  │      │                     │              │  │
│  │  │  │  - Gateway    │  │      │                     │              │  │
│  │  │  │  - Services   │  │      │                     │              │  │
│  │  │  │  - Kafka      │  │      │                     │              │  │
│  │  │  └───────────────┘  │      │                     │              │  │
│  │  └─────────────────────┘      └─────────────────────┘              │  │
│  │                                                                     │  │
│  │  ┌─────────────────────┐      ┌─────────────────────┐              │  │
│  │  │   Private Subnet    │      │   Private Subnet    │              │  │
│  │  │   (10.0.10.0/24)    │      │   (10.0.11.0/24)    │              │  │
│  │  │                     │      │                     │              │  │
│  │  │  ┌───────────────┐  │      │                     │              │  │
│  │  │  │     RDS       │  │      │                     │              │  │
│  │  │  │  PostgreSQL   │──┼──────┼─── Multi-AZ        │              │  │
│  │  │  └───────────────┘  │      │    (optional)       │              │  │
│  │  └─────────────────────┘      └─────────────────────┘              │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                          │
│  ┌─────────────┐                                                         │
│  │     S3      │  Product Images                                         │
│  └─────────────┘                                                         │
└──────────────────────────────────────────────────────────────────────────┘
```

## Prerequisites

1. **AWS CLI** configured with appropriate credentials
   ```bash
   aws configure
   ```

2. **Terraform** >= 1.0.0
   ```bash
   # macOS
   brew install terraform

   # Windows (with Chocolatey)
   choco install terraform
   ```

3. **SSH Key Pair** (optional - can use SSM Session Manager instead)
   ```bash
   aws ec2 create-key-pair --key-name micromart-key --query 'KeyMaterial' --output text > micromart-key.pem
   chmod 400 micromart-key.pem
   ```

## Quick Start

1. **Copy the example variables file:**
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

2. **Edit `terraform.tfvars` with your values** (especially `rds_master_password`)

3. **Initialize Terraform:**
   ```bash
   terraform init
   ```

4. **Review the plan:**
   ```bash
   terraform plan
   ```

5. **Apply the configuration:**
   ```bash
   terraform apply
   ```

6. **After deployment, create databases:**
   ```bash
   # Connect to RDS
   psql -h <rds_endpoint> -U postgres -d micromart

   # Create databases
   CREATE DATABASE user_db;
   CREATE DATABASE product_db;
   CREATE DATABASE order_db;
   ```

## Files Structure

```
terraform/
├── main.tf              # Provider and locals
├── variables.tf         # Input variables
├── outputs.tf           # Output values
├── vpc.tf               # VPC, subnets, routing
├── security_groups.tf   # Security groups
├── ec2.tf               # EC2 instance
├── rds.tf               # RDS PostgreSQL
├── s3.tf                # S3 bucket
├── iam.tf               # IAM roles and policies
├── templates/
│   └── user_data.sh     # EC2 bootstrap script
├── terraform.tfvars.example
└── README.md
```

## Estimated Costs (us-east-1)

| Resource | Instance Type | Monthly Cost (approx) |
|----------|--------------|----------------------|
| EC2 | t3.medium | ~$30 |
| RDS | db.t3.micro | ~$15 |
| S3 | Standard | ~$1-5 |
| NAT Gateway | (if enabled) | ~$32 |
| **Total (without NAT)** | | **~$50/month** |

## Accessing the Infrastructure

### SSH (if key pair configured)
```bash
ssh -i micromart-key.pem ec2-user@<public_ip>
```

### SSM Session Manager (recommended)
```bash
aws ssm start-session --target <instance_id>
```

### Application URLs
After starting Docker containers:
- API Gateway: `http://<public_ip>:8080`
- Eureka Dashboard: `http://<public_ip>:8761`
- User Service Swagger: `http://<public_ip>:8081/swagger-ui.html`
- Product Service Swagger: `http://<public_ip>:8082/swagger-ui.html`
- Order Service Swagger: `http://<public_ip>:8083/swagger-ui.html`

## Starting the Application

After Terraform applies, SSH into EC2 and:

```bash
cd /opt/micromart

# Start infrastructure (Kafka, Zookeeper)
docker-compose up -d zookeeper kafka

# Start services (after building/pushing images)
docker-compose up -d
```

## Destroying Infrastructure

```bash
# Destroy all resources
terraform destroy

# Or destroy specific resources
terraform destroy -target=aws_instance.docker_host
```

## Security Best Practices

1. **Never commit `terraform.tfvars`** - it contains sensitive data
2. **Use Secrets Manager** for credentials instead of hardcoding
3. **Enable MFA** for AWS console access
4. **Use SSM Session Manager** instead of SSH when possible
5. **Set `deletion_protection = true`** for production RDS

## Troubleshooting

### Cannot connect to RDS
- Check security group allows traffic from EC2
- Verify RDS is in private subnet
- Check credentials in Secrets Manager

### EC2 not starting services
- Check `/var/log/user-data.log` for bootstrap errors
- Verify Docker images are available
- Check CloudWatch logs

### Terraform state issues
- Consider using S3 backend for team environments
- Never manually edit `.tfstate` files
