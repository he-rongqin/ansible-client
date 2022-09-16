package org.github.rongqin.ansible;

import lombok.Data;

import java.util.List;

/**
 * @author herongqin
 */
@Data
public class AnsibleExecResponse  {

	private List<String> stdout;

	private List<String> exception;



}
