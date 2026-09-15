terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "project_name" {
  description = "Project name used as a naming prefix."
  type        = string
  default     = "oficina"
}

variable "environment" {
  description = "Infrastructure environment name."
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region."
  type        = string
  default     = "us-east-1"
}

variable "auth_lambda_jar_path" {
  description = "Caminho local do jar shaded (gerado por: mvn package, na raiz deste repositorio)."
  type        = string
  default     = "../../../../target/oficina-auth-lambda.jar"
}

variable "jwt_secret" {
  description = "Mesmo valor de jwt.secret / JWT_SECRET da aplicacao principal (repo oficina-app)."
  type        = string
  sensitive   = true
}

variable "internal_api_key" {
  description = "Mesmo valor de app.internal.api-key / APP_INTERNAL_API_KEY da aplicacao principal (repo oficina-app)."
  type        = string
  sensitive   = true
}

variable "app_public_url" {
  description = <<-EOT
    URL publica da aplicacao principal (oficina-app), sem barra final.
    Publicada pelo repo oficina-app/oficina-infra-k8s apos o deploy do
    Service oficina-app-lb. O default abaixo e um placeholder valido (para
    nao quebrar o primeiro apply, antes do oficina-app estar rodando no
    cluster) e PRECISA ser substituido em seguida por um novo apply com o
    valor real.
  EOT
  type        = string
  default     = "https://example.invalid"
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
  tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "terraform"
    Repo        = "oficina-auth-lambda"
  }
}

module "auth_lambda" {
  source = "../../modules/auth-lambda"

  name_prefix              = local.name_prefix
  lambda_jar_path          = var.auth_lambda_jar_path
  jwt_secret               = var.jwt_secret
  internal_api_key         = var.internal_api_key
  internal_status_base_url = "${var.app_public_url}/internal/clientes"
  tags                     = local.tags
}

output "lambda_function_name" {
  value       = module.auth_lambda.lambda_function_name
  description = "Nome da funcao Lambda (consumido pelo repo oficina-infra-k8s para configurar a rota do API Gateway)."
}

output "lambda_function_arn" {
  value = module.auth_lambda.lambda_function_arn
}

output "lambda_invoke_arn" {
  value       = module.auth_lambda.lambda_invoke_arn
  description = "invoke_arn (consumido pelo repo oficina-infra-k8s para a integracao do API Gateway)."
}
