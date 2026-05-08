# Mocara LocalStack Deployment Guide

This guide helps you run the current project in a local AWS-like environment using `LocalStack`.

It is designed for the current codebase:

- `Mocara-backend`: Spring Boot + PostgreSQL
- `Mocara-andriod`: Android client

## What LocalStack Is Doing Here

For this project today, `LocalStack` is best used to simulate AWS infrastructure workflows locally:

- `Amazon ECR`: practice image repository creation
- `AWS Secrets Manager`: practice secret creation and lookup
- `Amazon S3`: available for future expansion

Important limitation for the current backend:

- the backend does **not** yet fetch secrets from AWS APIs
- it still reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` from environment variables

That means this setup gives you:

- local app deployment with `Docker Compose`
- local PostgreSQL
- local AWS service simulation with `LocalStack`
- a rehearsal path before moving to real AWS

## Target Local Architecture

You will run:

- `postgres` container
- `backend` container
- `localstack` container

The backend continues to connect to PostgreSQL directly, while `LocalStack` exposes an AWS-compatible endpoint on `http://localhost:4566`.

## 1. Prerequisites

Install:

- `Docker Desktop`
- `Docker Compose`
- `AWS CLI`

Optional but convenient:

- `awslocal` wrapper

If you do not want to install `awslocal`, you can use:

```bash
aws --endpoint-url=http://localhost:4566 ...
```

## 2. Prepare Backend Environment

From `Mocara-backend`, copy the example file.

### PowerShell

```powershell
Copy-Item .env.example .env
```

### Bash

```bash
cp .env.example .env
```

Edit `.env` and set strong values:

```env
POSTGRES_DB=mocara
POSTGRES_USER=mocara
POSTGRES_PASSWORD=change-this-db-password
JWT_SECRET=change-this-to-a-strong-secret-at-least-32-characters
AWS_REGION=eu-west-1
```

Notes:

- `JWT_SECRET` must be at least `32` bytes
- `AWS_REGION` is used by the LocalStack bootstrap script

## 3. Start PostgreSQL, Backend, and LocalStack

From `Mocara-backend` run:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml up -d --build
```

This starts:

- the existing backend stack
- a `LocalStack` container on port `4566`
- an initialization script that creates local `ECR` and `Secrets Manager` resources

## 4. Verify the Containers

Check status:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml ps
```

Check backend logs:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml logs backend --tail=100
```

Check LocalStack logs:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml logs localstack --tail=100
```

## 5. Verify the API Health Endpoint

The backend exposes:

```text
http://localhost:8080/actuator/health
```

Test it:

```bash
curl http://localhost:8080/actuator/health
```

If this returns `UP`, the application and database are working locally.

## 6. Verify LocalStack Resources

Set local AWS credentials for your terminal session:

### PowerShell

```powershell
$env:AWS_ACCESS_KEY_ID="test"
$env:AWS_SECRET_ACCESS_KEY="test"
$env:AWS_DEFAULT_REGION="eu-west-1"
```

### Bash

```bash
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=eu-west-1
```

List ECR repositories:

```bash
aws --endpoint-url=http://localhost:4566 ecr describe-repositories
```

You should see a repository named:

- `mocara-backend`

List secrets:

```bash
aws --endpoint-url=http://localhost:4566 secretsmanager list-secrets
```

You should see:

- `mocara/dev/db`
- `mocara/dev/jwt`

Read the JWT secret:

```bash
aws --endpoint-url=http://localhost:4566 secretsmanager get-secret-value --secret-id mocara/dev/jwt
```

Read the DB secret:

```bash
aws --endpoint-url=http://localhost:4566 secretsmanager get-secret-value --secret-id mocara/dev/db
```

## 7. Practice an ECR-Like Local Image Flow

Build the backend image:

```bash
docker build -t mocara-backend:latest .
```

At this stage, the useful thing to validate is:

- the repository exists in LocalStack
- your app image builds successfully
- you understand the image promotion flow before moving to real AWS ECR

Because the current project does not yet deploy via ECS task definitions or Terraform/CDK, this is mainly an infrastructure rehearsal step.

## 8. Point the Android App to the Local Backend

If you want the Android emulator to use this local deployment, update the base URL to:

- `http://10.0.2.2:8080/`

That maps the emulator back to your host machine.

## 9. Stop or Reset the Stack

Stop containers:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml down
```

Stop and remove volumes:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml down -v
```

Use `-v` only if you want to wipe:

- PostgreSQL data
- LocalStack persisted state

## 10. Common Issues

### Backend fails with JWT secret error

Cause:

- `JWT_SECRET` is missing
- or it is shorter than `32` bytes

Fix:

- update `.env`
- rebuild and restart the stack

### Backend cannot connect to PostgreSQL

Check:

- `postgres` container is healthy
- `POSTGRES_PASSWORD` is set in `.env`

Then restart:

```bash
docker compose -f docker-compose.yml -f docker-compose.localstack.yml up -d --build
```

### AWS CLI cannot talk to LocalStack

Check:

- `LocalStack` is running on `http://localhost:4566`
- your terminal has `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, and `AWS_DEFAULT_REGION`

Then retry with:

```bash
aws --endpoint-url=http://localhost:4566 secretsmanager list-secrets
```

## Recommended Next Step

If you want a tighter LocalStack integration after this first local deployment, the next improvement is:

1. add AWS SDK configuration to the backend
2. read `JWT_SECRET` and DB settings from `Secrets Manager`
3. optionally add `S3` usage for file or document storage

That would move this setup from "AWS workflow rehearsal" to "application-level AWS integration".
