# Mocara System AWS Deployment Guide

This guide is tailored to the current project structure:

- `Mocara-backend`: Spring Boot + PostgreSQL
- `Mocara-andriod`: Android client

## Recommended AWS Architecture

For a portfolio-ready deployment that also shows AWS knowledge, use:

- `Amazon ECR`: store backend Docker image
- `Amazon ECS Fargate`: run Spring Boot container
- `Application Load Balancer`: public HTTPS entry point
- `Amazon RDS for PostgreSQL`: managed database
- `AWS Secrets Manager`: store `JWT_SECRET` and database credentials
- `Amazon CloudWatch`: logs and monitoring
- `AWS Certificate Manager`: TLS certificate
- `Amazon Route 53`: optional custom domain

This is a strong middle ground:

- more impressive than a single EC2 VM
- much simpler than full Kubernetes/EKS
- production-shaped enough for interviews and demos

## Lowest-Cost Alternative

If your goal is to learn AWS with minimal monthly cost, use:

- `1 x EC2`
- `Docker Compose`
- `Spring Boot container`
- `PostgreSQL container`
- `Nginx`

See:

- `docs/EC2_LOW_COST_DEPLOYMENT.md`
- `docs/LOCALSTACK_DEPLOYMENT_GUIDE.md`

This is the best starting point if:

- you are new to AWS
- you want to avoid `ALB`, `Fargate`, and `RDS` charges
- you still want a real public deployment to demonstrate

## Suggested Deployment Flow

1. Build the backend container locally.
2. Push the image to ECR.
3. Create PostgreSQL in RDS.
4. Store secrets in Secrets Manager.
5. Create ECS task definition with environment variables and secrets.
6. Create ECS service on Fargate behind an ALB.
7. Attach ACM certificate and optional Route 53 domain.
8. Update Android client to call the ALB or custom domain.
9. Validate auth, database migration, and API flows.

## Environment Variables

The backend now supports environment-driven configuration:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `PORT` (optional)

Production profile:

- Docker entrypoint uses `spring.profiles.active=prod`
- `application-prod.yaml` switches JPA to `validate`

## Local Container Test

From `Mocara-backend`:

```bash
docker build -t mocara-backend .
```

Run with environment variables:

```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/mocara \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=your-password \
  -e JWT_SECRET=your-very-strong-secret \
  mocara-backend
```

## AWS Resource Checklist

## 1. ECR

Create one repository:

- `mocara-backend`

Push image:

```bash
aws ecr get-login-password --region <your-region> | docker login --username AWS --password-stdin <account>.dkr.ecr.<your-region>.amazonaws.com
docker tag mocara-backend:latest <account>.dkr.ecr.<your-region>.amazonaws.com/mocara-backend:latest
docker push <account>.dkr.ecr.<your-region>.amazonaws.com/mocara-backend:latest
```

## 2. RDS PostgreSQL

Create:

- PostgreSQL instance
- private subnet placement
- security group allowing inbound `5432` only from ECS task security group

Recommended starter settings:

- instance class: `db.t3.micro` or `db.t4g.micro`
- storage: small General Purpose SSD
- public access: `No`

## 3. Secrets Manager

Store:

- DB username
- DB password
- JWT secret

You can also store a full JDBC URL if preferred.

## 4. ECS Task Definition

Container:

- image: ECR backend image
- port mapping: `8080`
- cpu/memory: start small, for example `0.5 vCPU` and `1 GB`

Environment or secrets:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Logging:

- send stdout/stderr to CloudWatch Logs

## 5. Load Balancer

Create an `Application Load Balancer`:

- listener `80` redirect to `443`
- listener `443` forward to ECS target group
- health check path: use an API path that returns `200`

If you later add Spring Actuator, use `/actuator/health`.

## 6. Domain and HTTPS

If you want a polished demo:

- request certificate in ACM
- create Route 53 hosted zone
- point domain or subdomain such as `api.mocara-demo.com` to the ALB

## 7. Android Client Update

Before demo deployment, replace the emulator-only base URL:

- current: `http://10.0.2.2:8080/`
- production target: `https://<your-domain-or-alb-dns>/`

For a cleaner setup, move the base URL into a build config field or a dedicated environment config layer.

## Talking Points For Interviews

When you present the deployment, emphasize:

- containerized Java backend with Docker
- managed PostgreSQL on RDS
- secret management with Secrets Manager
- stateless API on ECS Fargate
- HTTPS via ACM + ALB
- centralized logs in CloudWatch
- security groups restricting traffic paths

## Good Next Upgrades

- add `Spring Boot Actuator` for health checks
- add CI/CD with GitHub Actions to ECR + ECS deploy
- move CORS from `*` to an allowlist
- parameterize Android base URL by build type
- add a `/health` endpoint if you do not want to expose actuator
