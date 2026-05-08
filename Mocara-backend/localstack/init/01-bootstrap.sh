#!/bin/sh
set -eu

REGION="${AWS_DEFAULT_REGION:-eu-west-1}"
REPOSITORY_NAME="${ECR_REPOSITORY_NAME:-mocara-backend}"
DB_SECRET_NAME="${DB_SECRET_NAME:-mocara/dev/db}"
JWT_SECRET_NAME="${JWT_SECRET_NAME:-mocara/dev/jwt}"
POSTGRES_DB="${POSTGRES_DB:-mocara}"
POSTGRES_USER="${POSTGRES_USER:-mocara}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-change-this-db-password}"
JWT_SECRET_VALUE="${JWT_SECRET:-change-this-to-a-strong-secret-at-least-32-characters}"

awslocal ecr describe-repositories \
  --repository-names "$REPOSITORY_NAME" \
  --region "$REGION" >/dev/null 2>&1 || \
  awslocal ecr create-repository \
    --repository-name "$REPOSITORY_NAME" \
    --region "$REGION" >/dev/null

DB_SECRET_PAYLOAD=$(printf '{"username":"%s","password":"%s","url":"jdbc:postgresql://postgres:5432/%s"}' \
  "$POSTGRES_USER" \
  "$POSTGRES_PASSWORD" \
  "$POSTGRES_DB")

awslocal secretsmanager describe-secret \
  --secret-id "$DB_SECRET_NAME" \
  --region "$REGION" >/dev/null 2>&1 && \
  awslocal secretsmanager put-secret-value \
    --secret-id "$DB_SECRET_NAME" \
    --secret-string "$DB_SECRET_PAYLOAD" \
    --region "$REGION" >/dev/null || \
  awslocal secretsmanager create-secret \
    --name "$DB_SECRET_NAME" \
    --secret-string "$DB_SECRET_PAYLOAD" \
    --region "$REGION" >/dev/null

awslocal secretsmanager describe-secret \
  --secret-id "$JWT_SECRET_NAME" \
  --region "$REGION" >/dev/null 2>&1 && \
  awslocal secretsmanager put-secret-value \
    --secret-id "$JWT_SECRET_NAME" \
    --secret-string "$JWT_SECRET_VALUE" \
    --region "$REGION" >/dev/null || \
  awslocal secretsmanager create-secret \
    --name "$JWT_SECRET_NAME" \
    --secret-string "$JWT_SECRET_VALUE" \
    --region "$REGION" >/dev/null

echo "LocalStack bootstrap complete for region $REGION"
