packer {
  required_plugins {
    amazon = {
      source  = "github.com/hashicorp/amazon"
      version = "~> 1.2.6"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

source "amazon-ebs" "ubuntu" {
  region        = var.aws_region
  instance_type = "t2.micro"

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-jammy-22.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"]  # Canonical owner ID
  }

  ami_name        = "csye6225-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  ami_description = "Ubuntu 22.04 with CloudWatch Agent for CSYE 6225"

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }

  ssh_username = "ubuntu"

  # Added SSH configurations to improve stability
  ssh_timeout             = "10m"
  ssh_handshake_attempts  = "100"
  pause_before_connecting = "10s"

  # Additional AWS-specific configurations
  communicator              = "ssh"
  ssh_interface             = "public_ip"
  ssh_keep_alive_interval   = "10s"
  ssh_clear_authorized_keys = true
}

build {
  sources = ["source.amazon-ebs.ubuntu"]

  # Add pause before file uploads
  provisioner "shell" {
    inline = ["sleep 10"]
  }

  # Initial setup and package installation
  provisioner "shell" {
    inline = [
      "sudo apt-get update",
      "sudo DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-17-jre-headless wget",
      "echo 'Initial setup complete'"
    ]
    timeout = "10m"
  }

  # Create application user with improved approach
  provisioner "shell" {
    inline = [
      "sudo groupadd -f saurabh_group",
      "sudo useradd -m -g saurabh_group -s /bin/bash saurabh_user || echo 'User creation failed, will use ubuntu user'"
    ]
  }

  # Add extra debugging to verify directory structure
  provisioner "shell" {
    inline = [
      "ls -la /tmp/",
      "echo 'Checking for uploaded files...'"
    ]
  }

  # Copy application files with debugging (one at a time)
  provisioner "file" {
    source      = "../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp-0.0.1-SNAPSHOT.jar"
    max_retries = 5
    timeout     = "30m"
  }

  # Verify JAR file upload
  provisioner "shell" {
    inline = [
      "ls -la /tmp/webapp-0.0.1-SNAPSHOT.jar || echo 'JAR FILE NOT FOUND'",
      "du -h /tmp/webapp-0.0.1-SNAPSHOT.jar || echo 'Cannot check JAR file size'"
    ]
  }

  provisioner "file" {
    source      = "../src/main/resources/application.properties"
    destination = "/tmp/application.properties"
    max_retries = 3
  }

  provisioner "file" {
    source      = "webapp.service"
    destination = "/tmp/webapp.service"
    max_retries = 3
  }

  # Copy CloudWatch Agent configuration
  provisioner "file" {
    source      = "amazon-cloudwatch-agent.json"
    destination = "/tmp/amazon-cloudwatch-agent.json"
    max_retries = 3
  }

  # Verify all files uploaded successfully
  provisioner "shell" {
    inline = [
      "echo 'Verifying all uploaded files:'",
      "ls -la /tmp/webapp-0.0.1-SNAPSHOT.jar || echo 'JAR FILE NOT FOUND'",
      "ls -la /tmp/application.properties || echo 'APPLICATION PROPERTIES NOT FOUND'",
      "ls -la /tmp/webapp.service || echo 'SERVICE FILE NOT FOUND'",
      "ls -la /tmp/amazon-cloudwatch-agent.json || echo 'CLOUDWATCH CONFIG NOT FOUND'"
    ]
  }

  # Install CloudWatch Agent and set up the system
  provisioner "shell" {
    inline = [
      "echo 'Downloading CloudWatch agent...'",
      "wget https://amazoncloudwatch-agent.s3.amazonaws.com/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -O /tmp/amazon-cloudwatch-agent.deb",
      "echo 'Installing CloudWatch agent...'",
      "sudo dpkg -i /tmp/amazon-cloudwatch-agent.deb",

      "echo 'Creating application directories...'",
      "sudo mkdir -p /opt/csye6225",

      "echo 'Creating log file instead of directory...'",
      "sudo touch /var/log/webapp.log",

      "echo 'Moving application files...'",
      "sudo cp /tmp/webapp-0.0.1-SNAPSHOT.jar /opt/csye6225/ || echo 'FAILED TO COPY JAR FILE'",
      "sudo cp /tmp/application.properties /opt/csye6225/ || echo 'FAILED TO COPY PROPERTIES FILE'",

      "echo 'Setting up CloudWatch Agent...'",
      "sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc",
      "sudo cp /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/ || echo 'FAILED TO COPY CLOUDWATCH CONFIG'",

      "echo 'Setting permissions...'",
      "if id saurabh_user &>/dev/null; then",
      "  sudo chown -R saurabh_user:saurabh_group /opt/csye6225",
      "  sudo chown saurabh_user:saurabh_group /var/log/webapp.log",
      "else",
      "  echo 'Using ubuntu user instead'",
      "  sudo chown -R ubuntu:ubuntu /opt/csye6225",
      "  sudo chown ubuntu:ubuntu /var/log/webapp.log",
      "fi",
      "sudo chmod -R 750 /opt/csye6225",
      "sudo chmod 644 /var/log/webapp.log",

      "echo 'Adding CloudWatch agent to necessary groups...'",
      "sudo usermod -a -G adm cwagent",
      "sudo usermod -a -G syslog cwagent",

      "echo 'Setting up service...'",
      "sudo cp /tmp/webapp.service /etc/systemd/system/ || echo 'FAILED TO COPY SERVICE FILE'",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp.service",

      "echo 'Enabling CloudWatch Agent...'",
      "sudo systemctl enable amazon-cloudwatch-agent",

      "echo 'Setup complete!'"
    ]
    timeout           = "16m"
    max_retries       = 2
    expect_disconnect = true
  }
}