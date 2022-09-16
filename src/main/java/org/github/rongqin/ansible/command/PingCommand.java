package org.github.rongqin.ansible.command;

import org.github.rongqin.ansible.Module;

import java.util.List;

/**
 * @author herongqin
 */
public class PingCommand extends Command{

	public PingCommand(List<String> hosts) {
		this(hosts, null, null);
	}

	public PingCommand(List<String> hosts, List<String> moduleArgs, List<String> options) {
		super(hosts, moduleArgs, Module.ping.toString(), options);
	}
}
