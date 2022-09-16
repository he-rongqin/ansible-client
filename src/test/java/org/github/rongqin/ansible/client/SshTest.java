package org.github.rongqin.ansible.client;

import cn.hutool.core.collection.ListUtil;
import org.github.rongqin.ansible.command.PlaybookCommand;
import org.github.rongqin.ansible.command.SetupCommand;
import org.github.rongqin.ansible.AnsibleExecResponse;
import org.github.rongqin.ansible.response.ReturnValue;
import org.github.rongqin.ssh.SshConfig;

import java.util.Map;

public class SshTest {

	public static void main(String[] args) {
		SshConfig sshConfig = new SshConfig();
		sshConfig.setHost("192.168.0.59");
		sshConfig.setPort(22);
		sshConfig.setPassword("123123");
		sshConfig.setUsername("root");

//		SshdClient sshdClient = new SshdClient(sshConfig);

//		sshdClient.scpDownload("./" ,"/data/ansible/hosts");
//		sshdClient.scpPutFile("./HELP.md", "/data/files");
//		CommandResponse commandResponse = sshdClient.execCommand("/usr/bin/ansible -i /data/ansible/hosts 192.168.0.172 -m setup -a 'filter=ansible_*_mb'");
//		System.out.println(commandResponse);
//		String[] result = commandResponse.getInfo().split("\\n");
//		for (int i = 0; i < result.length; i++) {
//			System.out.println(i+" - "+ result[i]);
//		}
//		System.out.println(commandResponse.getInfo());
//		System.out.println(commandResponse.getError());

		AnsibleClient ansibleClient = new AnsibleClient("/data/ansible/hosts", sshConfig);

		SetupCommand setupCommand = new SetupCommand(ListUtil.toList("192.168.0.172", "192.168.0.176"), ListUtil.toList("filter=ansible_*_mb"), null);
		PlaybookCommand playbookCommand = new PlaybookCommand("/data/ansible/ssh.yml");
		Map<String, ReturnValue> stringReturnValueMap = ansibleClient.execCommand(setupCommand);
		stringReturnValueMap.forEach((k , v) ->{
			System.out.println("k::"+ k);
			System.out.println("v::"+ v);
			System.out.println("v-v::"+ v.getValue());
			System.out.println("===========================");
		});
		stringReturnValueMap = ansibleClient.execCommand(playbookCommand);
		stringReturnValueMap.forEach((k , v) ->{
			System.out.println("k::"+ k);
			System.out.println("v::"+ v);
			System.out.println("v-v::"+ v.getValue());
			System.out.println("===========================");
		});



	}
}
