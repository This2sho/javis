output "instance_id" {
  description = "EC2 instance ID."
  value       = aws_instance.app.id
}

output "public_ip" {
  description = "Elastic IP address of the EC2 instance."
  value       = aws_eip.app.public_ip
}

output "public_dns" {
  description = "Public DNS name of the EC2 instance."
  value       = aws_instance.app.public_dns
}

output "app_url" {
  description = "HTTP URL of the deployed application."
  value       = "http://${aws_eip.app.public_ip}"
}

output "ssm_start_session_command" {
  description = "AWS CLI command to open an SSM shell session to the instance."
  value       = "aws ssm start-session --target ${aws_instance.app.id}"
}

output "ssh_command" {
  description = "SSH command when key_name and allowed_ssh_cidrs are configured."
  value       = var.key_name != null ? "ssh ec2-user@${aws_eip.app.public_ip}" : null
}
