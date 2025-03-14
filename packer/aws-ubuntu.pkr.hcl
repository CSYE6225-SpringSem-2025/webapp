packer {
  required_plugins {
    amazon = {
      source  = "github.com/hashicorp/amazon"
      version = "~> 1.2.6"
    }
    googlecompute = {
      source  = "github.com/hashicorp/googlecompute"
      version = "~> 1.1.1"
    }
  }
}

# Variables
variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "new" {
  type    = string
  default = env("GCP_SA_KEY")
}

variable "gcp_project_id" {
  type    = string
  default = "assignment4-451820"
}

variable "gcp_zone" {
  type    = string
  default = "us-central1-a"
}

# AWS Source
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
    owners      = ["099720109477"]
  }

  ami_name        = "csye6225-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  ami_description = "Ubuntu 24.04 AMI for CSYE 6225"

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }

  ssh_username = "ubuntu"

  # Added SSH configurations
  ssh_timeout             = "10m"
  ssh_handshake_attempts  = "100"
  pause_before_connecting = "10s"

  # Added AWS-specific configurations
  communicator              = "ssh"
  ssh_interface             = "public_ip"
  ssh_keep_alive_interval   = "10s"
  ssh_clear_authorized_keys = true
}


source "googlecompute" "ubuntu" {
  project_id          = var.gcp_project_id
  account_file        = var.gcp_account_file
  source_image_family = "ubuntu-2204-lts"
  zone                = var.gcp_zone
  image_name          = "csye6225-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  image_description   = "Ubuntu 24.04 Image for CSYE 6225"
  ssh_username        = "ubuntu"
  machine_type        = "e2-medium"
  disk_size           = 25
}

build {
  sources = [
    "source.amazon-ebs.ubuntu",
    "source.googlecompute.ubuntu"
  ]

  # Copy application files with improved file transfer settings
  provisioner "file" {
    source      = "../target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp-0.0.1-SNAPSHOT.jar"
    max_retries = 3
    timeout     = "10m"
  }

  provisioner "file" {
    source      = "../src/main/resources/application.properties"
    destination = "/tmp/application.properties"
    max_retries = 3
  }

  provisioner "file" {
    source      = "setup_webapp.sh"
    destination = "/tmp/setup_webapp.sh"
    max_retries = 3
  }

  provisioner "file" {
    source      = "webapp.service"
    destination = "/tmp/webapp.service"
    max_retries = 3
  }

  # Added pause between file uploads and script execution
  provisioner "shell" {
    inline = ["sleep 10"]
  }

  # Run setup script with improved error handling
  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup_webapp.sh",
      "sudo /tmp/setup_webapp.sh"
    ]
    timeout           = "16m"
    max_retries       = 2
    expect_disconnect = true
  }
}