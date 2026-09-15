terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "auth_cpf" {
  name               = "${var.name_prefix}-auth-cpf-role"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = var.tags
}

resource "aws_iam_role_policy_attachment" "auth_cpf_basic_execution" {
  role       = aws_iam_role.auth_cpf.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_lambda_function" "auth_cpf" {
  function_name    = "${var.name_prefix}-auth-cpf"
  role             = aws_iam_role.auth_cpf.arn
  handler          = "io.github.gabrielivo.oficina.lambda.auth.AuthCpfHandler::handleRequest"
  runtime          = "java21"
  filename         = var.lambda_jar_path
  source_code_hash = filebase64sha256(var.lambda_jar_path)
  memory_size      = var.memory_size
  timeout          = var.timeout

  environment {
    variables = {
      JWT_SECRET               = var.jwt_secret
      JWT_EXPIRATION_MS        = tostring(var.jwt_expiration_ms)
      INTERNAL_API_KEY         = var.internal_api_key
      INTERNAL_STATUS_BASE_URL = var.internal_status_base_url
    }
  }

  tags = merge(var.tags, {
    Name = "${var.name_prefix}-auth-cpf"
  })
}
