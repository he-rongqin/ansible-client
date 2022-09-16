package org.github.rongqin.ansible.client;

import lombok.Data;

/**
 * 主机清单
 * @author herongqin
 */
@Data
public class AnsibleInventoryHost {

	private String host_name;
	private String ansible_port;
	private String ansible_user;
	private String ansible_ssh_private_key_file;
	private String ansible_ssh_pass;
	private String ansible_become_password;
	private String ansible_become;


	@Override
	public String toString(){
		if (host_name == null || host_name.equals("")){
			return "";
		}
		String line = host_name;
		if (ansible_port != null && !ansible_port.equals("")){
			line += " ansible_port="+ansible_port;
		}
		if (ansible_user != null && !ansible_user.equals("")){
			line += " ansible_user="+ansible_user;
		}
		if (ansible_ssh_private_key_file != null && !ansible_ssh_private_key_file.equals("")){
			line += " ansible_ssh_private_key_file="+ansible_ssh_private_key_file;
		}
		if (ansible_ssh_pass != null && !ansible_ssh_pass.equals("")){
			line += " ansible_ssh_pass="+ansible_ssh_pass;
		}
		if (ansible_become_password != null && !ansible_become_password.equals("")){
			line += " ansible_become_password="+ansible_become_password;
		}
		if (ansible_become != null && !ansible_become.equals("")){
			line += " ansible_become="+ansible_become;
		}
		return line+"\n";
	}
}
