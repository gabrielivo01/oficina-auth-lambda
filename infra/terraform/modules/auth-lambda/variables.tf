variable "name_prefix" {
  type = string
}

variable "lambda_jar_path" {
  description = "Caminho local para o jar shaded gerado por lambda-auth-cpf (mvn package)."
  type        = string
}

variable "memory_size" {
  type    = number
  default = 512
}

variable "timeout" {
  type    = number
  default = 10
}

variable "jwt_secret" {
  description = "Mesmo segredo usado por jwt.secret na aplicação principal (AWS Secrets Manager em hml/prod)."
  type        = string
  sensitive   = true
}

variable "jwt_expiration_ms" {
  type    = number
  default = 1800000
}

variable "internal_api_key" {
  description = "Mesma chave usada por app.internal.api-key na aplicação principal."
  type        = string
  sensitive   = true
}

variable "internal_status_base_url" {
  description = "URL base da aplicação principal até /internal/clientes (ex.: http://<host>/internal/clientes)."
  type        = string
}

variable "tags" {
  type    = map(string)
  default = {}
}
