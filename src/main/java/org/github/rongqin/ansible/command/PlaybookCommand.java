package org.github.rongqin.ansible.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.github.rongqin.ansible.client.AnsibleClient;
import org.github.rongqin.ansible.response.ResultValueHeader;
import org.github.rongqin.ansible.response.ReturnValue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author herongqin
 */
@Data
@AllArgsConstructor
public class PlaybookCommand extends Command {

	static Pattern play_recap_pattern = Pattern.compile("PLAY RECAP (\\*)*");
	static Pattern unreachable_pattern = Pattern.compile("unreachable=\\d");
	static Pattern failed_pattern = Pattern.compile("failed=\\d");
	static Pattern changed_pattern = Pattern.compile("changed=\\d");
	static Pattern ok_pattern = Pattern.compile("ok=\\d");
	static Pattern digital_pattern = Pattern.compile("\\d");

	private String playbookPath;

	@Override
	public String getExecRoot() {
		return "ansible-playbook";
	}

	@Override
	public List<String> buildAnsibleCommands(AnsibleClient client, Command command) {
		List<String> commands = new ArrayList();
		commands.add(client.getAnsibleRootPath() + command.getExecRoot());
		if (client.getInventoryPath() != null) {
			commands.add("-i");
			commands.add(client.getInventoryPath());
		}

		commands.add(this.playbookPath);
		if (null != command.getOptions() && command.getOptions().size() > 0) {
			commands.add(command.getOptions().stream().collect(Collectors.joining(" ")));
		}

		return commands;
	}

	@Override
	public Map<String, ReturnValue> parseReturnValues(List<String> rawOutput) {
		Pattern ip_pattern = ResultValueHeader.head_ip_pattern;
		boolean recap = false;
		Map<String, ReturnValue> responses = new HashMap();
		Iterator var5 = rawOutput.iterator();

		while(true) {
			while(true) {
				String line;
				Matcher matchNotInInventory;
				label52:
				do {
					while(var5.hasNext()) {
						line = (String)var5.next();
						if (recap) {
							matchNotInInventory = ip_pattern.matcher(line);
							continue label52;
						}

						if (line.contains("[WARNING]")) {
							matchNotInInventory = ResultValueHeader.head_inventory_no_host_pattern.matcher(line);
							if (matchNotInInventory.matches()) {
								Matcher matchIp = ResultValueHeader.head_ip_pattern.matcher(line);
								if (matchIp.find()) {
									String ip = matchIp.group();
									responses.put(ip, new ReturnValue());
									((ReturnValue)responses.get(ip)).setResult(ReturnValue.Result.unmanaged);
									((ReturnValue)responses.get(ip)).setStdout(rawOutput);
								}
							}
						} else if (line.startsWith("PLAY") && play_recap_pattern.matcher(line).matches()) {
							recap = true;
						}
					}

					return responses;
				} while(!matchNotInInventory.find());

				String ip = matchNotInInventory.group();
				responses.put(ip, new ReturnValue());
				((ReturnValue)responses.get(ip)).setStdout(rawOutput);
				Matcher match = failed_pattern.matcher(line);
				Matcher match2;
				if (match.find()) {
					match2 = digital_pattern.matcher(match.group());
					if (match2.find() && Integer.valueOf(match2.group()) > 0) {
						((ReturnValue)responses.get(ip)).setResult(ReturnValue.Result.failed);
						continue;
					}
				}

				match = unreachable_pattern.matcher(line);
				if (match.find()) {
					match2 = digital_pattern.matcher(match.group());
					if (match2.find() && Integer.valueOf(match2.group()) > 0) {
						((ReturnValue)responses.get(ip)).setResult(ReturnValue.Result.unreachable);
						continue;
					}
				}

				match = ok_pattern.matcher(line);
				if (match.find()) {
					match2 = digital_pattern.matcher(match.group());
					if (match2.find() && Integer.valueOf(match2.group()) > 0) {
						((ReturnValue)responses.get(ip)).setResult(ReturnValue.Result.success);
						continue;
					}
				}

				match = changed_pattern.matcher(line);
				if (match.find()) {
					match2 = digital_pattern.matcher(match.group());
					if (match2.find() && Integer.valueOf(match2.group()) > 0) {
						((ReturnValue)responses.get(ip)).setResult(ReturnValue.Result.changed);
					}
				}
			}
		}
	}
}
