output "lambda_function_name" {
  value = aws_lambda_function.auth_cpf.function_name
}

output "lambda_function_arn" {
  value = aws_lambda_function.auth_cpf.arn
}

output "lambda_invoke_arn" {
  value = aws_lambda_function.auth_cpf.invoke_arn
}
