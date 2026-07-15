variable "aws_region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Project name used for resource naming."
  type        = string
  default     = "learn-hub"
}

variable "environment" {
  description = "Deployment environment name."
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.30.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR block for the public subnet."
  type        = string
  default     = "10.30.0.0/24"
}

variable "allowed_http_cidrs" {
  description = "CIDRs allowed to access the app over HTTP."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "allowed_https_cidrs" {
  description = "CIDRs allowed to access the app over HTTPS."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "allowed_ssh_cidrs" {
  description = "CIDRs allowed to access SSH. Leave empty to keep SSH closed and use SSM only."
  type        = list(string)
  default     = []
}

variable "key_name" {
  description = "Optional EC2 key pair name for SSH access."
  type        = string
  default     = null
}

variable "instance_type" {
  description = "EC2 instance type. t3.micro is cheapest, t3.small is safer if memory gets tight."
  type        = string
  default     = "t4g.micro"
}

variable "instance_architecture" {
  description = "EC2 architecture. Use arm64 for Graviton instances such as t4g."
  type        = string
  default     = "arm64"
}

variable "root_volume_size" {
  description = "Root EBS volume size in GiB."
  type        = number
  default     = 20
}

variable "enable_swap" {
  description = "Create swap space on the instance to reduce OOM risk on small instances."
  type        = bool
  default     = true
}

variable "swap_size_mb" {
  description = "Swap file size in MiB when enable_swap is true."
  type        = number
  default     = 1024
}

variable "container_image" {
  description = "App container image to run. Can be Docker Hub, ECR, or any registry reachable from the EC2 instance."
  type        = string
  default     = "leegeonho/learn-hub-backend:0.1.6"
}

variable "mysql_image" {
  description = "MySQL container image."
  type        = string
  default     = "mysql:8.4"
}

variable "caddy_image" {
  description = "Caddy image used for automatic HTTPS and reverse proxying."
  type        = string
  default     = "caddy:2.10"
}

variable "app_container_port" {
  description = "Internal port exposed by the Spring Boot container."
  type        = number
  default     = 8080
}

variable "java_opts" {
  description = "JVM options passed to the Spring Boot container."
  type        = string
  default     = "-Xms256m -Xmx512m"
}

variable "db_name" {
  description = "MySQL database name."
  type        = string
  default     = "learn_hub"
}

variable "db_username" {
  description = "MySQL application username."
  type        = string
  default     = "learnhub"
}

variable "db_password" {
  description = "MySQL application password."
  type        = string
  sensitive   = true
}

variable "db_root_password" {
  description = "Optional MySQL root password. If empty, db_password is reused."
  type        = string
  default     = ""
  sensitive   = true
}

variable "jwt_secret_key" {
  description = "JWT secret key for the Spring application."
  type        = string
  sensitive   = true
}

variable "kakao_client_id" {
  description = "Kakao OAuth client ID."
  type        = string
  sensitive   = true
}

variable "kakao_client_secret" {
  description = "Kakao OAuth client secret."
  type        = string
  sensitive   = true
}

variable "kakao_redirect_uri" {
  description = "Kakao OAuth redirect URI."
  type        = string
}

variable "domain_name" {
  description = "Primary domain used for the service."
  type        = string
  default     = "learn-hub.kr"
}

variable "www_domain_name" {
  description = "Optional www domain redirected to the primary domain."
  type        = string
  default     = "www.learn-hub.kr"
}

variable "gemini_api_key" {
  description = "Gemini API key."
  type        = string
  sensitive   = true
}

variable "gemini_model_name" {
  description = "Gemini model name passed to Spring AI."
  type        = string
  default     = "gemini-2.5-flash"
}

variable "prometheus_metrics_enabled" {
  description = "Whether to expose Prometheus metrics from the application."
  type        = bool
  default     = true
}

variable "additional_tags" {
  description = "Additional tags applied to supported resources."
  type        = map(string)
  default     = {}
}
