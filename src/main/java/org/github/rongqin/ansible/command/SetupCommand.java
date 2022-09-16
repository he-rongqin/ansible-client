package org.github.rongqin.ansible.command;

import org.github.rongqin.ansible.Module;

import java.util.List;

/**
 * @author herongqin
 */
public class SetupCommand extends Command{

	public SetupCommand(List<String> hosts) {
		this(hosts, null, null);
	}

	public SetupCommand(List<String> hosts, List<String> moduleArgs, List<String> options) {
		super(hosts, moduleArgs, Module.setup.toString(), options);
	}

}
