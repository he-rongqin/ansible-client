package org.github.rongqin.ansible.client;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileAppender;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.SshException;
import org.github.rongqin.ansible.command.Command;
import org.github.rongqin.ansible.AnsibleExecResponse;
import org.github.rongqin.ansible.response.ReturnValue;
import org.github.rongqin.model.CommandResponse;
import org.github.rongqin.ssh.SshConfig;
import org.github.rongqin.ssh.SshdClient;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** ssh client for apache sshd
 * @author herongqin
 */
@Slf4j
public final class AnsibleClient {

	@Getter
	@Setter
	private String ansibleRootPath = "/usr/bin/";

	@Getter
	private SshdClient sshdClient;

	@Getter
	private String inventoryPath = "/etc/ansible/hosts";

	public AnsibleClient(String inventoryPath, SshConfig sshConfig){
		this.sshdClient = new SshdClient(sshConfig);
		this.inventoryPath = inventoryPath;
	}

	public Map<String, ReturnValue> execCommand(Command command){
		Map<String, ReturnValue> responses = new HashMap();
		List<String> commands = command.buildAnsibleCommands(this, command);
		String commandStr = commands.stream().collect(Collectors.joining(" "));
		System.out.println(commandStr);
		log.debug("AnsibleClient ======= exec ansible playbook command: {}", command);
		CommandResponse commandResponse = sshdClient.execCommand(commandStr);
		log.debug("AnsibleClient ======= exec ansible playbook command result: {}", commandResponse);
		System.out.println(commandResponse);
		// TODO: 格式化 playbook 执行日志
		responses = command.parseReturnValues(ListUtil.toList(commandResponse.getInfo().split("\n")));
		return responses;
	}

	/**
	 * 覆盖主机清单文件，覆盖 AnsibleClient.inventoryPath 目录下hosts文件
	 * @param localFilePath 本地主机清单文件路径
	 */
	public void overrideRemoteInventory(String localFilePath){
		sshdClient.scpPutFile(localFilePath, inventoryPath);
	}

	/**
	 * 覆盖主机清单文件，覆盖 AnsibleClient.inventoryPath 目录下hosts文件
	 * @param groupName 主机清单分组名称[groupName]
	 * @param inventoryHosts 主机清单集合
	 */
	public void overrideRemoteInventory(String groupName, List<AnsibleInventoryHost> inventoryHosts){
		String _tempPath = "./ansible/hosts";
		File file = new File(_tempPath);
		// 创建临时文件
		FileUtil.touch(file);
		// 文件内容追加
		FileAppender fileAppender = new FileAppender(file, 16, true);
		fileAppender.append("["+groupName+"]\n");
		inventoryHosts.forEach(t ->{
			fileAppender.append(t.toString()+"\n");
		});
		// 上传文件
		overrideRemoteInventory(_tempPath);
		// 删除临时文件
		FileUtil.del(_tempPath);
	}

	/**
	 * 推送剧本到ansible server 指定目录<>br</>
	 * 可以是目录、也可以是文件
	 * @param localPath
	 */
	public void pushPlaybookToRemote(String localPath, String remotePath){
		sshdClient.scpPutFile(localPath, remotePath);
	}




}
