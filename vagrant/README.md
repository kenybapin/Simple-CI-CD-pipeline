# Pre-requisites

## Ansible 
**1. Installation**
```bash
sudo apt-get update && sudo apt-get upgrade && sudo apt-get autoremove
sudo apt-get install ansible
sudo apt-get install -y python-pip libssl-dev
```
**2. Check**
```bash
which ansible
ansible --version
```

**3. Create a new test playbook:** ansible-test.yml
```yaml
---
- hosts: localhost
  tasks:
    - debug: msg="Ansible is working!"
```

4. **Run the playbook**
```bash
ansible-playbook ansible-test.yml --connection=local
```
<sup>**Ansible might warn about no inventory file being present, but since you're using --connection=local, the localhost host should automatically work.**<sup><br>

## Vagrant
**1. Installation**
```bash
sudo pip install vagrant
sudo apt-get -y install libvirt-dev
```
*For Windows users with WSL, export or add these commands to the bottom of your shell (~/.bashrc or ~/.zshrc)*
```bash
export PATH="$PATH:/mnt/c/Program Files/Oracle/VirtualBox"
export VAGRANT_WSL_ENABLE_WINDOWS_ACCESS="1"
```

**2. Check** : In your HOME dir, create a new VirtualBox VM
```bash
cd {YOUR WORKING DIR}
vagrant --version
vagrant init alpine/alpine64
vagrant up
```
&nbsp;

# Installation

1. Copy these files to your working Dir  
2. Build according to Vagrantfile (This may take 10/15 minutes)
```
cd {YOUR WORKING DIR}
vagrant up
```
