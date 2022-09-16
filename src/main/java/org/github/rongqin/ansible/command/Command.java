package org.github.rongqin.ansible.command;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.github.rongqin.ansible.client.AnsibleClient;
import org.github.rongqin.ansible.response.ResultValueHeader;
import org.github.rongqin.ansible.response.ReturnValue;
import org.github.rongqin.ansible.response.ReturnValue.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command
 * @author herongqin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Command {

	private List<String> hosts;

	private List<String> moduleArgs;

	private String module;

	private List<String> options;


	public String getExecRoot(){
		return "ansible";
	}

	public List<String> buildAnsibleCommands(AnsibleClient client, Command command) {
		List<String> commands = new ArrayList();
		commands.add(client.getAnsibleRootPath() + command.getExecRoot());
		if (client.getInventoryPath() != null) {
			commands.add("-i");
			commands.add(client.getInventoryPath());
		}

		commands.add(command.getHosts().stream().collect(Collectors.joining(":")));
		if (command.getModule() != null) {
			commands.add("-m " + command.getModule());
		}

		if (null != command.getModuleArgs() && command.getModuleArgs().size() > 0) {
			if (client.getSshdClient().getSshConfig() != null) {
				commands.add("-a '" + command.getModuleArgs().stream().collect(Collectors.joining(" ")) + "'");
			} else {
				commands.add("-a " + command.getModuleArgs().stream().collect(Collectors.joining(" ")));
			}
		}

		if (null != command.getOptions() && command.getOptions().size() > 0) {
			commands.add(command.getOptions().stream().collect(Collectors.joining(" ")));
		}

		return commands;
	}
	public Map<String, ReturnValue> parseReturnValues(List<String> rawOutput) {
		Map<String, ReturnValue> returnValues = new HashMap();
		String currentKey = null;
		Iterator var4 = rawOutput.iterator();

		String key;
		while(var4.hasNext()) {
			key = (String)var4.next();
			ResultValueHeader header = ResultValueHeader.createHeader(key);
			if (header != null) {
				ReturnValue resultValue = new ReturnValue();
				resultValue.setRc(header.getRc());
				resultValue.setResult(header.getResult());
				returnValues.put(header.getIp(), resultValue);
				currentKey = header.getIp();
			} else if (currentKey != null) {
				((ReturnValue)returnValues.get(currentKey)).getStdout().add(key);
			}
		}

		var4 = returnValues.keySet().iterator();

		while(var4.hasNext()) {
			key = (String)var4.next();
			ReturnValue returnValue = (ReturnValue)returnValues.get(key);
			if (returnValue.getResult() == ReturnValue.Result.unmanaged) {
				returnValue.getStdout().add("[WARNING]: Could not match supplied host pattern, ignoring:" + key);
			} else {
				try {
					String outputJson = (String)returnValue.getStdout().stream().collect(Collectors.joining());
					returnValue.setValue(JSONUtil.parseObj("{" + outputJson));
				} catch (Exception var8) {
				}
			}
		}

		return returnValues;
	}
}
